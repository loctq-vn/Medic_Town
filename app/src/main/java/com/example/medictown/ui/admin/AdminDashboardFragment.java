package com.example.medictown.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.models.RevenueDailySummary;
import com.example.medictown.data.models.RevenueDashboard;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    private TextView tvRevenueTrend;
    private TextView tvTodayRevenue;
    private TextView tvTodayRevenueTrend;
    private TextView tvWeekRevenue;
    private TextView tvWeekRevenueTrend;
    private TextView tvMonthRevenue;
    private TextView tvMonthRevenueTrend;
    private TextView tvYearRevenue;
    private TextView tvYearRevenueTrend;
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
    private GridLayout topProductsContainer;
    private RecyclerView rvRecentOrders;
    private RecentOrdersAdapter recentOrdersAdapter;
    private String currentShopId;
    private LocalDate currentFromDate;
    private LocalDate currentToDate;
    private String currentChartGroupBy = "day";
    private List<RevenueDailySummary.Item> dailySummaryItems = new ArrayList<>();

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
        tvRevenueTrend = view.findViewById(R.id.tvRevenueTrend);
        tvTodayRevenue = view.findViewById(R.id.tvTodayRevenue);
        tvTodayRevenueTrend = view.findViewById(R.id.tvTodayRevenueTrend);
        tvWeekRevenue = view.findViewById(R.id.tvWeekRevenue);
        tvWeekRevenueTrend = view.findViewById(R.id.tvWeekRevenueTrend);
        tvMonthRevenue = view.findViewById(R.id.tvMonthRevenue);
        tvMonthRevenueTrend = view.findViewById(R.id.tvMonthRevenueTrend);
        tvYearRevenue = view.findViewById(R.id.tvYearRevenue);
        tvYearRevenueTrend = view.findViewById(R.id.tvYearRevenueTrend);
        tvNetRevenue = view.findViewById(R.id.tvNetRevenue);
        tvRefundAmount = view.findViewById(R.id.tvRefundAmount);
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
        topProductsContainer = view.findViewById(R.id.inventory_grid);
        rvRecentOrders = view.findViewById(R.id.rvRecentOrders);

        recentOrdersAdapter = new RecentOrdersAdapter();
        rvRecentOrders.setAdapter(recentOrdersAdapter);
        renderTopProducts(null);

        SessionManager sessionManager = new SessionManager(requireContext());
        String currentShopName = sessionManager.getCurrentShopName();
        if (currentShopName != null && !currentShopName.isEmpty()) {
            tvDashboardTitle.setText(currentShopName);
        }

        MaterialCardView btnManageOrders = view.findViewById(R.id.btnManageOrders);
        MaterialButton btnViewAllOrders = view.findViewById(R.id.btnViewAllOrders);
        MaterialButton btnQuickAddProduct = view.findViewById(R.id.btnQuickAddProduct);

        viewModel.getRevenueDailySummary().observe(getViewLifecycleOwner(), this::updateRevenueDailySummary);
        viewModel.getRevenueTopProducts().observe(getViewLifecycleOwner(), this::renderTopProducts);
        viewModel.getAllOrders().observe(getViewLifecycleOwner(), recentOrdersAdapter::setOrders);

        setupRevenueFilters();
        setupChartGroupFilters();
        updateChartGroupStyle();

        currentShopId = sessionManager.getCurrentShopId();
        if (currentShopId != null && !currentShopId.isEmpty()) {
            viewModel.fetchRevenueDailySummary(currentShopId);
            viewModel.fetchShopOrders(currentShopId);
            selectRevenueRange(RevenueRange.TODAY);
        } else {
            renderEmptyRevenue();
        }

        btnManageOrders.setOnClickListener(v -> navigateTo(new AdminOrdersFragment()));
        btnViewAllOrders.setOnClickListener(v -> navigateTo(new AdminOrdersFragment()));
        btnQuickAddProduct.setOnClickListener(v -> {
            if (currentFromDate != null && currentToDate != null) {
                navigateTo(AdminTopProductsFragment.newInstance(
                        currentFromDate.format(API_DATE_FORMAT),
                        currentToDate.format(API_DATE_FORMAT)
                ));
            } else {
                navigateTo(new AdminInventoryFragment());
            }
        });
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
            updateRevenueFromDailySummary();
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

        updateRevenueFromDailySummary();
        viewModel.fetchRevenueTopProducts(
                currentShopId,
                fromDate.format(API_DATE_FORMAT),
                toDate.format(API_DATE_FORMAT)
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

    private void updateRevenueDailySummary(RevenueDailySummary summary) {
        dailySummaryItems = summary != null && summary.items != null
                ? summary.items
                : new ArrayList<>();
        updateRevenueFromDailySummary();
    }

    private void updateRevenueFromDailySummary() {
        if (currentFromDate == null || currentToDate == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate yearStart = today.withDayOfYear(1);

        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeekStart = weekStart.minusWeeks(1);
        LocalDate lastWeekEnd = weekStart.minusDays(1);
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = monthStart.minusDays(1);
        LocalDate lastYearStart = yearStart.minusYears(1);
        LocalDate lastYearEnd = yearStart.minusDays(1);

        double todayRevenue = 0;
        double yesterdayRevenue = 0;
        double weekRevenue = 0;
        double lastWeekRevenue = 0;
        double monthRevenue = 0;
        double lastMonthRevenue = 0;
        double yearRevenue = 0;
        double lastYearRevenue = 0;
        double grossRevenue = 0;
        double previousGrossRevenue = 0;
        double refundAmount = 0;
        double netRevenue = 0;
        int totalOrders = 0;
        int completedOrders = 0;
        int pendingOrders = 0;
        int cancelledOrders = 0;
        Map<String, RevenueDashboard.PaymentMethod> paymentMethodTotals = new HashMap<>();

        long daysCount = ChronoUnit.DAYS.between(currentFromDate, currentToDate) + 1;
        LocalDate prevFromDate = currentFromDate.minusDays(daysCount);
        LocalDate prevToDate = currentFromDate.minusDays(1);

        for (RevenueDailySummary.Item item : dailySummaryItems) {
            LocalDate itemDate = parseSummaryDate(item);
            if (itemDate == null) {
                continue;
            }

            if (itemDate.isEqual(today)) {
                todayRevenue += item.grossRevenue;
            }
            if (itemDate.isEqual(yesterday)) {
                yesterdayRevenue += item.grossRevenue;
            }
            if (!itemDate.isBefore(weekStart) && !itemDate.isAfter(today)) {
                weekRevenue += item.grossRevenue;
            }
            if (!itemDate.isBefore(lastWeekStart) && !itemDate.isAfter(lastWeekEnd)) {
                lastWeekRevenue += item.grossRevenue;
            }
            if (!itemDate.isBefore(monthStart) && !itemDate.isAfter(today)) {
                monthRevenue += item.grossRevenue;
            }
            if (!itemDate.isBefore(lastMonthStart) && !itemDate.isAfter(lastMonthEnd)) {
                lastMonthRevenue += item.grossRevenue;
            }
            if (!itemDate.isBefore(yearStart) && !itemDate.isAfter(today)) {
                yearRevenue += item.grossRevenue;
            }
            if (!itemDate.isBefore(lastYearStart) && !itemDate.isAfter(lastYearEnd)) {
                lastYearRevenue += item.grossRevenue;
            }

            // Tính doanh thu kỳ trước
            if (!itemDate.isBefore(prevFromDate) && !itemDate.isAfter(prevToDate)) {
                previousGrossRevenue += item.grossRevenue;
            }

            if (itemDate.isBefore(currentFromDate) || itemDate.isAfter(currentToDate)) {
                continue;
            }

            grossRevenue += item.grossRevenue;
            refundAmount += item.refundAmount;
            netRevenue += item.netRevenue;
            totalOrders += item.totalOrders;
            completedOrders += item.completedOrders;
            pendingOrders += item.pendingOrders;
            cancelledOrders += item.cancelledOrders;
            addPaymentMethods(paymentMethodTotals, item.paymentMethods);
        }

        tvTodayRevenue.setText(formatCurrency(todayRevenue));
        updateTrendView(tvTodayRevenueTrend, todayRevenue, yesterdayRevenue);

        tvWeekRevenue.setText(formatCurrency(weekRevenue));
        updateTrendView(tvWeekRevenueTrend, weekRevenue, lastWeekRevenue);

        tvMonthRevenue.setText(formatCurrency(monthRevenue));
        updateTrendView(tvMonthRevenueTrend, monthRevenue, lastMonthRevenue);

        tvYearRevenue.setText(formatCurrency(yearRevenue));
        updateTrendView(tvYearRevenueTrend, yearRevenue, lastYearRevenue);

        tvTotalRevenue.setText(formatCurrency(grossRevenue));

        // Cập nhật xu hướng doanh thu
        if (previousGrossRevenue > 0) {
            double percentageChange = ((grossRevenue - previousGrossRevenue) / previousGrossRevenue) * 100;
            tvRevenueTrend.setVisibility(View.VISIBLE);
            
            if (percentageChange > 0) {
                tvRevenueTrend.setText(String.format(Locale.getDefault(), "↗ +%.0f%% so với kỳ trước", percentageChange));
            } else if (percentageChange < 0) {
                tvRevenueTrend.setText(String.format(Locale.getDefault(), "↘ %.0f%% so với kỳ trước", Math.abs(percentageChange)));
            } else {
                tvRevenueTrend.setText("0% so với kỳ trước");
            }
        } else {
            tvRevenueTrend.setVisibility(View.GONE);
        }

        tvNetRevenue.setText(formatCurrency(netRevenue));
        tvRefundAmount.setText(formatCurrency(refundAmount));
        tvTotalOrders.setText(String.format(Locale.getDefault(), "%,d \u0111\u01a1n", totalOrders));

        revenueChartView.setPoints(buildChartPointsFromDailySummary());
        renderPaymentMethods(buildPaymentMethodSummary(paymentMethodTotals, grossRevenue));
    }

    private LocalDate parseSummaryDate(RevenueDailySummary.Item item) {
        if (item == null || item.date == null) {
            return null;
        }
        try {
            return LocalDate.parse(item.date, API_DATE_FORMAT);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void addPaymentMethods(
            Map<String, RevenueDashboard.PaymentMethod> totals,
            List<RevenueDashboard.PaymentMethod> paymentMethods
    ) {
        if (paymentMethods == null) {
            return;
        }

        for (RevenueDashboard.PaymentMethod method : paymentMethods) {
            if (method == null) {
                continue;
            }
            String key = method.method != null ? method.method : "unknown";
            RevenueDashboard.PaymentMethod total = totals.get(key);
            if (total == null) {
                total = new RevenueDashboard.PaymentMethod();
                total.method = key;
                totals.put(key, total);
            }
            total.revenue += method.revenue;
            total.transactionCount += method.transactionCount;
        }
    }

    private List<RevenueDashboard.PaymentMethod> buildPaymentMethodSummary(
            Map<String, RevenueDashboard.PaymentMethod> totals,
            double totalRevenue
    ) {
        List<RevenueDashboard.PaymentMethod> result = new ArrayList<>(totals.values());
        for (RevenueDashboard.PaymentMethod method : result) {
            method.percentage = totalRevenue > 0 ? (method.revenue / totalRevenue) * 100 : 0;
        }
        result.sort((left, right) -> Double.compare(right.revenue, left.revenue));
        return result;
    }

    private void updateRevenueDashboard(RevenueDashboard dashboard) {
        if (dashboard == null) {
            renderEmptyRevenue();
            return;
        }

        RevenueDashboard.FixedKpis fixedKpis = dashboard.fixedKpis;
        RevenueDashboard.FilteredSummary summary = dashboard.filteredSummary;

        tvTodayRevenue.setText(formatCurrency(fixedKpis != null ? fixedKpis.todayRevenue : 0));
        tvWeekRevenue.setText(formatCurrency(0));
        tvMonthRevenue.setText(formatCurrency(fixedKpis != null ? fixedKpis.monthRevenue : 0));
        tvYearRevenue.setText(formatCurrency(0));
        tvTotalRevenue.setText(formatCurrency(summary != null ? summary.grossRevenue : 0));
        tvNetRevenue.setText(formatCurrency(summary != null ? summary.netRevenue : 0));
        tvRefundAmount.setText(formatCurrency(summary != null ? summary.refundAmount : 0));
        tvTotalOrders.setText(String.format(
                Locale.getDefault(),
                "%,d đơn",
                summary != null ? summary.totalOrders : 0
        ));

        renderRevenueChart(dashboard.chart);
        renderPaymentMethods(dashboard.paymentMethods);
        renderTopProducts(dashboard.topProducts);
    }

    private void updateTrendView(TextView trendView, double current, double previous) {
        if (trendView == null) return;
        if (previous > 0) {
            double percentageChange = ((current - previous) / previous) * 100;
            trendView.setVisibility(View.VISIBLE);
            if (percentageChange > 0) {
                trendView.setText(String.format(Locale.getDefault(), "↑ +%.0f%%", percentageChange));
            } else if (percentageChange < 0) {
                trendView.setText(String.format(Locale.getDefault(), "↓ %.0f%%", Math.abs(percentageChange)));
            } else {
                trendView.setText("0%");
            }
        } else {
            trendView.setVisibility(View.GONE);
        }
    }

    private void renderEmptyRevenue() {
        tvTodayRevenue.setText(formatCurrency(0));
        if (tvTodayRevenueTrend != null) tvTodayRevenueTrend.setVisibility(View.GONE);
        tvWeekRevenue.setText(formatCurrency(0));
        if (tvWeekRevenueTrend != null) tvWeekRevenueTrend.setVisibility(View.GONE);
        tvMonthRevenue.setText(formatCurrency(0));
        if (tvMonthRevenueTrend != null) tvMonthRevenueTrend.setVisibility(View.GONE);
        tvYearRevenue.setText(formatCurrency(0));
        if (tvYearRevenueTrend != null) tvYearRevenueTrend.setVisibility(View.GONE);
        tvTotalRevenue.setText(formatCurrency(0));
        if (tvRevenueTrend != null) tvRevenueTrend.setVisibility(View.GONE);
        tvNetRevenue.setText(formatCurrency(0));
        tvRefundAmount.setText(formatCurrency(0));
        tvTotalOrders.setText("0 đơn");
        revenueChartView.setPoints(null);
        recentOrdersAdapter.setOrders(null);
        renderPaymentMethods(null);
        renderTopProducts(null);
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

    private List<RevenueChartView.ChartPoint> buildChartPointsFromDailySummary() {
        List<RevenueChartView.ChartPoint> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate chartStart;
        LocalDate chartEnd;

        Map<String, Double> revenueByPeriod = new HashMap<>();
        switch (currentChartGroupBy) {
            case "week":
                chartEnd = weekStart(today);
                chartStart = chartEnd.minusWeeks(3);
                break;
            case "month":
                chartEnd = today.withDayOfMonth(1);
                chartStart = chartEnd.minusMonths(11);
                break;
            case "day":
            default:
                chartEnd = today;
                chartStart = today.minusDays(6);
                break;
        }

        for (RevenueDailySummary.Item item : dailySummaryItems) {
            LocalDate itemDate = parseSummaryDate(item);
            if (itemDate == null || itemDate.isBefore(chartStart) || itemDate.isAfter(today)) {
                continue;
            }

            String key = chartPeriodKey(itemDate);
            revenueByPeriod.put(key, revenueByPeriod.getOrDefault(key, 0.0) + item.grossRevenue);
        }

        switch (currentChartGroupBy) {
            case "week":
                addFixedWeeklyChartPoints(result, revenueByPeriod, chartStart, chartEnd);
                break;
            case "month":
                addFixedMonthlyChartPoints(result, revenueByPeriod, chartStart, chartEnd);
                break;
            case "day":
            default:
                addFixedDailyChartPoints(result, revenueByPeriod, chartStart, chartEnd);
                break;
        }
        return result;
    }

    private String chartPeriodKey(LocalDate date) {
        switch (currentChartGroupBy) {
            case "week":
                return weekStart(date).format(API_DATE_FORMAT);
            case "month":
                return date.withDayOfMonth(1).format(API_DATE_FORMAT);
            case "day":
            default:
                return date.format(API_DATE_FORMAT);
        }
    }

    private LocalDate weekStart(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1L);
    }

    private void addFixedDailyChartPoints(
            List<RevenueChartView.ChartPoint> result,
            Map<String, Double> revenueByPeriod,
            LocalDate start,
            LocalDate end
    ) {
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String key = date.format(API_DATE_FORMAT);
            result.add(new RevenueChartView.ChartPoint(
                    weekdayLabel(date),
                    revenueByPeriod.getOrDefault(key, 0.0)
            ));
        }
    }

    private void addFixedWeeklyChartPoints(
            List<RevenueChartView.ChartPoint> result,
            Map<String, Double> revenueByPeriod,
            LocalDate start,
            LocalDate end
    ) {
        for (LocalDate date = start; !date.isAfter(end); date = date.plusWeeks(1)) {
            String key = date.format(API_DATE_FORMAT);
            String label = date.format(DateTimeFormatter.ofPattern("dd/MM"));
            result.add(new RevenueChartView.ChartPoint(label, revenueByPeriod.getOrDefault(key, 0.0)));
        }
    }

    private void addFixedMonthlyChartPoints(
            List<RevenueChartView.ChartPoint> result,
            Map<String, Double> revenueByPeriod,
            LocalDate start,
            LocalDate end
    ) {
        for (LocalDate date = start; !date.isAfter(end); date = date.plusMonths(1)) {
            String key = date.format(API_DATE_FORMAT);
            String label = date.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            result.add(new RevenueChartView.ChartPoint(label, revenueByPeriod.getOrDefault(key, 0.0)));
        }
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

    private void renderTopProducts(List<RevenueDashboard.TopProduct> topProducts) {
        if (topProductsContainer == null) return;

        topProductsContainer.removeAllViews();
        if (topProducts == null || topProducts.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("Ch\u01b0a c\u00f3 s\u1ea3n ph\u1ea9m b\u00e1n trong kho\u1ea3ng n\u00e0y");
            emptyView.setTextColor(Color.parseColor("#6C7A71"));
            emptyView.setTextSize(12);
            topProductsContainer.addView(emptyView, new GridLayout.LayoutParams());
            return;
        }

        for (int index = 0; index < topProducts.size(); index++) {
            topProductsContainer.addView(createTopProductRow(topProducts.get(index), index + 1));
        }
    }

    private View createTopProductRow(RevenueDashboard.TopProduct product, int rank) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);

        GridLayout.LayoutParams rowParams = new GridLayout.LayoutParams();
        rowParams.width = GridLayout.LayoutParams.MATCH_PARENT;
        rowParams.height = dp(64);
        if (rank > 1) {
            rowParams.topMargin = dp(6);
        }
        row.setLayoutParams(rowParams);

        ImageView image = new ImageView(requireContext());
        image.setBackgroundResource(R.drawable.bg_buy_sheet_image);
        image.setClipToOutline(true);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this)
                .load(product.productImage)
                .placeholder(R.drawable.ic_medicine_placeholder)
                .error(R.drawable.ic_medicine_placeholder)
                .into(image);
        row.addView(image, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout textColumn = new LinearLayout(requireContext());
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        textParams.setMargins(dp(12), 0, dp(10), 0);

        TextView name = new TextView(requireContext());
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        name.setIncludeFontPadding(false);
        name.setMaxLines(1);
        name.setText(product.productName != null && !product.productName.trim().isEmpty()
                ? product.productName
                : "S\u1ea3n ph\u1ea9m");
        name.setTextColor(Color.parseColor("#0B1C30"));
        name.setTextSize(13);
        textColumn.addView(name, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView stock = new TextView(requireContext());
        stock.setIncludeFontPadding(false);
        stock.setMaxLines(1);
        stock.setText(String.format(Locale.getDefault(), "Kho: %,d", product.stock));
        stock.setTextColor(Color.parseColor("#8A9990"));
        stock.setTextSize(11);
        LinearLayout.LayoutParams stockParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        stockParams.topMargin = dp(3);
        textColumn.addView(stock, stockParams);
        row.addView(textColumn, textParams);

        LinearLayout valueColumn = new LinearLayout(requireContext());
        valueColumn.setGravity(android.view.Gravity.END);
        valueColumn.setOrientation(LinearLayout.VERTICAL);

        TextView revenue = new TextView(requireContext());
        revenue.setEllipsize(android.text.TextUtils.TruncateAt.END);
        revenue.setIncludeFontPadding(false);
        revenue.setMaxLines(1);
        revenue.setText(formatCurrency(product.revenue));
        revenue.setTextColor(Color.parseColor("#0B1C30"));
        revenue.setTextSize(12);
        revenue.setTypeface(null, android.graphics.Typeface.BOLD);
        valueColumn.addView(revenue, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView sold = new TextView(requireContext());
        sold.setBackgroundResource(R.drawable.round_corner_item);
        sold.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#D8F8E8")));
        sold.setIncludeFontPadding(false);
        sold.setMaxLines(1);
        sold.setPadding(dp(8), dp(3), dp(8), dp(3));
        sold.setText(String.format(Locale.getDefault(), "\u0110\u00e3 b\u00e1n %,d", product.quantitySold));
        sold.setTextColor(Color.parseColor("#006C49"));
        sold.setTextSize(10);
        LinearLayout.LayoutParams soldParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        soldParams.topMargin = dp(3);
        valueColumn.addView(sold, soldParams);

        row.addView(valueColumn, new LinearLayout.LayoutParams(dp(104), LinearLayout.LayoutParams.WRAP_CONTENT));
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
