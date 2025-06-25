package com.example.pertemuan5_shafa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GempaDirasakanActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    GempaDirasakanAdapter adapter;
    List<GempaModel> gempaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gempa_dirasakan);

        recyclerView = findViewById(R.id.recyclerViewDirasakan);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.back_toolbar);
        adapter = new GempaDirasakanAdapter(gempaList, this);
        recyclerView.setAdapter(adapter);

//        ambilDataGempaDirasakan();
        setSupportActionBar(toolbar);
        getLokasiPengguna();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private double userLat = 0, userLon = 0;

    private void getLokasiPengguna() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLon = location.getLongitude();
                    ambilDataGempaDirasakan(); // ← Panggil hanya setelah dapat lokasi
                } else {
                    Toast.makeText(this, "Gagal mendapatkan lokasi Anda", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }


    private void ambilDataGempaDirasakan() {
        String url = "https://data.bmkg.go.id/DataMKG/TEWS/gempadirasakan.json";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject root = new JSONObject(response);
                        JSONArray arrayGempa = root.getJSONObject("Infogempa").getJSONArray("gempa");

                        for (int i = 0; i < arrayGempa.length(); i++) {
                            JSONObject gempa = arrayGempa.getJSONObject(i);

                            String tanggal = gempa.getString("Tanggal");
                            String jam = gempa.getString("Jam");
                            String wilayah = gempa.getString("Wilayah");
                            String magnitude = gempa.getString("Magnitude");

                            // Ambil dan parsing koordinat
                            String koordinat = gempa.getString("Coordinates"); // Format: "lat,lon"
                            String[] split = koordinat.split(",");
                            double lat = Double.parseDouble(split[0].trim());
                            double lon = Double.parseDouble(split[1].trim());
                            // Hitung jarak ke user
                            float[] hasil = new float[1];
                            Location.distanceBetween(userLat, userLon, lat, lon, hasil);
                            double jarakKm = hasil[0] / 1000.0;

                            GempaModel model = new GempaModel(tanggal, jam, magnitude, wilayah, lat, lon);
                            model.setJarakKeUser(jarakKm);
                            gempaList.add(model);

                        }


                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Gagal ambil data", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}