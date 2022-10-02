package com.example.demo.controller;

import com.example.demo.model.SystemItemImportRequest;
import com.example.demo.service.SystemItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class SystemItemController {

    @Autowired
    SystemItemService service;


    @DeleteMapping({"/delete/{id}", "/delete"})
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> deleteFiles(@PathVariable @Nullable String id) {
        return service.deleteFilesResponse(id);
    }

    @PostMapping("/imports")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> importFiles(@RequestBody SystemItemImportRequest request) {
        return service.importFilesResponse(request);
    }

    @GetMapping("/updates")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> findRecentFiles(@RequestParam(name="date") String date) {
        return service.findRecentFilesResponse(date);
    }

    @GetMapping({"/nodes/{id}", "/nodes"})
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> findFiles(@PathVariable @Nullable String id) {
        return service.findFilesResponse(id);
    }

    @GetMapping({"/node/{id}/history"})
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> findHistory(@PathVariable @Nullable String id,
                                                               @RequestParam(name="dateStart")@Nullable String dateStart,
                                                               @RequestParam(name="dateEnd") @Nullable String dateEnd) {
        return service.findHistoryResponse(id, dateStart, dateEnd);
    }
}
