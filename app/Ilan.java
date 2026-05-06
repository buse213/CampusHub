package com.busenurcam.campushub;

public class Ilan {
    private String baslik;
    private String fiyat;
    private String kategori;
    private String resimUrl;

    // Firebase için boş constructor şarttır
    public Ilan() {
    }

    public Ilan(String baslik, String fiyat, String kategori, String resimUrl) {
        this.baslik = baslik;
        this.fiyat = fiyat;
        this.kategori = kategori;
        this.resimUrl = resimUrl;
    }

    // Getter ve Setter metotları
    public String getBaslik() { return baslik; }
    public void setBaslik(String baslik) { this.baslik = baslik; }

    public String getFiyat() { return fiyat; }
    public void setFiyat(String fiyat) { this.fiyat = fiyat; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getResimUrl() { return resimUrl; }
    public void setResimUrl(String resimUrl) { this.resimUrl = resimUrl; }
}