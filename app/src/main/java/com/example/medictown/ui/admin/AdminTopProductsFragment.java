package com.example.medictown.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.RevenueDashboard;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminTopProductsFragment extends Fragment {

    private AdminViewModel viewModel;
    private TopProductsAdapter adapter;
    private String fromDate;
    private String toDate;

    public static AdminTopProductsFragment newInstance(String fromDate, String toDate) {
        AdminTopProductsFragment fragment = new AdminTopProductsFragment();
        Bundle args = new Bundle();
        args.putString("from_date", fromDate);
        args.putString("to_date", toDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fromDate = getArguments().getString("from_date");
            toDate = getArguments().getString("to_date");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_top_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        
        RecyclerView rvTopProducts = view.findViewById(R.id.rvTopProducts);
        TextView tvEmpty = view.findViewById(R.id.tvEmpty);
        
        adapter = new TopProductsAdapter();
        rvTopProducts.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setVisibility(View.VISIBLE);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        viewModel.getRevenueTopProducts().observe(getViewLifecycleOwner(), products -> {
            if (products == null || products.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvTopProducts.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvTopProducts.setVisibility(View.VISIBLE);
                adapter.setProducts(products);
            }
        });

        SessionManager sessionManager = new SessionManager(requireContext());
        String shopId = sessionManager.getCurrentShopId();
        if (shopId != null && fromDate != null && toDate != null) {
            viewModel.fetchRevenueTopProducts(shopId, fromDate, toDate);
        }
    }

    private static class TopProductsAdapter extends RecyclerView.Adapter<TopProductsAdapter.ViewHolder> {
        private List<RevenueDashboard.TopProduct> products = new ArrayList<>();

        public void setProducts(List<RevenueDashboard.TopProduct> products) {
            this.products = products;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_top_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RevenueDashboard.TopProduct product = products.get(position);
            holder.tvProductName.setText(product.productName != null ? product.productName : "S\u1ea3n ph\u1ea9m");
            holder.tvStockCount.setText(String.format(Locale.getDefault(), "Kho: %,d", product.stock));
            holder.tvRevenue.setText(formatCurrency(product.revenue));
            holder.tvSoldIndicator.setText(String.format(Locale.getDefault(), "\u0110\u00e3 b\u00e1n %,d", product.quantitySold));

            Glide.with(holder.itemView.getContext())
                    .load(product.productImage)
                    .placeholder(R.drawable.ic_medicine_placeholder)
                    .error(R.drawable.ic_medicine_placeholder)
                    .into(holder.ivProduct);
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        private String formatCurrency(double amount) {
            return String.format(Locale.getDefault(), "%,.0f\u0111", amount);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivProduct;
            TextView tvProductName, tvStockCount, tvRevenue, tvSoldIndicator;

            ViewHolder(View itemView) {
                super(itemView);
                ivProduct = itemView.findViewById(R.id.ivProduct);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvStockCount = itemView.findViewById(R.id.tvStockCount);
                tvRevenue = itemView.findViewById(R.id.tvRevenue);
                tvSoldIndicator = itemView.findViewById(R.id.tvSoldIndicator);
            }
        }
    }
}
