package com.example.demo.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

public class SystemItemImportRequest {
    private ArrayList<SystemItemImport> items;
    private OffsetDateTime updateDate;

    public SystemItemImportRequest(ArrayList<SystemItemImport> items, OffsetDateTime updateDate) {
        this.items = items;
        this.updateDate = updateDate;
    }

    public ArrayList<SystemItemImport> getItems() {
        return items;
    }

    public void setItems(ArrayList<SystemItemImport> items) {
        this.items = items;
    }

    public OffsetDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(OffsetDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public boolean validateIdsUnicity() {
        ArrayList<String> ids = new ArrayList<>(this.getItems().stream().map(SystemItemImport::getId).collect(Collectors.toList()));
        return new HashSet<String>(ids).size() == ids.size();
    }
}
