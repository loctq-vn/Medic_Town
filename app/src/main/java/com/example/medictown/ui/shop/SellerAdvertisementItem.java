package com.example.medictown.ui.shop;

import java.io.Serializable;

public class SellerAdvertisementItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        DRAFT,
        ACTIVE,
        PAUSED,
        EXPIRED
    }

    public final String id;
    public final String imageUrl;
    public final String title;
    public final String description;
    public final String position;
    public final String dates;
    public final String views;
    public final String clicks;
    public final String budget;
    public final int performance;
    public Status status;

    public SellerAdvertisementItem(
            String id,
            String imageUrl,
            String title,
            String description,
            String position,
            String dates,
            String views,
            String clicks,
            String budget,
            int performance,
            Status status
    ) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.position = position;
        this.dates = dates;
        this.views = views;
        this.clicks = clicks;
        this.budget = budget;
        this.performance = performance;
        this.status = status;
    }
}
