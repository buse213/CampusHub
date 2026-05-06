package com.busenurcam.campushub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class GorselSliderAdapter extends RecyclerView.Adapter<GorselSliderAdapter.SliderViewHolder> {

    private List<String> gorselListesi;

    public GorselSliderAdapter(List<String> gorselListesi) {
        this.gorselListesi = gorselListesi;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Not: Burada 'item_slider_gorsel' adında basit bir ImageView içeren layout kullanabilirsin
        // veya direkt kodla ImageView oluşturabilirsin. En kolayı basit bir layout:
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider_gorsel, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(gorselListesi.get(position))
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return gorselListesi != null ? gorselListesi.size() : 0;
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgSliderItem);
        }
    }
}