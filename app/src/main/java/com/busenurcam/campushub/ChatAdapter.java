package com.busenurcam.campushub;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int MESAJ_GIDEN = 1;
    private static final int MESAJ_GELEN = 2;

    // Uyarıyı gidermek için bu alanları 'final' yaptık
    private final ArrayList<Mesaj> mesajListesi;
    private final String mevcutKullaniciEmail;

    public ChatAdapter(ArrayList<Mesaj> mesajListesi, String mevcutKullaniciEmail) {
        this.mesajListesi = mesajListesi;
        this.mevcutKullaniciEmail = mevcutKullaniciEmail;
    }

    @Override
    public int getItemViewType(int position) {
        if (mesajListesi.get(position).gonderen.equals(mevcutKullaniciEmail)) {
            return MESAJ_GIDEN;
        } else {
            return MESAJ_GELEN;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MESAJ_GIDEN) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mesaj_giden, parent, false);
            return new GidenMesajHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mesaj_gelen, parent, false);
            return new GelenMesajHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mesaj mesaj = mesajListesi.get(position);

        if (holder instanceof GidenMesajHolder) {
            ((GidenMesajHolder) holder).txtMesaj.setText(mesaj.mesaj);
        } else if (holder instanceof GelenMesajHolder) {
            GelenMesajHolder gelenHolder = (GelenMesajHolder) holder;
            gelenHolder.txtMesaj.setText(mesaj.mesaj);

            String gonderenEmail = mesaj.gonderen;
            if (gonderenEmail != null && !gonderenEmail.isEmpty()) {
                // Baş harfi al
                String ilkHarf = gonderenEmail.substring(0, 1).toUpperCase();
                gelenHolder.txtAvatarHarf.setText(ilkHarf);

                // Renk seçimi
                int[] renkler = {
                        Color.parseColor("#8A2BE2"),
                        Color.parseColor("#1E88E5"),
                        Color.parseColor("#43A047"),
                        Color.parseColor("#E53935"),
                        Color.parseColor("#FB8C00")
                };
                int renkIndex = Math.abs(gonderenEmail.hashCode()) % renkler.length;

                // Arka plan rengini ata
                if (gelenHolder.viewAvatarArkaPlan.getBackground() != null) {
                    gelenHolder.viewAvatarArkaPlan.getBackground().setTint(renkler[renkIndex]);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mesajListesi.size();
    }

    // --- HOLDER SINIFLARI (UYARI İÇİN STATIC YAPILDI) ---

    static class GidenMesajHolder extends RecyclerView.ViewHolder {
        TextView txtMesaj;
        GidenMesajHolder(@NonNull View itemView) {
            super(itemView);
            txtMesaj = itemView.findViewById(R.id.txtMesajGiden);
        }
    }

    static class GelenMesajHolder extends RecyclerView.ViewHolder {
        TextView txtMesaj, txtAvatarHarf; // Hata veren değişkeni buraya ekledik
        View viewAvatarArkaPlan;        // Hata veren değişkeni buraya ekledik

        GelenMesajHolder(@NonNull View itemView) {
            super(itemView);
            txtMesaj = itemView.findViewById(R.id.txtMesajGelen);

            // XML ID'leri ile eşleştiriyoruz
            txtAvatarHarf = itemView.findViewById(R.id.txtAvatarHarf);
            viewAvatarArkaPlan = itemView.findViewById(R.id.viewAvatarArkaPlan);
        }
    }
}