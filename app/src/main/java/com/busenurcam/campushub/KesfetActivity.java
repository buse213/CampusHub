package com.busenurcam.campushub;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class KesfetActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ArrayList<Ilan> ilanListesi;
    private ArrayList<Ilan> filtreliListe;
    private IlanAdapter adapter;
    private EditText searchEdit;

    // Konum İçin Gerekli Değişkenler
    private ImageButton btnKonumAl;
    private ImageButton btnGeri; // Geri butonu eklendi
    private ChipGroup chipGroupMesafe;
    private FusedLocationProviderClient fusedLocationClient;
    private double kullaniciLat = 0.0, kullaniciLng = 0.0;
    private float aktifMesafeFiltresi = -1; // -1: Tüm ilanlar demek

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kesfet);

        // Firebase ve Liste Tanımlamaları
        db = FirebaseFirestore.getInstance();
        ilanListesi = new ArrayList<>();
        filtreliListe = new ArrayList<>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // View Tanımlamaları
        searchEdit = findViewById(R.id.searchIlan);
        recyclerView = findViewById(R.id.recyclerViewKesfet);
        btnKonumAl = findViewById(R.id.btnKonumFiltre);
        chipGroupMesafe = findViewById(R.id.chipGroupMesafe);

        // GERİ BUTONU TANIMLAMASI VE TIKLAMA OLAYI
        btnGeri = findViewById(R.id.btnGeri);
        if (btnGeri != null) {
            btnGeri.setOnClickListener(v -> finish()); // Activity'yi kapatarak bir öncekine döner
        }

        // RecyclerView Hazırlığı
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IlanAdapter(filtreliListe);
        recyclerView.setAdapter(adapter);

        // 1. Verileri Çek
        tumIlanlariGetir();

        // 2. Metin Arama Dinleyicisi
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrelemeUygula(); // Metin değişince filtreleri yeniden çalıştır
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 3. Konum Butonu Tıklama
        btnKonumAl.setOnClickListener(v -> konumIzniniKontrolEt());

        // 4. Mesafe Chip Seçimi Dinleyicisi
        chipGroupMesafe.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip1km) aktifMesafeFiltresi = 1000;
            else if (checkedId == R.id.chip5km) aktifMesafeFiltresi = 5000;
            else if (checkedId == R.id.chip10km) aktifMesafeFiltresi = 10000;
            else aktifMesafeFiltresi = -1; // "Tümü" seçilirse

            filtrelemeUygula();
        });
    }

    private void konumIzniniKontrolEt() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            mevcutKonumuAl();
        }
    }

    private void mevcutKonumuAl() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    kullaniciLat = location.getLatitude();
                    kullaniciLng = location.getLongitude();
                    Toast.makeText(this, "Konumunuz başarıyla alındı!", Toast.LENGTH_SHORT).show();
                    filtrelemeUygula();
                } else {
                    // DÜZENLEME: Emülatör kullanıcıları için daha net bir hata mesajı
                    Toast.makeText(this, "Konum bulunamadı! Lütfen emülatör ayarlarına gidip (Location) manuel bir konum belirleyin (SET LOCATION).", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void tumIlanlariGetir() {
        db.collection("Ilanlar").addSnapshotListener((value, error) -> {
            if (value != null) {
                ilanListesi.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Ilan ilan = doc.toObject(Ilan.class);
                    ilan.ilanId = doc.getId();
                    ilanListesi.add(ilan);
                }
                filtrelemeUygula();
            }
        });
    }

    // HEM METİN HEM KONUM FİLTRESİNİ AYNI ANDA ÇALIŞTIRIR
    private void filtrelemeUygula() {
        String arananKelime = searchEdit.getText().toString().toLowerCase().trim();
        filtreliListe.clear();

        for (Ilan ilan : ilanListesi) {
            boolean metinUygun = ilan.baslik.toLowerCase().contains(arananKelime);
            boolean konumUygun = true;

            // Eğer bir mesafe filtresi aktifse mesafe kontrolü yap
            if (aktifMesafeFiltresi != -1) {
                if (kullaniciLat != 0.0) {
                    float[] mesafeSonucu = new float[1];
                    Location.distanceBetween(kullaniciLat, kullaniciLng, ilan.lat, ilan.lng, mesafeSonucu);
                    konumUygun = mesafeSonucu[0] <= aktifMesafeFiltresi;
                } else {
                    // Mesafe seçili ama konum alınmamışsa uyarı ver
                    konumUygun = true;
                }
            }

            if (metinUygun && konumUygun) {
                filtreliListe.add(ilan);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mevcutKonumuAl();
        }
    }
}