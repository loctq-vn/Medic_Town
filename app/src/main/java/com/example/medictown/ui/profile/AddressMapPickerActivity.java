package com.example.medictown.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medictown.databinding.ActivityAddressMapPickerBinding;

import org.json.JSONObject;
import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.maplibre.android.camera.CameraUpdateFactory;

import org.json.JSONArray;

public class AddressMapPickerActivity extends AppCompatActivity {
    private static final double DEFAULT_LAT = 10.7769;
    private static final double DEFAULT_LNG = 106.7009;

    private ActivityAddressMapPickerBinding binding;
    private MapLibreMap mapLibreMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final OkHttpClient client = new OkHttpClient();


    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                        Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                        if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                            moveToCurrentLocation();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Bạn cần cấp quyền vị trí để định vị",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
    private static final String OSM_STYLE_JSON =
            "{"
                    + "\"version\":8,"
                    + "\"sources\":{"
                    + "\"osm\":{"
                    + "\"type\":\"raster\","
                    + "\"tiles\":[\"https://tile.openstreetmap.org/{z}/{x}/{y}.png\"],"
                    + "\"tileSize\":256,"
                    + "\"attribution\":\"© OpenStreetMap contributors\""
                    + "}"
                    + "},"
                    + "\"layers\":["
                    + "{"
                    + "\"id\":\"osm\","
                    + "\"type\":\"raster\","
                    + "\"source\":\"osm\""
                    + "}"
                    + "]"
                    + "}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        binding = ActivityAddressMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.mapView.onCreate(savedInstanceState);

        setupButtons();
        setupMap();
    }

    private void setupButtons() {
        binding.btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        binding.btnUseLocation.setOnClickListener(v -> confirmSelectedLocation());
        binding.btnCurrentLocation.setOnClickListener(v -> requestOrMoveToCurrentLocation());
        binding.btnSearchAddress.setOnClickListener(v -> {
            String query = binding.etSearchAddress.getText() != null
                    ? binding.etSearchAddress.getText().toString().trim()
                    : "";

            searchAddress(query);
        });
        binding.etSearchAddress.setOnEditorActionListener((v, actionId, event) -> {
            String query = binding.etSearchAddress.getText() != null
                    ? binding.etSearchAddress.getText().toString().trim()
                    : "";

            searchAddress(query);
            return true;
        });
    }

    private void setupMap() {
        binding.mapView.getMapAsync(map -> {
            mapLibreMap = map;

            double startLat = getIntent().hasExtra("latitude")
                    ? getIntent().getDoubleExtra("latitude", DEFAULT_LAT)
                    : DEFAULT_LAT;

            double startLng = getIntent().hasExtra("longitude")
                    ? getIntent().getDoubleExtra("longitude", DEFAULT_LNG)
                    : DEFAULT_LNG;

            mapLibreMap.setStyle(new Style.Builder().fromJson(OSM_STYLE_JSON));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(startLat, startLng))
                    .zoom(15)
                    .build();

            mapLibreMap.setCameraPosition(cameraPosition);

            mapLibreMap.addOnCameraIdleListener(() -> {
                LatLng center = mapLibreMap.getCameraPosition().target;
                String text = String.format(
                        Locale.US,
                        "Vị trí đã chọn: %.6f, %.6f",
                        center.getLatitude(),
                        center.getLongitude()
                );
                binding.tvSelectedAddress.setText(text);
            });
        });
    }

    private void confirmSelectedLocation() {
        if (mapLibreMap == null) {
            Toast.makeText(this, "Bản đồ chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng target = mapLibreMap.getCameraPosition().target;
        reverseGeocode(target.getLatitude(), target.getLongitude());
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestOrMoveToCurrentLocation() {
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }

        moveToCurrentLocation();
    }

    @SuppressLint("MissingPermission")
    private void moveToCurrentLocation() {
        setLoading(true);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    setLoading(false);

                    if (location == null) {
                        Toast.makeText(
                                this,
                                "Không lấy được vị trí hiện tại",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    moveCamera(location.getLatitude(), location.getLongitude(), 17);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(
                            this,
                            "Lỗi định vị vị trí hiện tại",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void moveCamera(double lat, double lng, double zoom) {
        if (mapLibreMap == null) {
            return;
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(zoom)
                .build();

        mapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void searchAddress(String query) {
        if (query.isEmpty()) {
            Toast.makeText(this, "Nhập địa chỉ cần tìm", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        HttpUrl url = HttpUrl.parse("https://nominatim.openstreetmap.org/search")
                .newBuilder()
                .addQueryParameter("format", "jsonv2")
                .addQueryParameter("q", query)
                .addQueryParameter("limit", "1")
                .addQueryParameter("countrycodes", "vn")
                .addQueryParameter("addressdetails", "1")
                .addQueryParameter("accept-language", "vi")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "MedicTown/1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(
                            AddressMapPickerActivity.this,
                            "Không tìm được địa chỉ",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> {
                    setLoading(false);

                    try {
                        JSONArray array = new JSONArray(body);

                        if (array.length() == 0) {
                            Toast.makeText(
                                    AddressMapPickerActivity.this,
                                    "Không có kết quả phù hợp",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        JSONObject item = array.getJSONObject(0);

                        double lat = Double.parseDouble(item.optString("lat"));
                        double lng = Double.parseDouble(item.optString("lon"));
                        String displayName = item.optString("display_name");

                        binding.tvSelectedAddress.setText(displayName);
                        moveCamera(lat, lng, 17);
                    } catch (Exception e) {
                        Toast.makeText(
                                AddressMapPickerActivity.this,
                                "Lỗi đọc kết quả tìm kiếm",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }
    private void reverseGeocode(double lat, double lng) {
        setLoading(true);

        HttpUrl url = HttpUrl.parse("https://nominatim.openstreetmap.org/reverse")
                .newBuilder()
                .addQueryParameter("format", "jsonv2")
                .addQueryParameter("lat", String.valueOf(lat))
                .addQueryParameter("lon", String.valueOf(lng))
                .addQueryParameter("addressdetails", "1")
                .addQueryParameter("accept-language", "vi")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "MedicTown/1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(
                            AddressMapPickerActivity.this,
                            "Không lấy được địa chỉ",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> {
                    setLoading(false);

                    try {
                        JSONObject json = new JSONObject(body);

                        String displayName = json.optString("display_name", "");
                        String placeId = json.optString("place_id", "");

                        if (displayName.trim().isEmpty()) {
                            displayName = String.format(Locale.US, "%.6f, %.6f", lat, lng);
                        }

                        Intent result = new Intent();
                        result.putExtra("location", displayName);
                        result.putExtra("latitude", lat);
                        result.putExtra("longitude", lng);
                        result.putExtra("provider_place_id", placeId);
                        result.putExtra("raw_address", body);

                        setResult(RESULT_OK, result);
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(
                                AddressMapPickerActivity.this,
                                "Lỗi đọc dữ liệu địa chỉ",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnUseLocation.setEnabled(!loading);
        binding.btnCancel.setEnabled(!loading);
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onPause() {
        binding.mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        binding.mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        binding.mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        binding.mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
}