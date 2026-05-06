package com.busenurcam.campushub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource; // Eklendi
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private ArrayList<Ilan> ilanListesi;
    private IlanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        ilanListesi = new ArrayList<>();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewIlanlar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IlanAdapter(ilanListesi);
        recyclerView.setAdapter(adapter);

        setupInitialViews();
        verileriGetir();
        istatistikleriGetir(); // Sayıları canlı çekmek için eklendi
    }

    private void istatistikleriGetir() {
        // 1. AKTİF İLAN SAYISINI SAY
        db.collection("Ilanlar").count().get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long ilanSayisi = task.getResult().getCount();
                updateStat(R.id.stat1, String.valueOf(ilanSayisi), "Aktif İlan");
            }
        });

        // 2. ÖĞRENCİ (KULLANICI) SAYISINI SAY
        // Not: Koleksiyon adın "Kullanicilar" değilse burayı "Users" vb. ile değiştirebilirsin.
        db.collection("Kullanicilar").count().get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long ogrenciSayisi = task.getResult().getCount();
                String formatliSayi;

                if (ogrenciSayisi >= 1000) {
                    formatliSayi = (ogrenciSayisi / 1000) + "." + ((ogrenciSayisi % 1000) / 100) + "k+";
                } else {
                    formatliSayi = String.valueOf(ogrenciSayisi);
                }
                updateStat(R.id.stat2, formatliSayi, "Öğrenci");
            }
        });
    }

    private void verileriGetir() {
        db.collection("Ilanlar")
                .limit(4)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FirebaseError", "Hata: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        ilanListesi.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Ilan ilan = doc.toObject(Ilan.class);
                            ilan.ilanId = doc.getId();
                            ilanListesi.add(ilan);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupInitialViews() {
        setupCategory(R.id.catHome, "#8A2BE2", "🏠 Ev Arkadaşı", "Odanı paylaş veya yeni bir eve çık.");
        setupCategory(R.id.catMarket, "#FF4500", "📦 İkinci El Eşya", "Bilgisayar, masa... Uygun fiyata kap.");
        setupCategory(R.id.catTransfer, "#00A36C", "🔑 Ev Devretme", "Mezun oluyorum, düzenimi devrediyorum.");

        findViewById(R.id.catHome).setOnClickListener(v -> openCategoryDetail("🏠 Ev Arkadaşı", "#8A2BE2"));
        findViewById(R.id.catMarket).setOnClickListener(v -> openCategoryDetail("📦 İkinci El Eşya", "#FF4500"));
        findViewById(R.id.catTransfer).setOnClickListener(v -> openCategoryDetail("🔑 Ev Devretme", "#00A36C"));

        findViewById(R.id.fab_menu).setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, v);
            popup.getMenu().add("🏠 Ev Arkadaşı");
            popup.getMenu().add("📦 İkinci El Eşya");
            popup.getMenu().add("🔑 Ev Devretme");
            popup.getMenu().add("💬 Mesajlarım");
            popup.getMenu().add("➕ Yeni İlan Oluştur");

            popup.setOnMenuItemClickListener(item -> {
                String secim = item.getTitle().toString();
                if (secim.contains("Ev Arkadaşı")) {
                    openCategoryDetail("🏠 Ev Arkadaşı", "#8A2BE2");
                } else if (secim.contains("İkinci El Eşya")) {
                    openCategoryDetail("📦 İkinci El Eşya", "#FF4500");
                } else if (secim.contains("Ev Devretme")) {
                    openCategoryDetail("🔑 Ev Devretme", "#00A36C");
                } else if (secim.contains("Mesajlarım")) {
                    startActivity(new Intent(MainActivity.this, MesajlarListeActivity.class));
                } else if (secim.contains("Yeni İlan")) {
                    startActivity(new Intent(MainActivity.this, IlanEkleActivity.class));
                }
                return true;
            });
            popup.show();
        });

        // Üçüncü istatistik (Güvenlik) sabit kalabilir
        updateStat(R.id.stat3, "%100", "Güvenli");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfilActivity.class));
                return true;
            } else if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_explore) {
                startActivity(new Intent(MainActivity.this, KesfetActivity.class));
                return true;
            } else if (itemId == R.id.nav_messages) {
                startActivity(new Intent(MainActivity.this, MesajlarListeActivity.class));
                return true;
            }
            return false;
        });
    }

    private void openCategoryDetail(String title, String color) {
        Intent intent = new Intent(MainActivity.this, KategoriDetayActivity.class);
        intent.putExtra("kategoriAdi", title);
        intent.putExtra("renk", color);
        startActivity(intent);
    }

    private void setupCategory(int containerId, String colorHex, String title, String desc) {
        View container = findViewById(containerId);
        if (container != null) {
            View bg = container.findViewById(R.id.categoryBackground);
            TextView txtTitle = container.findViewById(R.id.categoryTitle);
            TextView txtDesc = container.findViewById(R.id.categoryDesc);
            if (bg != null) bg.setBackgroundColor(Color.parseColor(colorHex));
            if (txtTitle != null) txtTitle.setText(title);
            if (txtDesc != null) txtDesc.setText(desc);
        }
    }

    private void updateStat(int id, String val, String lab) {
        View v = findViewById(id);
        if (v != null) {
            TextView txtVal = v.findViewById(R.id.statValue);
            TextView txtLabel = v.findViewById(R.id.statLabel);
            if (txtVal != null) txtVal.setText(val);
            if (txtLabel != null) txtLabel.setText(lab);
        }
    }
}