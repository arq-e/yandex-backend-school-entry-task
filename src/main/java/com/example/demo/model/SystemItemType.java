package com.example.demo.model;

public enum SystemItemType {
    FILE("FILE"), FOLDER("FOLDER");
    String type;
    SystemItemType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
