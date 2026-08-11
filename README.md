# FinSight

**FinSight**, fon analizi ve portföy karar desteği için geliştirilmiş bir web uygulamasıdır.
Infina'dan fon ve piyasa verisini çeker; fon panosu, stres testleri, AI tabanlı dağılım önerileri,
manuel senaryolar, karar geçmişi ve kural tabanlı bir fon asistanı sunar. Karar her zaman
kullanıcıda kalır — uygulama öneri üretir, uygulamaz.

> İnfina Akademi 2026 dönem projesi. Depo: `gitea.infina.com.tr/akademi-26/FirstClass`

---

## İçindekiler

1. [Depo Yapısı](#1-depo-yapısı)
2. [Mimari](#2-mimari)
3. [Ön Koşullar](#3-ön-koşullar)
4. [Hızlı Başlangıç](#4-hızlı-başlangıç)
5. [Adım Adım Kurulum](#5-adım-adım-kurulum)
6. [Giriş ve Kullanıcı Oluşturma](#6-giriş-ve-kullanıcı-oluşturma)
7. [Servis Adresleri](#7-servis-adresleri)
8. [Ortam Değişkenleri](#8-ortam-değişkenleri)
9. [Dış Bağımlılıklar — Neler Çalışır, Neler Çalışmaz](#9-dış-bağımlılıklar--neler-çalışır-neler-çalışmaz)
10. [Build & Test](#10-build--test)
11. [Geliştirme Akışı](#11-geliştirme-akışı)
12. [Veritabanı Migration Kuralları](#12-veritabanı-migration-kuralları)
13. [Sorun Giderme](#13-sorun-giderme)
14. [Bilinen Eksikler](#14-bilinen-eksikler)
15. [Ekip](#15-ekip)

---

## 1. Depo Yapısı

Bu depo iki bağımsız uygulamayı barındıran bir **monorepo**'dur:

```
FirstClass/
├── finsight-backend/     Spring Boot 4.1 REST API (Java 21)
│   ├── compose.yaml      Altyapı servisleri (Redis, Kafka, Prometheus, Grafana, opsiyonel MSSQL)
│   ├── .env.example      Ortam değişkeni şablonu → kopyalanıp .env yapılır
│   ├── pom.xml           Maven yapılandırması
│   ├── mvnw / mvnw.cmd   Maven wrapper (ayrıca Maven kurmanıza gerek yok)
│   └── src/
└── finsight-frontend/    React 19 + Vite SPA (TypeScript)
    ├── .env.example      VITE_API_BASE_URL şablonu
    ├── package.json
    └── src/
```

Her iki alt projenin kendi ayrıntılı README'si vardır — modül modül ne yapıldığı, kimin geliştirdiği
ve iç mimari orada anlatılır:

- [`finsight-backend/README.md`](finsight-backend/README.md)
- [`finsight-frontend/README.md`](finsight-frontend/README.md)

Bu dosya ise **projeyi sıfırdan ayağa kaldırma** rehberidir.

---

## 2. Mimari

```
┌──────────────────┐        REST /api/v1        ┌──────────────────────┐
│  finsight-       │ ─────────────────────────► │  finsight-backend    │
│  frontend        │ ◄───────────────────────── │  (Spring Boot :8080) │
│  (Vite :5173)    │        JSON + JWT          └──────────┬───────────┘
└──────────────────┘                                       │
                                                            │
        ┌──────────────┬──────────────┬─────────────┬───────┴──────┬──────────────┐
        ▼              ▼              ▼             ▼              ▼              ▼
  ┌──────────┐   ┌──────────┐   ┌──────────┐  ┌──────────┐  ┌───────────┐  ┌──────────┐
  │ SQL      │   │  Redis   │   │  Kafka   │  │   SMTP   │  │  Infina   │  │  Diğer   │
  │ Server   │   │  (cache, │   │ (bildirim│  │(Mailpit, │  │   API     │  │ dış API  │
  │ (Flyway) │   │  oturum) │   │  kuyruğu)│  │   dev)   │  │  (VPN!)   │  │ News,    │
  └──────────┘   └──────────┘   └──────────┘  └──────────┘  └───────────┘  │ DeepL    │
                                                                            └──────────┘
```

**Akış özeti:** Frontend yalnızca backend ile konuşur. Backend, veriyi SQL Server'da tutar; sıcak
okumaları Redis + Caffeine ile önbellekler; bildirimleri Kafka üzerinden asenkron kuyruğa alıp
SMTP ile gönderir; fon/piyasa verisini Infina'dan, haberleri World News API'den, çeviriyi DeepL'den
çeker. Şema Flyway ile uygulama açılışında otomatik oluşturulur.

---

## 3. Ön Koşullar

| Araç | Sürüm | Not |
|------|-------|-----|
| **JDK** | 21 | Zorunlu. `java -version` ile doğrulayın. |
| **Docker** + Docker Compose | güncel | Redis, Kafka ve (isteğe bağlı) SQL Server için. Docker Desktop yeterli. |
| **Node.js** | LTS (20+) | Frontend için. `npm` ile birlikte gelir. |
| **Git** | — | — |
| Maven | — | **Gerekmez** — depodaki `./mvnw` wrapper'ı kullanılır. |

> **Apple Silicon (M1/M2/M3/M4) kullanıcıları:** Mutlaka **arm64** JDK 21 kullanın. Rosetta altındaki
> x86_64 JDK ile `onnxruntime` native kütüphanesi yüklenemiyor ve uygulama açılışta patlıyor.
> Kontrol: `java -XshowSettings:properties -version 2>&1 | grep os.arch` → `aarch64` görmelisiniz.
> Değilse `JAVA_HOME`'u arm64 bir JDK 21'e yönlendirin.

> **Windows kullanıcıları:** `./mvnw` yerine `mvnw.cmd` kullanın. Docker Desktop'ın WSL2 backend'i
> önerilir.

---

## 4. Hızlı Başlangıç

Aceleniz varsa — her komut kendi dizininde çalıştırılır:

```bash
git clone https://gitea.infina.com.tr/akademi-26/FirstClass.git
cd FirstClass
```

**Terminal 1 — Backend:**

```bash
cd finsight-backend
cp .env.example .env
# .env'i açıp en az DB_* / LOCALDB_* (bkz. 5.3) ve ADMIN_* (bkz. 5.6) değerlerini doldurun
docker compose --profile localdb up -d
docker run -d --name finsight-mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
./mvnw spring-boot:run
```

**Terminal 2 — Frontend:**

```bash
cd finsight-frontend
cp .env.example .env
npm install
npm run dev
```

Ardından http://localhost:5173 adresine gidin ve `.env`'de **kendi belirlediğiniz** `ADMIN_EMAIL` /
`ADMIN_PASSWORD` ile giriş yapın.

Ayrıntılar ve neden bu adımlar gerektiği için devam edin.

---

## 5. Adım Adım Kurulum

### 5.1 Depoyu klonlayın

```bash
git clone https://gitea.infina.com.tr/akademi-26/FirstClass.git
cd FirstClass
```

Aktif geliştirme dalı `develop`'tır; `main` sürüm dalıdır. Katkı yapacaksanız:

```bash
git checkout develop
```

### 5.2 Backend ortam dosyasını hazırlayın

```bash
cd finsight-backend
cp .env.example .env
```

`.env` **git'e commit edilmez** (`.gitignore`'da). Tüm yapılandırma buradan okunur —
`application.yaml` içindeki `spring.config.import: optional:file:.env[.properties]` satırı sayesinde
Spring Boot bu dosyayı doğrudan yükler. Aynı dosyayı `docker compose` de kullanır.

Şimdilik dosyayı açık bırakın; sonraki adımlarda dolduracağız.

### 5.3 Veritabanını seçin

Projenin iki veritabanı seçeneği var:

| Seçenek | Kimin için | Gereksinim |
|---------|-----------|------------|
| **Troya** (paylaşımlı sunucu) | İnfina ağındaki ekip üyeleri | **VPN zorunlu** |
| **Lokal MSSQL** (Docker) | Depoyu dışarıdan klonlayan herkes | Sadece Docker |

**Dışarıdan kuruyorsanız lokal MSSQL kullanın.** `compose.yaml` içindeki `mssql` servisi
`localdb` profiline bağlıdır, yani yalnızca açıkça istendiğinde ayağa kalkar.

`.env` dosyanızda şu iki blok tutarlı olmalı — `DB_*` uygulamanın bağlanacağı adres,
`LOCALDB_*` ise Docker'ın oluşturacağı veritabanı:

```dotenv
# Uygulamanın bağlanacağı veritabanı
DB_HOST=localhost
DB_PORT=1433
DB_NAME=finsight
DB_USERNAME=sa
DB_PASSWORD=<kendi belirlediğiniz güçlü parola>
DB_ENCRYPT=true
DB_TRUST_SERVER_CERTIFICATE=true

# Docker'ın ayağa kaldıracağı lokal MSSQL (compose 'localdb' profili)
LOCALDB_PORT=1433
LOCALDB_NAME=finsight
LOCALDB_PASSWORD=<DB_PASSWORD ile aynı değer>
MSSQL_PID=Developer
```

> ⚠️ **`LOCALDB_PORT`, `LOCALDB_NAME` ve `LOCALDB_PASSWORD` şu anda `.env.example`'da yok** —
> `compose.yaml` bunları beklediği için yukarıdaki üç satırı `.env`'inize elle eklemeniz gerekir.
> Eklemezseniz MSSQL konteyneri boş SA parolasıyla başlatılmaya çalışılır ve ayağa kalkmaz.
> (Bkz. [Bölüm 14](#14-bilinen-eksikler))

> **SA parola kuralı:** SQL Server en az 8 karakter, büyük/küçük harf + rakam + özel karakter ister.
> `LOCALDB_PASSWORD` ile `DB_PASSWORD` **aynı** olmalıdır. Parolayı kendiniz belirleyin — bu
> dokümandaki veya `.env.example`'daki örnek değerleri olduğu gibi kullanmayın.

**İki ortam arasında geçiş.** İnfina ağındaysanız Troya ile lokal veritabanı arasında gidip gelmeniz
gerekebilir. Pratik yöntem, `.env` içinde iki `DB_*` bloğunu birlikte tutup birini yorum satırı
yapmaktır:

```dotenv
# --- Troya (VPN gerekir) ---
DB_HOST=<troya-sunucu-adresi>
DB_PORT=<troya-port>
DB_NAME=<troya-veritabani>
DB_USERNAME=<kullanici>
DB_PASSWORD=<parola>

# --- Lokal MSSQL (yukarıdakileri yorum yapıp bunları açın) ---
# DB_HOST=localhost
# DB_PORT=1433
# DB_NAME=finsight
# DB_USERNAME=sa
# DB_PASSWORD=<lokal parolanız>
```

`LOCALDB_*` satırları her iki durumda da kalabilir — yalnızca `--profile localdb` ile compose
çalıştırdığınızda kullanılırlar.

> Troya bağlantı bilgileri **bu depoda tutulmaz**; ekip sorumlusundan alın ve yalnızca kendi
> `.env`'inizde saklayın.

### 5.4 Altyapı servislerini başlatın

```bash
# Lokal veritabanı ile (dışarıdan kuranlar):
docker compose --profile localdb up -d

# Troya kullananlar (VPN açık, DB_* Troya'yı gösteriyor):
docker compose up -d
```

Bu komut şunları ayağa kaldırır:

| Servis | Konteyner | Amaç |
|--------|-----------|------|
| Redis | `finsight-redis` | Cache, oturum, rate-limit sayaçları, idempotency |
| Kafka | `finsight-kafka` | Bildirim kuyruğu (KRaft modu, ZooKeeper yok) |
| Kafka UI | `finsight-kafka-ui` | Kuyruk gözlem arayüzü |
| Prometheus | `finsight-prometheus` | Metrik toplama |
| Grafana | `finsight-grafana` | Metrik panoları |
| MSSQL | `finsight-mssql` | **yalnızca `--profile localdb` ile** |
| MSSQL init | `finsight-mssql-init` | `finsight` veritabanını oluşturur, sonra kapanır |

Durum kontrolü:

```bash
docker compose ps
```

Kafka ve MSSQL'in healthcheck'leri var; `healthy` olmalarını bekleyin (MSSQL ilk açılışta ~30 sn).

> **Not:** `dev` profilinde `spring.docker.compose.enabled=true` olduğu için Spring Boot uygulamayı
> çalıştırırken compose servislerini kendi de yönetmeye çalışır. Ancak **`localdb` profilindeki
> MSSQL'i başlatmaz** — veritabanını yukarıdaki komutla kendiniz ayağa kaldırmalısınız.

### 5.5 E-posta gönderimini ayarlayın

Uygulama; e-posta doğrulama, **OTP**, şifre sıfırlama ve hesap kilitleme bildirimlerini SMTP ile
gönderir. Şifre değiştirdikten sonra her giriş OTP istediği için, **bu adım opsiyonel değildir** —
SMTP çalışmazsa sisteme giremezsiniz.

İki seçenek var:

#### Seçenek A — Mailpit (lokal geliştirme için önerilir)

Sahte bir SMTP sunucusu; dışarıya gerçek posta çıkmaz, tüm e-postaları bir web arayüzünde
okursunuz. `.env.example` varsayılan olarak bunu gösterir — **fakat Mailpit şu anda `compose.yaml`'de
tanımlı değil**, elle başlatmanız gerekir:

```bash
docker run -d --name finsight-mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
```

Gönderilen her şeyi http://localhost:8025 adresinden okuyun — **OTP kodlarını buradan alacaksınız.**

İlgili `.env` ayarları (`.env.example`'daki varsayılanlar zaten böyledir, değiştirmenize gerek yok):

```dotenv
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=no-reply@finsight.local
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS_ENABLE=false
```

#### Seçenek B — Gerçek SMTP (ör. Gmail)

E-postaların gerçekten gerçek adreslere ulaşmasını istiyorsanız (ekip içinde şu anda bu kullanılıyor):

```dotenv
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<kendi e-posta adresiniz>
MAIL_PASSWORD=<kendi uygulama şifreniz>
MAIL_FROM=<kendi e-posta adresiniz>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

Gmail için normal hesap parolanız çalışmaz; **uygulama şifresi (app password)** üretmeniz gerekir:
Google Hesabı → Güvenlik → 2 Adımlı Doğrulama'yı açın → Uygulama şifreleri → 16 haneli şifreyi
boşluksuz olarak `MAIL_PASSWORD`'e yazın.

> 🔐 **Herkes kendi hesabını ve kendi uygulama şifresini kullanır.** Uygulama şifresi kişisel bir
> kimlik bilgisidir; `.env` dışında hiçbir yere yazılmaz, ekip arkadaşlarıyla paylaşılmaz, ekran
> görüntüsü/mesaj/dokümana kopyalanmaz. Yanlışlıkla paylaştıysanız Google Hesabı → Güvenlik →
> Uygulama şifreleri üzerinden **hemen iptal edip yenisini üretin**.

> Mailpit'e geri dönmek için Seçenek A'daki değerleri geri yazmanız yeterli — kod tarafında
> değişiklik gerekmez.

### 5.6 Kalan zorunlu değişkenleri doldurun

`.env` içinde en az şunları gözden geçirin:

```dotenv
# 256-bit'lik rastgele bir değerle değiştirin
JWT_SECRET=<kendi ürettiğiniz secret>

# dev profili: SQL logları açık, Swagger açık, compose entegrasyonu açık
SPRING_PROFILES_ACTIVE=dev

# Frontend'in origin'i buraya dahil olmalı
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

`JWT_SECRET` üretmek için:

```bash
openssl rand -base64 32
```

**Admin hesabı (`ADMIN_*`)**

Uygulamada kayıt ekranı yoktur; ilk kullanıcı, açılışta Flyway'in `V28__Seed_admin_user`
migration'ı ile `.env`'deki `ADMIN_*` değerlerinden seed edilir. Diğer tüm kullanıcıları bu admin
Yönetim Paneli üzerinden oluşturur.

`.env.example` içindeki `ADMIN_EMAIL`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `ADMIN_FIRST_NAME`,
`ADMIN_LAST_NAME`, `ADMIN_PHONE_NUMBER` alanlarını **kendi belirlediğiniz değerlerle doldurun**.
Bu değerler size özeldir; paylaşılan ortamlardaki admin bilgileri bu depoda tutulmaz ve
dokümante edilmez.

> ⚠️ `.env.example` içindeki örnek `ADMIN_*` değerlerini **olduğu gibi bırakmayın** — özellikle
> parolayı. Örnek değerler herkese açık olduğu için, değiştirilmediğinde bilinen kimlik bilgisine
> sahip bir ADMIN hesabı oluşmuş olur.

> Seed yalnızca **bir kez** çalışır: aynı e-posta veya kullanıcı adı zaten varsa migration atlanır.
> Dolayısıyla `ADMIN_*` değerlerini sonradan değiştirmek mevcut admini güncellemez — parolayı
> uygulama içinden değiştirin.

Dış API anahtarları (`INFINA_API_KEY`, `NEWS_API_TOKEN`, `DEEPL_API_KEY`) **boş bırakılabilir** —
uygulama yine ayağa kalkar, yalnızca ilgili özellikler veri döndürmez.
Ayrıntı için [Bölüm 9](#9-dış-bağımlılıklar--neler-çalışır-neler-çalışmaz).

### 5.7 Backend'i çalıştırın

```bash
./mvnw spring-boot:run
```

İlk çalıştırmada:

1. Flyway `db/migration` altındaki **V1–V27 SQL migration'larını** ve Java tabanlı
   **V28 admin seed**'ini uygular — şemayı elle oluşturmanıza gerek yok.
2. `.env`'deki `ADMIN_*` değerlerinden bir ADMIN kullanıcısı oluşturur (parola BCrypt'lenir).
   Aynı e-posta veya kullanıcı adı zaten varsa seed atlanır.
3. Kafka'da bildirim topic'i oluşturulur.
4. Açılış senkronizasyonları tetiklenir (fon verisi, makro veri, hisse fiyatları, haberler) —
   bunlar dış API'lere gider; anahtar/VPN yoksa loglara hata düşer ama **uygulama ayakta kalır**.

Sağlık kontrolü:

```bash
curl http://localhost:8080/actuator/health
```

### 5.8 Frontend'i çalıştırın

Yeni bir terminalde:

```bash
cd finsight-frontend
cp .env.example .env
npm install
npm run dev
```

`.env` tek bir değişken içerir:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

> Bu değişken tanımlı değilse uygulama açılışta `VITE_API_BASE_URL is not defined` hatası fırlatır.

Uygulama http://localhost:5173 adresinde açılır.

---

## 6. Giriş ve Kullanıcı Oluşturma

Kayıt ekranı **yoktur** — kullanıcıları yalnızca ADMIN oluşturur. Kendi kurulumunuzda, `.env`'deki
`ADMIN_*` değerlerinden seed edilen admin hesabıyla başlarsınız ([bkz. 5.6](#56-kalan-zorunlu-değişkenleri-doldurun)).

1. **Giriş:** http://localhost:5173/login → `.env`'de kendi belirlediğiniz `ADMIN_EMAIL`
   (veya `ADMIN_USERNAME`) ve `ADMIN_PASSWORD`.
2. **Şifre değiştirme (zorunlu):** Seed edilen hesap `first_login = 1` ile gelir. İlk girişte OTP
   sorulmaz, ama şifre değiştirilene kadar diğer uçlar `403 PASSWORD_CHANGE_REQUIRED` döner.
   Uygulama sizi `/change-password` ekranına yönlendirir.
3. **Sonraki girişler — OTP:** Şifre değiştirdikten sonra her girişte e-posta ile 2FA kodu
   gönderilir. Mailpit kullanıyorsanız kodu http://localhost:8025 arayüzünden, gerçek SMTP
   kullanıyorsanız `MAIL_USERNAME`'deki gerçek posta kutusundan okuyun.
4. **Yeni kullanıcı açma:** Yönetim Paneli → Kullanıcılar. Oluşturulan kullanıcıya doğrulama
   e-postası ve geçici şifre gider; o da aynı posta kutusuna düşer.

> SMTP çalışmıyorsa 2. adımdan sonra sisteme **giremezsiniz** — OTP e-postası hiçbir yere ulaşmaz.
> Bu duruma düşerseniz [Sorun Giderme](#13-sorun-giderme) bölümüne bakın.

> **Paylaşımlı ortamlar (Troya):** Oradaki admin hesabının kimlik bilgileri bu depoda **tutulmaz**
> ve dokümante edilmez. İhtiyacınız varsa ekip sorumlusundan talep edin; kendi lokal kurulumunuzda
> ise yukarıdaki gibi kendi admin hesabınızı seed edersiniz.

---

## 7. Servis Adresleri

Varsayılan `dev` yapılandırmasıyla:

| Servis | Adres | Kimlik bilgisi |
|--------|-------|----------------|
| Frontend (Vite) | http://localhost:5173 | — |
| Backend API | http://localhost:8080/api/v1 | JWT |
| Swagger UI | http://localhost:8080/swagger-ui.html | — |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | — |
| Actuator health | http://localhost:8080/actuator/health | — |
| Prometheus metrikleri | http://localhost:8080/actuator/prometheus | — |
| Mailpit (e-posta) | http://localhost:8025 | — (yalnızca Seçenek A kullanılıyorsa) |
| Kafka UI | http://localhost:8090 | — |
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3001 | `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD` |
| SQL Server | `localhost:1433` | `sa` / `LOCALDB_PASSWORD` |
| Redis | `localhost:6379` | — |
| Kafka | `localhost:9092` | — |

Portlar `.env` üzerinden değiştirilebilir (`KAFKA_UI_PORT`, `PROMETHEUS_PORT`, `GRAFANA_PORT`,
`REDIS_PORT`, `KAFKA_PORT`, `LOCALDB_PORT`).

---

## 8. Ortam Değişkenleri

`.env.example` tüm anahtarları örnek değerleriyle içerir. Aşağıda gruplar ve kritik olanlar:

| Grup | Anahtarlar | Zorunlu mu? |
|------|-----------|-------------|
| **Veritabanı** | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_ENCRYPT`, `DB_TRUST_SERVER_CERTIFICATE` | ✅ Zorunlu |
| **Lokal DB (Docker)** | `LOCALDB_PORT`, `LOCALDB_NAME`, `LOCALDB_PASSWORD`, `MSSQL_PID` | ✅ `--profile localdb` kullanıyorsanız |
| **Redis** | `REDIS_HOST`, `REDIS_PORT` | ✅ Zorunlu |
| **Kafka** | `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_PORT`, `NOTIFICATION_*`, `KAFKA_*_TIMEOUT_MS`, `KAFKA_UI_PORT` | ✅ Zorunlu (varsayılanlar yeterli) |
| **Mail** | `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_*` | ✅ Zorunlu (OTP için) — Mailpit ya da gerçek SMTP, [bkz. 5.5](#55-e-posta-gönderimini-ayarlayın) |
| **JWT** | `JWT_SECRET`, `JWT_ACCESS_TOKEN_EXPIRY`, `JWT_REFRESH_TOKEN_EXPIRY` | ✅ Zorunlu — secret'ı değiştirin |
| **Admin seed** | `ADMIN_EMAIL`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `ADMIN_FIRST_NAME`, `ADMIN_LAST_NAME`, `ADMIN_PHONE_NUMBER` | ✅ Zorunlu — ilk giriş bununla |
| **CORS / Profil** | `CORS_ALLOWED_ORIGINS`, `SPRING_PROFILES_ACTIVE` | ✅ Zorunlu |
| **Infina** | `INFINA_API_URL`, `INFINA_API_KEY` | ⚠️ Fon verisi için (VPN gerekir) |
| **Haberler** | `NEWS_API_URL`, `NEWS_API_TOKEN` | ➖ Opsiyonel |
| **Çeviri** | `DEEPL_API_URL`, `DEEPL_API_KEY` | ➖ Opsiyonel |
| **OTP** | `OTP_EXPIRE_DURATION`, `OTP_COOLDOWN_DURATION`, `OTP_MAX_ATTEMPTS`, `OTP_ABUSE_*` | ✅ Zorunlu (varsayılanlar yeterli) |
| **Doğrulama / Şifre sıfırlama** | `VERIFICATION_BASE_URL`, `VERIFICATION_EXPIRE_DAYS`, `PASSWORD_RESET_BASE_URL`, `PASSWORD_RESET_EXPIRE_DURATION` | ✅ Zorunlu |
| **Rate limit** | `LOGIN_RATE_LIMIT_*`, `PASSWORD_RESET_RATE_LIMIT_*`, `PASSWORD_RESET_COOLDOWN_DURATION` | ✅ Zorunlu (varsayılanlar yeterli) |
| **Şifre geçmişi** | `PASSWORD_HISTORY_SIZE` | ✅ Zorunlu |
| **Cache** | `PERF_CACHE_MAX_SIZE`, `PERF_CACHE_EXPIRE_HOURS`, `DECISION_HISTORY_CACHE_*` | ✅ Zorunlu (varsayılanlar yeterli) |
| **Hisse fiyatı** | `STOCK_PRICE_REFRESH_CRON`, `STOCK_PRICE_REFRESH_ZONE`, `STOCK_PRICE_HISTORY_WINDOW_DAYS` | ➖ Varsayılanlı |
| **ONNX** | `INTRA_OP_NUM_THREADS`, `INTER_OP_NUM_THREADS` | ✅ Zorunlu |
| **İzleme** | `PROMETHEUS_PORT`, `GRAFANA_PORT`, `GRAFANA_ADMIN_USER`, `GRAFANA_ADMIN_PASSWORD` | ✅ Zorunlu (compose için) |
| **Loglama** | `LOG_PATH` | ➖ Varsayılan `./logs` |

**Frontend** (`finsight-frontend/.env`):

| Anahtar | Varsayılan | Açıklama |
|---------|-----------|----------|
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1` | Backend taban adresi. Tanımsızsa uygulama açılmaz. |

### Gizli bilgi hijyeni

`.env` dosyaları `.gitignore`'dadır ve depoya hiç girmemiştir — öyle kalmalı. Kurallar:

- **`.env` asla commit edilmez.** Yeni bir değişken eklerken `.env.example`'a **boş ya da
  placeholder** değerle ekleyin; gerçek değeri yalnızca kendi `.env`'inizde tutun.
- **`.env` içeriği kopyalanıp yapıştırılmaz** — issue, PR açıklaması, sohbet, ekran görüntüsü,
  doküman fark etmez. Bir ekip arkadaşının kuruluma ihtiyacı varsa `.env.example`'ı ve bu README'yi
  paylaşın, değerleri değil.
- **Herkes kendi kimlik bilgisini kullanır:** kendi SMTP uygulama şifresi, kendi `JWT_SECRET`,
  kendi admin parolası. Paylaşımlı olanlar (Troya, Infina) ekip sorumlusundan alınır.
- **Sızdıysa döndürün.** Bir anahtar/parola yanlışlıkla paylaşıldıysa "kimse görmemiştir" diye
  geçiştirmeyin: SMTP uygulama şifresini Google Hesabı'ndan iptal edin, Infina/News/DeepL
  anahtarlarını sağlayıcıdan yenileyin, `JWT_SECRET`'ı değiştirin (mevcut tüm token'lar geçersiz
  olur, kullanıcılar yeniden giriş yapar), veritabanı parolasını ekiple birlikte güncelleyin.
- Yanlışlıkla commit ettiyseniz **dosyayı silmek yetmez** — git geçmişinde kalır. Anahtarı
  döndürün, sonra geçmiş temizliğini ekiple konuşun.

---

## 9. Dış Bağımlılıklar — Neler Çalışır, Neler Çalışmaz

Depoyu İnfina ağı dışından klonladıysanız bazı özellikler veri göremez. Uygulama yine de ayağa
kalkar ve çöker değil — eksik entegrasyonlar loglara hata basar, geri kalan her şey çalışır.

| Özellik | Bağımlılık | VPN/anahtar yoksa |
|---------|-----------|-------------------|
| Giriş, OTP, şifre sıfırlama, kullanıcı yönetimi | Yok (yerel) | ✅ Tam çalışır |
| Yönetim Paneli, denetim (audit) logları | Yok (yerel) | ✅ Tam çalışır |
| Bildirim / e-posta hattı (Kafka + SMTP) | Mailpit ya da gerçek SMTP | ✅ Tam çalışır |
| İzleme (Prometheus / Grafana) | Yok (yerel) | ✅ Tam çalışır |
| Fon panosu, benchmark, portföy dağılımı | **Infina API + VPN** | ❌ Fon tablosu boş kalır |
| Performans karşılaştırma, karar geçmişi | Fon verisi | ❌ Veri olmadan boş görünür |
| AI dağılım önerisi, stres testi (ONNX) | **ONNX model dosyaları** | ❌ Model dosyaları depoda 0 byte — çalışmaz |
| Stres testi (FastAPI motoru) | **Ayrı FastAPI servisi** | ❌ Servis bu depoda yok |
| Haberler | World News API anahtarı | ❌ Liste boş |
| Haber çevirisi | DeepL API anahtarı | ⚠️ Başlıklar İngilizce kalır (haberler yine listelenir) |

Ayrıntılar:

- **Infina API** — `INFINA_API_URL` preprod ortamını gösterir ve **İnfina VPN'i üzerinden**
  erişilebilir. Anahtar için ekip sorumlusuna başvurun. Fon verisi olmadan pano, performans
  karşılaştırma ve karar geçmişi ekranları boş görünür.
- **ONNX modelleri** — `src/main/resources/model/` altındaki `faiz_stress_model.onnx` ve
  `hisse_stress_model.onnx` dosyaları depoda **0 byte** (yer tutucu). Gerçek model dosyalarını
  ekipten alıp bu yola koymadan AI/stres testi çıkarımı çalışmaz.
- **FastAPI çıkarım servisi** — Stres testinin `FASTAPI_AI` motoru `FASTAPI_BASE_URL`
  (varsayılan `http://localhost:8000`) adresindeki ayrı bir Python servisine gider. Bu servis bu
  depoda yer almaz; çalışmıyorsa ilgili motor hata döner.
- **DeepL** — anahtar geçersiz ya da çeviri başarısız olursa `TranslationServiceImpl` hatayı
  yakalayıp **orijinal (İngilizce) başlığa düşer** ve uyarı loglar; haber listesi bu yüzden
  boşalmaz. Yani DeepL anahtarı olmadan da haberler görünür, sadece çevrilmemiş olur.
- **World News API / DeepL** — ücretsiz katmanlarından kendi anahtarınızı alabilirsiniz
  (`api.worldnewsapi.com`, `api-free.deepl.com`).

---

## 10. Build & Test

### Backend

```bash
cd finsight-backend

./mvnw test          # birim testler
./mvnw verify        # tam build + testler
./mvnw clean package # çalıştırılabilir jar → target/finsight-0.0.1-SNAPSHOT.jar
```

- **Testcontainers kullanılmıyor.** SQL Server / Redis gerektiren entegrasyon testleri lokal Docker
  Compose altyapısına karşı koşar — testlerden önce `docker compose up -d` yapın.
- Persistence testleri Flyway migration'larına dayanır; migration'ların temiz bir veritabanında
  sorunsuz koşabildiğinden emin olun.

Üretilen jar'ı çalıştırmak için:

```bash
java -jar target/finsight-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd finsight-frontend

npm run lint      # ESLint (flat config)
npm run build     # tsc -b tip kontrolü + prod build → dist/
npm run preview   # prod build'i lokal önizle
```

- Otomatik test koşucusu (Jest/Vitest) **henüz yapılandırılmadı**; kalite kapısı şimdilik
  lint + tip kontrolüdür.

---

## 11. Geliştirme Akışı

**Dallar (branch)**

- `develop` entegrasyon dalıdır — tüm işler ondan dallanır ve PR ile geri birleşir.
- `main` sürüm dalıdır; doğrudan geliştirme yapılmaz.
- Dal adları `<tip>/<kebab-açıklama>` biçimindedir:
  `feature/fund-dashboard`, `fix/otp-abuse`, `refactor/fund-chat`, `docs/readme`, `test/...`, `chore/...`

**Commit'ler — [Conventional Commits](https://www.conventionalcommits.org/)**

- Biçim: `tip(scope): konu` — örn. `feat(fund-chat): add rule-based assistant`
- Tipler: `feat`, `fix`, `refactor`, `chore`, `db`, `test`, `docs`
- Scope ilgili modül/alandır: `fund`, `fund-chat`, `auth`, `notification`, `stresstest`, `dashboard`, …

**Pull request'ler**

- PR'ı Gitea üzerinde `develop` dalına karşı açın.
- Build ve **SonarQube** kalite kapısı geçmeden merge edilmez.
- Merge için en az **1 onay** gerekir.
- Bir PR = bir mantıksal değişiklik. İlgisiz bir PR'da başkasının modülünü refactor etmeyin.

---

## 12. Veritabanı Migration Kuralları

- Konum: `finsight-backend/src/main/resources/db/migration` — dialect **SQL Server**.
- İsimlendirme: `V<n>__snake_case_aciklama.sql` (örn. `V22__create_fund_price_data_table.sql`).
- Parola hash'lenmesi gibi Java gerektiren durumlarda Java migration:
  `src/main/java/com/akademi/finsight/common/migration/V28__Seed_admin_user.java`.
- Migration'lar uygulama açılışında otomatik koşar (`spring.flyway.enabled=true`).

**Kurallar**

1. **Yalnızca ileri (forward-only).** Ortak ortama ulaşmış bir migration geri alınmaz — düzeltmeyi
   yeni ve daha yüksek numaralı bir migration ile yapın.
2. **Uygulanmış bir migration'ı asla düzenlemeyin.** Flyway checksum doğrular; koşmuş bir dosyayı
   değiştirmek açılışı bozar.
3. **Sıradaki boş sürüm numarasını** alın. Aynı `V<n>` numarasını iki dalda birden kullanmak yaygın
   bir çakışma kaynağıdır — ekip içinde koordine olun.
4. Dosya başına tek mantıksal şema değişikliği; mümkün olduğunca idempotent yazın.

> **Not:** `spring.jpa.hibernate.ddl-auto` şu anda `update`. Yani Hibernate, migration'ların
> dışında da şemaya müdahale edebiliyor. Şema değişikliklerinizi yine de migration ile yazın.

---

## 13. Sorun Giderme

<details>
<summary><b>MSSQL konteyneri başlamıyor / hemen kapanıyor</b></summary>

En yaygın neden `.env`'de `LOCALDB_PASSWORD` tanımsız olması (bkz. [5.3](#53-veritabanını-seçin))
veya parolanın SQL Server karmaşıklık kuralını karşılamaması.

```bash
docker compose logs mssql
```

`Password validation failed` görüyorsanız `LOCALDB_PASSWORD`'ü en az 8 karakter + büyük/küçük harf +
rakam + özel karakter olacak şekilde güncelleyip konteyneri yeniden yaratın:

```bash
docker compose --profile localdb down -v
docker compose --profile localdb up -d
```
</details>

<details>
<summary><b>Backend açılışta veritabanına bağlanamıyor</b></summary>

- MSSQL `healthy` mi? → `docker compose ps`
- `DB_HOST=localhost` ve `DB_PORT` = `LOCALDB_PORT` mi?
- `DB_PASSWORD` = `LOCALDB_PASSWORD` mi?
- Troya kullanıyorsanız **VPN açık mı?**
- 1433 portu başka bir SQL Server tarafından kullanılıyor olabilir: `lsof -i :1433`
</details>

<details>
<summary><b>Flyway "checksum mismatch" veya "Detected applied migration not resolved" hatası</b></summary>

Dal değiştirdiğinizde farklı migration setleriyle karşılaşan veritabanı bu hatayı verir. Lokal
geliştirme veritabanında en temiz çözüm sıfırdan başlamaktır:

```bash
docker compose --profile localdb down -v   # volume'ü de siler
docker compose --profile localdb up -d
```

⚠️ `-v` yalnızca veritabanını değil, projenin **tüm named volume'lerini** siler (MSSQL, Redis,
Kafka, Prometheus, Grafana verisi). Lokal geliştirmede sorun değildir. Paylaşımlı (Troya)
veritabanı kullanıyorsanız `-v` o veritabanına dokunmaz — ama şema sorununu da çözmez; o durumda
`flyway_schema_history` tablosundaki ilgili satırı ekiple birlikte düzeltin.
</details>

<details>
<summary><b>Giriş yaptım ama OTP kodu gelmiyor</b></summary>

Önce hangi SMTP seçeneğini kullandığınıza bakın ([bkz. 5.5](#55-e-posta-gönderimini-ayarlayın)).

**Mailpit kullanıyorsanız** — konteyner ayakta mı?

```bash
docker ps | grep mailpit
```

Çalışmıyorsa 5.5'teki `docker run` komutuyla başlatın; kodları http://localhost:8025 adresinden
okuyun.

**Gerçek SMTP (Gmail) kullanıyorsanız** — sık karşılaşılanlar:

- `MAIL_PASSWORD` normal hesap parolası olamaz; **uygulama şifresi** olmalı (16 hane, boşluksuz).
- Hesapta **2 Adımlı Doğrulama** açık değilse uygulama şifresi üretilemez.
- `MAIL_SMTP_AUTH=true` ve `MAIL_SMTP_STARTTLS_ENABLE=true` olmalı, port `587`.
- Uygulama şifresi iptal edilmişse `535 Authentication failed` alırsınız — yenisini üretin.
- E-posta spam/gereksiz klasörüne düşmüş olabilir.

Her iki durumda da backend logunda SMTP hatası arayın. Bildirimler Kafka üzerinden **asenkron**
gittiği için giriş isteği başarılı görünse bile gönderim arka planda patlamış olabilir:

```bash
grep -i "mail\|smtp" logs/server-log.txt | tail -20
```

Kuyruğa hiç düşüp düşmediğini Kafka UI'dan (http://localhost:8090) `finsight.notification.*`
topic'ine bakarak doğrulayabilirsiniz.
</details>

<details>
<summary><b>Uygulama açılışta ONNX / native library hatasıyla patlıyor (Mac)</b></summary>

Apple Silicon'da x86_64 JDK kullanıyorsunuz demektir. arm64 JDK 21'e geçin:

```bash
java -XshowSettings:properties -version 2>&1 | grep os.arch   # aarch64 olmalı
```

`x86_64` görüyorsanız `JAVA_HOME`'u arm64 bir JDK 21'e yönlendirin ve `./mvnw clean` sonrası tekrar
deneyin.
</details>

<details>
<summary><b>Frontend "VITE_API_BASE_URL is not defined" hatası veriyor</b></summary>

`finsight-frontend/.env` dosyasını oluşturmayı unutmuşsunuz:

```bash
cp .env.example .env
```

Vite `.env`'i yalnızca başlangıçta okur — dosyayı oluşturduktan sonra dev sunucusunu yeniden
başlatın.
</details>

<details>
<summary><b>Frontend'den istek atınca CORS hatası alıyorum</b></summary>

`CORS_ALLOWED_ORIGINS` frontend'in origin'ini içermeli. Vite 5173 yerine başka bir porta düştüyse
(port meşgulse olur) o portu da ekleyin ve backend'i yeniden başlatın.
</details>

<details>
<summary><b>Fon panosu boş / "veri yok" diyor</b></summary>

Beklenen davranış — Infina erişimi (VPN + geçerli `INFINA_API_KEY`) olmadan fon verisi
senkronize edilemez. Bkz. [Bölüm 9](#9-dış-bağımlılıklar--neler-çalışır-neler-çalışmaz).
</details>

<details>
<summary><b>Port çakışması (8080, 5173, 6379, 9092, 1433 …)</b></summary>

Portu kim tutuyor:

```bash
lsof -i :8080
```

Altyapı portları `.env` üzerinden değiştirilebilir (`REDIS_PORT`, `KAFKA_PORT`, `KAFKA_UI_PORT`,
`PROMETHEUS_PORT`, `GRAFANA_PORT`, `LOCALDB_PORT`). Backend portu için `.env`'e
`SERVER_PORT=8081` benzeri bir değer değil, `--server.port` argümanı kullanın:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```
</details>

<details>
<summary><b>Her şeyi sıfırlamak istiyorum</b></summary>

```bash
cd finsight-backend
docker compose --profile localdb down -v
docker rm -f finsight-mailpit
./mvnw clean
docker compose --profile localdb up -d
docker run -d --name finsight-mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
./mvnw spring-boot:run
```
</details>

---

## 14. Bilinen Eksikler

Yeni gelen birinin takılabileceği, henüz giderilmemiş noktalar:

| # | Konu | Geçici çözüm |
|---|------|--------------|
| 1 | `.env.example`'da `LOCALDB_PORT` / `LOCALDB_NAME` / `LOCALDB_PASSWORD` yok, ama `compose.yaml` bunları bekliyor | Üç satırı elle ekleyin ([5.3](#53-veritabanını-seçin)) |
| 2 | Mailpit `compose.yaml`'den düşmüş; `.env.example` hâlâ `localhost:1025`'i gösteriyor | `docker run` ile elle başlatın ([5.5](#55-e-posta-gönderimini-ayarlayın)) |
| 3 | `src/main/resources/model/*.onnx` dosyaları depoda 0 byte | Gerçek modelleri ekipten alın |
| 4 | `FASTAPI_BASE_URL` `.env.example`'da tanımlı değil (kodda varsayılanı `http://localhost:8000`) | Gerekirse `.env`'e elle ekleyin |
| 5 | Stres testinin FastAPI motoru ayrı bir serviste; bu depoda yok | İlgili motoru kullanmayın |
| 6 | Frontend'de otomatik test altyapısı yok | Lint + tip kontrolü ile yetinin |
| 7 | `ddl-auto: update` açık — Hibernate şemaya migration dışında da müdahale edebiliyor | Şema değişikliklerini yine migration ile yazın |
| 8 | `.env.example`'daki `NOTIFICATION_TOPIC` `...v1`, ekibin kullandığı `.env` ise `...v2` | Lokal Kafka'da sorun değil; paylaşımlı bir broker'a bağlanacaksanız ekiple aynı sürümü kullanın |
| 9 | `.env.example`'daki `ADMIN_*` ve parola alanları gerçek görünümlü örnek değerler içeriyor | Kopyaladıktan sonra **mutlaka** kendi değerlerinizle değiştirin ([5.6](#56-kalan-zorunlu-değişkenleri-doldurun)) |

Bunlardan birini düzeltirseniz bu tabloyu da güncelleyin.

---

## 15. Ekip

Modül bazlı sahiplik ve kimin neyi geliştirdiği alt README'lerde ayrıntılı olarak yer alır:

- **Backend** → [`finsight-backend/README.md`](finsight-backend/README.md#8-modül-sahipliği)
- **Frontend** → [`finsight-frontend/README.md`](finsight-frontend/README.md#7-feature-sahipliği)

| Kişi | Başlıca alanlar |
|------|-----------------|
| Melis Kara | Infina entegrasyonu, fon domain & senkronizasyon, fon panosu, fon asistanı (chatbot) |
| Ali Rıza Kaygusuz | `common` altyapı, auth orchestration, refresh token, security, kullanıcı yönetimi, performans karşılaştırma motoru, audit, izleme |
| Beyzanur Yücel | AI dağılım önerisi (ONNX), manuel senaryolar, makro/piyasa verisi, haber & çeviri modülü, e-posta doğrulama token'ları |
| Mehmet Çavdar | Karar geçmişi, admin karar raporu, Kafka bildirim & e-posta altyapısı, şifre sıfırlama & auth sertleştirme |
| Ece Nisa Uğur | Stres testi motoru, makro/market verisi |

Bir modülle ilgili sorunuz olduğunda önce ilgili sahibine sorun.
