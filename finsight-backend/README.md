# FinSight — Backend

FinSight, kurum içi bir **fon analizi ve karar destek** platformudur. Bu servis onun backend'idir:
**Infina**'dan fon ve piyasa verisini çeker; fon panoları, stres testleri, AI önerileri ve bir fon
asistanı sunar; FinSight web arayüzüne REST API üzerinden hizmet verir. Sayıları ve o sayıların
ardındaki gerekçeyi tek yerde görmek isteyen **fon analistleri ve portföy karar vericileri** için
tasarlanmıştır — karar her zaman kullanıcıda kalır.

## 1. Teknoloji Yığını

| Alan | Teknoloji | Versiyon |
|------|-----------|----------|
| Dil / Runtime | Java | 21 |
| Framework | Spring Boot | 4.1.0 |
| Web / Persistence / Security | Spring Web MVC, Data JPA, Security, Validation, Actuator | (Boot ile yönetilir) |
| Veritabanı | Microsoft SQL Server | 2022 |
| Şema migration | Flyway (SQL Server dialect) | (Boot ile yönetilir) |
| Önbellek / Oturum | Redis | 7 |
| Mesajlaşma | Apache Kafka | 3.9.0 |
| E-posta (dev) | Mailpit (SMTP) | latest |
| API dokümantasyonu | springdoc-openapi (Swagger UI) | 3.0.2 |
| Eşleme (mapping) | MapStruct | 1.6.3 |
| ML runtime | ONNX Runtime | 1.28.0 |
| İzleme | Prometheus + Grafana | latest |

> Veritabanı **SQL Server**'dır — Flyway `flyway-sqlserver` dialect'ini,
> JDBC sürücüsü de `mssql-jdbc` kullanır.

## 2. Proje Yapısı

```
src/main/java/com/akademi/finsight/
├── auth/            Kimlik doğrulama, OTP, şifre sıfırlama/geçmişi, refresh token, rate limiter
├── security/        JWT üretme/doğrulama, Spring Security yapılandırması
├── user/            Kullanıcı hesapları ve profil yönetimi
├── integration/
│   └── infina/      Infina piyasa-veri servisleri için dış istemci (fon, benchmark, FX, ...)
├── fund/            Fon CRUD, dashboard, Infina senkronizasyonu, hisse fiyatları, performans karşılaştırma
│   ├── chat/        Kural tabanlı fon asistanı (chatbot) + Redis oturum hafızası
│   └── stockprice/  Hisse fiyat geçmişi önbelleği ve yenileme
├── stresstest/      Stres testi motoru, senaryolar ve AI yorumları
├── news/            Haber çekme entegrasyonu
├── notification/    Kafka tabanlı bildirim + e-posta gönderimi
├── audit/           Hassas işlemlerin denetim (audit) kaydı
├── monitoring/      Metrik / gözlemlenebilirlik altyapısı
├── common/          Ortak config, yanıt zarfı, mapper'lar, maskeleme, sabitler
└── bootstrap/       Açılış seed'leri ve uygulama başlangıcı

src/main/resources/
├── db/migration/    Flyway sürümlü migration'lar (V<n>__*.sql)
├── fund-chat/       Chatbot bilgi tabanı (dil bazlı intents.json / faq.json)
└── model/           ONNX model dosyaları
```

## 3. Kurulum & Çalıştırma

Ön koşullar: **JDK 21**, **Docker**.

```bash
cp .env.example .env          # 1) ortam değişkenleri
docker compose up -d          # 2) altyapı: MSSQL, Redis, Kafka, Prometheus, Grafana, Mailpit
./mvnw spring-boot:run        # 3) uygulamayı çalıştır (profil: dev)
```

Varsayılan adresler (dev):

| Servis | Adres |
|--------|-------|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Kafka UI | http://localhost:8090 |
| Mailpit | http://localhost:8025 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 |

Şema, açılışta Flyway tarafından otomatik oluşturulur/güncellenir. Admin hesabı `.env` içindeki
`ADMIN_*` değerlerinden seed edilir.

## 4. Geliştirme Akışı

**Dallar (branch)**
- `develop` entegrasyon dalıdır — tüm işler ondan dallanır ve PR ile geri birleşir.
- Dal isimleri `<tip>/<kebab-açıklama>` biçiminde, örn. `feature/fund-dashboard`, `fix/otp-abuse`,
  `refactor/fund-chat`, `docs/readme`.

