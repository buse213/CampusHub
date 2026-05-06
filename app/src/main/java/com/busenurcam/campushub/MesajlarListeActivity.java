package com.busenurcam.campushub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MesajlarListeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtBosListe;
    private ArrayList<Mesaj> mesajListesi;
    private SohbetAdapter adapter;
    private FirebaseFirestore db;
    private String mevcutKullaniciEmail;
    private ImageButton btnGeri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesajlar_liste);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mevcutKullaniciEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }

        btnGeri = findViewById(R.id.btnGeriMesajlar);
        if (btnGeri != null) {
            btnGeri.setOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recyclerMesajListesi);
        txtBosListe = findViewById(R.id.txtBosListeUyarisi);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mesajListesi = new ArrayList<>();

        adapter = new SohbetAdapter(mesajListesi, mevcutKullaniciEmail);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new SohbetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Mesaj mesaj) {
                if (mesaj != null && mevcutKullaniciEmail != null) {
                    Intent intent = new Intent(MesajlarListeActivity.this, ChatActivity.class);
                    String konusulanKisi = mevcutKullaniciEmail.equals(mesaj.gonderen) ? mesaj.alici : mesaj.gonderen;
                    intent.putExtra("aliciMail", konusulanKisi);
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(Mesaj mesaj) {
                if (mesaj != null && mevcutKullaniciEmail != null) {
                    String konusulanKisi = mevcutKullaniciEmail.equals(mesaj.gonderen) ? mesaj.alici : mesaj.gonderen;

                    new AlertDialog.Builder(MesajlarListeActivity.this)
                            .setTitle("Sohbeti Sil")
                            .setMessage("Bu sohbet sadece sizden silinecek. Emin misiniz?")
                            .setPositiveButton("Benden Sil", (dialog, which) -> sohbetiGizle(konusulanKisi))
                            .setNegativeButton("İptal", null)
                            .show();
                }
            }
        });

        mesajlariGetir();
    }

    private void sohbetiGizle(String digerKisiEmail) {
        if (mevcutKullaniciEmail == null || digerKisiEmail == null) return;

        db.collection("Mesajlar")
                .whereIn("gonderen", Arrays.asList(mevcutKullaniciEmail, digerKisiEmail))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null) return;

                    WriteBatch batch = db.batch();
                    boolean guncellenecekVarMi = false;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Mesaj m = doc.toObject(Mesaj.class);
                        if (m != null) {
                            // Ben gönderdiysem 'gonderenSildi' bayrağını true yap
                            if (m.gonderen.equals(mevcutKullaniciEmail) && m.alici.equals(digerKisiEmail)) {
                                batch.update(doc.getReference(), "gonderenSildi", true);
                                guncellenecekVarMi = true;
                            }
                            // Ben aldıysam 'aliciSildi' bayrağını true yap
                            else if (m.gonderen.equals(digerKisiEmail) && m.alici.equals(mevcutKullaniciEmail)) {
                                batch.update(doc.getReference(), "aliciSildi", true);
                                guncellenecekVarMi = true;
                            }
                        }
                    }

                    if (guncellenecekVarMi) {
                        batch.commit().addOnSuccessListener(aVoid -> {
                            Toast.makeText(MesajlarListeActivity.this, "Sohbet sizden gizlendi", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void mesajlariGetir() {
        if (mevcutKullaniciEmail == null) return;

        db.collection("Mesajlar")
                .orderBy("tarih", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        HashMap<String, Mesaj> sonMesajlarMap = new HashMap<>();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Mesaj m = doc.toObject(Mesaj.class);

                            if (m != null && m.alici != null && m.gonderen != null) {
                                // Sadece kullanıcının dahil olduğu ve henüz SİLMEDİĞİ mesajları kontrol et
                                boolean benGonderenimVeSildim = mevcutKullaniciEmail.equals(m.gonderen) && m.gonderenSildi;
                                boolean benAliciyimVeSildim = mevcutKullaniciEmail.equals(m.alici) && m.aliciSildi;

                                if (!benGonderenimVeSildim && !benAliciyimVeSildim) {
                                    if (mevcutKullaniciEmail.equals(m.alici) || mevcutKullaniciEmail.equals(m.gonderen)) {
                                        String konusulanKisi = mevcutKullaniciEmail.equals(m.gonderen) ? m.alici : m.gonderen;
                                        if (!sonMesajlarMap.containsKey(konusulanKisi)) {
                                            sonMesajlarMap.put(konusulanKisi, m);
                                        }
                                    }
                                }
                            }
                        }

                        mesajListesi.clear();
                        mesajListesi.addAll(sonMesajlarMap.values());

                        if (mesajListesi.isEmpty()) {
                            txtBosListe.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            txtBosListe.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}