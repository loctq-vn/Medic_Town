package com.example.medictown.ui.profile;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medictown.data.models.Address;
import com.example.medictown.databinding.ItemAddressBinding;
import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private List<Address> addressList = new ArrayList<>();
    private OnAddressClickListener listener;

    public interface OnAddressClickListener {
        void onEditClick(Address address);
        void onDeleteClick(Address address);
    }

    public void setOnAddressClickListener(OnAddressClickListener listener) {
        this.listener = listener;
    }

    public void setAddressList(List<Address> addressList) {
        this.addressList = addressList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddressBinding binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AddressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.bind(address, listener);
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddressBinding binding;

        public AddressViewHolder(ItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Address address, OnAddressClickListener listener) {
            binding.locationName.setText(address.location_name); // Assuming name for the location
            binding.recipientName.setText(address.recipient_name);
            binding.phoneNumber.setText(address.phone_number);
            binding.location.setText(address.location);

            binding.edit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(address);
                }
            });

            binding.delete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(address);
                }
            });
        }
    }
}
