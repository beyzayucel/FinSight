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
| `fund` (karar geçmişi, admin karar raporu) | Mehmet Çavdar |
| `notification` | Mehmet Çavdar |
| `auth/passwordreset`, `auth/passwordhistory`, `auth/ratelimiter` | Mehmet Çavdar |

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

Karar geçmişi (decision history) hattını, admin karar raporunu, Kafka tabanlı bildirim/e-posta
altyapısını ve şifre sıfırlama ile birlikte gelen auth sertleştirmelerini geliştirdim.

### Karar Geçmişi (`fund` — `DecisionHistoryServiceImpl`)
- **Birleşik karar geçmişi** — AI önerileri (`AiRecommendation`) ile manuel senaryoları
  (`ManualScenario`) tek bir `DecisionRecordResponse` akışında birleştirip tarihe göre tersten
  sıralayan servis; `GET /funds/{fundId}/decisions` ucu. AI tarafında henüz karara bağlanmamış
  (`PENDING`) kayıtlar geçmişe düşmez.
- **Performans anlık görüntüsü (snapshot)** — karar anındaki `PerformanceMetrics` kaydı; getiri,
  volatilite ve **benchmark farkı (bps)** alanları karar satırıyla birlikte döner.
- **Hisse kırılımı** — hem AI hem manuel kararlar için ağırlıkların hisse bazlı kırılımı; eksik
  varlık kategorileri sıfıra düşürülerek (`weights` map'i) grafiklerin boşluk görmemesi sağlandı.
- **Veri tarihi** — metriklerin hesaplandığı `dataDate` artık `ManualScenario` ve
  `AiRecommendation` üzerinde saklanıyor; karar satırında veri tarihi, altında işlem zamanı, detay
  panelinde ise metriklerin hesaplandığı analiz dönemi gösteriliyor.

### Admin Karar Raporu (`fund` — `AdminDecisionServiceImpl`)
- **ADMIN rolüne özel** `GET /admin/decisions` ucu; AI ve manuel kararları tek raporda birleştirir.
- **JPA Specification tabanlı filtreleme** — `AiRecommendationSpecification` /
  `ManualScenarioSpecification` ile kullanıcı, karar tipi (`DecisionType`: `AI_APPROVED`,
  `AI_REJECTED`, `MANUAL`) ve son *n* gün filtreleri; filtre eşleşmediğinde ilgili repository'ye hiç
  gidilmez (`Stream.empty()`).
- `RecommendationStatus` → `DecisionType` eşlemesi ve tüm kayıtların `createdAt`'e göre tek sıralı
  akışta birleştirilmesi.

### Bildirim & E-posta Altyapısı (`notification`)
Kafka üzerinden asenkron çalışan, şablonlu ve **idempotent** bildirim hattını kurdum.
- **`NotificationEventPublisher` / `KafkaNotificationPublisher`** — uygulama içi bildirim
  komutlarını (`NotificationCommand`) `NotificationRequestedEvent`'e çevirip Kafka'ya basar.
- **`NotificationKafkaListener` → `NotificationDispatcher`** — olayı tüketir, render eder ve
  gönderime verir.
- **`NotificationRenderer`** — `notification.<tip>.<subject|body>` anahtar düzeniyle
  `MessageSource` üzerinden **TR/EN şablon** çözümlemesi; locale bulunamazsa varsayılan locale'e
  düşer, çözülememiş `{placeholder}` kalırsa bildirim geçersiz sayılır.
- **`RedisIdempotencyStore`** — `eventId` bazlı idempotency; Kafka'nın at-least-once teslimi
  nedeniyle aynı e-postanın iki kez gitmesi engellenir.
- **`EmailNotificationSender`** — SMTP gönderimi (dev'de Mailpit), `MailProperties` ile
  yapılandırılır.
- **Kayıt akışına entegrasyon** — e-posta doğrulama bildirimi kayıt (register) akışına bağlandı.
- Modülün ilk hâlinden sonra yaptığım düzenlemeler: hardcode Kafka yapılandırmasının
  `NotificationProperties`/`KafkaConfiguration` ile dışarı alınması, `BaseException` + `ErrorType`
  desenine geçiş (`NotificationErrorType`), Lombok ile constructor injection, paket yapısının
  sadeleştirilmesi ve kullanılmayan `EmailService` facade'ının kaldırılması.

### Şifre Sıfırlama & Auth Sertleştirme (`auth`, `security`)
- **Şifremi unuttum / şifre sıfırlama akışı (`auth/passwordreset`)** — tek kullanımlık token
  üretimi, **hash'lenmiş** saklama, son kullanma süresi ve `PasswordResetTokenScheduler` ile süresi
  dolmuş tokenların periyodik temizliği. Uçlar: `POST /auth/forgot-password`,
  `POST /auth/reset-password`.
- **Şifre geçmişi (`auth/passwordhistory`)** — son *n* şifrenin hash'i saklanarak **tekrar
  kullanım engellenir**; sınır `PasswordHistoryProperties` ile yapılandırılabilir.
- **Oturum geçersizleştirme (`security/jwt`)** — `RedisTokenInvalidationService`; şifre
  değişiminden **önce** üretilmiş access token'lar `JwtAuthenticationFilter` seviyesinde reddedilir,
  böylece sıfırlama sonrası eski oturumlar düşer.
- **Rate limiting (`auth/ratelimiter`)** — şifre sıfırlama istek ve gönderim uçları için Redis
  tabanlı sayaçlar (`PasswordResetRateLimitInterceptor`, `...SubmitRateLimitInterceptor`),
  `CachedBodyFilter` ile gövdenin bir kez okunup tekrar kullanılabilmesi. Ayrıca hatalı giriş
  denemesinde login sayacının artmaması hatası düzeltildi ve identifier hash'leme sorumluluğu
  `LoginRateLimitService`'e taşındı. Aynı kullanıcının **cooldown süresi içinde arka arkaya sıfırlama
  isteği** göndermesi engellendi; IP bazlı limit ise ortak NAT arkasındaki kullanıcıları yanlışlıkla
  kilitlediği için bu akıştan kaldırıldı.
- Şifre değişiminde kullanıcıya bilgilendirme e-postası (yukarıdaki bildirim hattı üzerinden).

### Ortak / Altyapı
- Bilinmeyen uçlar için **500 yerine 404** dönülmesi (`GlobalExceptionHandler`).
- `ApiEndpoints` sabitlerinin ve TR/EN `messages*.properties` bildirim şablonlarının genişletilmesi.

### Testler
- `notification` — `NotificationRenderer`, `NotificationDispatcher`, `KafkaNotificationPublisher`,
  `EmailNotificationSender`, `RedisIdempotencyStore` ve `NotificationServiceImpl` için birim
  testleri; ortak veri üretimi `NotificationFixtures` altında.
- `auth` — `PasswordResetTokenServiceImpl`, `PasswordResetTokenScheduler`,
  `PasswordHistoryServiceImpl`, `PasswordResetRateLimitServiceImpl` ve
  `RedisTokenInvalidationService` testleri.
- `fund` — `DecisionHistoryServiceImpl`, `AdminDecisionServiceImpl` ile karar filtreleme
  specification'larının (`AiRecommendationSpecificationTest`, `ManualScenarioSpecificationTest`)
  birim testleri.

## Beyzanur Yücel

Yapay Zeka (AI) Fon Dağılım Önerisi, Kullanıcı Manuel Senaryoları, Makro/Piyasa Verileri Senkronizasyonu, CDS Entegrasyonu, Haber Çeviri/Yönetim Modülü ve E-posta Doğrulama Token altyapısını geliştirdim.

### AI Öneri Entegrasyonu & Karar Katmanı (`fund/decision`)
- **`AiRecommendationServiceImpl`** — ONNX formatındaki makine öğrenmesi modelinin (ONNX Runtime) sisteme entegrasyonu. Model girdisi olarak enflasyon, politika faizi, CDS, döviz kuru, altın ve Brent petrol gibi makroekonomik verileri toplayıp işleyen ve fon ağırlık dağılım önerileri üreten boru hattı.
- Model çıktılarını (`AiRecommendation`, `AiRecommendationWeight` ve `AiRecommendationStockWeight`) veritabanına kaydetme, yönetme ve listeleme API uçları.

### Manuel Senaryo & Doğrulama Katmanı
- **`ManualScenarioServiceImpl`** — Kullanıcıların kendi belirledikleri ağırlıklara göre senaryo simülasyonları tasarlayabilmesi ve kaydedebilmesi.
- **`ScenarioValidationService`** — Senaryo girişleri için iş kurallarının işletilmesi; toplam ağırlık kontrolü (%99.99 - %100.01), hisse senedi taban sınırı (min %80), ana varlık grubu sapma limiti (maks %10) ve hisse bazlı sapma limiti (maks %5) kontrolleri.
- AI ve Manuel senaryolar için hisse alt kategorilerinin (subcategories) entegrasyonu.

### Makro Veri Entegrasyonu & Senkronizasyonu (`ai/model`)
- **`CdsDataServiceImpl`** — `data/TRGV5YUSAC=R.csv` dosyasından tarihsel Türkiye CDS spread verilerini parse ederek belleğe alan ve sorgulatan veri sağlayıcısı.
- **`ModelDataSyncServiceImpl`** — Günlük USD/TRY, Altın, Brent petrol, ABD 10 Yıllık tahvil getirileri, enflasyon, politika faizi ve CDS spreadlerini senkronize eden; geçmişe dönük arama (backward search) desteği sunan senkronizasyon servisi.
- **`ModelDataSyncScheduler`** — Belirtilen cron zonelarında saatlik olarak ve uygulama başlangıcında (startup) otomatik makro veri güncellemelerini tetikleyen mekanizma.

### E-posta Doğrulama Token Yapısı (`auth/verificationtoken`)
- **`VerificationTokenServiceImpl`** — Kullanıcı kaydı sırasında benzersiz e-posta doğrulama tokenları üreten, bunları şifreleyerek veritabanında saklayan ve doğrulama akışını (verify/resend) yöneten servis.
- **`VerificationTokenScheduler`** — Süresi dolmuş doğrulama tokenlarını periyodik olarak temizleyen zamanlanmış arka plan görevi.

### Haber Entegrasyonu & Çeviri Modülü (`news`)
- **`NewsServiceImpl`** — Harici News API servisinden en güncel finans ve piyasa haberlerini çeken mekanizma.
- **`TranslationServiceImpl` & `DeepLServiceImpl`** — İngilizce gelen haber başlıklarını ve içeriklerini DeepL API entegrasyonu üzerinden Türkçe'ye çeviren sistem.
- **`NewsScheduler`** — En güncel haberleri belirli aralıklarla arka planda otomatik olarak güncelleyen ve veritabanına kaydeden zamanlayıcı.

### Paket Refaktörü ve Testler
- Karar destek (AI/Manuel) yapısı ile makro veri senkronizasyonu bileşenlerinin kod kalitesini artırmak üzere `com.akademi.finsight.fund.decision` ve `com.akademi.finsight.ai.model` bağımsız paketlerine refaktör edilmesi.
- `AiRecommendationServiceImpl`, `ManualScenarioServiceImpl`, `ScenarioValidationService`, `CdsDataServiceImpl` ve `NewsServiceImpl` için unit test yazımı ve test kapsamının genişletilmesi.


