package com.example.pertemuan5_shafa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.example.pertemuan5_shafa.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private double lat, lon;
    private String wilayah;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LatLng lokasiPengguna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lat = getIntent().getDoubleExtra("latitude", 0);
        lon = getIntent().getDoubleExtra("longitude", 0);
        wilayah = getIntent().getStringExtra("wilayah");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng lokasiGempa = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(lokasiGempa).title("Gempa di " + wilayah));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiGempa, 6f));

        // Cek permission lokasi
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            mMap.setOnMyLocationChangeListener(location -> {
                lokasiPengguna = new LatLng(location.getLatitude(), location.getLongitude());

                // Hitung jarak
                float[] results = new float[1];
                Location.distanceBetween(
                        lokasiPengguna.latitude, lokasiPengguna.longitude,
                        lokasiGempa.latitude, lokasiGempa.longitude,
                        results
                );

                float jarakMeter = results[0];
                String jarakText = "Jarak ke pusat gempa: " + String.format("%.2f", jarakMeter / 1000) + " km";

                // Marker pengguna dengan jarak
                mMap.addMarker(new MarkerOptions()
                        .position(lokasiPengguna)
                        .title("Lokasi Anda")
                        .snippet(jarakText))
                        .showInfoWindow();

                // Tampilkan kedua lokasi dengan kamera
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(lokasiGempa)
                        .include(lokasiPengguna)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            });

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan untuk menampilkan jarak", Toast.LENGTH_SHORT).show();
            }
        }
    }
}