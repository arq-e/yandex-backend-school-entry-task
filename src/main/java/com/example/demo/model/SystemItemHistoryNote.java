package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.*;

@Entity
@Table(name="history")
public class SystemItemHistoryNote {
    @Id
    private String id;
    private String url;
    private String parentId;
    private SystemItemType type;
    private Long size;
    private Date date;

    public SystemItemHistoryNote() {
    }

    public SystemItemHistoryNote(SystemItem item) {
        this.id = item.getId();
        this.url = item.getUrl();
        this.type = item.getType();
        this.size = item.getSize();
        this.parentId = item.getParentId();
        this.date = item.getDate();
    }

    public SystemItemHistoryNote(String id) {
        this.id = id;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
