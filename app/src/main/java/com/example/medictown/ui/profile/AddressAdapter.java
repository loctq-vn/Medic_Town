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
            binding.locationName.setText(address.location_name != null && !address.location_name.isEmpty() ? address.location_name : address.recipient_name);
            binding.recipientName.setText(address.recipient_name);
            binding.phoneNumber.setText(address.phone_number);
            binding.location.setText(address.location);

            // Set initials and background color
            if (address.recipient_name != null && !address.recipient_name.isEmpty()) {
                String[] parts = address.recipient_name.trim().split("\\s+");
                StringBuilder initial = new StringBuilder();
                if (parts.length >= 2) {
                    initial.append(parts[0].charAt(0)).append(parts[parts.length - 1].charAt(0));
                } else if (parts.length == 1) {
                    initial.append(parts[0].charAt(0));
                }
                binding.tvAvatar.setText(initial.toString().toUpperCase());

                // Pick a color based on the name
                int[] colors = {0xFFFFE0B2, 0xFFB2DFDB, 0xFFFFCDD2, 0xFFE1BEE7, 0xFFC5CAE9};
                int colorIndex = Math.abs(address.recipient_name.hashCode()) % colors.length;
                int selectedColor = colors[colorIndex];
                
                binding.tvAvatar.getBackground().setTint(selectedColor);
                binding.vSideAccent.setBackgroundColor(selectedColor);
            }

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
