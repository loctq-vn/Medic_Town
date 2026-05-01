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

        mViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(new CartAdapter.CartListener() {
            @Override
            public void onIncrease(String cartItemId) { mViewModel.changeQuantity(cartItemId, 1); }
            @Override
            public void onDecrease(String cartItemId) { mViewModel.changeQuantity(cartItemId, -1); }
            @Override
            public void onDelete(String cartItemId) { mViewModel.removeItem(cartItemId); }
        });

        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(adapter);
    }

    private void observeViewModel() {
        mViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
        });
        mViewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            DecimalFormat df = new DecimalFormat("###,###,###");
            tvTotalAmount.setText(df.format(total) + "đ");
        });
    }
}