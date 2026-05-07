# 🎓 CampusHub

CampusHub, üniversite öğrencilerinin kampüs yaşamını kolaylaştırmak amacıyla geliştirilmiş, güvenli bir ikinci el eşya alım-satım ve paylaşım platformudur. Özellikle **Manisa Celal Bayar Üniversitesi** gibi büyük kampüslerdeki öğrencilerin birbirleriyle hızlı ve güvenli bir şekilde iletişim kurmasını hedefler.

---

## ✨ Temel Özellikler

*   **Gelişmiş İlan Yönetimi:** Kullanıcılar satmak istedikleri eşyaları veya kiralık oda duyurularını kolayca sisteme yükleyebilir.
*   **Çoklu Görsel Desteği:** Bir ilana birden fazla fotoğraf eklenebilir ve bu fotoğraflar ilan detay sayfasında akıcı bir **Slider (ViewPager2)** yapısıyla görüntülenebilir.
*   **Akıllı Kategorizasyon:** İlanlar; "İkinci El Eşya", "Ev Arkadaşı" ve "Ev Devretme" gibi spesifik kategorilere ayrılmıştır.
*   **Konum Tabanlı Arama:** **Google Maps API** entegrasyonu sayesinde kullanıcılar ilanların konumunu harita üzerinde görebilir ve kendi konumlarını belirleyebilir.
*   **Anlık Sohbet Sistemi:** Alıcı ve satıcılar arasında Firebase tabanlı, gerçek zamanlı bir mesajlaşma arayüzü sunulur.
*   **Kişiselleştirilmiş Profil:** Kullanıcılar yayındaki ilanlarını tek bir ekrandan takip edebilir, güncelleyebilir veya silebilirler.

---

## 🛠️ Teknik Altyapı ve Teknolojiler

Bu proje, modern yazılım prensipleri ve modüler bir mimari gözetilerek geliştirilmiştir.

*   **Platform:** Android (Java & XML).
*   **Veritabanı ve Kimlik Doğrulama:** **Firebase Firestore** (NoSQL veritabanı) ve **Firebase Authentication**.
*   **Görsel Depolama:** Görselleri bulutta saklamak ve uygulama performansını artırmak için **ImgBB API** entegrasyonu kullanılmıştır.
*   **Network & API:** HTTP istekleri için **OkHttp**, görsel yükleme ve önbelleğe alma (caching) işlemleri için **Glide** kütüphanesi tercih edilmiştir.
*   **UI Bileşenleri:** Material Design rehberine uygun `CardView`, `RecyclerView`, `TabLayout` ve özel `Drawable` tasarımları kullanılmıştır.

---

## ⚙️ Kurulum ve Çalıştırma

Projeyi yerel ortamınızda çalıştırmak için aşağıdaki adımları izleyebilirsiniz:

1.  **Repoyu Klonlayın:**
    ```bash
    git clone https://github.com/buse213/CampusHub.git
    ```
2.  **Firebase Yapılandırması:** 
    *   Firebase konsolunda bir proje oluşturun ve `google-services.json` dosyasını `app/` klasörüne yerleştirin.
3.  **API Anahtarları:** 
    *   `IlanEkleActivity.java` dosyasındaki `IMGBB_API_KEY` alanına kendi API anahtarınızı tanımlayın.
    *   Google Maps özelliklerini kullanabilmek için `AndroidManifest.xml` dosyasına Google Maps API anahtarınızı ekleyin.
4.  **Derleme:** Android Studio ile projeyi açın ve Gradle senkronizasyonunun tamamlanmasını bekleyin.

---

---

**Geliştirici:** Busenur Çam,Metin Serinkaya
**Üniversite:** Manisa Celal Bayar Üniversitesi - Yazılım Mühendisliği
```