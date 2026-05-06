package com.busenurcam.campushub; // Bu satır sendeki paket ismiyle aynı kalsın

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class HaritaSecActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng secilenNokta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harita_sec);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.btnKonumOnayla).setOnClickListener(v -> {
            if (secilenNokta != null) {
                Intent intent = new Intent();
                intent.putExtra("lat", secilenNokta.latitude);
                intent.putExtra("lng", secilenNokta.longitude);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Varsayılan olarak Manisa/Üniversite konumu
        LatLng varsayilan = new LatLng(38.61, 27.42);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(varsayilan, 13f));

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
            secilenNokta = latLng;
        });
    }
}