package com.example.medictown.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;
import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.databinding.FragmentOrderDetailBinding;
import com.example.medictown.data.models.OrderItem;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.models.Reviews;
import com.example.medictown.data.repositories.ReviewRepository;
import com.example.medictown.ui.payment.PaymentFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.RatingBar;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetailFragment extends Fragment {
    private FragmentOrderDetailBinding binding;
    private HistoryViewModel viewModel;
    private ReviewRepository reviewRepository;
    private static final String ARG_ORDER_ID = "order_id";

    public static OrderDetailFragment newInstance(String orderId) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setNavBarsVisibility(false);
        }

        // Ensure we get the ViewModel from the Activity scope to access the same data as HistoryFragment
        viewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);
        reviewRepository = new ReviewRepository();

        String orderId = getArguments() != null ? getArguments().getString(ARG_ORDER_ID) : null;

        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        if (orderId != null) {
            setupRecyclerView();
            observeOrderDetails(orderId);
            
            // If the orders list is empty, fetch them
            if (viewModel.orders.getValue() == null || viewModel.orders.getValue().isEmpty()) {
                SessionManager sessionManager = new SessionManager(requireContext());
                if (sessionManager.isLoggedIn()) {
                    viewModel.fetchOrders(sessionManager.getUserId());
                }
            }
        }
    }

    private void setupRecyclerView() {
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void observeOrderDetails(String orderId) {
        // Use allOrders to find the specific order regardless of the current status filter
        viewModel.allOrders.observe(getViewLifecycleOwner(), orders -> {
            if (orders == null) return;
            for (com.example.medictown.data.models.Orders o : orders) {
                if (o.id.equals(orderId)) {
                    bindOrderData(o);
                    return;
                }
            }
        });
    }

    private void bindOrderData(com.example.medictown.data.models.Orders order) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        String displayId = order.id.length() > 8 ? order.id.substring(0, 8) : order.id;
        binding.tvOrderId.setText(getString(R.string.order_id_format, displayId));
        
        // Update Order Date
        if (order.created_at != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            binding.tvOrderDate.setText(sdf.format(order.created_at));
        }

        // Update Progress Bar based on status
        updateProgress(order.status);

        double totalAmount = order.total_amount != null ? order.total_amount : 0;
        binding.tvTotalAmount.setText(formatter.format(totalAmount));
        binding.tvSubtotal.setText(formatter.format(totalAmount));
        
        binding.tvRecipientName.setText(order.shipping_name != null ? order.shipping_name : "N/A");
        binding.tvRecipientPhone.setText(order.shipping_phone != null ? order.shipping_phone : "N/A");
        binding.tvShippingAddress.setText(order.shipping_address != null ? order.shipping_address : "N/A");
        binding.tvPaymentMethod.setText(order.payment_method != null ? order.payment_method.toUpperCase() : "COD");

        // Setup product list
        OrderDetailProductAdapter adapter = new OrderDetailProductAdapter(order.order_items);
        adapter.setOnReviewClickListener(order.status, this::showReviewDialog);
        binding.rvOrderItems.setAdapter(adapter);
        binding.btnReorder.setOnClickListener(v -> openPaymentForReorder(order));

        // Check which items are already reviewed
        if ("completed".equals(order.status) && order.order_items != null && !order.order_items.isEmpty()) {
            java.util.List<String> itemIds = new java.util.ArrayList<>();
            for (OrderItem item : order.order_items) {
                itemIds.add(item.id);
            }
            reviewRepository.getReviewsForOrderItems(itemIds, new Callback<List<Reviews>>() {
                @Override
                public void onResponse(Call<List<Reviews>> call, Response<List<Reviews>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        java.util.List<String> reviewedIds = new java.util.ArrayList<>();
                        for (Reviews r : response.body()) {
                            reviewedIds.add(r.order_item_id);
                        }
                        adapter.setReviewedItemIds(reviewedIds);
                    }
                }

                @Override
                public void onFailure(Call<List<Reviews>> call, Throwable t) {
                    // Ignore error for this check
                }
            });
        }
    }

    private void openPaymentForReorder(com.example.medictown.data.models.Orders order) {
        if (order.order_items == null || order.order_items.isEmpty()) {
            Toast.makeText(getContext(), "Đơn hàng không có sản phẩm để mua lại", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<CartItem> paymentItems = new ArrayList<>();
        for (OrderItem orderItem : order.order_items) {
            if (orderItem.product_id == null || orderItem.product_id.trim().isEmpty()) {
                continue;
            }

            CartItem cartItem = new CartItem();
            cartItem.product_id = orderItem.product_id;
            cartItem.quantity = Math.max(orderItem.quantity, 1);
            cartItem.products = buildProductFromOrderItem(orderItem);
            paymentItems.add(cartItem);
        }

        if (paymentItems.isEmpty()) {
            Toast.makeText(getContext(), "Không thể mua lại đơn hàng này", Toast.LENGTH_SHORT).show();
            return;
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, PaymentFragment.newInstance(
                        paymentItems,
                        order.payment_method,
                        order.note,
                        order.shipping_address
                ))
                .addToBackStack(null)
                .commit();
    }

    private Products buildProductFromOrderItem(OrderItem orderItem) {
        Products product = new Products();
        product.id = orderItem.product_id;
        product.name = orderItem.product_name != null ? orderItem.product_name : "Sản phẩm";
        product.price = orderItem.price;
        product.sale_price = null;
        product.unit = null;
        product.stock = Integer.MAX_VALUE;
        product.images = orderItem.product_image != null && !orderItem.product_image.trim().isEmpty()
                ? Collections.singletonList(orderItem.product_image)
                : new ArrayList<>();
        return product;
    }

    private void showReviewDialog(OrderItem item) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_review, null);
        dialog.setContentView(view);

        TextView tvName = view.findViewById(R.id.tvProductName);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etComment = view.findViewById(R.id.etComment);
        View btnSubmit = view.findViewById(R.id.btnSubmitReview);

        tvName.setText(item.product_name);

        btnSubmit.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            if (rating == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }

            SessionManager sessionManager = new SessionManager(requireContext());
            Reviews review = new Reviews();
            review.user_id = sessionManager.getUserId();
            review.product_id = item.product_id;
            review.order_item_id = item.id;
            review.rating = rating;
            review.comment = etComment.getText().toString().trim();
            review.created_at = new Date();

            btnSubmit.setEnabled(false);
            reviewRepository.submitReview(review, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        // Refresh order details to update review buttons
                        String orderId = getArguments() != null ? getArguments().getString(ARG_ORDER_ID) : null;
                        if (orderId != null) {
                            observeOrderDetails(orderId);
                        }
                    } else {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(getContext(), "Lỗi khi gửi đánh giá", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void updateProgress(String status) {
        int progress = 0;
        String statusText = "Chờ xác nhận";
        
        if (status == null) status = "pending";
        
        switch (status) {
            case "pending":
                progress = 20;
                statusText = "Chờ xác nhận";
                break;
            case "confirmed":
                progress = 50;
                statusText = "Đã xác nhận";
                break;
            case "shipping":
                progress = 80;
                statusText = "Đang giao hàng";
                break;
            case "completed":
                progress = 100;
                statusText = "Giao hàng thành công";
                break;
            case "cancelled":
                progress = 0;
                statusText = "Đã hủy";
                binding.orderProgressIndicator.setIndicatorColor(getResources().getColor(R.color.error, null));
                break;
        }
        
        binding.orderProgressIndicator.setProgress(progress);
        binding.tvProgressStatus.setText(statusText);
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
