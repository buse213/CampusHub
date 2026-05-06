package com.busenurcam.campushub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // View Tanımlamaları
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView txtRegister = findViewById(R.id.txtRegister);
        TextView txtSifremiUnuttum = findViewById(R.id.txtSifremiUnuttum);

        // --- GİRİŞ YAP BUTONU ---
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            Toast.makeText(LoginActivity.this, "Hoş geldin!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userEmail", email);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(LoginActivity.this, "Giriş Başarısız: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(this, "E-posta ve şifre alanlarını doldurun", Toast.LENGTH_SHORT).show();
            }
        });

        // --- KAYIT OL YAZISI ---
        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // --- ŞİFREMİ UNUTTUM YAZISI ---
        if (txtSifremiUnuttum != null) {
            txtSifremiUnuttum.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SifreSifirlaActivity.class);
                startActivity(intent);
            });
        }
    }
}