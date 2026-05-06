package com.busenurcam.campushub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SifreSifirlaActivity extends AppCompatActivity {

    private EditText editEmail;
    private Button btnSifirla;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sifre_sifirla);

        mAuth = FirebaseAuth.getInstance();
        editEmail = findViewById(R.id.editSifirlaEmail);
        btnSifirla = findViewById(R.id.btnSifreSifirla);

        btnSifirla.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Lütfen mail adresinizi girin!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Şifre Sıfırlama Metodu
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Sıfırlama bağlantısı mail kutunuza gönderildi!", Toast.LENGTH_LONG).show();
                            finish(); // Giriş ekranına geri döner
                        } else {
                            Toast.makeText(this, "Hata: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}