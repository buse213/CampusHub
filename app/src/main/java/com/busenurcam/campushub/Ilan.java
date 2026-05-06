package com.busenurcam.campushub;

import java.util.List;

public class Ilan {
    public String ilanId, baslik, aciklama, ilanTipi, saticiMail;

    // 1. ESKİ VERİLER İÇİN: Firebase'deki 'resimUrl' (String) alanını okur
    public String resimUrl;

    // 2. YENİ VERİLER İÇİN: Firebase'deki 'resimUrls' (Array) alanını okur
    public List<String> resimUrls;

    public double fiyat, lat, lng;

    public Ilan() {}

    public Ilan(String ilanId, String baslik, String aciklama, String ilanTipi, double fiyat, String saticiMail, double lat, double lng, List<String> resimUrls, String resimUrl) {
        this.ilanId = ilanId;
        this.baslik = baslik;
        this.aciklama = aciklama;
        this.ilanTipi = ilanTipi;
        this.fiyat = fiyat;
        this.saticiMail = saticiMail;
        this.lat = lat;
        this.lng = lng;
        this.resimUrls = resimUrls;
        this.resimUrl = resimUrl;
    }

    // --- KRİTİK YARDIMCI METOT ---
    // Bu metot, hangi veri varsa onu döndürerek görsellerin kaybolmasını engeller.
    public String getKapakResmi() {
        // Eğer yeni liste doluysa ilk resmi döndür
        if (resimUrls != null && !resimUrls.isEmpty()) {
            return resimUrls.get(0);
        }
        // Liste boşsa ama eski tekil URL varsa onu döndür
        if (resimUrl != null && !resimUrl.isEmpty()) {
            return resimUrl;
        }
        // İkisi de yoksa null döner
        return null;
    }

    // --- GETTER VE SETTER METODLARI ---
    public String getIlanId() { return ilanId; }
    public void setIlanId(String ilanId) { this.ilanId = ilanId; }
    public String getBaslik() { return baslik; }
    public String getAciklama() { return aciklama; }
    public String getIlanTipi() { return ilanTipi; }
    public String getSaticiMail() { return saticiMail; }
    public List<String> getResimUrls() { return resimUrls; }
    public void setResimUrls(List<String> resimUrls) { this.resimUrls = resimUrls; }
    public double getFiyat() { return fiyat; }
    public double getLatitude() { return lat; }
    public double getLongitude() { return lng; }
}