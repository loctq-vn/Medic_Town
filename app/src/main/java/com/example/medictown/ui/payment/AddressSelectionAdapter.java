package com.example.medictown.ui.payment;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medictown.R;
import com.example.medictown.data.models.Address;
import com.example.medictown.databinding.ItemAddressSelectionBinding;
import java.util.ArrayList;
import java.util.List;

public class AddressSelectionAdapter extends RecyclerView.Adapter<AddressSelectionAdapter.ViewHolder> {

    private List<Address> addressList = new ArrayList<>();
    private Address selectedAddress;
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(Address address);
    }

    public void setData(List<Address> list, Address selected) {
        this.addressList = list;
        this.selectedAddress = selected;
        notifyDataSetChanged();
    }

    public void setOnAddressSelectedListener(OnAddressSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddressSelectionBinding binding = ItemAddressSelectionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = addressList.get(position);
        
        holder.binding.tvLocationName.setText(address.location_name);
        holder.binding.tvRecipientInfo.setText(address.recipient_name + " | " + address.phone_number);
        holder.binding.tvAddress.setText(address.location);

        boolean isSelected = selectedAddress != null && selectedAddress.id.equals(address.id);
        holder.binding.rbSelected.setChecked(isSelected);
        
        int primaryColor = holder.itemView.getContext().getResources().getColor(R.color.primary);
        int outlineColor = holder.itemView.getContext().getResources().getColor(R.color.outline_variant);
        holder.binding.cardAddress.setStrokeColor(isSelected ? primaryColor : outlineColor);
        holder.binding.cardAddress.setStrokeWidth(isSelected ? 4 : 2);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressSelected(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAddressSelectionBinding binding;
        public ViewHolder(ItemAddressSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
