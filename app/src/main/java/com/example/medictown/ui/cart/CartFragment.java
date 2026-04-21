package com.example.medictown.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medictown.R;

public class CartFragment extends Fragment {

    private CartViewModel mViewModel;

    private RecyclerView rvCartItems;
    private TextView tvTotalAmount;
    private Button btnCheckout;

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
        btnCheckout = view.findViewById(R.id.btnCheckout);

        mViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));

        // 4. Bắt sự kiện khi mày bấm vào nút Thanh toán
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạm thời để trống, sau này sẽ viết code chuyển màn hình ở đây
            }
        });
    }
}