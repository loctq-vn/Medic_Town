package com.example.medictown.data.models;

import java.util.ArrayList;
import java.util.List;

public class RevenueDailySummary {
    public List<Item> items = new ArrayList<>();

    public static class Item {
        public String date;
        public double grossRevenue;
        public double refundAmount;
        public double netRevenue;
        public int totalOrders;
        public int completedOrders;
        public int pendingOrders;
        public int confirmedOrders;
        public int shippingOrders;
        public int cancelledOrders;
        public List<RevenueDashboard.PaymentMethod> paymentMethods = new ArrayList<>();
    }
}
