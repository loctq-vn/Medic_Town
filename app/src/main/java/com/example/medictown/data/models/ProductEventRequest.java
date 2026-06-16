package com.example.medictown.data.models;

import java.util.Map;

public class ProductEventRequest {
    public String product_id;
    public String event_type;
    public Map<String, Object> metadata;

    public ProductEventRequest(String productId, String eventType, Map<String, Object> metadata) {
        this.product_id = productId;
        this.event_type = eventType;
        this.metadata = metadata;
    }
}
