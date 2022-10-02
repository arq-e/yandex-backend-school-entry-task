
package com.example.demo.model;

import jakarta.persistence.*;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
@Entity
@Table(name = "main")
public class SystemItem {
    @Id
    private String id;
    @Column(name = "url")
    private String url;
    @Column(name = "date")
    private OffsetDateTime date;
    @Column(name = "parentId")
    private String parentId;
    @Column(name = "type")
    private SystemItemType type;
    @Column(name = "size")
    private Long size;

    @Transient
    private ArrayList<SystemItem> children;

    public SystemItem() {
    }

    public SystemItem(SystemItemImport importItem, OffsetDateTime date) {
        this.id = importItem.getId();
        this.url = importItem.getUrl();
        this.parentId = importItem.getParentId();
        this.size = importItem.getSize();
        this.type = importItem.getType();
        this.date = date;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public SystemItemType getType() {
        return type;
    }

    public void setType(SystemItemType type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public ArrayList<SystemItem> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<SystemItem> children) {
        this.children = children;
    }

    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("id", this.getId());
        outputMap.put("url", this.getUrl());
        outputMap.put("type", this.getType().getType());
        outputMap.put("parentId", this.getParentId());
        outputMap.put("date", getDate());
        outputMap.put("size", getSize());
        if(this.getType().getType().equals("FILE")) {
            outputMap.put("children", null);
        } else if (this.getChildren() == null) {
            outputMap.put("children", new SystemItem[0]);
        } else {
            ArrayList<HashMap<String, Object>> children = new ArrayList<>();
            this.getChildren().forEach((x) -> children.add(x.toHashMap()));
            outputMap.put("children", children);
        }
        return outputMap;
    }

    // Этот метод проверяет элемент на корректность формата. Так как условия корректности различаются
    // при импорте элемента и при получении/удалении из базы, он принимает аргумент, уточняющий формат
    public boolean isValidItem(boolean isImport) {
        return !(this.getType().getType().equals("FILE") && (this.getSize() <= 0 || this.getUrl().length() > 255))
                && !(this.getType().getType().equals("FOLDER") && ((this.getSize() != null && isImport)
                || this.getUrl() != null))
                && this.getId() != null && this.getDate() != null && this.getType().getType() != null;
    }

}