**Commit'ler — [Conventional Commits](https://www.conventionalcommits.org/)**
- Biçim: `tip(scope): konu`, örn. `feat(fund-chat): add rule-based assistant`.
- Kullanılan tipler: `feat`, `fix`, `refactor`, `chore`, `db`, `test`, `docs`.
- Scope ilgili modül/alandır (`fund`, `fund-chat`, `auth`, `notification`, `stresstest`, ...).

**Pull request'ler**
- PR'ı `develop` dalına karşı Gitea üzerinde aç (`gitea.infina.com.tr/akademi-26/FirstClass`).
- Build ve **SonarQube** kalite kapısı geçmeden merge edilmez.
- Merge için en az **1 onay (review approval)** gerekir. <!-- TODO: takım kesin sayıyı onaylasın -->
- Bir PR tek bir mantıksal değişiklik olsun; ilgisiz bir PR'da başkasının modülünü refactor etme.

## 5. Test

```bash
./mvnw test        # birim (unit) testler
./mvnw verify      # tam build + testler
```

- **Testcontainers kullanılmıyor.** MSSQL/Redis gerektiren entegrasyon testleri lokal Docker Compose
  altyapısına karşı koşar — testleri çalıştırmadan önce `docker compose up -d` ile ayağa kaldır.
- Persistence testleri Flyway migration'larına dayanır; migration'ların temiz bir veritabanında
  sorunsuz koşabilir olmasını koru.

## 6. Veritabanı Migration'ları (Flyway)

- Konum: `src/main/resources/db/migration`. Dialect: **SQL Server**.
- İsimlendirme: `V<n>__snake_case_aciklama.sql` (örn. `V22__create_fund_price_data_table.sql`).
- Migration'lar uygulama açılışında otomatik çalışır.

**Kurallar**
- **Yalnızca ileri (forward-only).** Ortak/prod ortama ulaşmış bir migration geri alınmaz —
  düzeltmeyi yeni ve daha yüksek numaralı bir migration ile yap.
- **Uygulanmış bir migration'ı asla düzenleme.** Flyway checksum doğrular; koşmuş bir dosyayı
  değiştirmek açılışı bozar. Değişikliği yeni bir migration ile yap.
- **Sıradaki boş sürüm numarasını** seç — `V<n>` çakışmalarını önlemek için ekip içinde koordine ol.
- Dosya başına tek mantıksal şema değişikliği; mümkün olduğunda güvenli/idempotent tut.

## 7. API Dokümantasyonu

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Ortam bazlı: `dev`'de açık; **prod'da kapalı/korumalı**. <!-- TODO: prod politikasını doğrula -->

## 8. Modül Sahipliği

Modül başına birincil sahip (ilk kime sorulur). Şimdilik yalnızca aşağıdaki bölümler dolu;
diğerleri ilgili kişilerce doldurulacak.

| Modül | Sahip |
|-------|-------|
| `integration/infina` (temel entegrasyon, fon bilgisi & benchmark), `fund` (dashboard, sync), `fund/chat` | Melis Kara |
| `auth`, `security`, `user`, `audit` | _(boş)_ |
| `fund` (AI öneri, manuel senaryo) | _(boş)_ |
| `fund` (performans karşılaştırma) | _(boş)_ |
| `stresstest`, makro/market verisi | _(boş)_ |
| `notification` | _(boş)_ |

---

# Katkıda Bulunanlar

Her bölüm ilgili kişi tarafından, kendi geliştirdiği kısımlar anlatılarak doldurulur.

## Melis Kara

Infina entegrasyonu, fon persistence/sync hattı, fon panosu (dashboard) ve kural tabanlı fon
asistanı katmanlarını geliştirdim.

### Infina Entegrasyonu (`integration/infina`)
Bu entegrasyonun **temelini** ben kurdum (istemci iskeleti, config, hata yönetimi); üzerine sonradan
birkaç kişi daha ekledi (ör. `FundDailyReturn` ile ekonomik/makro uçları). Benim yaptıklarım:
- **`FonKunye.v2` fon bilgisi** entegrasyonu ve fon bilgi yanıtının yeniden tasarımı.
- **`BenchmarkInfo`** servis entegrasyonu ve dashboard'a beslenen benchmark karşılaştırma serisi.
- **`FundPortfolioAllocation`** — fon portföy dağılımı entegrasyonu ve ucu.
- Ortak **istemci yapılandırmasının merkezileştirilmesi** ve istek başına **connect/read timeout**'lar.
- Tek bir hata-yönetim yolu ile upstream/HTTP hatalarını, ham istemci hatasını yukarı sızdırmadan,
  **typed exception**'lara maplama.
- `periods` parametresinin hizalanması ve DTO tip düzeltmeleri.

