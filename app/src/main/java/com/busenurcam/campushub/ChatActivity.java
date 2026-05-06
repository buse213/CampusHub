package com.busenurcam.campushub;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private ArrayList<Mesaj> mesajListesi;
    private EditText etMesaj;
    private ImageButton btnGonder, btnGeri;
    private TextView txtSohbetBaslik;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String aliciMail;
    private String mevcutKullaniciEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mevcutKullaniciEmail = mAuth.getCurrentUser().getEmail();
        } else {
            mevcutKullaniciEmail = "";
        }

        aliciMail = getIntent().getStringExtra("aliciMail");

        if (aliciMail == null || aliciMail.isEmpty()) {
            aliciMail = "";
            Toast.makeText(this, "Hata: Alıcı adresi alınamadı!", Toast.LENGTH_LONG).show();
        }

        recyclerView = findViewById(R.id.recyclerChat);
        etMesaj = findViewById(R.id.etMesaj);
        btnGonder = findViewById(R.id.btnGonder);
        btnGeri = findViewById(R.id.btnGeri);
        txtSohbetBaslik = findViewById(R.id.txtSohbetBaslik);

        if (!aliciMail.isEmpty()) {
            txtSohbetBaslik.setText(aliciMail);
        }

        btnGeri.setOnClickListener(v -> finish());

        mesajListesi = new ArrayList<>();
        adapter = new ChatAdapter(mesajListesi, mevcutKullaniciEmail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        mesajlariGetir();

        btnGonder.setOnClickListener(v -> {
            mesajGonder();
        });
    }

    private void mesajGonder() {
        String mesajMetni = etMesaj.getText().toString().trim();

        if (mesajMetni.isEmpty()) {
            Toast.makeText(this, "Mesaj boş olamaz", Toast.LENGTH_SHORT).show();
            return;
        }

        if (aliciMail.isEmpty()) {
            Toast.makeText(this, "Hata: Alıcı mail boş!", Toast.LENGTH_SHORT).show();
            return;
        }

        Mesaj yeniMesaj = new Mesaj(
                mevcutKullaniciEmail,
                aliciMail,
                mesajMetni,
                System.currentTimeMillis()
        );

        db.collection("Mesajlar").add(yeniMesaj)
                .addOnSuccessListener(documentReference -> {
                    etMesaj.setText("");
                    bildirimMailiGonder(aliciMail, mesajMetni);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void bildirimMailiGonder(String saticiEmail, String mesajMetni) {
        OkHttpClient client = new OkHttpClient();

        String serviceId = "service_b6zt555";
        String templateId = "template_u7g9cxa";
        String publicKey = "RzCv8lt7M6BL-gGW5";

        String json = "{"
                + "\"service_id\": \"" + serviceId + "\","
                + "\"template_id\": \"" + templateId + "\","
                + "\"user_id\": \"" + publicKey + "\","
                + "\"template_params\": {"
                + "    \"alici_mail\": \"" + saticiEmail + "\","
                + "    \"mesaj_icerigi\": \"" + mesajMetni + "\""
                + "}"
                + "}";

        RequestBody body = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("EmailJS", "Mail gönderim hatası: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("EmailJS", "Bildirim maili başarıyla gönderildi!");
                }
            }
        });
    }

    private void mesajlariGetir() {
        if (mevcutKullaniciEmail.isEmpty() || aliciMail.isEmpty()) return;

        db.collection("Mesajlar")
                .orderBy("tarih", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        mesajListesi.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Mesaj m = doc.toObject(Mesaj.class);

                            if (m != null && m.gonderen != null && m.alici != null) {

                                // --- BENDEN SİL FİLTRESİ BURADA ---
                                // Eğer mesajı ben gönderdiysem 'gonderenSildi'ye bak, bana geldiyse 'aliciSildi'ye bak
                                boolean benSildimMi = (mevcutKullaniciEmail.equals(m.gonderen) && m.gonderenSildi) ||
                                        (mevcutKullaniciEmail.equals(m.alici) && m.aliciSildi);

                                if (!benSildimMi) {
                                    // Sadece silmediğim mesajları, doğru kişiler arasındaysa göster
                                    boolean durum1 = mevcutKullaniciEmail.equals(m.gonderen) && aliciMail.equals(m.alici);
                                    boolean durum2 = aliciMail.equals(m.gonderen) && mevcutKullaniciEmail.equals(m.alici);

                                    if (durum1 || durum2) {
                                        mesajListesi.add(m);
                                    }
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (mesajListesi.size() > 0) {
                            recyclerView.scrollToPosition(mesajListesi.size() - 1);
                        }
                    }
                });
    }
}