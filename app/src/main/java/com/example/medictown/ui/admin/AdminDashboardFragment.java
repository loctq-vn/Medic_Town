package com.example.medictown.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.models.RevenueDashboard;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardFragment extends Fragment {

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private AdminViewModel viewModel;
    private TextView tvDashboardTitle;
    private TextView tvTotalRevenue;
    private TextView tvTodayRevenue;
    private TextView tvMonthRevenue;
    private TextView tvNetRevenue;
    private TextView tvRefundAmount;
    private TextView tvTotalOrders;
    private TextView filterToday;
    private TextView filterSevenDays;
    private TextView filterThirtyDays;
    private TextView filterCustom;
    private TextView chartGroupDay;
    private TextView chartGroupWeek;
    private TextView chartGroupMonth;
    private RevenueChartView revenueChartView;
    private LinearLayout paymentMethodsContainer;
    private RecyclerView rvRecentOrders;
    private RecentOrdersAdapter recentOrdersAdapter;
    private String currentShopId;
    private LocalDate currentFromDate;
    private LocalDate currentToDate;
    private String currentChartGroupBy = "day";

    private enum RevenueRange {
        TODAY,
        SEVEN_DAYS,
        THIRTY_DAYS,
        CUSTOM
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        tvDashboardTitle = view.findViewById(R.id.tvDashboardTitle);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTodayRevenue = view.findViewById(R.id.tvTodayRevenue);
        tvMonthRevenue = view.findViewById(R.id.tvMonthRevenue);
        tvNetRevenue = view.findViewById(R.id.tvNewCustomers);
        tvRefundAmount = view.findViewById(R.id.tvLowStock);
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        filterToday = view.findViewById(R.id.filterToday);
        filterSevenDays = view.findViewById(R.id.filterSevenDays);
        filterThirtyDays = view.findViewById(R.id.filterThirtyDays);
        filterCustom = view.findViewById(R.id.filterCustom);
        chartGroupDay = view.findViewById(R.id.chartGroupDay);
        chartGroupWeek = view.findViewById(R.id.chartGroupWeek);
        chartGroupMonth = view.findViewById(R.id.chartGroupMonth);
        revenueChartView = view.findViewById(R.id.revenueChartView);
        paymentMethodsContainer = view.findViewById(R.id.paymentMethodsContainer);
        rvRecentOrders = view.findViewById(R.id.rvRecentOrders);

        recentOrdersAdapter = new RecentOrdersAdapter();
        rvRecentOrders.setAdapter(recentOrdersAdapter);

        SessionManager sessionManager = new SessionManager(requireContext());
        String currentShopName = sessionManager.getCurrentShopName();
        if (currentShopName != null && !currentShopName.isEmpty()) {
            tvDashboardTitle.setText(currentShopName);
        }

        MaterialCardView btnManageInventory = view.findViewById(R.id.btnManageInventory);
        MaterialCardView btnManageOrders = view.findViewById(R.id.btnManageOrders);
        MaterialButton btnViewAllOrders = view.findViewById(R.id.btnViewAllOrders);
        MaterialButton btnQuickAddProduct = view.findViewById(R.id.btnQuickAddProduct);

        viewModel.getRevenueDashboard().observe(getViewLifecycleOwner(), this::updateRevenueDashboard);
        viewModel.getAllProducts().observe(getViewLifecycleOwner(), this::updateProductStats);

        setupRevenueFilters();
        setupChartGroupFilters();
        updateChartGroupStyle();

        currentShopId = sessionManager.getCurrentShopId();
        if (currentShopId != null && !currentShopId.isEmpty()) {
            selectRevenueRange(RevenueRange.SEVEN_DAYS);
            viewModel.fetchShopProducts(currentShopId);
        } else {
            renderEmptyRevenue();
            viewModel.fetchAllProducts();
        }

        btnManageInventory.setOnClickListener(v -> navigateTo(new AdminInventoryFragment()));
        btnManageOrders.setOnClickListener(v -> navigateTo(new AdminOrdersFragment()));
        btnViewAllOrders.setOnClickListener(v -> navigateTo(new AdminOrdersFragment()));
        btnQuickAddProduct.setOnClickListener(v -> navigateTo(new AdminInventoryFragment()));
    }

    private void setupRevenueFilters() {
        filterToday.setOnClickListener(v -> selectRevenueRange(RevenueRange.TODAY));
        filterSevenDays.setOnClickListener(v -> selectRevenueRange(RevenueRange.SEVEN_DAYS));
        filterThirtyDays.setOnClickListener(v -> selectRevenueRange(RevenueRange.THIRTY_DAYS));
        filterCustom.setOnClickListener(v -> showCustomDateRangePicker());
    }

    private void setupChartGroupFilters() {
        chartGroupDay.setOnClickListener(v -> selectChartGroup("day"));
        chartGroupWeek.setOnClickListener(v -> selectChartGroup("week"));
        chartGroupMonth.setOnClickListener(v -> selectChartGroup("month"));
    }

    private void selectChartGroup(String groupBy) {
        currentChartGroupBy = groupBy;
        updateChartGroupStyle();
        if (currentFromDate != null && currentToDate != null) {
            fetchRevenueDashboard(currentFromDate, currentToDate);
        }
    }

    private void selectRevenueRange(RevenueRange range) {
        updateRevenueFilterStyle(range);
        LocalDate today = LocalDate.now();

        switch (range) {
            case TODAY:
                fetchRevenueDashboard(today, today);
                break;
            case SEVEN_DAYS:
                fetchRevenueDashboard(today.minusDays(6), today);
                break;
            case THIRTY_DAYS:
                fetchRevenueDashboard(today.minusDays(29), today);
                break;
            case CUSTOM:
                showCustomDateRangePicker();
                break;
        }
    }

    private void showCustomDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText("Chọn khoảng ngày")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) {
                return;
            }

            LocalDate fromDate = millisToLocalDate(selection.first);
            LocalDate toDate = millisToLocalDate(selection.second);
            updateRevenueFilterStyle(RevenueRange.CUSTOM);
            fetchRevenueDashboard(fromDate, toDate);
        });
        picker.show(getParentFragmentManager(), "revenue_date_range_picker");
    }

    private LocalDate millisToLocalDate(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void fetchRevenueDashboard(LocalDate fromDate, LocalDate toDate) {
        currentFromDate = fromDate;
        currentToDate = toDate;

        if (currentShopId == null || currentShopId.isEmpty()) {
            renderEmptyRevenue();
            return;
        }

        viewModel.fetchRevenueDashboard(
                currentShopId,
                fromDate.format(API_DATE_FORMAT),
                toDate.format(API_DATE_FORMAT),
                currentChartGroupBy
        );
    }

    private void updateRevenueFilterStyle(RevenueRange selectedRange) {
        for (TextView filter : Arrays.asList(
                filterToday,
                filterSevenDays,
                filterThirtyDays,
                filterCustom
        )) {
            filter.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            filter.setTextColor(Color.parseColor("#0B1C30"));
            filter.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        TextView selectedView;
        switch (selectedRange) {
            case TODAY:
                selectedView = filterToday;
                break;
            case THIRTY_DAYS:
                selectedView = filterThirtyDays;
                break;
            case CUSTOM:
                selectedView = filterCustom;
                break;
            case SEVEN_DAYS:
            default:
                selectedView = filterSevenDays;
                break;
        }

        selectedView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                Color.parseColor("#10B981")
        ));
        selectedView.setTextColor(Color.parseColor("#002113"));
        selectedView.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void updateChartGroupStyle() {
        for (TextView filter : Arrays.asList(chartGroupDay, chartGroupWeek, chartGroupMonth)) {
            filter.setBackgroundResource(R.drawable.round_corner_item);
            filter.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
            filter.setTextColor(Color.parseColor("#6C7A71"));
            filter.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        TextView selectedView;
        switch (currentChartGroupBy) {
            case "week":
                selectedView = chartGroupWeek;
                break;
            case "month":
                selectedView = chartGroupMonth;
                break;
            case "day":
            default:
                selectedView = chartGroupDay;
                break;
        }

        selectedView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        selectedView.setTextColor(Color.parseColor("#006C49"));
        selectedView.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void navigateTo(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateRevenueDashboard(RevenueDashboard dashboard) {
        if (dashboard == null) {
            renderEmptyRevenue();
            return;
        }

        RevenueDashboard.FixedKpis fixedKpis = dashboard.fixedKpis;
        RevenueDashboard.FilteredSummary summary = dashboard.filteredSummary;

        tvTodayRevenue.setText(formatCurrency(fixedKpis != null ? fixedKpis.todayRevenue : 0));
        tvMonthRevenue.setText(formatCurrency(fixedKpis != null ? fixedKpis.monthRevenue : 0));
        tvTotalRevenue.setText(formatCurrency(summary != null ? summary.grossRevenue : 0));
        tvNetRevenue.setText(formatCurrency(summary != null ? summary.netRevenue : 0));
        tvRefundAmount.setText(formatCurrency(summary != null ? summary.refundAmount : 0));
        tvTotalOrders.setText(String.format(
                Locale.getDefault(),
                "%,d đơn",
                summary != null ? summary.totalOrders : 0
        ));

        renderRevenueChart(dashboard.chart);
        recentOrdersAdapter.setOrders(dashboard.recentOrders);
        renderPaymentMethods(dashboard.paymentMethods);
    }

    private void renderEmptyRevenue() {
        tvTodayRevenue.setText(formatCurrency(0));
        tvMonthRevenue.setText(formatCurrency(0));
        tvTotalRevenue.setText(formatCurrency(0));
        tvNetRevenue.setText(formatCurrency(0));
        tvRefundAmount.setText(formatCurrency(0));
        tvTotalOrders.setText("0 đơn");
        revenueChartView.setPoints(null);
        recentOrdersAdapter.setOrders(null);
        renderPaymentMethods(null);
    }

    private void renderRevenueChart(RevenueDashboard.Chart chart) {
        revenueChartView.setPoints(buildChartPoints(chart));
    }

    private List<RevenueChartView.ChartPoint> buildChartPoints(RevenueDashboard.Chart chart) {
        List<RevenueChartView.ChartPoint> result = new ArrayList<>();
        if (currentFromDate == null || currentToDate == null) {
            return result;
        }

        Map<String, Double> revenueByPeriod = new HashMap<>();
        if (chart != null && chart.items != null) {
            for (RevenueDashboard.ChartItem item : chart.items) {
                if (item != null && item.period != null) {
                    revenueByPeriod.put(item.period, item.revenue);
                }
            }
        }

        String groupBy = chart != null && chart.groupBy != null ? chart.groupBy : currentChartGroupBy;
        switch (groupBy) {
            case "week":
                addWeeklyChartPoints(result, revenueByPeriod);
                break;
            case "month":
                addMonthlyChartPoints(result, revenueByPeriod);
                break;
            case "day":
            default:
                addDailyChartPoints(result, revenueByPeriod);
                break;
        }
        return result;
    }

    private void addDailyChartPoints(
            List<RevenueChartView.ChartPoint> result,
            Map<String, Double> revenueByPeriod
    ) {
        long dayCount = java.time.temporal.ChronoUnit.DAYS.between(currentFromDate, currentToDate) + 1;
        for (LocalDate date = currentFromDate; !date.isAfter(currentToDate); date = date.plusDays(1)) {
            String key = date.format(API_DATE_FORMAT);
            String label = dayCount <= 7 ? weekdayLabel(date) : date.format(DateTimeFormatter.ofPattern("dd/MM"));
            result.add(new RevenueChartView.ChartPoint(label, revenueByPeriod.getOrDefault(key, 0.0)));
        }
    }

    private void addWeeklyChartPoints(
            List<RevenueChartView.ChartPoint> result,
            Map<String, Double> revenueByPeriod
    ) {
        LocalDate weekStart = currentFromDate.minusDays(currentFromDate.getDayOfWeek().getValue() - 1L);
        LocalDate weekEnd = currentToDate.minusDays(currentToDate.getDayOfWeek().getValue() - 1L);
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusWeeks(1)) {
            String key = date.format(API_DATE_FORMAT);
            String label = date.format(DateTimeFormatter.ofPattern("dd/MM"));
            result.add(new RevenueChartView.ChartPoint(label, revenueByPeriod.getOrDefault(key, 0.0)));
        }
    }

    private void addMonthlyChartPoints(
            List<RevenueChartView.ChartPoint> result,
            Map<String, Double> revenueByPeriod
    ) {
        LocalDate monthStart = currentFromDate.withDayOfMonth(1);
        LocalDate monthEnd = currentToDate.withDayOfMonth(1);
        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusMonths(1)) {
            String key = date.format(API_DATE_FORMAT);
            String label = date.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            result.add(new RevenueChartView.ChartPoint(label, revenueByPeriod.getOrDefault(key, 0.0)));
        }
    }

    private String weekdayLabel(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY:
                return "T2";
            case TUESDAY:
                return "T3";
            case WEDNESDAY:
                return "T4";
            case THURSDAY:
                return "T5";
            case FRIDAY:
                return "T6";
            case SATURDAY:
                return "T7";
            case SUNDAY:
            default:
                return "CN";
        }
    }

    private void renderPaymentMethods(List<RevenueDashboard.PaymentMethod> paymentMethods) {
        paymentMethodsContainer.removeAllViews();
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("Chưa có giao dịch");
            emptyView.setTextColor(Color.parseColor("#6C7A71"));
            emptyView.setTextSize(12);
            paymentMethodsContainer.addView(emptyView);
            return;
        }

        for (int index = 0; index < paymentMethods.size(); index++) {
            RevenueDashboard.PaymentMethod method = paymentMethods.get(index);
            paymentMethodsContainer.addView(createPaymentMethodRow(method, index > 0));
        }
    }

    private View createPaymentMethodRow(RevenueDashboard.PaymentMethod method, boolean withTopMargin) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (withTopMargin) {
            rowParams.topMargin = dp(14);
        }
        row.setLayoutParams(rowParams);

        LinearLayout titleRow = new LinearLayout(requireContext());
        titleRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView methodName = new TextView(requireContext());
        methodName.setText("•  " + paymentMethodLabel(method.method));
        methodName.setTextColor(paymentMethodColor(method.method));
        methodName.setTextSize(12);
        titleRow.addView(methodName, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        TextView percentage = new TextView(requireContext());
        percentage.setText(String.format(Locale.getDefault(), "%.0f%%", method.percentage));
        percentage.setTextColor(Color.parseColor("#0B1C30"));
        percentage.setTextSize(12);
        titleRow.addView(percentage);
        row.addView(titleRow);

        LinearProgressIndicator progress = new LinearProgressIndicator(requireContext());
        progress.setMax(100);
        progress.setProgress((int) Math.round(method.percentage));
        progress.setIndicatorColor(paymentMethodColor(method.method));
        progress.setTrackColor(Color.parseColor("#E5EEFF"));
        progress.setTrackThickness(dp(6));
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        progressParams.topMargin = dp(7);
        row.addView(progress, progressParams);

        LinearLayout valueRow = new LinearLayout(requireContext());
        valueRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        valueRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        valueParams.topMargin = dp(3);

        TextView revenue = new TextView(requireContext());
        revenue.setText(formatCurrency(method.revenue));
        revenue.setTextColor(Color.parseColor("#B7C1BC"));
        revenue.setTextSize(10);
        valueRow.addView(revenue, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        TextView transactions = new TextView(requireContext());
        transactions.setText(String.format(Locale.getDefault(), "%,d GD", method.transactionCount));
        transactions.setTextColor(Color.parseColor("#B7C1BC"));
        transactions.setTextSize(10);
        valueRow.addView(transactions);
        row.addView(valueRow, valueParams);

        return row;
    }

    private void updateProductStats(List<Products> products) {
        if (products == null) return;

        View view = getView();
        if (view != null && products.size() >= 2) {
            updateInventoryItem(view, 0, products.get(0));
            updateInventoryItem(view, 1, products.get(1));
        }
    }

    private void updateInventoryItem(View parent, int index, Products product) {
        android.widget.GridLayout grid = parent.findViewById(R.id.inventory_grid);
        if (grid != null && index < grid.getChildCount()) {
            View card = grid.getChildAt(index);
            TextView tvName = card.findViewById(R.id.tvInventoryProductName);
            TextView tvCount = card.findViewById(R.id.tvInventoryStockCount);
            View indicator = card.findViewById(R.id.inventory_indicator);

            if (tvName != null) {
                String productName = product.name != null ? product.name : "";
                tvName.setText(productName.toUpperCase(Locale.ROOT));
            }
            if (tvCount != null) tvCount.setText(product.stock + " đv");
            if (indicator != null) {
                indicator.getBackground().setTint(product.stock < 10 ? 0xFFA33500 : 0xFF003D9B);
            }
        }
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "%,.0fđ", amount);
    }

    private String paymentMethodLabel(String method) {
        if (method == null) return "Khác";
        switch (method.toLowerCase(Locale.ROOT)) {
            case "momo":
                return "MoMo";
            case "vnpay":
                return "VNPay";
            case "cash":
                return "Tiền mặt";
            case "wallet":
                return "Ví";
            default:
                return method;
        }
    }

    private int paymentMethodColor(String method) {
        if (method == null) return Color.parseColor("#855300");
        switch (method.toLowerCase(Locale.ROOT)) {
            case "momo":
                return Color.parseColor("#A50064");
            case "vnpay":
                return Color.parseColor("#005BAA");
            case "cash":
                return Color.parseColor("#006C49");
            case "wallet":
                return Color.parseColor("#855300");
            default:
                return Color.parseColor("#3C4A42");
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
