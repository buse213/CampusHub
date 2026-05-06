package com.busenurcam.campushub;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class IlanAdapter extends RecyclerView.Adapter<IlanAdapter.IlanViewHolder> {

    private ArrayList<Ilan> ilanListesi;

    public IlanAdapter(ArrayList<Ilan> ilanListesi) {
        this.ilanListesi = ilanListesi;
    }

    @NonNull
    @Override
    public IlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tasarim_yeni, parent, false);
        return new IlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IlanViewHolder holder, int position) {
        Ilan ilan = ilanListesi.get(position);

        holder.txtBaslik.setText(ilan.getBaslik());
        holder.txtKategori.setText(ilan.getIlanTipi());

        String fiyatText = ilan.getFiyat() + " TL";
        holder.txtFiyat.setText(fiyatText);

        // --- RESİM YÜKLEME (ESKİ VE YENİ VERİ UYUMLU) ---
        // getKapakResmi() metodu sayesinde veri hangi formatta olursa olsun doğru linki çekeriz.
        Glide.with(holder.itemView.getContext())
                .load(ilan.getKapakResmi())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .centerCrop()
                .into(holder.imgIlan);

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, IlanDetayActivity.class);

            intent.putExtra("ilanId", ilan.getIlanId());
            intent.putExtra("ilanBaslik", ilan.getBaslik());
            intent.putExtra("ilanFiyat", fiyatText);
            intent.putExtra("ilanAciklama", ilan.getAciklama() != null ? ilan.getAciklama() : "Açıklama belirtilmemiş.");
            intent.putExtra("ilanSahibiEmail", ilan.getSaticiMail());

            // --- DETAY SAYFASI İÇİN RESİM PAKETLEME ---
            // Detay sayfası liste beklediği için eski veriyi de listeye çevirip gönderiyoruz.
            ArrayList<String> gonderilecekResimler = new ArrayList<>();
            if (ilan.getResimUrls() != null && !ilan.getResimUrls().isEmpty()) {
                gonderilecekResimler.addAll(ilan.getResimUrls());
            } else if (ilan.resimUrl != null && !ilan.resimUrl.isEmpty()) {
                gonderilecekResimler.add(ilan.resimUrl);
            }
            intent.putStringArrayListExtra("ilanResimler", gonderilecekResimler);

            intent.putExtra("lat", ilan.getLatitude());
            intent.putExtra("lng", ilan.getLongitude());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ilanListesi != null ? ilanListesi.size() : 0;
    }

    public static class IlanViewHolder extends RecyclerView.ViewHolder {
        TextView txtBaslik, txtFiyat, txtKategori;
        ImageView imgIlan;

        public IlanViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBaslik = itemView.findViewById(R.id.txtIlanBaslik);
            txtFiyat = itemView.findViewById(R.id.txtIlanFiyat);
            txtKategori = itemView.findViewById(R.id.txtIlanKategori);
            imgIlan = itemView.findViewById(R.id.imgIlan);
        }
    }
}