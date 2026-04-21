HBYS - Hastane Bilgi Yönetim Sistemi

Bu proje, Java Swing kullanılarak geliştirilmiş, tek dosyadan oluşan (HastaneOtomasyonu.java) kapsamlı bir masaüstü Hastane Bilgi Yönetim Sistemi (HBYS) uygulamasıdır. Veri kalıcılığı için SQLite veritabanı kullanılmaktadır.

🚀 Özellikler (Modüller)

Sistem, bir hastanenin temel işleyişini simüle eden çeşitli sekmelerden (modüllerden) oluşur:

Hasta Kabul & Danışma: Yeni hasta kaydı oluşturma, SGK sorgulama ve hasta arama işlemleri.

Poliklinik & Muayene: Poliklinik ve doktor seçimi, şikayet/teşhis girişi, röntgen/lab ve e-reçete istemleri.

Acil & Triyaj: Acil servis renk kodlaması (Kırmızı, Sarı, Yeşil) ve müşahade takibi.

Yatan Hasta & Hemşire: Servis/yatak takibi, ateş/nabız/tansiyon izleme ve ilaç uygulama.

Laboratuvar & Röntgen: LIS ve PACS/RIS entegrasyon arayüzleri, sonuç onaylama.

Ameliyathane: Ameliyat masası planlama ve sarf malzeme düşümü.

Medula & Fatura: SGK Medula provizyon alma, hizmet gönderme, fatura ve cari hesap yönetimi.

Eczane & Stok: Hastane ana deposu ve eczane için stok/maliyet takibi.

Personel & Döner Sermaye: İnsan kaynakları ve doktor performans (döner sermaye) hesaplamaları.

🛠 Sistem Gereksinimleri

Java Development Kit (JDK): Sürüm 8 veya üzeri.

Veritabanı Sürücüsü: sqlite-jdbc.jar (SQLite ile bağlantı kurmak için gereklidir).

💾 Veritabanı Yapısı

Uygulama ilk kez çalıştırıldığında, bulunduğu dizine otomatik olarak hastane.db adında bir SQLite veritabanı dosyası oluşturur. Aşağıdaki tablolar otomatik olarak kurulur:

Hastalar (TC, Ad, Soyad, Telefon, Kayıt Tarihi)

Muayeneler (HastaTC, Poliklinik, Doktor, Şikayet, Teşhis, Tarih)

Cari (HastaTC, İşlem Tipi, Tutar, Tarih)

Stok (Malzeme, Miktar, Birim, Fiyat)

⚙️ Kurulum ve Çalıştırma

Projenin derlenmesi ve çalıştırılması için aşağıdaki adımları izleyin:

Projeyi İndirin: HastaneOtomasyonu.java dosyasını bilgisayarınıza kaydedin.

JDBC Sürücüsünü İndirin: İnternetten en güncel sqlite-jdbc.jar dosyasını indirin ve Java dosyası ile aynı klasöre koyun.

Derleme: Komut satırını (Terminal / CMD) açın ve aşağıdaki komutla projeyi derleyin:

javac HastaneOtomasyonu.java


Çalıştırma: Kütüphaneyi (classpath) belirterek uygulamayı çalıştırın:

Windows için:

java -cp ".;sqlite-jdbc-X.X.X.jar" HastaneOtomasyonu


Mac/Linux için:

java -cp ".:sqlite-jdbc-X.X.X.jar" HastaneOtomasyonu


(Not: X.X.X kısmını indirdiğiniz jar dosyasının sürüm numarası ile değiştirin.)

📝 Notlar

Kullanıcı arayüzü (UI) işletim sisteminizin varsayılan temasına (SystemLookAndFeel) uyum sağlayacak şekilde tasarlanmıştır.

Cari hesap kayıtları ve stok düşüm işlemleri otomatik olarak arka planda gerçekleşmektedir.
