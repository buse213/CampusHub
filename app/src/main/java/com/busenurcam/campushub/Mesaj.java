package com.busenurcam.campushub;

public class Mesaj {
    public String gonderen, alici, mesaj;
    public long tarih;

    // Mesajın kimler tarafından görülebilir olduğunu takip eden alanlar
    public boolean gonderenSildi = false;
    public boolean aliciSildi = false;

    // Firebase'in verileri otomatik olarak nesneye dönüştürebilmesi (deserialization) için boş constructor şarttır.
    public Mesaj() {}

    public Mesaj(String gonderen, String alici, String mesaj, long tarih) {
        this.gonderen = gonderen;
        this.alici = alici;
        this.mesaj = mesaj;
        this.tarih = tarih;

        // Yeni bir mesaj oluşturulduğunda varsayılan olarak kimse silmemiş kabul edilir.
        this.gonderenSildi = false;
        this.aliciSildi = false;
    }
}