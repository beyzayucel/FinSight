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
| Uygulama içi cache | Caffeine | (Boot ile yönetilir) |
| Kimlik doğrulama (token) | JWT (jjwt) | 0.12.6 |
| Mesajlaşma | Apache Kafka | 3.9.0 |
| E-posta (dev) | Mailpit (SMTP) | latest |
| API dokümantasyonu | springdoc-openapi (Swagger UI) | 3.0.2 |
| Eşleme (mapping) | MapStruct | 1.6.3 |
| Boilerplate azaltma | Lombok | (Boot ile yönetilir) |
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

| Modül                                                                                                                                                                                                                                                                                                       | Sahip |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------|
| `integration/infina` (temel entegrasyon, fon bilgisi & benchmark), `fund` (dashboard, sync), `fund/chat`                                                                                                                                                                                                    | Melis Kara |
| `common`<br>`auth` (orchestration ve auth ile ilgili servisler)<br>`auth/refreshtoken`<br>`auth/otp`, `auth/verificationtoken`, `auth/ratelimiter` (entegrasyonu sağlandı auth ile,otp-abuse-limiter)<br>`security`<br>`user`<br>`fund/performancecomparison`, `fund/stockprice`<br>`audit`<br>`monitoring` | Ali Rıza Kaygusuz |
| `fund` (AI öneri, manuel senaryo)                                                                                                                                                                                                                                                                           | _(boş)_ |
| `stresstest`, makro/market verisi                                                                                                                                                                                                                                                                           | _(boş)_ |
| `fund` (karar geçmişi, admin karar raporu)                                                                                                                                                                                                                                                                  | Mehmet Çavdar |
| `notification`                                                                                                                                                                                                                                                                                              | Mehmet Çavdar |
| `auth/passwordreset`, `auth/passwordhistory`, `auth/ratelimiter` (password-reset rate-limit — `PasswordResetRateLimitService`, `PasswordResetRateLimitInterceptor`)                                                                                                                                         | Mehmet Çavdar |

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

Projenin ortak (`common`) altyapısını kurdum; `auth` altındaki giriş/oturum akışlarının
orchestration'ını, refresh token mekanizmasını ve OTP/giriş denemesi kötüye kullanım korumasını
geliştirdim. Security katmanında JWT doğrulama ve ilk-giriş zorunluluğu akışlarını, kullanıcı
yönetiminde tüm CRUD ve yaşam döngüsü işlemlerini yaptım. Fon tarafında performans karşılaştırma
ve simülasyon motorunu (hisse fiyatları, cache dahil) baştan kurdum; ayrıca denetim (audit) log
sistemini, izleme/metrik altyapısını (Prometheus/Grafana, dosya loglama) ve bildirim
e-postalarındaki logo entegrasyonunu geliştirdim.

### Ortak Altyapı (`common`)
Projenin tüm servislerinin üzerine kurulduğu ortak katman:
- **`ApiStandardResponse<T>`** — tüm API yanıtlarının tek tip zarfı (`success`, `data`, `message`,
  `error`); başarı ve hata durumları için ayrı factory metodları (`of`, `message`, `error`).
- **`GlobalExceptionHandler`** — tüm exception'ların tek merkezden, tutarlı bir `ErrorDetail` formatına
  çevrildiği `@RestControllerAdvice`; validasyon hataları, bozuk JSON, desteklenmeyen HTTP metodu,
  bilinmeyen endpoint (404), rate-limit/OTP/login limit exception'ları, veri bütünlüğü ihlali ve
  beklenmeyen hatalar için ayrı handler'lar; her yanıta `requestId` (MDC üzerinden) eklenmesi.
- **`BaseException` / `BaseErrorType` / `ErrorType`** — tüm modüllerin kendi typed exception'larını
  üzerine kurduğu ortak hata sözleşmesi (HTTP status + error code + mesaj anahtarı).
- **`BaseEntity` / `SoftDeletableEntity`** — id, `createdAt`/`updatedAt` ve soft-delete (`deleted`
  bayrağı) gibi ortak alanları sağlayan JPA taban sınıfları; projedeki entity'lerin büyük kısmı
  bunlardan türüyor.
