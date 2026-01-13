🌍 Info BMKG Hybrid - Android App

Aplikasi Android sederhana namun canggih yang memadukan performa Native Java dengan fleksibilitas WebView (Leaflet.js) untuk memantau informasi Gempa Bumi dan Cuaca secara real-time di Indonesia.

📱 Screenshots

Tampilan Peta & Jarak

Detail Gempa (Bottom Sheet)

Info Cuaca Terkini







Visualisasi lokasi & garis jarak

Informasi detail gempa

Suhu & Cuaca lokasi user

Catatan: Gambar di atas adalah representasi fitur aplikasi.

✨ Fitur Utama

Data Gempa Real-time: Mengambil data gempa terkini langsung dari API terbuka BMKG.

Peta Interaktif (Hybrid):

Menggunakan Leaflet.js via WebView (Lebih ringan & Gratis dibanding Google Maps SDK).

Menampilkan marker lokasi gempa dengan warna indikator (Merah: M ≥ 5.0, Oranye: M < 5.0).

Marker lokasi pengguna (User Location).

Distance Tracker: Menghitung dan menggambar garis jarak (KM) dari lokasi pengguna ke pusat gempa secara otomatis.

Detail Gempa: Menggunakan Native Bottom Sheet untuk pengalaman pengguna yang mulus (Potensi Tsunami, Kedalaman, Waktu).

Info Cuaca Lokal: Mengambil data cuaca (Suhu, Angin, Kondisi) berdasarkan lokasi GPS pengguna menggunakan Open-Meteo API.

🛠️ Teknologi yang Digunakan

Bahasa Pemrograman: Java (Native Android).

Minimum SDK: 24 (Android 7.0).

Target SDK: 33 (Android 13).

Networking: Retrofit 2 & GSON Converter.

Maps: WebView + Leaflet.js + OpenStreetMap Tile.

UI Components: Material Design 3, RecyclerView, CardView, BottomSheetDialog.

🔌 API Sources

Aplikasi ini menggunakan data dari sumber publik berikut:

Gempa Bumi: Data Terbuka BMKG

Cuaca: Open-Meteo API

Peta: OpenStreetMap

🚀 Cara Menjalankan Project

Clone Repositori

git clone [https://github.com/hafizfauzinugraha20/infobmkg.git](https://github.com/hafizfauzinugraha20/infobmkg.git)


Buka di Android Studio

Buka Android Studio -> File -> Open -> Pilih folder infobmkg.

Sync Gradle

Tunggu hingga proses indexing dan download library selesai.

Jalankan (Run)

Pastikan Emulator atau HP fisik terhubung internet.

Penting: Izinkan akses Lokasi (GPS) saat aplikasi pertama kali dibuka agar fitur jarak dan cuaca berfungsi.

📝 Struktur Project Hybrid

Aplikasi ini menggunakan pendekatan unik untuk efisiensi:

MainActivity.java: Mengontrol logika utama, GPS, dan bridge komunikasi ke WebView.

assets/map.html: File HTML/JS lokal yang berisi logika peta Leaflet.

adapter/GempaAdapter.java: Mengurus tampilan list native Android.

👤 Author

Hafiz Fauzi Nugraha

GitHub: @hafizfauzinugraha20

Disclaimer: Aplikasi ini dibuat untuk tujuan edukasi dan pembelajaran pengembangan Android Hybrid. Data yang ditampilkan bergantung sepenuhnya pada ketersediaan API pihak ketiga.
