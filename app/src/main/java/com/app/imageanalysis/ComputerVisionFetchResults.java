package com.app.imageanalysis;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ComputerVisionFetchResults {

    @SerializedName("categories")
    private ArrayList categories;

    public ArrayList getCategories() {
        return categories;
    }

}
