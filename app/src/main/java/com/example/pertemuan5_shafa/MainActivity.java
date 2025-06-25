package com.example.pertemuan5_shafa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

public class  MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private ImageView imageGempa, share;
    private TextView tvTanggal, tvJam, tvMagnitude, tvKedalaman, tvWilayah, tvKoordinat;
    private double latGempa = 0.0;
    private double lonGempa = 0.0;
    private String lastShakemapFileName = ""; // DIBUAT GLOBAL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OneSignal.initWithContext(this);
        OneSignal.setAppId("f0b98772-3ee0-4809-a26f-a52530b068ef");

        setContentView(R.layout.activity_main);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        imageGempa = findViewById(R.id.imageGempa);
        share = findViewById(R.id.share);
        tvTanggal = findViewById(R.id.tvTanggal);
        tvJam = findViewById(R.id.tvJam);
        tvMagnitude = findViewById(R.id.tvMagnitude);
        tvKedalaman = findViewById(R.id.tvKedalaman);
        tvWilayah = findViewById(R.id.tvWilayah);
        tvKoordinat = findViewById(R.id.tvKoordinat);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ambilDataGempa();
    }

    private void ambilDataGempa() {
        String url = "https://data.bmkg.go.id/DataMKG/TEWS/autogempa.json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject gempa = response.getJSONObject("Infogempa").getJSONObject("gempa");

                        String tanggal = gempa.getString("Tanggal");
                        String jam = gempa.getString("Jam");
                        String magnitude = gempa.getString("Magnitude");
                        String kedalaman = gempa.getString("Kedalaman");
                        String wilayah = gempa.getString("Wilayah");
                        String koordinat = gempa.getString("Coordinates");
                        lastShakemapFileName = gempa.getString("Shakemap");

                        String shakemapUrl = "https://data.bmkg.go.id/DataMKG/TEWS/" + lastShakemapFileName;

                        tvTanggal.setText("Tanggal: " + tanggal);
                        tvJam.setText("Jam: " + jam);
                        tvMagnitude.setText("Magnitude: " + magnitude);
                        tvKedalaman.setText("Kedalaman: " + kedalaman);
                        tvWilayah.setText("Wilayah: " + wilayah);
                        tvKoordinat.setText("Koordinat: " + koordinat);

                        Glide.with(this).load(shakemapUrl).into(imageGempa);

                        // Parsing koordinat
                        String[] koordinatSplit = koordinat.split(",");
                        if (koordinatSplit.length == 2) {
                            latGempa = Double.parseDouble(koordinatSplit[0].trim());
                            lonGempa = Double.parseDouble(koordinatSplit[1].trim());

                            if (mMap != null) {
                                tampilkanMarker();
                            }
                        }


                        try {
                            double magnitudo = Double.parseDouble(magnitude);
                            if (magnitudo >= 3) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Peringatan Gempa!")
                                        .setMessage("Terjadi gempa dengan magnitudo " + magnitude +
                                                "\nSegera waspada dan cek info resmi dari BMKG.")
                                        .setPositiveButton("Oke", null)
                                        .show();
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);

        // LOGIC UNTUK TOMBOL SHARE
        share.setOnClickListener(v -> {
            String imageUrl = "https://data.bmkg.go.id/DataMKG/TEWS/" + lastShakemapFileName;

            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource,  Transition<? super Bitmap> transition) {
                            try {
                                File cachePath = new File(getCacheDir(), "images");
                                cachePath.mkdirs();
                                File file = new File(cachePath, "shakemap.png");
                                FileOutputStream stream = new FileOutputStream(file);
                                resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                stream.close();

                                Uri contentUri = FileProvider.getUriForFile(
                                        MainActivity.this,
                                        getPackageName() + ".fileprovider",
                                        file
                                );

                                if (contentUri != null) {
                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Cek info gempa terbaru dari BMKG!");
                                    startActivity(Intent.createChooser(shareIntent, "Bagikan dengan"));
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                        }
                    });
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (latGempa != 0.0 && lonGempa != 0.0) {
            tampilkanMarker();
        }
    }

    private void tampilkanMarker() {
        LatLng lokasiGempa = new LatLng(latGempa, lonGempa);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(lokasiGempa).title("Lokasi Gempa"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiGempa, 5));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_gempa_terbaru:
                return true;
            case R.id.menu_gempa_5plus:
                startActivity(new Intent(this, Gempa5PlusActivity.class));
                return true;
            case R.id.menu_gempa_dirasakan:
                startActivity(new Intent(this, GempaDirasakanActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}