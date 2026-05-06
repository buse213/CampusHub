package com.busenurcam.campushub;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IlanEkleActivity extends AppCompatActivity {

    private EditText editBaslik, editFiyat, editAciklama;
    private Spinner spinnerKategori;
    private TextView txtKonumDurum;
    private ImageView imgOnizleme;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button btnKaydet;

    private double latitude = 0.0, longitude = 0.0;
    private ArrayList<Uri> secilenGorselListesi = new ArrayList<>();
    private ArrayList<String> yuklenenUrlListesi = new ArrayList<>();

    private boolean isUpdate = false;
    private String ilanId = "";
    private List<String> mevcutResimUrls = new ArrayList<>();

    private static final int HARITA_SEC_CODE = 150;
    private static final int GALERI_CODE = 102;
    private final String IMGBB_API_KEY = "cca6aa8fb367a7f8680d0e3879c89193";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ilan_ekle);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editBaslik = findViewById(R.id.editBaslik);
        editFiyat = findViewById(R.id.editFiyat);
        editAciklama = findViewById(R.id.editAciklama);
        spinnerKategori = findViewById(R.id.spinnerKategori);
        txtKonumDurum = findViewById(R.id.txtKonumDurum);
        imgOnizleme = findViewById(R.id.imgIlanFoto);
        btnKaydet = findViewById(R.id.btnIlanKaydet);

        ImageButton btnGeri = findViewById(R.id.btnGeri);
        Button btnKonumAl = findViewById(R.id.btnKonumAl);
        Button btnFotoSec = findViewById(R.id.btnFotoSec);

        if (btnGeri != null) btnGeri.setOnClickListener(v -> finish());

        String[] kategoriler = {"🏠 Ev Arkadaşı", "📦 İkinci El Eşya", "🔑 Ev Devretme"};
        spinnerKategori.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kategoriler));

        isUpdate = getIntent().getBooleanExtra("isUpdate", false);
        if (isUpdate) {
            ilanId = getIntent().getStringExtra("ilanId");
            editBaslik.setText(getIntent().getStringExtra("eskiBaslik"));
            editFiyat.setText(getIntent().getStringExtra("eskiFiyat"));
            editAciklama.setText(getIntent().getStringExtra("eskiAciklama"));
            latitude = getIntent().getDoubleExtra("lat", 0.0);
            longitude = getIntent().getDoubleExtra("lng", 0.0);
            mevcutResimUrls = getIntent().getStringArrayListExtra("ilanResimler");

            if (latitude != 0.0) {
                txtKonumDurum.setText("Eski Konum Kayıtlı ✔️");
                txtKonumDurum.setTextColor(Color.BLUE);
            }
            if (mevcutResimUrls != null && !mevcutResimUrls.isEmpty()) {
                Glide.with(this).load(mevcutResimUrls.get(0)).into(imgOnizleme);
            }
            btnKaydet.setText("Değişiklikleri Kaydet");
        }

        btnFotoSec.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Fotoğrafları Seç"), GALERI_CODE);
        });

        btnKonumAl.setOnClickListener(v -> startActivityForResult(new Intent(this, HaritaSecActivity.class), HARITA_SEC_CODE));

        btnKaydet.setOnClickListener(v -> resimKontrolEtVeBaslat());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == HARITA_SEC_CODE) {
                latitude = data.getDoubleExtra("lat", 0.0);
                longitude = data.getDoubleExtra("lng", 0.0);
                txtKonumDurum.setText("Konum Seçildi ✔️");
                txtKonumDurum.setTextColor(Color.GREEN);
            } else if (requestCode == GALERI_CODE) {
                secilenGorselListesi.clear();
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        secilenGorselListesi.add(data.getClipData().getItemAt(i).getUri());
                    }
                } else if (data.getData() != null) {
                    secilenGorselListesi.add(data.getData());
                }
                if (!secilenGorselListesi.isEmpty()) {
                    imgOnizleme.setImageURI(secilenGorselListesi.get(0));
                    Toast.makeText(this, secilenGorselListesi.size() + " görsel seçildi", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void resimKontrolEtVeBaslat() {
        if (editBaslik.getText().toString().isEmpty() || editFiyat.getText().toString().isEmpty()) {
            Toast.makeText(this, "Lütfen gerekli alanları doldurun!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isUpdate && secilenGorselListesi.isEmpty()) {
            Toast.makeText(this, "Lütfen en az bir fotoğraf seçin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- KRİTİK: ÇİFT TIKLAMAYI ENGELLE ---
        btnKaydet.setEnabled(false);
        btnKaydet.setText("Yükleniyor...");

        if (isUpdate && secilenGorselListesi.isEmpty()) {
            ilanKaydet(mevcutResimUrls);
        } else {
            yuklenenUrlListesi.clear();
            imgBBSiraliYukle(0);
        }
    }

    private void imgBBSiraliYukle(int index) {
        if (index >= secilenGorselListesi.size()) {
            runOnUiThread(() -> ilanKaydet(yuklenenUrlListesi));
            return;
        }

        try {
            Uri currentUri = secilenGorselListesi.get(index);
            InputStream is = getContentResolver().openInputStream(currentUri);
            byte[] bytes = getBytes(is);
            String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder().add("image", base64Image).build();
            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY)
                    .post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        // --- HATA DURUMUNDA BUTONU GERİ AÇ ---
                        btnKaydet.setEnabled(true);
                        btnKaydet.setText("Tekrar Dene");
                        Toast.makeText(IlanEkleActivity.this, (index+1) + ". görsel yüklenemedi!", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String url = json.getJSONObject("data").getString("url");
                            yuklenenUrlListesi.add(url);
                            imgBBSiraliYukle(index + 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> { btnKaydet.setEnabled(true); btnKaydet.setText("Hata Oluştu"); });
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            btnKaydet.setEnabled(true);
        }
    }

    private void ilanKaydet(List<String> urls) {
        String baslik = editBaslik.getText().toString();
        String fiyatStr = editFiyat.getText().toString();
        String aciklama = editAciklama.getText().toString();
        String kategori = spinnerKategori.getSelectedItem().toString();
        String saticiMail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "bilinmiyor";

        Map<String, Object> ilan = new HashMap<>();
        ilan.put("baslik", baslik);
        try {
            double fiyat = Double.parseDouble(fiyatStr.replaceAll("[^0-9.]", ""));
            ilan.put("fiyat", fiyat);
        } catch (Exception e) { ilan.put("fiyat", 0.0); }

        ilan.put("aciklama", aciklama);
        ilan.put("ilanTipi", kategori);
        ilan.put("saticiMail", saticiMail);
        ilan.put("lat", latitude);
        ilan.put("lng", longitude);
        ilan.put("resimUrls", urls);

        if (isUpdate) {
            db.collection("Ilanlar").document(ilanId).update(ilan)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "İlan Güncellendi!", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e -> {
                        // --- HATA OLURSA BUTONU GERİ AÇ ---
                        btnKaydet.setEnabled(true);
                        btnKaydet.setText("Güncellemeyi Dene");
                        Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("Ilanlar").add(ilan).addOnSuccessListener(d -> {
                Toast.makeText(this, "İlan Başarıyla Yayınlandı!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                // --- HATA OLURSA BUTONU GERİ AÇ ---
                btnKaydet.setEnabled(true);
                btnKaydet.setText("Yayınla");
                Toast.makeText(this, "Firestore hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) byteBuffer.write(buffer, 0, len);
        return byteBuffer.toByteArray();
    }
}