package id.hafiz.infobmkg;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity implements LocationListener {

    // --- VARIABEL UI ---
    private WebView webViewMap;
    private RecyclerView rvGempa;
    private FloatingActionButton fabWeather;

    // --- VARIABEL LOGIKA ---
    private GempaAdapter adapter;
    private LocationManager locationManager;
    private double currentUserLat = 0.0;
    private double currentUserLng = 0.0;

    // --- INTERFACE RETROFIT (API) ---

    // 1. Interface untuk API BMKG
    public interface BmkgApi {
        @GET("DataMKG/TEWS/gempaterkini.json")
        Call<GempaData> getGempaTerkini();
    }

    // 2. Interface untuk API Cuaca (Open-Meteo)
    public interface WeatherApi {
        @GET("v1/forecast")
        Call<WeatherData> getWeather(
                @Query("latitude") double lat,
                @Query("longitude") double lng,
                @Query("current_weather") boolean current,
                @Query("windspeed_unit") String windUnit // tambah unit kmh
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inisialisasi View
        webViewMap = findViewById(R.id.webViewMap);
        rvGempa = findViewById(R.id.rvGempa);
        fabWeather = findViewById(R.id.fabWeather);

        // 2. Setup RecyclerView
        rvGempa.setLayoutManager(new LinearLayoutManager(this));

        // 3. Setup WebView (Peta)
        setupWebView();

        // 4. Setup Tombol Cuaca
        fabWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserLat != 0.0 && currentUserLng != 0.0) {
                    fetchWeatherData();
                } else {
                    Toast.makeText(MainActivity.this, "Sedang mencari lokasi GPS...", Toast.LENGTH_SHORT).show();
                    // Coba pancing update lokasi lagi
                    checkLocationPermission();
                }
            }
        });

        // 5. Ambil Data Gempa saat aplikasi dibuka
        fetchGempaData();
    }

    // =================================================================================
    // BAGIAN 1: WEBVIEW & PETA
    // =================================================================================

    private void setupWebView() {
        WebSettings webSettings = webViewMap.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Agar halaman tidak membuka browser eksternal
        webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Setelah peta siap, baru minta izin lokasi
                checkLocationPermission();
            }
        });

        // Muat file HTML dari folder assets
        webViewMap.loadUrl("file:///android_asset/map.html");
    }

    // =================================================================================
    // BAGIAN 2: DATA GEMPA (BMKG) & DETAIL SHEET
    // =================================================================================

    private void fetchGempaData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://data.bmkg.go.id/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BmkgApi api = retrofit.create(BmkgApi.class);
        Call<GempaData> call = api.getGempaTerkini();

        call.enqueue(new Callback<GempaData>() {
            @Override
            public void onResponse(Call<GempaData> call, Response<GempaData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GempaData.Gempa> list = response.body().infoGempa.gempaList;
                    setupList(list);
                }
            }

            @Override
            public void onFailure(Call<GempaData> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Gagal mengambil data BMKG: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupList(List<GempaData.Gempa> list) {
        adapter = new GempaAdapter(list, new GempaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GempaData.Gempa gempa) {
                // Aksi 1: Update Peta (Gambar Garis & Jarak)
                updateMapFocus(gempa);

                // Aksi 2: Tampilkan Detail Bottom Sheet
                showDetailBottomSheet(gempa);
            }
        });
        rvGempa.setAdapter(adapter);
    }

    private void updateMapFocus(GempaData.Gempa gempa) {
        try {
            // Format koordinat dari API biasanya "-6.21,106.84"
            String[] coords = gempa.coordinates.split(",");
            String quakeLat = coords[0];
            String quakeLng = coords[1];

            // Panggil fungsi Javascript di map.html
            // focusGempa(userLat, userLng, quakeLat, quakeLng, wilayah, magnitude)
            String jsCommand = String.format(Locale.US, "javascript:focusGempa(%f, %f, %s, %s, '%s', '%s')",
                    currentUserLat, currentUserLng,
                    quakeLat, quakeLng,
                    gempa.wilayah, gempa.magnitude);

            webViewMap.evaluateJavascript(jsCommand, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDetailBottomSheet(GempaData.Gempa gempa) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        // Inflate layout detail
        View view = LayoutInflater.from(this).inflate(R.layout.layout_bottom_sheet, null);

        // Binding View
        TextView tvWilayah = view.findViewById(R.id.bsWilayah);
        TextView tvTanggal = view.findViewById(R.id.bsTanggal);
        TextView tvMagnitude = view.findViewById(R.id.bsMagnitude);
        TextView tvKedalaman = view.findViewById(R.id.bsKedalaman);
        TextView tvKoordinat = view.findViewById(R.id.bsKoordinat);
        TextView tvPotensi = view.findViewById(R.id.bsPotensi);
        CardView cardMag = view.findViewById(R.id.bsCardMag);

        // Set Data
        tvWilayah.setText(gempa.wilayah);
        tvTanggal.setText(gempa.tanggal + " • " + gempa.jam);
        tvMagnitude.setText(gempa.magnitude);
        tvKedalaman.setText(gempa.kedalaman);
        tvKoordinat.setText(gempa.coordinates);
        tvPotensi.setText(gempa.potensi);

        // Warna Badge Dinamis
        try {
            double mag = Double.parseDouble(gempa.magnitude);
            if (mag >= 5.0) {
                cardMag.setCardBackgroundColor(Color.parseColor("#EF4444")); // Merah
            } else {
                cardMag.setCardBackgroundColor(Color.parseColor("#FB923C")); // Orange
            }
        } catch (Exception e) {
            cardMag.setCardBackgroundColor(Color.GRAY);
        }

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    // =================================================================================
    // BAGIAN 3: DATA CUACA (OPEN-METEO)
    // =================================================================================

    private void fetchWeatherData() {
        Retrofit weatherRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi api = weatherRetrofit.create(WeatherApi.class);

        // Request cuaca berdasarkan lokasi saat ini
        Call<WeatherData> call = api.getWeather(currentUserLat, currentUserLng, true, "kmh");

        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showWeatherBottomSheet(response.body().currentWeather);
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Gagal memuat cuaca", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showWeatherBottomSheet(WeatherData.CurrentWeather weather) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_weather_sheet, null);

        TextView tvSuhu = view.findViewById(R.id.tvSuhu);
        TextView tvDesc = view.findViewById(R.id.tvDeskripsiCuaca);
        TextView tvAngin = view.findViewById(R.id.tvAngin);
        ImageView imgCuaca = view.findViewById(R.id.imgCuaca);

        // Set Data
        tvSuhu.setText(String.format(Locale.US, "%.1f°C", weather.temperature));
        tvAngin.setText(String.format(Locale.US, "Angin: %.1f km/h", weather.windspeed));

        // Logika kode cuaca WMO
        String desc;
        int iconRes;
        int code = weather.weathercode;

        if (code == 0) {
            desc = "Cerah";
            iconRes = android.R.drawable.ic_menu_day;
        } else if (code <= 3) {
            desc = "Berawan";
            iconRes = android.R.drawable.ic_menu_sort_by_size;
        } else if (code <= 48) {
            desc = "Kabut";
            iconRes = android.R.drawable.ic_menu_view;
        } else if (code <= 67) {
            desc = "Hujan Ringan";
            iconRes = android.R.drawable.ic_menu_upload;
        } else if (code <= 77) {
            desc = "Salju";
            iconRes = android.R.drawable.ic_menu_compass;
        } else if (code <= 99) {
            desc = "Hujan Badai";
            iconRes = android.R.drawable.ic_menu_close_clear_cancel;
        } else {
            desc = "Mendung";
            iconRes = android.R.drawable.ic_menu_help;
        }

        tvDesc.setText(desc);
        imgCuaca.setImageResource(iconRes);

        dialog.setContentView(view);
        dialog.show();
    }

    // =================================================================================
    // BAGIAN 4: LOKASI (GPS)
    // =================================================================================

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Jika izin belum ada, minta izin
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            // Jika izin sudah ada, mulai GPS
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            // 1. Coba ambil lokasi terakhir (Cache) agar cepat
            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnown != null) {
                updateUserLocation(lastKnown);
            } else {
                // Coba Network provider jika GPS kosong
                lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastKnown != null) updateUserLocation(lastKnown);
            }

            // 2. Minta update berkala
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Izin lokasi ditolak. Fitur jarak & cuaca tidak akan akurat.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        updateUserLocation(location);
    }

    private void updateUserLocation(Location location) {
        currentUserLat = location.getLatitude();
        currentUserLng = location.getLongitude();

        // Update Marker User di Peta (Javascript)
        String jsCommand = String.format(Locale.US, "javascript:setUserLocation(%f, %f)", currentUserLat, currentUserLng);
        webViewMap.evaluateJavascript(jsCommand, null);
    }

    // Implementasi method interface LocationListener lainnya (kosongkan saja jika tidak dipakai)
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(@NonNull String provider) {}
    @Override public void onProviderDisabled(@NonNull String provider) {}
}