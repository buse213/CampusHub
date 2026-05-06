package com.busenurcam.campushub;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SohbetAdapter extends RecyclerView.Adapter<SohbetAdapter.SohbetHolder> {

    private final ArrayList<Mesaj> sohbetListesi;
    private final String mevcutKullaniciEmail;
    private OnItemClickListener listener;

    // Interface güncellendi: Silme işlemi için onItemLongClick eklendi
    public interface OnItemClickListener {
        void onItemClick(Mesaj mesaj);
        void onItemLongClick(Mesaj mesaj);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public SohbetAdapter(ArrayList<Mesaj> sohbetListesi, String mevcutKullaniciEmail) {
        this.sohbetListesi = sohbetListesi;
        this.mevcutKullaniciEmail = mevcutKullaniciEmail;
    }

    @NonNull
    @Override
    public SohbetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sohbet_satiri, parent, false);
        return new SohbetHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SohbetHolder holder, int position) {
        Mesaj mesaj = sohbetListesi.get(position);

        String gosterilecekKisi;
        if (mevcutKullaniciEmail != null) {
            gosterilecekKisi = mevcutKullaniciEmail.equals(mesaj.gonderen) ? mesaj.alici : mesaj.gonderen;
        } else {
            gosterilecekKisi = mesaj.gonderen;
        }

        holder.txtKisi.setText(gosterilecekKisi);
        holder.txtSonMesaj.setText(mesaj.mesaj);

        // Avatar ve Renk Mantığı
        if (gosterilecekKisi != null && !gosterilecekKisi.isEmpty()) {
            String ilkHarf = gosterilecekKisi.substring(0, 1).toUpperCase();
            holder.txtAvatarHarf.setText(ilkHarf);

            int[] renkler = {
                    Color.parseColor("#8A2BE2"),
                    Color.parseColor("#1E88E5"),
                    Color.parseColor("#43A047"),
                    Color.parseColor("#E53935"),
                    Color.parseColor("#FB8C00")
            };
            int renkIndex = Math.abs(gosterilecekKisi.hashCode()) % renkler.length;
            holder.viewAvatarArkaPlan.getBackground().setTint(renkler[renkIndex]);
        }

        // Kısa Tıklama: Sohbeti açar
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(mesaj);
            }
        });

        // Uzun Tıklama: Silme özelliğini tetikler
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(mesaj);
            }
            return true; // Tıklama olayının tüketildiğini belirtir
        });
    }

    @Override
    public int getItemCount() {
        return sohbetListesi.size();
    }

    class SohbetHolder extends RecyclerView.ViewHolder {
        TextView txtKisi, txtSonMesaj, txtAvatarHarf;
        View viewAvatarArkaPlan;

        public SohbetHolder(@NonNull View itemView) {
            super(itemView);
            txtKisi = itemView.findViewById(R.id.txtSohbetKisi);
            txtSonMesaj = itemView.findViewById(R.id.txtSonMesaj);
            txtAvatarHarf = itemView.findViewById(R.id.txtAvatarHarf);
            viewAvatarArkaPlan = itemView.findViewById(R.id.viewAvatarArkaPlan);
        }
    }
}