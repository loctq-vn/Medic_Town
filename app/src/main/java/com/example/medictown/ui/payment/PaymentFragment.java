package com.example.medictown.ui.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.medictown.R;

public class PaymentFragment extends Fragment {

    private ImageView btnBack;
    private Button btnConfirmPayment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack = view.findViewById(R.id.btnBack);
        btnConfirmPayment = view.findViewById(R.id.btnConfirmPayment);

        btnBack.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        btnConfirmPayment.setOnClickListener(v -> {
            // Logic xử lý thanh toán thực tế sẽ ở đây
            Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
            
            // Sau khi đặt hàng thành công, có thể quay về màn hình chính hoặc lịch sử
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });
    }
}
