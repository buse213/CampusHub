package com.busenurcam.campushub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog; // AlertDialog için gerekli
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;

public class ProfilActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageButton btnGeri;

    private RecyclerView rvKullaniciIlanlari;
    private IlanAdapter adapter;
    private ArrayList<Ilan> ilanListesi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // View Tanımlamaları
        TextView txtEmail = findViewById(R.id.txtProfileEmail);
        TextView txtName = findViewById(R.id.txtProfileName);
        Button btnLogout = findViewById(R.id.btnLogout);
        btnGeri = findViewById(R.id.btnGeriProfil);

        // RecyclerView Kurulumu
        rvKullaniciIlanlari = findViewById(R.id.rvKullaniciIlanlari);
        rvKullaniciIlanlari.setLayoutManager(new LinearLayoutManager(this));
        ilanListesi = new ArrayList<>();
        adapter = new IlanAdapter(ilanListesi);
        rvKullaniciIlanlari.setAdapter(adapter);

        // --- GERİ BUTONU ---
        if (btnGeri != null) {
            btnGeri.setOnClickListener(v -> finish());
        }

        txtName.setText("Yükleniyor...");

        if (user != null) {
            txtEmail.setText(user.getEmail());

            // 1. Kullanıcı Bilgilerini Getir (Ad-Soyad)
            db.collection("Kullanicilar").document(user.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String adSoyad = document.getString("adSoyad");
                                txtName.setText(adSoyad != null ? adSoyad : "İsim bilgisi boş");
                            } else {
                                txtName.setText("Profil kaydı bulunamadı");
                            }
                        }
                    });

            // 2. Kullanıcının Kendi İlanlarını Getir
            kullaniciIlanlariniYukle(user.getEmail());

        } else {
            // Kullanıcı oturumu kapalıysa Login'e gönder
            startActivity(new Intent(ProfilActivity.this, LoginActivity.class));
            finish();
        }

        // --- GÜVENLİ ÇIKIŞ YAP BUTONU (ONAY KUTULU) ---
        btnLogout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfilActivity.this);
            builder.setTitle("Güvenli Çıkış");
            builder.setMessage("Hesabınızdan çıkış yapmak istediğinize emin misiniz?");

            builder.setPositiveButton("Evet, Çıkış Yap", (dialog, which) -> {
                // Firebase oturumunu kapat
                mAuth.signOut();

                Intent intent = new Intent(ProfilActivity.this, LoginActivity.class);
                // Güvenlik: Tüm aktivite geçmişini temizle
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();
                Toast.makeText(ProfilActivity.this, "Başarıyla çıkış yapıldı", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Vazgeç", (dialog, which) -> {
                dialog.dismiss(); // Diyaloğu kapat
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void kullaniciIlanlariniYukle(String email) {
        db.collection("Ilanlar")
                .whereEqualTo("saticiMail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ilanListesi.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Ilan ilan = doc.toObject(Ilan.class);
                        if (ilan != null) {
                            ilan.setIlanId(doc.getId());
                            ilanListesi.add(ilan);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfilActivity.this, "İlanlar yüklenemedi", Toast.LENGTH_SHORT).show();
                });
    }
}