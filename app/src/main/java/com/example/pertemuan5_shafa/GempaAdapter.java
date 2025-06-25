package com.example.pertemuan5_shafa;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GempaAdapter extends RecyclerView.Adapter<GempaAdapter.ViewHolder> {

    private final List<GempaModel> gempaList;
    private final Context context;

    public GempaAdapter(List<GempaModel> gempaList, Context context) {
        this.gempaList = gempaList;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gempa_dirasakan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        GempaModel gempa = gempaList.get(position);
        holder.txtWaktu.setText(gempa.getTanggal() + " " + gempa.getJam());
        holder.txtWilayah.setText(gempa.getWilayah());
        holder.txtMagnitude.setText("Magnitude: " + gempa.getMagnitude());
        holder.txtJarak.setText(String.format("Jarak dari lokasi Anda: %.2f km", gempa.getJarakKeUser()));


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("latitude", gempa.getLat());
            intent.putExtra("longitude", gempa.getLon());
            intent.putExtra("wilayah", gempa.getWilayah());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return gempaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtWaktu, txtWilayah, txtMagnitude, txtJarak;

        public ViewHolder( View itemView) {
            super(itemView);
            txtWaktu = itemView.findViewById(R.id.txtWaktu);
            txtWilayah = itemView.findViewById(R.id.txtWilayah);
            txtMagnitude = itemView.findViewById(R.id.txtMagnitude);
            txtJarak = itemView.findViewById(R.id.txtJarak);
        }
    }
}