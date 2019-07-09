package com.fuelware.app.fw_manager.models;

import com.google.gson.annotations.SerializedName;

public class Pagination {
    @SerializedName("count")
    long offset = 30;
    @SerializedName("current_page")
    public long currentPage = 0;
    @SerializedName("total_pages")
    public long totalPages = 0;

    public long total = 0;
}