- **`MaskType`** — loglarda e-posta/telefon gibi hassas verilerin maskelenmesi (`MaskType.EMAIL.mask(...)`),
  proje genelinde tutarlı bir gizlilik pratiği için.
- **Config katmanı** — `JacksonConfig`, `I18nConfig` (TR/EN mesaj çözümleme), `OpenApiConfig`,
  `AuditConfig`.
- **`RequestIdFilter` / `WebMvcConfig`** — her isteğe benzersiz bir `requestId` atanması ve bunun
  loglarla hata yanıtları arasında izlenebilirlik için MDC'ye yazılması.
- **`BaseController`** — controller'lar arası ortak davranışların merkezileştirilmesi.

### Auth Orchestration (`auth`)
`AuthServiceImpl` üzerinden tüm giriş/oturum akışının orchestration'ı:
- **Giriş (`login`)** — kimlik doğrulama (`AuthenticationManager`), e-posta doğrulanmamışsa
  reddetme, ilk girişte OTP'siz doğrudan token üretimi, sonraki girişlerde 2FA için OTP gönderimi.
  Hatalı denemede `LoginRateLimitService` sayacı artar; limit aşılırsa hesap kilitlenip
  bildirim e-postası gönderilir (`LoginLimitException`, rollback'ten etkilenmez).
- **OTP ile giriş (`otpLogin`)** — aktif OTP kontrolü, kod doğrulama, başarılıysa token üretimi ve
  audit log kaydı.
- **Token yenileme (`refreshTokens`)** — `RefreshTokenService` ile rotasyon, yeni access token üretimi.
- **E-posta doğrulama (`verifyEmail`)** — `VerificationTokenService`'e delege.
- Tüm akışlarda `AppMetrics` üzerinden başarı/başarısızlık sayaçlarının (login, OTP, token yenileme,
  şifre sıfırlama, hesap kilitlenmesi) işlenmesi.

### Refresh Token (`auth/refreshtoken`)
**`RefreshTokenServiceImpl`** üzerinden tüm token yaşam döngüsü:
- **Güvenli üretim ve saklama** — `SecureRandom` ile 32 byte'lık ham token üretilip Base64 URL-safe
  encode edilir; DB'de token'ın kendisi değil SHA-256 hash'i tutulur, böylece veritabanı sızıntısında
  ham token ele geçmez.
- **Rotasyon (`rotateToken`)** — her yenilemede eski token geçersiz kılınıp (`revoked=true`) yeni bir
  token üretilir; süresi dolmuş veya zaten iptal edilmiş bir token kullanılmaya çalışılırsa
  (`REFRESH_TOKEN_EXPIRED`/`REFRESH_TOKEN_REVOKED`) reddedilir.
- **Çalınmış token tespiti** — iptal edilmiş bir token tekrar kullanılmaya çalışılırsa
  (`TOKEN_REUSE` event'i loglanır) bu, token'ın çalınmış olabileceğinin işareti olarak ele alınır.
- **Logout (`revokeToken`)** — token'ı iptal eder ve audit log kaydı (`LOGOUT`) düşer.
- **Toplu iptal (`revokeAllByUser`)** — şifre değişimi/sıfırlama ve kullanıcı silme akışlarında
  kullanıcının tüm refresh token'larının tek seferde geçersiz kılınması.
- **`RefreshTokenCleanupScheduler`** — her gece süresi dolmuş token'ları toplu iptal eden
  (`revokeExpiredTokens`, 03:00) ve 7 günden eski iptal edilmiş kayıtları veritabanından temizleyen
  (`deleteOldRevokedTokens`, 03:30) iki ayrı zamanlanmış görev.

### OTP Kötüye Kullanım Koruması — entegrasyon ve ekstra kontroller (`auth/otp`, `auth/verificationtoken`, `auth/ratelimiter`)
`auth` altındaki akışların orchestration'ını sağladım; `otp` ve `verificationtoken`
mekanizmalarına da entegrasyon ve ek kontroller kattım:
- **OTP abuse limiter** — başarısız deneme sonrası abuse döngüsü artırımı
  (`handleFailedAttempt` → `incrementAbuseCycle`), limit aşıldığında hesabın geçici kilitlenmesi ve
  bildirim gönderilmesi (`sendOtpAbuseNotification`), `OtpLimitException` ile typed hata.
- **Giriş denemesi blocklist/rate-limit** (`auth/ratelimiter`) — `LoginRateLimitService` /
  `LoginBlocklistService` ile engelleme (`blockUser`/`unblockUser`/`checkBlockedOrThrow`) ve
  şifre sıfırlama sonrası tüm kısıtlamaların temizlenmesi (`clearAllRestrictions`);
  `LoginLimitException` ile `GlobalExceptionHandler`'a yeni handler eklenmesi.
- **Verification token mekanizmasına** ekstra kontroller eklendi.
- `AuthServiceImpl` üzerinde limit aşıldığında `LoginLimitException` fırlatılması ve
  `@Transactional(noRollbackFor = ...)` ile sayaç güncellemesinin rollback'ten etkilenmemesi.
- Limit aşıldığında **hesap kilitlendi** bildirim e-postası (şifre sıfırlama linkiyle birlikte) ve
  frontend OTP ekranında kalan hakkın gösterilmesi.

### Security (`security`)
- **`JwtService`** — access token üretimi (roller ve `firstLogin` claim'i ile), token doğrulama ve
  claim çözümleme; `JwtErrorType`'a göre süresi dolmuş/imzası geçersiz/bozuk token'ların ayrı ayrı
  ele alınması.
- **`JwtAuthenticationFilter`** — her istekte token'ı doğrulayıp `SecurityContext`'e authentication
  yazan filtre; `TokenInvalidationService` üzerinden şifre değişiminden önce üretilmiş token'ların
  reddedilmesi.
- **`FirstLoginInterceptor`** — ilk girişte (`firstLogin=true`) şifre değiştirmeden diğer uçlara
  erişimi `403 PASSWORD_CHANGE_REQUIRED` ile engelleyen interceptor.
- **`SecurityConfig`** — Spring Security filter chain yapılandırması, `JwtAuthenticationEntryPoint`/
  `JwtAccessDeniedHandler` ile 401/403 yanıtlarının `ApiStandardResponse` formatına oturtulması.
- `CustomUserDetailsService`, `PasswordEncoderConfig`, `AuthenticationProviderConfig`, `CorsProperties`.

### Kullanıcı Yönetimi (`user`)
**`UserServiceImpl`** üzerinden tüm kullanıcı yaşam döngüsü:
- **Kullanıcı oluşturma** — e-posta normalize edilir (`EmailNormalizer`), e-posta/telefon çakışması
  kontrol edilir, `CredentialsGenerator` ile benzersiz kullanıcı adı ve geçici şifre üretilir;
  oluşturulan kullanıcıya doğrulama token'ı gönderilir (`VerificationTokenService`) ve audit log
  kaydı düşülür.
- **Profil güncelleme** — telefon numarası müsaitlik kontrolü, admin tarafından güncelleme.
- **Aktif/pasif etme ve silme** — kendi kendine işlem yapmayı (`SELF_ACTION_NOT_ALLOWED`) ve
  `ADMIN` rolündeki kullanıcıların değiştirilmesini (`ADMIN_PROTECTED`) engelleyen kontroller;
  silme işleminde kullanıcının tüm refresh token'larının iptal edilmesi.
- **Kimlik doğrulama akışı için sorgular** — e-posta veya kullanıcı adına göre bulma
  (`findByIdentifier`), son giriş zamanı güncelleme, şifre güncelleme (`firstLogin` bayrağını
  temizleyerek).
- **İstatistikler** — toplam/aktif/pasif kullanıcı sayısı ve son 24 saatte giriş yapan kullanıcı
  sayısı (`UserStatsResponse`).
- **Doğrulamayı yeniden gönderme** — zaten doğrulanmış kullanıcıyı reddeden kontrol, yeni geçici
  şifre üretimi ve audit log kaydı.
- Tüm kritik işlemlerde (`USER_CREATED`, `USER_UPDATED`, `USER_ACTIVATED`/`USER_DEACTIVATED`,
  `USER_DELETED`, `VERIFICATION_RESENT`) audit log entegrasyonu.

### Performans Karşılaştırma & Simülasyon Motoru (`fund/performancecomparison`, `fund/stockprice`)
- **`PerformanceComparisonServiceImpl`** — mevcut portföy, simülasyon portföyü ve benchmark için
  güncel değer, toplam getiri, maksimum düşüş ve günlük oynaklık hesaplarını tek yanıtta birleştiren
  `GET /funds/{code}/performance-comparison` ucu; sonradan Infina çağrısı kaldırılıp tamamen DB'den
  beslenecek şekilde refactor edildi.
- **`PortfolioSimulationCalculationServiceImpl`** — manuel senaryo/AI önerisi ağırlıklarından
  simülasyon eğrisi türeten hesaplama servisi; kategori bazlı fallback formülü ile top-10 hisseye
  ait gerçek fiyat verisiyle çalışan per-stock formülü ayrı yollar olarak destekler.
- **`StockPriceServiceImpl`** — top-10 hisse için günlük fiyat geçmişinin arka planda çekilip
  (`backfillIfMissing`, `refreshDay`) belirli bir pencere dışındaki eski verinin temizlenmesi
  (`purgeBefore`).
- **`PortfolioCalculationUtil`** — kümülatif getiriden günlük getiri türetme, max drawdown ve
  günlük oynaklık hesap fonksiyonları; benchmark ve simülasyon eğrilerinin aynı gün üzerinden
  başlaması için anchor noktası eklenmesi.
- Simülasyon/benchmark eğrilerinin gerçek veriyle tutarlılığının 10 gün ve 90 gün pencerelerinde
  doğrulanması.
- **Önbellekleme** — Caffeine tabanlı in-memory cache entegrasyonu (`CaffeineCacheConfig`),
  `FundProperties` üzerinden yapılandırılabilir TTL/boyut ayarları; `PerformanceComparisonServiceImpl`
  için `@Cacheable`, `ManualScenarioServiceImpl`/`AiRecommendationServiceImpl` üzerinde senaryo
  güncellendiğinde ilgili cache girdisini geçersiz kılan `@CacheEvict`.

### Denetim Log Sistemi (`audit`)
- `AuditLog` entity'si, `AuditActionType` / `AuditLogScope` sınıflandırmasıyla; `AuthServiceImpl`
  ve `UserServiceImpl` üzerindeki kritik işlemlere (giriş, kayıt, şifre değişimi, rol güncelleme vb.)
  audit log çağrılarının eklenmesi.
- **`AuditLogArchiveScheduler`** — 90 günden eski kayıtları periyodik olarak arşivleyen zamanlanmış görev.
- `GET /admin/audit-logs` ucu (`AuditLogController`), `AuditLogSpecification` ile filtreleme.
- Admin panelindeki **ActivityTimeline** bileşeni (frontend) — 10 farklı action type için birbirinden
  ayırt edilebilir renk paleti, kullanıcı tablosu ile profil panelinin genişlik/scroll uyumu.

### İzleme & Metrikler (`monitoring`)
- **`AppMetrics`** — Micrometer tabanlı özel metrikler; `AuthServiceImpl` içindeki login
  başarı/başarısızlık gibi olayların sayaç olarak dışa açılması.
- **Actuator**'ın projeye eklenmesi ve `application.yaml`'a Prometheus endpoint yapılandırması
  (dev'de `show-details: always`).
- `compose.yaml`'e Prometheus + Grafana servislerinin eklenmesi.
- **`logback-spring.xml`** — console'a ek olarak dosyaya loglama (`logs/server-log.txt`); boyut ve
  zaman bazlı rotasyon (`SizeAndTimeBasedRollingPolicy`, dosya başına 50MB), 90 günlük saklama ve
  toplam 2GB disk sınırı; her log satırında `requestId`'nin (MDC) izlenebilirlik için basılması.

### E-posta Şablonları
- Tüm bildirim e-postalarına (TR/EN) gömülü (`cid:`) logo eklenmesi — `EmailNotificationSender`'ın
  multipart MIME'a geçirilip `addInline()` ile logo görselinin gövdeye gömülmesi.

### Testler
- `StockPriceServiceImplTest`, `PortfolioSimulationCalculationServiceImplTest`,
  `PerformanceComparisonServiceImplTest` — Mockito tabanlı birim testleri.
- `AuditLogServiceImplTest`, `RefreshTokenServiceImplTest`, `AuthServiceImplTest`,
  `UserServiceImplTest` — core servisler için birim testleri.

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


