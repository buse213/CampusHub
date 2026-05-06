package com.busenurcam.campushub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2; // Eklendi
import com.google.android.material.tabs.TabLayout; // Eklendi
import com.google.android.material.tabs.TabLayoutMediator; // Eklendi
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class IlanDetayActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double lat = 0.0, lng = 0.0;
    private String saticiMail = "";
    private String ilanId = "";
    private ArrayList<String> resimUrls;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageButton btnGeri;

    // Slider bileşenleri
    private ViewPager2 viewPagerGorseller;
    private TabLayout tabLayoutDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ilan_detay);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView tvBaslik = findViewById(R.id.txtDetayBaslik);
        TextView tvFiyat = findViewById(R.id.txtDetayFiyat);
        TextView tvAciklama = findViewById(R.id.txtDetayAciklama);

        // --- SLIDER BAĞLANTILARI ---
        viewPagerGorseller = findViewById(R.id.viewPagerGorseller);
        tabLayoutDots = findViewById(R.id.tabLayoutDots);

        Button btnSaticiMesaj = findViewById(R.id.btnSaticiMesaj);
        LinearLayout layoutSahipButonlari = findViewById(R.id.layoutSahipButonlari);
        Button btnIlanSil = findViewById(R.id.btnIlanSil);
        Button btnIlanGuncelle = findViewById(R.id.btnIlanGuncelle);

        btnGeri = findViewById(R.id.btnGeri);
        if (btnGeri != null) {
            btnGeri.setOnClickListener(v -> finish());
        }

        // Verileri al
        ilanId = getIntent().getStringExtra("ilanId");
        String baslik = getIntent().getStringExtra("ilanBaslik");
        String fiyat = getIntent().getStringExtra("ilanFiyat");
        String aciklama = getIntent().getStringExtra("ilanAciklama");
        resimUrls = getIntent().getStringArrayListExtra("ilanResimler");
        saticiMail = getIntent().getStringExtra("ilanSahibiEmail");
        lat = getIntent().getDoubleExtra("lat", 0.0);
        lng = getIntent().getDoubleExtra("lng", 0.0);

        if (baslik != null) tvBaslik.setText(baslik);
        if (fiyat != null) tvFiyat.setText(fiyat.contains("TL") ? fiyat : fiyat + " TL");
        if (aciklama != null) tvAciklama.setText(aciklama);

        // --- SLIDER KURULUMU ---
        if (resimUrls != null && !resimUrls.isEmpty()) {
            GorselSliderAdapter adapter = new GorselSliderAdapter(resimUrls);
            viewPagerGorseller.setAdapter(adapter);

            // Alt kısımdaki noktaları (indicator) ViewPager'a bağla
            new TabLayoutMediator(tabLayoutDots, viewPagerGorseller, (tab, position) -> {
                // Noktaların içinde metin olmasın diye boş bırakıyoruz
            }).attach();
        }

        // --- SAHİPLİK VE DİĞER İŞLEMLER ---
        if (mAuth.getCurrentUser() != null) {
            String mevcutKullaniciEmail = mAuth.getCurrentUser().getEmail();
            if (mevcutKullaniciEmail != null && mevcutKullaniciEmail.equals(saticiMail)) {
                btnSaticiMesaj.setVisibility(View.GONE);
                layoutSahipButonlari.setVisibility(View.VISIBLE);
            } else {
                btnSaticiMesaj.setVisibility(View.VISIBLE);
                layoutSahipButonlari.setVisibility(View.GONE);
            }
        }

        btnIlanSil.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("İlanı Sil")
                    .setMessage("Bu ilanı kalıcı olarak silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet, Sil", (dialog, which) -> {
                        if (ilanId != null && !ilanId.isEmpty()) {
                            db.collection("Ilanlar").document(ilanId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "İlan başarıyla silindi.", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("Vazgeç", null)
                    .show();
        });

        btnIlanGuncelle.setOnClickListener(v -> {
            Intent updateIntent = new Intent(IlanDetayActivity.this, IlanEkleActivity.class);
            updateIntent.putExtra("isUpdate", true);
            updateIntent.putExtra("ilanId", ilanId);
            updateIntent.putExtra("eskiBaslik", baslik);
            updateIntent.putExtra("eskiFiyat", fiyat);
            updateIntent.putExtra("eskiAciklama", aciklama);
            updateIntent.putStringArrayListExtra("ilanResimler", resimUrls);
            updateIntent.putExtra("lat", lat);
            updateIntent.putExtra("lng", lng);
            startActivity(updateIntent);
        });

        btnSaticiMesaj.setOnClickListener(v -> {
            if (saticiMail != null && !saticiMail.isEmpty()) {
                Intent intent = new Intent(IlanDetayActivity.this, ChatActivity.class);
                intent.putExtra("aliciMail", saticiMail);
                startActivity(intent);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (lat != 0.0 && lng != 0.0) {
            LatLng ilanKonum = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(ilanKonum).title("İlan Konumu"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ilanKonum, 15f));
        }
    }
}