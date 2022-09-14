package com.github.kouyoquotient.utils;

import java.util.ArrayList;

public class JSONRead {
    private String result;
    private String response;
    ArrayList<Object> data = new ArrayList<>();
    private float limit;
    private float offset;
    private float total;

    public ArrayList<Object> getData() {
        return data;
    }

    public String getResult() {
        return result;
    }

    public String getResponse() {
        return response;
    }

    public float getLimit() {
        return limit;
    }

    public float getOffset() {
        return offset;
    }

    public float getTotal() {
        return total;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setData(ArrayList<Object> data) {
        this.data = data;
    }

    public void setLimit(float limit) {
        this.limit = limit;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public void setTotal(float total) {
        this.total = total;
    }

}
