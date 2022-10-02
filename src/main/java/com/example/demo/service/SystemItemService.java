package com.example.demo.service;

import com.example.demo.model.SystemItem;
import com.example.demo.model.SystemItemHistoryNote;
import com.example.demo.model.SystemItemImport;
import com.example.demo.model.SystemItemImportRequest;
import com.example.demo.persistence.HistoryItemRepository;
import com.example.demo.persistence.SystemItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SystemItemService {

    @Autowired
    private SystemItemRepository systemItemRepository;

    @Autowired
    private HistoryItemRepository historyItemRepository;

    public ResponseEntity<HashMap<String, Object>> code400Response() {
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("code", 400);
        outputMap.put("message", "Validation Failed");
        return ResponseEntity.status(400).body(outputMap);
    }

    public ResponseEntity<HashMap<String, Object>> code404Response() {
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("code", 404);
        outputMap.put("message", "Item not found");
        return ResponseEntity.status(404).body(outputMap);
    }

   public ResponseEntity<HashMap<String, Object>> findFilesResponse(@Nullable String id) {
        if(id == null) {
            return code400Response();
        }
        Optional<SystemItem> outputItem = systemItemRepository.findById(id);
        outputItem.ifPresent(this::recursiveChildren);
        if(outputItem.isPresent()) {
            if(!outputItem.get().isValidItem(false)) {
                return code400Response();
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(outputItem.get().toHashMap());
            }
        } else {
            return code404Response();
        }
    }

    void recursiveChildren(SystemItem ancestor) {
        if(ancestor.getType().getType().equals("FOLDER")) {
            ArrayList<SystemItem> children = new ArrayList<>(systemItemRepository.findByParentId(ancestor.getId()));
            ancestor.setChildren(children);
            if (ancestor.getChildren().size() != 0) {
                ancestor.getChildren().forEach(this::recursiveChildren);
                ancestor.setSize(children.stream().map(SystemItem::getSize).reduce(Long::sum).get());
            } else {
                ancestor.setSize(0L);
            }
        }
    }

    public ResponseEntity<HashMap<String, Object>> importFilesResponse(SystemItemImportRequest request) {
        List<String> updatedElements = new ArrayList<>();

        boolean isValidItems = request.getItems().stream().allMatch((x) -> {
            SystemItem item = new SystemItem(x, request.getUpdateDate());
            if (item.isValidItem(true)) {
                if (x.getParentId() == null) return true;
                Optional<SystemItem> optionalItem = systemItemRepository.findById(x.getParentId());
                boolean isFileInRepository = optionalItem.isPresent() && optionalItem.get().getType().getType().equals("FILE");
                boolean isFileInRequest = request.getItems().stream().anyMatch((y) ->
                        (y.getId().equals(x.getParentId())) && y.getType().getType().equals("FILE"));
                if (optionalItem.isPresent() && !isFileInRepository && !isFileInRequest) {
                    updateDate(request.getUpdateDate(), optionalItem.get(), updatedElements);
                }
                return !isFileInRepository && !isFileInRequest;
            } else
                return false;
        });

        if (request.validateIdsUnicity() && isValidItems) {
            for (SystemItemImport itemImport : request.getItems()) {
                systemItemRepository.save(new SystemItem(itemImport, request.getUpdateDate()));
                updatedElements.add(itemImport.getId());
            }
            updateHistoryAfterPostRequest(updatedElements);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return code400Response();
        }
    }

    public void updateDate(OffsetDateTime newDate, SystemItem item, List<String> updatedElements) {
        item.setDate(newDate);
        systemItemRepository.save(item);
        updatedElements.add(item.getId());

        if (item.getParentId() != null) {
            Optional<SystemItem> optionalItem = systemItemRepository.findById(item.getParentId());
            optionalItem.ifPresent(systemItem -> updateDate(newDate, systemItem, updatedElements));
        }
    }

    public ResponseEntity<HashMap<String, Object>> deleteFilesResponse(@Nullable String id) {
        if(id == null) {
            return code400Response();
        }
        Optional<SystemItem> item = systemItemRepository.findById(id);
        if(item.isPresent()) {
            if(item.get().isValidItem(false)) {
                systemItemDelete(item.get());
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return code400Response();
            }
        } else {
            return code404Response();
        }
    }
    public void systemItemDelete(SystemItem item) {
        systemItemRepository.delete(item);
        historyItemRepository.deleteById(item.getId());
        if (item.getType().getType().equals("FOLDER")) {
            ArrayList<SystemItem> children = new ArrayList<>(systemItemRepository.findByParentId(item.getId()));
            for (SystemItem child : children) {
                this.systemItemDelete(child);
            }
        }
    }

    public ResponseEntity<HashMap<String, Object>> findRecentFilesResponse(String date) {
        if(date == null || dateParser(date).isEmpty()) {
            return code400Response();
        }
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("items", new ArrayList<>(systemItemRepository
                .findRecent(dateParser(date).get(), dateParser(date).get().minusDays(1)).stream().filter((x)
                        -> x.getType().getType().equals("FILE")).map(SystemItem::toHashMap).collect(Collectors.toList())));
        return ResponseEntity.status(HttpStatus.OK).body(outputMap);
    }

    public ResponseEntity<HashMap<String, Object>> findHistoryResponse(String id, String startDate, String endDate) {
        if (startDate == null) {
            startDate = new Date(0L).toString();
        }
        HashMap<String, Object> outputMap = new HashMap<>();
        if (!dateParser(startDate).isEmpty() || endDate == null) {
            outputMap.put("items", new ArrayList<>(historyItemRepository.findHistoryByItemId(id,
                    dateParser(startDate).get())));
        } else if (!dateParser(endDate).isEmpty()) {
                outputMap.put("items", new ArrayList<>(historyItemRepository.findHistoryByItemId(id,
                        dateParser(startDate).get(), dateParser(endDate).get())));
        }
        return ResponseEntity.status(HttpStatus.OK).body(outputMap);
    }

    public void updateHistoryAfterPostRequest(List<String> updatedElements) {
        for (String id : updatedElements) {
            historyItemRepository.save(new SystemItemHistoryNote(systemItemRepository.getReferenceById(id)));
        }
    }

    public static Optional<OffsetDateTime> dateParser(String str) {
        if (!str.contains("T")) return Optional.empty();
        ArrayList<String> dateSplit = new ArrayList<>(Arrays.asList(str.substring(0,str.indexOf("T")).split("-")));

        if (dateSplit.size() != 3 || dateSplit.get(0).length() != 4 || dateSplit.get(1).length() != 2
                || dateSplit.get(2).length() != 2) {
            return Optional.empty();
        }

        ArrayList<String> timeSplit = new ArrayList<>(Arrays.asList(str.substring(str.indexOf('T')+1).split(":")));

        if (timeSplit.size() != 3 || timeSplit.get(0).length() !=2 || timeSplit.get(1).length() != 2
                || timeSplit.get(2).length() != 3 || timeSplit.get(2).charAt(2) != 'Z') return Optional.empty();
        try {
            int year = Integer.parseInt(dateSplit.get(0));
            int month = Integer.parseInt(dateSplit.get(1));
            int day = Integer.parseInt(dateSplit.get(2));
            int hours = Integer.parseInt(timeSplit.get(0));
            int mins = Integer.parseInt(timeSplit.get(1));
            int seconds = Integer.parseInt(timeSplit.get(2).substring(0,2));
            int nanos = 0;
            boolean leapYear = year % 4 == 0 && year % 100 !=0;
            if (year <= 0 || month <= 0 || month > 12 || day <= 0 || hours < 0 || hours > 23 || mins < 0 || mins > 59
                    || seconds < 0 || seconds > 59
                    || ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30
                    || (month == 2 && (!leapYear && day > 28 || day > 29)) || day > 31)){
                return Optional.empty();
            }
            return Optional.of(OffsetDateTime.of(year,month,day,hours,mins,seconds, nanos, ZoneOffset.UTC));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

    }

}