### Fon Domain & Persistence (`fund`)
- `Fund`, `FundDistribution`, `FundPeriodMetric`, `FundStockAllocation` için JPA entity ve
  repository'leri; soft-delete (`deleted` bayrağı) ve sayfalama (pagination) ile.
- Bu varlıkların tümü için **CRUD uçları** (oluştur / listele-getir / güncelle / sil):
  fon ve fon dağılımı CRUD'u ile dönem metriği (`FundPeriodMetric`) ve hisse dağılımı
  (`FundStockAllocation`) CRUD uçları.
- Dashboard okuma yolunu ekstra join olmadan besleyen latest-by-code / latest-by-fund sorguları.

### Fon Verisi Senkronizasyonu
- Infina'dan **TIE** fon verisi çekimi; hem açılış işi (startup) hem sabit zamanlı (scheduled)
  çalışabilir; geçmiş tarihli (backdated) çekimi de destekler.
- Yazma `FundSyncPersister` üzerinden gider; bu bileşen
  `@CacheEvict(FUND_DASHBOARD, key = "#snapshot.fundCode()")` ile işaretli — her yazımda o fonun
  dashboard cache'i geçersiz kılınır, böylece sync sonrası bayat veri servis edilmez.

### Fon Panosu / Dashboard (`FundDashboardServiceImpl`)
- `GET /funds/{code}/dashboard`; fon başlığı, toplam değer, günlük ve dönemsel getiriler,
  benchmark farkı (**bps**), varlık dağılımı ve en yüksek ağırlıklı hisse kırılımını tek yanıtta
  birleştirir.
- **Cache-aside**, Spring Cache ile: `@Cacheable(FUND_DASHBOARD, key = "#fundCode")`; sıcak okumalar
  cache'ten gelir, tazelik yukarıdaki sync-taraflı eviction ile garanti edilir.
- En güncel AI öneri durumunu karar geçmişinden (decision history) türetir; böylece dashboard, ayrı
  bir sorgu gidiş-dönüşü olmadan en son kararı yansıtır.

### Fon Asistanı — Kural Tabanlı, Deterministik (`fund/chat`)
Harici LLM yok: cevaplar deterministik, tamamen fonun kendi verisine dayalı ve denetlenebilir.
- **`RuleBasedFundChatProvider`** — girdiyi normalize eder (küçük harf + Türkçe harf katlama
  `ı/ğ/ü/ş/ö/ç`), her intent'i anahtar-kelime eşleşme sayısıyla puanlar ve en yükseği seçer. Bir
  *knowledge-first* işaret kümesi, tanım/tavsiye sorularını veriye gitmeden FAQ'a yönlendirir;
  eşleşmeyen girdi önce FAQ'a, sonra sabit bir fallback'e düşer. Her cevap bir kaynak etiketi taşır:
  `RULE` / `KNOWLEDGE` / `FALLBACK`.
- **7 veri-tabanlı intent** — toplam değer, günlük getiri, dönemsel getiri, benchmark farkı, varlık
  dağılımı, en yüksek 5 hisse, veri tarihi. Cevaplar canlı snapshot'tan doldurulan şablonlardır;
  hedef dönem sorudan `\d+(gün|day)` regex'i ile çıkarılır.
- **`FundChatContextBuilder`**, cache'li `FundDashboardResponse`'u tekrar kullanır ve provider'ın
  okuduğu yapılandırılmış bir snapshot'a dönüştürür — chat ve dashboard tek doğruluk kaynağını paylaşır.
- **`RedisFundChatMemoryStore`** — konuşma geçmişi `(user, fundCode, sessionId)` bazında, JSON'a
  serileştirilmiş turlar hâlinde Redis'te TTL ile tutulur. Konuşmak için `POST /funds/{code}/chat`,
  oturumu sıfırlamak için `DELETE /funds/{code}/chat/{sessionId}`.

### Testler
- Infina yolları için servis ve mapper seviyesinde test kapsamı
  (`FonKunye.v2`, `BenchmarkInfo`, `FundPortfolioAllocation`).

## Ali Rıza Kaygusuz

_(Bu bölümü kendin doldurabilirsin — ör. auth/security, rate limiter, performans karşılaştırma, audit.)_

## Ece Nisa Uğur

_(Bu bölümü kendin doldurabilirsin — ör. stres testi motoru, makro/market verisi.)_

## Mehmet Çavdar

_(Bu bölümü kendin doldurabilirsin — ör. karar raporu/karar geçmişi, admin uçları, notification.)_

## Beyzanur Yücel

_(Bu bölümü kendin doldurabilirsin — ör. AI öneri, manuel senaryo, sync/makro veri entegrasyonu.)_
