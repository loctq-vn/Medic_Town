package com.example.medictown.ui.admin;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.Payments;
import com.example.medictown.databinding.FragmentAdminOrderDetailBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminOrderDetailFragment extends Fragment {
    private static final String ARG_ORDER = "order";
    private static final String ARG_SHOP_ID = "shop_id";

    private FragmentAdminOrderDetailBinding binding;
    private AdminViewModel viewModel;
    private AdminOrderDetailProductAdapter adapter;
    private Orders order;
    private String shopId;
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public static AdminOrderDetailFragment newInstance(Orders order, String shopId) {
        AdminOrderDetailFragment fragment = new AdminOrderDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER, order);
        args.putString(ARG_SHOP_ID, shopId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setNavBarsVisibility(false);
        }

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        shopId = getArguments() != null ? getArguments().getString(ARG_SHOP_ID) : null;
        if (shopId == null || shopId.isEmpty()) {
            shopId = new SessionManager(requireContext()).getCurrentShopId();
        }
        order = getArguments() != null ? (Orders) getArguments().getSerializable(ARG_ORDER) : null;

        adapter = new AdminOrderDetailProductAdapter();
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(adapter);
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        observeUpdates();
        bindOrder(order);
    }

    private void observeUpdates() {
        viewModel.getAllOrders().observe(getViewLifecycleOwner(), orders -> {
            if (orders == null || order == null) {
                return;
            }
            for (Orders updated : orders) {
                if (updated.id != null && updated.id.equals(order.id)) {
                    order = updated;
                    bindOrder(order);
                    return;
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindOrder(Orders order) {
        if (binding == null || order == null) {
            return;
        }

        binding.tvOrderId.setText(formatOrderCode(order.id));
        binding.tvStatus.setText(mapStatusText(order.status).toUpperCase(Locale.getDefault()));
        binding.tvStatus.getBackground().setTint(getStatusColor(order.status));
        binding.tvStatus.setTextColor(getStatusTextColor(order.status));
        binding.tvCreatedAt.setText(order.created_at != null
                ? "Ngày tạo: " + dateFormat.format(order.created_at)
                : "Ngày tạo: Chưa có thông tin");

        binding.tvRecipientName.setText("Người nhận: " + display(order.shipping_name));
        binding.tvPhone.setText("Số điện thoại: " + display(order.shipping_phone));
        binding.tvShippingAddress.setText("Địa chỉ: " + display(order.shipping_address));
        binding.tvNote.setText("Ghi chú: " + display(order.note));
        binding.tvPaymentMethod.setText("Phương thức: " + displayPaymentMethod(order.getPaymentMethod()));
        binding.tvPaymentStatus.setText("Trạng thái thanh toán: " + getPaymentStatusText(order.payments));
        binding.tvTotalAmount.setText(formatter.format(order.total_amount != null ? order.total_amount : 0));
        adapter.setItems(order.order_items);

        bindPrescription(order.prescription_url);
        bindActions(order);
        bindPhoneAction(order.shipping_phone);
    }

    private void bindActions(Orders order) {
        String status = order.status != null ? order.status.toLowerCase() : "pending";
        binding.layoutActions.setVisibility(View.VISIBLE);
        binding.btnCancelOrder.setVisibility(View.VISIBLE);
        binding.btnPrimaryAction.setVisibility(View.VISIBLE);

        switch (status) {
            case "pending":
                binding.btnPrimaryAction.setText("Xác nhận");
                binding.btnPrimaryAction.setOnClickListener(v -> updateStatus("confirmed"));
                binding.btnCancelOrder.setText("Hủy đơn");
                binding.btnCancelOrder.setOnClickListener(v -> updateStatus("cancelled"));
                break;
            case "confirmed":
                binding.btnPrimaryAction.setText("Giao hàng");
                binding.btnPrimaryAction.setOnClickListener(v -> updateStatus("shipping"));
                binding.btnCancelOrder.setText("Hủy đơn");
                binding.btnCancelOrder.setOnClickListener(v -> updateStatus("cancelled"));
                break;
            case "shipping":
                binding.btnPrimaryAction.setText("Hoàn thành");
                binding.btnPrimaryAction.setOnClickListener(v -> updateStatus("completed"));
                binding.btnCancelOrder.setText("Hủy đơn");
                binding.btnCancelOrder.setOnClickListener(v -> updateStatus("cancelled"));
                break;
            default:
                binding.layoutActions.setVisibility(View.GONE);
                break;
        }
    }

    private void updateStatus(String nextStatus) {
        if (shopId == null || shopId.isEmpty() || order == null || order.id == null) {
            Toast.makeText(getContext(), "Không thể cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.updateOrderStatus(shopId, order.id, nextStatus);
    }

    private void bindPhoneAction(String phone) {
        boolean hasPhone = phone != null && !phone.trim().isEmpty();
        binding.btnCall.setEnabled(hasPhone);
        binding.btnCall.setOnClickListener(v -> {
            if (!hasPhone) return;
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.trim()));
            startActivity(intent);
        });
    }

    private void bindPrescription(String prescriptionUrl) {
        if (prescriptionUrl == null || prescriptionUrl.trim().isEmpty()) {
            binding.tvPrescriptionTitle.setText("Đơn thuốc: Không có");
            binding.imgPrescription.setVisibility(View.GONE);
            return;
        }

        binding.tvPrescriptionTitle.setText("Đơn thuốc");
        binding.imgPrescription.setVisibility(View.VISIBLE);
        Glide.with(requireContext())
                .load(prescriptionUrl)
                .placeholder(R.drawable.ic_donthuoc)
                .error(R.drawable.ic_donthuoc)
                .into(binding.imgPrescription);
        binding.imgPrescription.setOnClickListener(v -> showPrescriptionPreview(prescriptionUrl));
    }

    private void showPrescriptionPreview(String prescriptionUrl) {
        Dialog dialog = new Dialog(requireContext());
        ImageView imageView = new ImageView(requireContext());
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        dialog.setContentView(imageView);
        Glide.with(requireContext())
                .load(prescriptionUrl)
                .placeholder(R.drawable.ic_donthuoc)
                .error(R.drawable.ic_donthuoc)
                .into(imageView);
        dialog.show();
    }

    private String getPaymentStatusText(List<Payments> payments) {
        Orders paymentOrder = new Orders();
        paymentOrder.payments = payments;
        return getPaymentStatusText(paymentOrder.getPrimaryPayment());
    }

    private String getPaymentStatusText(Payments payment) {
        if (payment == null || payment.status == null) {
            return "Chưa có thông tin";
        }
        switch (payment.status.toLowerCase()) {
            case "pending": return "Chưa thanh toán";
            case "processing": return "Đang xử lý";
            case "completed": return "Đã thanh toán";
            case "failed": return "Thất bại";
            case "refunded": return "Đã hoàn tiền";
            default: return payment.status;
        }
    }

    private String displayPaymentMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return "Chưa có thông tin";
        }
        if ("cash".equalsIgnoreCase(method) || "cod".equalsIgnoreCase(method)) return "Tiền mặt khi nhận hàng";
        if ("momo".equalsIgnoreCase(method)) return "MoMo";
        if ("vnpay".equalsIgnoreCase(method)) return "VNPay";
        return method;
    }

    private String mapStatusText(String status) {
        if (status == null) return "Chờ xác nhận";
        switch (status.toLowerCase()) {
            case "pending": return "Chờ xác nhận";
            case "confirmed": return "Đã xác nhận";
            case "shipping": return "Đang giao";
            case "completed": return "Hoàn thành";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return 0xFFC6E4F4;
        switch (status.toLowerCase()) {
            case "confirmed": return 0xFFFFE0B2; // Orange
            case "shipping": return 0xFF0052CC;
            case "completed": return 0xFFC8E6C9;
            case "cancelled": return 0xFFFFDAD6;
            default: return 0xFFC6E4F4;
        }
    }

    private int getStatusTextColor(String status) {
        if (status == null) return 0xFF2E4B57;
        switch (status.toLowerCase()) {
            case "confirmed": return 0xFFE65100; // Orange
            case "shipping": return 0xFFFFFFFF;
            case "completed": return 0xFF2E7D32;
            case "cancelled": return 0xFF93000A;
            default: return 0xFF2E4B57;
        }
    }

    private String formatOrderCode(String id) {
        if (id == null || id.isEmpty()) {
            return "#CE-UNKNOWN";
        }
        return "#CE-" + (id.length() > 8 ? id.substring(0, 8) : id);
    }

    private String display(String value) {
        return value == null || value.trim().isEmpty() ? "Chưa có thông tin" : value.trim();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setNavBarsVisibility(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setNavBarsVisibility(true);
        }
        binding = null;
    }
}
