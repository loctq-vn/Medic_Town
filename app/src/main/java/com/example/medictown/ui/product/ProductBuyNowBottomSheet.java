package com.example.medictown.ui.product;

import android.content.Context;
import android.view.LayoutInflater;

import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.Products;
import com.example.medictown.databinding.LayoutBuyNowBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.NumberFormat;
import java.util.Locale;

public final class ProductBuyNowBottomSheet {
    private ProductBuyNowBottomSheet() {}

    public interface OnProductPurchaseActionListener {
        void onAddToCart(Products product, int quantity);
        void onBuyNow(Products product, int quantity);
    }

    public static void show(
            Context context,
            LayoutInflater inflater,
            Products product,
            OnProductPurchaseActionListener listener
    ) {
        if (product == null) {
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        LayoutBuyNowBottomSheetBinding binding = LayoutBuyNowBottomSheetBinding.inflate(inflater);
        dialog.setContentView(binding.getRoot());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        int[] quantity = {1};

        bindProductInfo(context, binding, product, formatter, quantity[0]);
        updateQuantityState(binding, product, formatter, quantity[0]);

        binding.btnClose.setOnClickListener(v -> dialog.dismiss());
        binding.btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                updateQuantityState(binding, product, formatter, quantity[0]);
            }
        });
        binding.btnPlus.setOnClickListener(v -> {
            int stock = product.stock;
            if (stock <= 0 || quantity[0] < stock) {
                quantity[0]++;
                updateQuantityState(binding, product, formatter, quantity[0]);
            }
        });
        binding.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(product, quantity[0]);
            }
            dialog.dismiss();
        });
        binding.btnBuyNow.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBuyNow(product, quantity[0]);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private static void bindProductInfo(
            Context context,
            LayoutBuyNowBottomSheetBinding binding,
            Products product,
            NumberFormat formatter,
            int quantity
    ) {
        binding.tvProductName.setText(product.name);
        binding.tvUnit.setText(getProductUnit(product));
        binding.tvPrice.setText(formatPriceWithUnit(formatter, getCurrentPrice(product), product));

        if (product.images != null && !product.images.isEmpty()) {
            Glide.with(context)
                    .load(product.images.get(0))
                    .placeholder(R.drawable.ic_product)
                    .error(R.drawable.ic_product)
                    .into(binding.imgProduct);
        } else {
            binding.imgProduct.setImageResource(R.drawable.ic_product);
        }

        updateTotals(binding, product, formatter, quantity);
    }

    private static void updateQuantityState(
            LayoutBuyNowBottomSheetBinding binding,
            Products product,
            NumberFormat formatter,
            int quantity
    ) {
        binding.tvQuantity.setText(String.valueOf(quantity));
        binding.btnMinus.setAlpha(quantity > 1 ? 1f : 0.45f);

        boolean canIncrease = product.stock <= 0 || quantity < product.stock;
        binding.btnPlus.setAlpha(canIncrease ? 1f : 0.45f);
        binding.btnPlus.setEnabled(canIncrease);

        updateTotals(binding, product, formatter, quantity);
    }

    private static void updateTotals(
            LayoutBuyNowBottomSheetBinding binding,
            Products product,
            NumberFormat formatter,
            int quantity
    ) {
        double subtotal = getCurrentPrice(product) * quantity;
        double savings = Math.max(0, product.price - getCurrentPrice(product)) * quantity;
        binding.tvSubtotal.setText(formatter.format(subtotal));
        binding.tvSavings.setText(formatter.format(savings));
    }

    private static double getCurrentPrice(Products product) {
        if (product.sale_price != null && product.sale_price > 0 && product.sale_price < product.price) {
            return product.sale_price;
        }
        return product.price;
    }

    private static String formatPriceWithUnit(NumberFormat formatter, double price, Products product) {
        return formatter.format(price) + " / " + getProductUnit(product);
    }

    private static String getProductUnit(Products product) {
        if (product.unit == null || product.unit.trim().isEmpty()) {
            return "Sản phẩm";
        }
        return product.unit.trim();
    }
}
