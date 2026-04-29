package com.example.medictown.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medictown.R;
import com.example.medictown.data.models.Products;
import java.text.DecimalFormat;

public class CartFragment extends Fragment {

    private CartViewModel mViewModel;
    private CartAdapter adapter;
    private RecyclerView rvCartItems;
    private TextView tvTotalAmount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCartItems = view.findViewById(R.id.rvCartItems);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);

        // Khởi tạo ViewModel
        mViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        // Khởi tạo và thiết lập Adapter
        setupRecyclerView();

        // Lắng nghe dữ liệu thay đổi để update giao diện
        observeViewModel();

        // --- CHẠY THỬ XEM CÓ HIỂN THỊ KHÔNG ---
        taoDuLieuGiaDeTest();
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(new CartAdapter.CartListener() {
            @Override
            public void onIncrease(String productId) {
                mViewModel.changeQuantity(productId, 1); // Báo ViewModel cộng 1
            }

            @Override
            public void onDecrease(String productId) {
                mViewModel.changeQuantity(productId, -1); // Báo ViewModel trừ 1
            }

            @Override
            public void onDelete(String productId) {
                mViewModel.removeItem(productId); // Báo ViewModel xóa
            }
        });

        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Mỗi khi danh sách thay đổi (vì bị bấm + hoặc -), ném danh sách mới vào Adapter
        mViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
        });

        // Mỗi khi tổng tiền thay đổi, in lại chữ tổng tiền
        mViewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            DecimalFormat df = new DecimalFormat("###,###,###");
            tvTotalAmount.setText(df.format(total) + "đ");
        });
    }

    // Hàm tạo 2 sản phẩm ảo để test chức năng trên màn hình ngay lập tức
    private void taoDuLieuGiaDeTest() {
        if (mViewModel.getCartItems().getValue().isEmpty()) {
            Products p1 = new Products();
            p1.id = "p01";
            p1.name = "Panadol Extra (Vỉ 12 Viên)";
            p1.manufacturer = "GSK (Anh)";
            p1.price = 24000;

            Products p2 = new Products();
            p2.id = "p02";
            p2.name = "Khẩu trang N95";
            p2.manufacturer = "3M";
            p2.price = 35000;
            p2.sale_price = 29000.0; // Đang giảm giá

            mViewModel.addDummyData(p1);
            mViewModel.addDummyData(p2);
        }
    }
}