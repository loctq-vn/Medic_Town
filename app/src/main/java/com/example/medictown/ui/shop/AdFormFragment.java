package com.example.medictown.ui.shop;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Advertisement;
import com.example.medictown.data.models.AdvertisementRequest;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.repositories.AdvertisementRepository;
import com.example.medictown.data.repositories.ShopRepository;
import com.example.medictown.databinding.FragmentAdFormBinding;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdFormFragment extends Fragment {
    private static final String ARG_MODE = "mode";
    private static final String ARG_ITEM = "item";
    private static final String MODE_CREATE = "create";
    private static final String MODE_EDIT = "edit";
    private static final String MODE_DETAILS = "details";

    private static final String[] POSITION_LABELS = {
            "Trang chủ", "Banner sản phẩm", "Popup", "Thanh toán"
    };
    private static final String[] POSITION_VALUES = {
            "home_banner", "product_list", "popup", "checkout_banner"
    };
    private static final String[] TARGET_LABELS = {
            "Sản phẩm", "Shop", "Link ngoài", "Không điều hướng"
    };
    private static final String[] TARGET_VALUES = {
            "product", "shop", "external_url", "none"
    };

    private FragmentAdFormBinding binding;
    private AdvertisementRepository repository;
    private ShopRepository shopRepository;
    private SessionManager sessionManager;
    private Advertisement advertisement;
    private String mode = MODE_CREATE;
    private String selectedStatus = "draft";
    private String selectedImageUrl;
    private String selectedTargetId;
    private boolean imageUploading;
    private boolean suppressTargetReset;
    private ActivityResultLauncher<String> bannerPickerLauncher;

    public static AdFormFragment newCreateInstance() {
        AdFormFragment fragment = new AdFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, MODE_CREATE);
        fragment.setArguments(args);
        return fragment;
    }

    public static AdFormFragment newEditInstance(Advertisement item) {
        AdFormFragment fragment = new AdFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, MODE_EDIT);
        args.putSerializable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    public static AdFormFragment newDetailsInstance(Advertisement item) {
        AdFormFragment fragment = new AdFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, MODE_DETAILS);
        args.putSerializable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bannerPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::uploadSelectedBanner
        );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentAdFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new AdvertisementRepository();
        shopRepository = new ShopRepository();
        sessionManager = new SessionManager(requireContext());
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setNavBarsVisibility(false);
        }

        readArguments();
        setupSpinners();
        setupActions();
        if (advertisement != null) bindAdvertisement(advertisement);
        else setStatus("draft");
        applyMode();
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args == null) return;
        mode = args.getString(ARG_MODE, MODE_CREATE);
        advertisement = (Advertisement) args.getSerializable(ARG_ITEM);
    }

    private void setupSpinners() {
        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_admin_spinner,
                POSITION_LABELS
        );
        positionAdapter.setDropDownViewResource(R.layout.item_admin_spinner_dropdown);
        binding.spAdPosition.setAdapter(positionAdapter);

        ArrayAdapter<String> targetAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_admin_spinner,
                TARGET_LABELS
        );
        targetAdapter.setDropDownViewResource(R.layout.item_admin_spinner_dropdown);
        binding.spAdRedirectType.setAdapter(targetAdapter);
        binding.spAdRedirectType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!suppressTargetReset) {
                    selectedTargetId = null;
                    binding.etAdRedirectTarget.setText("");
                }
                updateRedirectTargetField();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupActions() {
        binding.btnAdFormBack.setOnClickListener(v -> goBack());
        binding.btnCancelAdForm.setOnClickListener(v -> goBack());
        binding.btnAdFormMore.setVisibility(View.GONE);
        binding.layoutBannerUpload.setOnClickListener(v -> {
            if (!MODE_DETAILS.equals(mode) && !imageUploading) {
                bannerPickerLauncher.launch("image/*");
            }
        });

        binding.btnStatusDraft.setOnClickListener(v -> setStatus("draft"));
        binding.btnStatusActive.setOnClickListener(v -> setStatus("active"));
        binding.btnStatusPaused.setOnClickListener(v -> setStatus("paused"));
        binding.btnStatusExpired.setEnabled(false);
        binding.switchAdActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!MODE_DETAILS.equals(mode)) setStatus(isChecked ? "active" : "paused");
        });

        binding.etAdRedirectTarget.setOnClickListener(v -> handleRedirectTargetClick());
        binding.etAdStartDate.setOnClickListener(v -> showDatePicker(binding.etAdStartDate));
        binding.etAdEndDate.setOnClickListener(v -> showDatePicker(binding.etAdEndDate));
        binding.btnSubmitAdForm.setOnClickListener(v -> handlePrimaryAction());
    }

    private void bindAdvertisement(Advertisement item) {
        binding.etAdName.setText(item.title);
        binding.etAdDescription.setText(item.description);
        binding.etAdBudget.setText(
                item.budget_amount == null ? "" : decimalText(item.budget_amount)
        );
        selectedImageUrl = item.image_url;
        selectedTargetId = item.target_id;
        selectSpinnerValue(binding.spAdPosition, POSITION_VALUES, item.position);
        suppressTargetReset = true;
        selectSpinnerValue(binding.spAdRedirectType, TARGET_VALUES, item.target_type);
        suppressTargetReset = false;
        if ("external_url".equals(item.target_type)) {
            binding.etAdRedirectTarget.setText(item.target_url);
        } else if ("shop".equals(item.target_type)) {
            binding.etAdRedirectTarget.setText(sessionManager.getCurrentShopName());
        } else if (item.target_id != null) {
            binding.etAdRedirectTarget.setText(item.target_id);
        }
        setDate(binding.etAdStartDate, item.start_date);
        setDate(binding.etAdEndDate, item.end_date);
        setStatus(item.status == null ? (item.is_active ? "active" : "paused") : item.status);
        if (selectedImageUrl != null && !selectedImageUrl.trim().isEmpty()) {
            showBanner(selectedImageUrl);
        }
        updateRedirectTargetField();
    }

    private void setStatus(String status) {
        selectedStatus = status;
        bindStatusButton(binding.btnStatusDraft, "draft".equals(status));
        bindStatusButton(binding.btnStatusActive, "active".equals(status));
        bindStatusButton(binding.btnStatusPaused, "paused".equals(status));
        bindStatusButton(binding.btnStatusExpired, "expired".equals(status));

        binding.switchAdActive.setOnCheckedChangeListener(null);
        binding.switchAdActive.setChecked("active".equals(status));
        binding.switchAdActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!MODE_DETAILS.equals(mode)) setStatus(isChecked ? "active" : "paused");
        });
    }

    private void bindStatusButton(TextView view, boolean selected) {
        view.setBackgroundResource(selected
                ? R.drawable.bg_ad_form_status_tab_selected
                : R.drawable.bg_ad_form_status_tab_clear);
        view.setTextColor(ContextCompat.getColor(
                requireContext(),
                selected ? R.color.ad_primary : R.color.ad_text
        ));
        view.setTypeface(ResourcesCompat.getFont(
                requireContext(),
                selected ? R.font.plus_jakarta_sans_bold : R.font.plus_jakarta_sans
        ));
    }

    private void applyMode() {
        boolean details = MODE_DETAILS.equals(mode);
        boolean edit = MODE_EDIT.equals(mode);
        binding.tvAdFormTitle.setText(
                details ? "Chi tiết quảng cáo" : edit ? "Chỉnh sửa quảng cáo" : "Tạo quảng cáo"
        );
        binding.btnCancelAdForm.setText(details ? "Đóng" : "Hủy");
        binding.btnSubmitAdForm.setText(
                details ? "Chỉnh sửa" : edit ? "Lưu thay đổi" : "Tạo quảng cáo"
        );
        setFormEnabled(!details);
    }

    private void setFormEnabled(boolean enabled) {
        binding.layoutBannerUpload.setEnabled(enabled);
        binding.etAdName.setEnabled(enabled);
        binding.etAdDescription.setEnabled(enabled);
        binding.etAdBudget.setEnabled(enabled);
        binding.spAdPosition.setEnabled(enabled);
        binding.spAdRedirectType.setEnabled(enabled);
        binding.switchAdActive.setEnabled(enabled && !"expired".equals(selectedStatus));
        binding.btnStatusDraft.setEnabled(enabled);
        binding.btnStatusActive.setEnabled(enabled);
        binding.btnStatusPaused.setEnabled(enabled);
        binding.btnStatusExpired.setEnabled(false);
        binding.etAdStartDate.setEnabled(enabled);
        binding.etAdEndDate.setEnabled(enabled);
        updateRedirectTargetField();
        keepReadable(binding.etAdName);
        keepReadable(binding.etAdDescription);
        keepReadable(binding.etAdBudget);
        keepReadable(binding.etAdRedirectTarget);
        keepReadable(binding.etAdStartDate);
        keepReadable(binding.etAdEndDate);
    }

    private void keepReadable(EditText editText) {
        editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.ad_text));
        editText.setHintTextColor(
                ContextCompat.getColor(requireContext(), R.color.ad_text_secondary)
        );
    }

    private void updateRedirectTargetField() {
        if (binding == null) return;
        String targetType = selectedTargetType();
        boolean editable = !MODE_DETAILS.equals(mode);

        if ("none".equals(targetType)) {
            binding.tvAdRedirectTargetLabel.setVisibility(View.GONE);
            binding.etAdRedirectTarget.setVisibility(View.GONE);
            return;
        }

        binding.tvAdRedirectTargetLabel.setVisibility(View.VISIBLE);
        binding.etAdRedirectTarget.setVisibility(View.VISIBLE);
        binding.etAdRedirectTarget.setEnabled(editable);
        binding.etAdRedirectTarget.setClickable(editable);

        if ("shop".equals(targetType)) {
            binding.tvAdRedirectTargetLabel.setText("Shop");
            binding.etAdRedirectTarget.setHint("Shop hiện tại");
            selectedTargetId = sessionManager.getCurrentShopId();
            binding.etAdRedirectTarget.setText(sessionManager.getCurrentShopName());
            binding.etAdRedirectTarget.setInputType(InputType.TYPE_NULL);
            binding.etAdRedirectTarget.setFocusable(false);
        } else if ("external_url".equals(targetType)) {
            binding.tvAdRedirectTargetLabel.setText("Link ngoài");
            binding.etAdRedirectTarget.setHint("https://...");
            binding.etAdRedirectTarget.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI
            );
            binding.etAdRedirectTarget.setFocusable(editable);
            binding.etAdRedirectTarget.setFocusableInTouchMode(editable);
        } else {
            binding.tvAdRedirectTargetLabel.setText("Chọn sản phẩm");
            binding.etAdRedirectTarget.setHint("Chọn sản phẩm");
            binding.etAdRedirectTarget.setInputType(InputType.TYPE_NULL);
            binding.etAdRedirectTarget.setFocusable(false);
        }
    }

    private void handleRedirectTargetClick() {
        if (MODE_DETAILS.equals(mode)) return;
        String targetType = selectedTargetType();
        if ("product".equals(targetType)) showProductPicker();
    }

    private void showProductPicker() {
        String shopId = sessionManager.getCurrentShopId();
        if (shopId == null || shopId.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa chọn gian hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        shopRepository.getShopProducts(shopId, new Callback<List<Products>>() {
            @Override
            public void onResponse(Call<List<Products>> call, Response<List<Products>> response) {
                if (binding == null) return;
                List<Products> products = response.body();
                if (!response.isSuccessful() || products == null || products.isEmpty()) {
                    Toast.makeText(
                            requireContext(),
                            "Shop chưa có sản phẩm để quảng cáo",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                String[] names = new String[products.size()];
                for (int index = 0; index < products.size(); index++) {
                    names[index] = products.get(index).name;
                }
                ArrayAdapter<String> productPickerAdapter = new ArrayAdapter<>(
                        requireContext(),
                        R.layout.item_ad_product_picker,
                        R.id.tv_ad_product_picker_name,
                        names
                );
                new AlertDialog.Builder(requireContext())
                        .setTitle("Chọn sản phẩm")
                        .setAdapter(productPickerAdapter, (dialog, which) -> {
                            selectedTargetId = products.get(which).id;
                            binding.etAdRedirectTarget.setText(products.get(which).name);
                        })
                        .show();
            }

            @Override
            public void onFailure(Call<List<Products>> call, Throwable throwable) {
                if (binding == null) return;
                Toast.makeText(
                        requireContext(),
                        "Không thể tải sản phẩm",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void showDatePicker(EditText target) {
        if (MODE_DETAILS.equals(mode)) return;
        Calendar calendar = Calendar.getInstance();
        Date current = parseFormDate(target.getText().toString().trim());
        if (current != null) calendar.setTime(current);
        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth, 0, 0, 0);
                    target.setText(formDateFormat().format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void handlePrimaryAction() {
        if (MODE_DETAILS.equals(mode)) {
            mode = MODE_EDIT;
            applyMode();
            return;
        }
        if (imageUploading) {
            Toast.makeText(requireContext(), "Vui lòng chờ ảnh tải lên xong", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        AdvertisementRequest request = buildRequest();
        if (request == null) return;
        submit(request);
    }

    private AdvertisementRequest buildRequest() {
        String title = text(binding.etAdName);
        if (title.isEmpty()) {
            binding.etAdName.setError("Vui lòng nhập tên quảng cáo");
            return null;
        }
        if (selectedImageUrl == null || selectedImageUrl.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn ảnh banner", Toast.LENGTH_SHORT).show();
            return null;
        }

        String targetType = selectedTargetType();
        String targetUrl = null;
        if ("product".equals(targetType) && empty(selectedTargetId)) {
            binding.etAdRedirectTarget.setError("Vui lòng chọn sản phẩm");
            return null;
        }
        if ("shop".equals(targetType)) selectedTargetId = sessionManager.getCurrentShopId();
        if ("external_url".equals(targetType)) {
            targetUrl = text(binding.etAdRedirectTarget);
            if (!targetUrl.toLowerCase(Locale.US).startsWith("https://")) {
                binding.etAdRedirectTarget.setError("Link ngoài phải bắt đầu bằng https://");
                return null;
            }
        }

        Date startDate = parseFormDate(text(binding.etAdStartDate));
        Date endDate = parseFormDate(text(binding.etAdEndDate));
        if (!text(binding.etAdStartDate).isEmpty() && startDate == null) {
            binding.etAdStartDate.setError("Ngày bắt đầu không hợp lệ");
            return null;
        }
        if (!text(binding.etAdEndDate).isEmpty() && endDate == null) {
            binding.etAdEndDate.setError("Ngày kết thúc không hợp lệ");
            return null;
        }
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            binding.etAdEndDate.setError("Ngày kết thúc phải sau ngày bắt đầu");
            return null;
        }

        Double budget = parseOptionalDouble(text(binding.etAdBudget));
        if (!text(binding.etAdBudget).isEmpty() && budget == null) {
            binding.etAdBudget.setError("Ngân sách không hợp lệ");
            return null;
        }

        AdvertisementRequest request = new AdvertisementRequest();
        request.title = title;
        request.description = emptyToNull(text(binding.etAdDescription));
        request.image_url = selectedImageUrl;
        request.target_type = targetType;
        request.target_id = "product".equals(targetType) || "shop".equals(targetType)
                ? selectedTargetId : null;
        request.target_url = targetUrl;
        request.position = POSITION_VALUES[binding.spAdPosition.getSelectedItemPosition()];
        request.priority = advertisement == null ? 0 : advertisement.priority;
        request.status = "expired".equals(selectedStatus) ? "paused" : selectedStatus;
        request.is_active = "active".equals(request.status);
        request.start_date = isoDate(startDate);
        request.end_date = isoDate(endDate);
        request.budget_amount = budget;
        return request;
    }

    private void submit(AdvertisementRequest request) {
        String shopId = sessionManager.getCurrentShopId();
        if (empty(shopId)) {
            Toast.makeText(requireContext(), "Chưa chọn gian hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.btnSubmitAdForm.setEnabled(false);
        Callback<Advertisement> callback = new Callback<Advertisement>() {
            @Override
            public void onResponse(
                    Call<Advertisement> call,
                    Response<Advertisement> response
            ) {
                if (binding == null) return;
                binding.btnSubmitAdForm.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(
                            requireContext(),
                            "Không thể lưu quảng cáo: HTTP " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                Toast.makeText(
                        requireContext(),
                        MODE_EDIT.equals(mode)
                                ? "Đã lưu thay đổi quảng cáo"
                                : "Đã tạo quảng cáo",
                        Toast.LENGTH_SHORT
                ).show();
                goBack();
            }

            @Override
            public void onFailure(Call<Advertisement> call, Throwable throwable) {
                if (binding == null) return;
                binding.btnSubmitAdForm.setEnabled(true);
                Toast.makeText(
                        requireContext(),
                        throwable.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        };
        if (advertisement == null) repository.create(shopId, request, callback);
        else repository.update(shopId, advertisement.id, request, callback);
    }

    private void uploadSelectedBanner(Uri uri) {
        if (uri == null || binding == null) return;
        String shopId = sessionManager.getCurrentShopId();
        if (empty(shopId)) {
            Toast.makeText(requireContext(), "Chưa chọn gian hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        imageUploading = true;
        binding.btnSubmitAdForm.setEnabled(false);
        showBanner(uri);
        repository.uploadImage(requireContext(), shopId, uri, new okhttp3.Callback() {
            @Override
            public void onFailure(
                    @NonNull okhttp3.Call call,
                    @NonNull IOException exception
            ) {
                if (getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    imageUploading = false;
                    binding.btnSubmitAdForm.setEnabled(true);
                    restoreUploadedBanner();
                    Toast.makeText(
                            requireContext(),
                            uploadFailureMessage(exception),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }

            @Override
            public void onResponse(
                    @NonNull okhttp3.Call call,
                    @NonNull okhttp3.Response response
            ) throws IOException {
                String body = response.body() == null ? "" : response.body().string();
                if (getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    imageUploading = false;
                    binding.btnSubmitAdForm.setEnabled(true);
                    if (!response.isSuccessful()) {
                        restoreUploadedBanner();
                        Toast.makeText(
                                requireContext(),
                                getBannerUploadError(response.code(), body),
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }
                    try {
                        selectedImageUrl = new JSONObject(body).getString("url");
                        showBanner(selectedImageUrl);
                    } catch (Exception exception) {
                        restoreUploadedBanner();
                        Toast.makeText(
                                requireContext(),
                                "Không đọc được đường dẫn ảnh từ máy chủ",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }

    private String uploadFailureMessage(IOException exception) {
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()
                || message.startsWith("Unable to read advertisement image")) {
            return "Không thể đọc hoặc tải ảnh quảng cáo";
        }
        return message;
    }

    private String getBannerUploadError(int statusCode, String responseBody) {
        try {
            String detail = new JSONObject(responseBody).optString("detail", "");
            if ("File exceeds max upload size".equals(detail)) {
                return "Ảnh vượt quá dung lượng tối đa 5 MB";
            }
            if ("Unsupported file extension".equals(detail)
                    || "File content does not match extension".equals(detail)
                    || "File content does not match MIME type".equals(detail)) {
                return "Định dạng ảnh không hợp lệ. Vui lòng chọn JPG, PNG hoặc WebP";
            }
            if ("Empty file".equals(detail)) {
                return "Ảnh đã chọn không có dữ liệu";
            }
        } catch (Exception ignored) {
        }
        if (statusCode == 401) {
            return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại";
        }
        if (statusCode == 403 || statusCode == 404) {
            return "Bạn không có quyền tải ảnh cho gian hàng này";
        }
        return "Không thể tải ảnh quảng cáo (mã lỗi " + statusCode + ")";
    }

    private void restoreUploadedBanner() {
        if (!empty(selectedImageUrl)) {
            showBanner(selectedImageUrl);
            return;
        }
        binding.ivAdBannerPreview.setImageDrawable(null);
        binding.ivAdBannerPreview.setVisibility(View.GONE);
        binding.layoutBannerPlaceholder.setVisibility(View.VISIBLE);
    }

    private void showBanner(Object image) {
        binding.layoutBannerPlaceholder.setVisibility(View.GONE);
        binding.ivAdBannerPreview.setVisibility(View.VISIBLE);
        Glide.with(this).load(image).centerCrop().into(binding.ivAdBannerPreview);
    }

    private void selectSpinnerValue(
            android.widget.Spinner spinner,
            String[] values,
            String value
    ) {
        if (value == null) return;
        for (int index = 0; index < values.length; index++) {
            if (value.equals(values[index])) {
                spinner.setSelection(index);
                return;
            }
        }
    }

    private String selectedTargetType() {
        int position = binding.spAdRedirectType.getSelectedItemPosition();
        return TARGET_VALUES[Math.max(0, position)];
    }

    private void setDate(EditText editText, Date date) {
        editText.setText(date == null ? "" : formDateFormat().format(date));
    }

    private SimpleDateFormat formDateFormat() {
        return new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    }

    private Date parseFormDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            SimpleDateFormat format = formDateFormat();
            format.setLenient(false);
            return format.parse(value);
        } catch (ParseException exception) {
            return null;
        }
    }

    private String isoDate(Date value) {
        if (value == null) return null;
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                Locale.US
        );
        format.setTimeZone(TimeZone.getDefault());
        return format.format(value);
    }

    private Double parseOptionalDouble(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            double parsed = Double.parseDouble(value);
            return parsed >= 0 ? parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String decimalText(double value) {
        if (value == Math.rint(value)) return String.valueOf((long) value);
        return String.valueOf(value);
    }

    private String text(EditText editText) {
        return editText.getText().toString().trim();
    }

    private String emptyToNull(String value) {
        return empty(value) ? null : value;
    }

    private boolean empty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void goBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setNavBarsVisibility(true);
        }
        binding = null;
    }
}
