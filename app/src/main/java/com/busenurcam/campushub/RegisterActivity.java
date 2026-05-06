package com.busenurcam.campushub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar; // Modern bildirim için
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText etName = findViewById(R.id.etRegName);
        EditText etEmail = findViewById(R.id.etRegEmail);
        EditText etPass = findViewById(R.id.etRegPassword);
        Button btnReg = findViewById(R.id.btnRegister);

        btnReg.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim().toLowerCase(); // Küçük harfe çevirerek kontrolü garantileyelim
            String pass = etPass.getText().toString().trim();

            // 1. Alanların boş olup olmadığını kontrol et
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                showModernMessage("Lütfen tüm alanları doldurun!");
                return;
            }

            // 2. Şifre uzunluğu kontrolü
            if (pass.length() < 6) {
                showModernMessage("Şifre en az 6 karakter olmalıdır!");
                return;
            }

            // 3. KRİTİK: Üniversite Uzantı Kontrolü (@cbu.edu.tr ve @ogr.cbu.edu.tr)
            if (email.endsWith("@ogr.cbu.edu.tr") || email.endsWith("@cbu.edu.tr")) {

                // Kayıt işlemini başlat
                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnSuccessListener(authResult -> {
                            String userId = mAuth.getCurrentUser().getUid();
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("adSoyad", name);
                            userMap.put("email", email);
                            userMap.put("universite", "Manisa Celal Bayar Üniversitesi");
                            // Kullanıcı tipini (öğrenci mi hoca mı) otomatik belirlemek istersen:
                            userMap.put("rol", email.contains("ogr") ? "Öğrenci" : "Akademisyen/Mezun");

                            db.collection("Kullanicilar").document(userId).set(userMap);

                            showModernMessage("Kayıt Başarılı! Hoş geldin.");

                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            showModernMessage("Hata: " + e.getLocalizedMessage());
                        });

            } else {
                // Uzantı uymuyorsa hata ver
                showModernMessage("Sadece @cbu.edu.tr uzantılı mailler kabul edilmektedir!");
            }
        });
    }

    // Modern bildirim (Snackbar) metodu
    private void showModernMessage(String mesaj) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), mesaj, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(Color.parseColor("#8A2BE2")); // CampusHub Moru
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }
}