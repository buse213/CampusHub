package com.busenurcam.campushub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class KategoriDetayActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ArrayList<Ilan> ilanListesi;
    private IlanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kategori_detay);

        db = FirebaseFirestore.getInstance();
        ilanListesi = new ArrayList<>();

        // View'ları Tanımla
        RelativeLayout header = findViewById(R.id.categoryHeader);
        TextView title = findViewById(R.id.txtCategoryTitle);
        TextView txtIlanSayisi = findViewById(R.id.txtIlanSayisi);
        ImageButton btnBack = findViewById(R.id.btnBack);
        FloatingActionButton fabEkle = findViewById(R.id.fab_kategori_ilan_ekle);

        // Intent'ten Verileri Al
        String kategoriAdi = getIntent().getStringExtra("kategoriAdi");
        String renk = getIntent().getStringExtra("renk");

        // Sayfayı Giydir
        if (kategoriAdi != null) title.setText(kategoriAdi);
        if (renk != null) header.setBackgroundColor(Color.parseColor(renk));

        // RecyclerView Hazırla
        recyclerView = findViewById(R.id.recyclerViewKategori);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IlanAdapter(ilanListesi);
        recyclerView.setAdapter(adapter);

        // Geri Butonu
        btnBack.setOnClickListener(v -> finish());

        // --- YENİ İLAN EKLEME BUTONU ---
        fabEkle.setOnClickListener(v -> {
            Intent intent = new Intent(KategoriDetayActivity.this, IlanEkleActivity.class);
            // Hangi kategoriden gidildiğini de gönderiyoruz
            intent.putExtra("kategoriAdi", kategoriAdi);
            startActivity(intent);
        });

        // Verileri Firestore'dan Getir
        kategoriyeGoreGetir(kategoriAdi, txtIlanSayisi);
    }

    private void kategoriyeGoreGetir(String kategori, TextView txtCount) {
        // DİKKAT: Firestore'da alan ismi "ilanTipi" ise burayı "ilanTipi" yapmalısın
        db.collection("Ilanlar")
                .whereEqualTo("ilanTipi", kategori)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        ilanListesi.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Ilan ilan = doc.toObject(Ilan.class);
                            ilan.ilanId = doc.getId();
                            ilanListesi.add(ilan);
                        }
                        adapter.notifyDataSetChanged();
                        txtCount.setText("Bu kategoride " + ilanListesi.size() + " aktif ilan var");
                    }
                });
    }
}