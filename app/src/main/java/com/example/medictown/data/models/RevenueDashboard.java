package com.example.medictown.data.models;

import java.util.ArrayList;
import java.util.List;

public class RevenueDashboard {
    public FixedKpis fixedKpis;
    public FilteredSummary filteredSummary;
    public Chart chart;
    public List<PaymentMethod> paymentMethods = new ArrayList<>();
    public List<TopProduct> topProducts = new ArrayList<>();
    public List<RecentOrder> recentOrders = new ArrayList<>();

    public static class FixedKpis {
        public double todayRevenue;
        public double monthRevenue;
    }

    public static class FilteredSummary {
        public double grossRevenue;
        public double refundAmount;
        public double netRevenue;
        public int totalOrders;
        public int completedOrders;
        public int pendingOrders;
        public int cancelledOrders;
    }

    public static class Chart {
        public String groupBy;
        public List<ChartItem> items = new ArrayList<>();
    }

    public static class ChartItem {
        public String period;
        public double revenue;
        public int orderCount;
    }

    public static class PaymentMethod {
        public String method;
        public double revenue;
        public int transactionCount;
        public double percentage;
    }

    public static class TopProduct {
        public String productId;
        public String productName;
        public String productImage;
        public int quantitySold;
        public int stock;
        public double revenue;
    }

    public static class RecentOrder {
        public String orderId;
        public String customerName;
        public double amount;
        public String paymentMethod;
        public String paymentStatus;
        public String orderStatus;
        public String createdAt;
        public String paidAt;
    }
}
