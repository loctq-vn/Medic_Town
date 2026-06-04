package com.example.medictown.ui.payment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Address;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.databinding.FragmentPaymentBinding;
import com.example.medictown.databinding.LayoutAddressBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentFragment extends Fragment {

    private static final String ARG_SELECTED_ITEMS = "selected_items";
    private static final String ARG_PAYMENT_METHOD = "payment_method";
    private static final String ARG_NOTE = "note";
    private static final String ARG_SHIPPING_ADDRESS = "shipping_address";

    private FragmentPaymentBinding binding;
    private PaymentViewModel viewModel;
    private PaymentProductAdapter adapter;
    private SessionManager sessionManager;
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private String prefillShippingAddress;
    private boolean prefillAddressApplied = false;

    public static PaymentFragment newInstance(List<CartItem> selectedItems) {
        PaymentFragment fragment = new PaymentFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SELECTED_ITEMS, new ArrayList<>(selectedItems));
        fragment.setArguments(args);
        return fragment;
    }

    public static PaymentFragment newInstance(
            List<CartItem> selectedItems,
            String paymentMethod,
            String note,
            String shippingAddress
    ) {
        PaymentFragment fragment = newInstance(selectedItems);
        Bundle args = fragment.getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(ARG_PAYMENT_METHOD, paymentMethod);
        args.putString(ARG_NOTE, note);
        args.putString(ARG_SHIPPING_ADDRESS, shippingAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPaymentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Hide BottomNavigationView and AppBar from MainActivity
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            View appBar = getActivity().findViewById(R.id.app_bar_main);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
            if (appBar != null) appBar.setVisibility(View.GONE);
        }

        viewModel = new ViewModelProvider(this).get(PaymentViewModel.class);
        sessionManager = new SessionManager(requireContext());
        
        setupRecyclerView();
        observeViewModel();
        
        if (getArguments() != null) {
            @SuppressWarnings("unchecked")
            List<CartItem> items = (List<CartItem>) getArguments().getSerializable(ARG_SELECTED_ITEMS);
            if (items != null) {
                viewModel.setSelectedItems(items);
            }
            prefillShippingAddress = getArguments().getString(ARG_SHIPPING_ADDRESS);
        }

        if (sessionManager.isLoggedIn()) {
            viewModel.fetchAddresses(sessionManager.getUserId());
        }

        binding.cardAddress.setOnClickListener(v -> showAddressSelectionDialog());

        binding.btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.btnConfirmPayment.setOnClickListener(v -> {
            String paymentMethod = getSelectedPaymentMethod();
            String note = binding.edtNote.getText() != null ? binding.edtNote.getText().toString() : "";
            viewModel.placeOrder(sessionManager.getUserId(), paymentMethod, note);
        });

        // Xử lý chọn duy nhất 1 phương thức thanh toán
        setupPaymentMethodSelection();
        applyOrderPrefill();
    }

    private void applyOrderPrefill() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        String paymentMethod = args.getString(ARG_PAYMENT_METHOD);
        if (paymentMethod != null) {
            if ("fake_momo".equalsIgnoreCase(paymentMethod)) {
                binding.rbFakeMomo.setChecked(true);
            } else if ("fake_vnpay".equalsIgnoreCase(paymentMethod)) {
                binding.rbFakeVnpay.setChecked(true);
            } else if ("momo".equalsIgnoreCase(paymentMethod)) {
                binding.rbMomo.setChecked(true);
            } else {
                binding.rbCod.setChecked(true);
            }
        }

        String note = args.getString(ARG_NOTE);
        if (note != null) {
            binding.edtNote.setText(note);
        }
        updatePaymentMethodUI();
    }

    private void navigateToHistory() {
        if (getActivity() != null) {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                // Xóa fragment thanh toán khỏi backstack để khi nhấn back không quay lại đây
                getParentFragmentManager().popBackStack();
                // Chuyển sang tab Lịch sử (điều này sẽ kích hoạt listener trong MainActivity)
                bottomNav.setSelectedItemId(R.id.nav_history);
            }
        }
    }

    private void setupPaymentMethodSelection() {
        binding.rbCod.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.rbMomo.setChecked(false);
                binding.rbFakeMomo.setChecked(false);
                binding.rbFakeVnpay.setChecked(false);
                updatePaymentMethodUI();
            }
        });

        binding.rbMomo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.rbCod.setChecked(false);
                binding.rbFakeMomo.setChecked(false);
                binding.rbFakeVnpay.setChecked(false);
                updatePaymentMethodUI();
            }
        });

        binding.rbFakeMomo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.rbCod.setChecked(false);
                binding.rbMomo.setChecked(false);
                binding.rbFakeVnpay.setChecked(false);
                updatePaymentMethodUI();
            }
        });

        binding.rbFakeVnpay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.rbCod.setChecked(false);
                binding.rbMomo.setChecked(false);
                binding.rbFakeMomo.setChecked(false);
                updatePaymentMethodUI();
            }
        });

        // Cho phép bấm vào cả vùng Card để chọn
        binding.cardCod.setOnClickListener(v -> binding.rbCod.setChecked(true));
        binding.cardMomo.setOnClickListener(v -> binding.rbMomo.setChecked(true));
        binding.cardFakeMomo.setOnClickListener(v -> binding.rbFakeMomo.setChecked(true));
        binding.cardFakeVnpay.setOnClickListener(v -> binding.rbFakeVnpay.setChecked(true));
        
        // Khởi tạo UI ban đầu
        updatePaymentMethodUI();
    }

    private String getSelectedPaymentMethod() {
        if (binding.rbFakeMomo.isChecked()) {
            return "fake_momo";
        }
        if (binding.rbFakeVnpay.isChecked()) {
            return "fake_vnpay";
        }
        if (binding.rbMomo.isChecked()) {
            return "Momo";
        }
        return "COD";
    }

    private void updatePaymentMethodUI() {
        // Sử dụng ContextCompat để tránh lỗi deprecated
        int primaryColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary);
        int outlineColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.outline_variant);

        // Highlight thẻ đang được chọn
        binding.cardCod.setStrokeColor(binding.rbCod.isChecked() ? primaryColor : outlineColor);
        binding.cardCod.setStrokeWidth(binding.rbCod.isChecked() ? 4 : 2);

        binding.cardMomo.setStrokeColor(binding.rbMomo.isChecked() ? primaryColor : outlineColor);
        binding.cardMomo.setStrokeWidth(binding.rbMomo.isChecked() ? 4 : 2);

        binding.cardFakeMomo.setStrokeColor(binding.rbFakeMomo.isChecked() ? primaryColor : outlineColor);
        binding.cardFakeMomo.setStrokeWidth(binding.rbFakeMomo.isChecked() ? 4 : 2);

        binding.cardFakeVnpay.setStrokeColor(binding.rbFakeVnpay.isChecked() ? primaryColor : outlineColor);
        binding.cardFakeVnpay.setStrokeWidth(binding.rbFakeVnpay.isChecked() ? 4 : 2);
    }

    private void showAddressSelectionDialog() {
        List<Address> addresses = viewModel.addresses.getValue();
        if (addresses == null || addresses.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa có địa chỉ nào. Vui lòng thêm địa chỉ trong hồ sơ.", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        LayoutAddressBottomSheetBinding sheetBinding = LayoutAddressBottomSheetBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        AddressSelectionAdapter selectionAdapter = new AddressSelectionAdapter();
        sheetBinding.rvAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        sheetBinding.rvAddresses.setAdapter(selectionAdapter);
        selectionAdapter.setData(addresses, viewModel.selectedAddress.getValue());

        selectionAdapter.setOnAddressSelectedListener(address -> {
            viewModel.selectAddress(address);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void setupRecyclerView() {
        adapter = new PaymentProductAdapter();
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.selectedItems.observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
        });

        viewModel.selectedAddress.observe(getViewLifecycleOwner(), address -> {
            if (address != null) {
                binding.tvRecipientName.setText(address.recipient_name + " | " + address.phone_number);
                binding.tvAddressDetail.setText(address.location);
            }
        });

        viewModel.addresses.observe(getViewLifecycleOwner(), addresses -> {
            if (prefillAddressApplied || prefillShippingAddress == null || prefillShippingAddress.trim().isEmpty()
                    || addresses == null || addresses.isEmpty()) {
                return;
            }
            for (Address address : addresses) {
                if (address.location != null && address.location.equals(prefillShippingAddress)) {
                    prefillAddressApplied = true;
                    viewModel.selectAddress(address);
                    return;
                }
            }
            prefillAddressApplied = true;
        });

        viewModel.subtotal.observe(getViewLifecycleOwner(), subtotal -> {
            binding.tvSubtotalValue.setText(formatter.format(subtotal));
        });

        viewModel.totalAmount.observe(getViewLifecycleOwner(), total -> {
            binding.tvTotalValue.setText(formatter.format(total));
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnConfirmPayment.setEnabled(!isLoading);
            binding.btnConfirmPayment.setText(isLoading ? "Đang xử lý..." : "Xác nhận đặt hàng");
        });

        viewModel.orderSuccess.observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                navigateToHistory();
            }
        });

        viewModel.momoPaymentTarget.observe(getViewLifecycleOwner(), paymentTarget -> {
            if (paymentTarget != null) {
                openMomoPayment(paymentTarget);
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openMomoPayment(PaymentViewModel.MomoPaymentTarget paymentTarget) {
        if (tryOpenPaymentUrl(paymentTarget.primaryUrl)) {
            Toast.makeText(getContext(), "Dang chuyen sang MoMo de thanh toan", Toast.LENGTH_SHORT).show();
            return;
        }

        if (paymentTarget.fallbackUrl != null && tryOpenPaymentUrl(paymentTarget.fallbackUrl)) {
            Toast.makeText(getContext(), "Dang mo trang thanh toan MoMo", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Khong mo duoc ung dung/link MoMo", Toast.LENGTH_LONG).show();
    }

    private boolean tryOpenPaymentUrl(String paymentUrl) {
        if (paymentUrl == null || paymentUrl.trim().isEmpty()) {
            return false;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show BottomNavigationView and AppBar when leaving
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            View appBar = getActivity().findViewById(R.id.app_bar_main);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
            if (appBar != null) appBar.setVisibility(View.VISIBLE);
        }
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setNavBarsVisibility(false);
        }
    }
}
