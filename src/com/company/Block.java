package com.company;

import java.io.Serializable;

public class Block implements Serializable {

    private String data;

    private final int id;

    public String getData() {
        return data;
    }

    public void setData(String blockData) {
        this.data = blockData;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return data.length();
    }

    public Block(String data, int id) {
        this.data = data;
        this.id = id;
    }
}
