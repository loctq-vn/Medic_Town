package com.example.medictown.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Orders;
import com.example.medictown.ui.admin.AdminOrdersAdapter;
import com.example.medictown.ui.admin.AdminViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.datepicker.MaterialDatePicker;
import androidx.core.util.Pair;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.example.medictown.R;
import android.widget.TextView;

public class AdminOrdersFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false);
    }

    private AdminViewModel viewModel;
    private AdminOrdersAdapter adapter;
    private List<Orders> allOrdersList = new ArrayList<>();
    private String currentShopId;
    private LocalDate startDate;
    private LocalDate endDate;
    private TextView tvDateRange;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd 'Th'MM, yyyy", new Locale("vi", "VN"));

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        currentShopId = new SessionManager(requireContext()).getCurrentShopId();
        adapter = new AdminOrdersAdapter();

        tvDateRange = view.findViewById(R.id.tvDateRange);
        View dateFilterContainer = (View) tvDateRange.getParent().getParent().getParent(); // The MaterialCardView
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        // Mặc định từ 01/01/2026 đến ngày hiện tại
        startDate = LocalDate.of(2026, 1, 1);
        endDate = LocalDate.now();
        updateDateRangeDisplay();

        dateFilterContainer.setOnClickListener(v -> showDateRangePicker());
        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            if (tabLayout != null) {
                TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
                if (tab != null && tab.getText() != null) {
                    filterOrders(tab.getText().toString());
                }
            }
        });

        RecyclerView rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null && tab.getText() != null) {
                    filterOrders(tab.getText().toString());
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        adapter.setOnOrderActionListener(new AdminOrdersAdapter.OnOrderActionListener() {
            @Override
            public void onQuickAction(Orders order) {
                String nextStatus = "";
                if ("pending".equalsIgnoreCase(order.status)) nextStatus = "confirmed";
                else if ("confirmed".equalsIgnoreCase(order.status)) nextStatus = "shipping";
                else if ("shipping".equalsIgnoreCase(order.status)) nextStatus = "completed";
                
                if (!nextStatus.isEmpty()) {
                    if (currentShopId != null && !currentShopId.isEmpty()) {
                        viewModel.updateOrderStatus(currentShopId, order.id, nextStatus);
                    }
                }
            }

            @Override
            public void onDetails(Orders order) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, AdminOrderDetailFragment.newInstance(order, currentShopId))
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onCancel(Orders order) {
                if (currentShopId != null && !currentShopId.isEmpty()) {
                    viewModel.updateOrderStatus(currentShopId, order.id, "cancelled");
                }
            }
        });

        viewModel.getAllOrders().observe(getViewLifecycleOwner(), orders -> {
            allOrdersList = orders;
            if (tabLayout != null) {
                TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
                if (tab != null && tab.getText() != null) {
                    filterOrders(tab.getText().toString());
                }
            }
        });

        if (currentShopId != null && !currentShopId.isEmpty()) {
            viewModel.fetchShopOrders(currentShopId);
        } else {
            viewModel.fetchAllOrders();
        }
    }

    private void filterOrders(String status) {
        List<Orders> filtered;
        
        // Lọc theo ngày trước
        filtered = allOrdersList.stream()
                .filter(o -> {
                    if (o.created_at == null) return false;
                    LocalDate orderDate = o.created_at.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    
                    // Kiểm tra orderDate nằm trong [startDate, endDate]
                    // Lưu ý: Nếu startDate > endDate, có thể không có kết quả
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        // Sau đó lọc theo trạng thái
        if (!"Tất cả".equalsIgnoreCase(status) && !"All".equalsIgnoreCase(status)) {
            String mappedStatus = mapStatus(status);
            filtered = filtered.stream()
                    .filter(o -> mappedStatus.equalsIgnoreCase(o.status))
                    .collect(Collectors.toList());
        }

        adapter.setOrders(filtered);
        updateOrderCount(filtered.size());
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Chọn khoảng thời gian")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && selection.first != null && selection.second != null) {
                startDate = Instant.ofEpochMilli(selection.first).atZone(ZoneId.systemDefault()).toLocalDate();
                endDate = Instant.ofEpochMilli(selection.second).atZone(ZoneId.systemDefault()).toLocalDate();
                updateDateRangeDisplay();
                
                View view = getView();
                if (view != null) {
                    TabLayout tl = view.findViewById(R.id.tabLayout);
                    if (tl != null) {
                        TabLayout.Tab tab = tl.getTabAt(tl.getSelectedTabPosition());
                        if (tab != null && tab.getText() != null) {
                            filterOrders(tab.getText().toString());
                        }
                    }
                }
            }
        });
        picker.show(getChildFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void updateDateRangeDisplay() {
        if (tvDateRange != null) {
            String formatted = startDate.format(DATE_FORMAT) + " - " + endDate.format(DATE_FORMAT);
            tvDateRange.setText(formatted);
        }
    }

    private String mapStatus(String tabText) {
        switch (tabText) {
            case "Chờ xác nhận": return "pending";
            case "Đã xác nhận": return "confirmed";
            case "Đang giao": return "shipping";
            case "Hoàn thành": return "completed";
            case "Đã hủy": return "cancelled";
            default: return tabText.toLowerCase();
        }
    }

    private void updateOrderCount(int count) {
        View view = getView();
        if (view != null) {
            android.widget.TextView tvOrderCount = view.findViewById(R.id.tvOrderCount);
            if (tvOrderCount != null) {
                tvOrderCount.setText("Tổng cộng " + count + " đơn hàng");
            }
        }
    }
}
