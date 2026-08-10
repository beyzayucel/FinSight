# FinSight — Frontend

FinSight'ın web arayüzü: Portföy karar vericilerinin fon panosunu, stres
testlerini, AI önerilerini ve fon asistanını kullandığı **React tek-sayfa uygulaması (SPA)**.
Backend REST API'sini (`/api/v1`) tüketir; kararı her zaman kullanıcıya bırakan bir karar-destek
arayüzü sunar.

## 1. Teknoloji Yığını

| Alan | Teknoloji | Versiyon |
|------|-----------|----------|
| Dil | TypeScript | 6.0 |
| UI kütüphanesi | React | 19.2 |
| Build / Dev server | Vite | 8.1 |
| Stil | Tailwind CSS | 4.3 |
| Yönlendirme (routing) | react-router-dom | 7.18 |
| HTTP istemcisi | axios | 1.19 |
| Grafikler | Recharts | 3.10 |
| İkonlar | react-icons | 5.7 |
| Lint / Format | ESLint 10 + Prettier 3 | — |

## 2. Proje Yapısı

Kod **feature-first** düzenlenmiştir: her ürün alanı kendi ekran ve mantığını
`src/features/<alan>` altında barındırır. Sayfalar arası paylaşılan yapı taşları (tasarım sistemi
primitifleri, ikonlar) `src/components` altındadır. `@/` alias'ı `src/`'e denk gelir.

```
src/
├── main.tsx                 # Uygulama girişi — <App>'i DOM'a mount eder
├── App.tsx                  # Kök bileşen / routing kabuğu (react-router)
├── index.css                # Global stiller + Tailwind tema token'ları
│
├── components/
│   └── ui/                  # Paylaşılan tasarım-sistemi primitifleri (TextField, Button, ...)
│
├── features/                # Kendi içinde bağımsız ürün alanları
│   ├── auth/                # Giriş (login) ekranı + bileşenleri
│   ├── dashboard/           # Fon panosu, chatbot widget, karar geçmişi, performans
│   │   ├── components/       #   fund-dashboard/ (kartlar, chat widget, modallar) dahil
│   │   ├── context/          #   DecisionContext / decisionStore
│   │   ├── lib/              #   API istemcileri + formatlayıcılar
│   │   └── pages/            #   routed sayfalar (fon panosu, AI karar, geçmiş, performans, stres)
│   ├── admin/               # Admin ekranları
│   ├── stresstest/          # Stres testi arayüzü
│   └── news/                # "Günün öne çıkanları" listesi
│
├── i18n/
│   └── translations.ts      # TR/EN metinler + Lang / Translations tipleri
│
└── lib/
    ├── api/                 # axios tabanlı API istemcisi
    └── utils.ts             # Genel yardımcılar

public/                      # Olduğu gibi servis edilen statik dosyalar (favicon, hero-bg.png, logo.png)
```

## 3. Kurulum & Çalıştırma

Ön koşul: **Node.js** (LTS önerilir).

```bash
cp .env.example .env      # 1) ortam değişkenleri (VITE_API_BASE_URL)
npm install               # 2) bağımlılıklar
npm run dev               # 3) dev sunucusu (http://localhost:5173)
```

Diğer komutlar:

```bash
npm run build             # tsc tip kontrolü + prod build (dist/)
npm run preview           # prod build'i lokal önizle
npm run lint              # ESLint
```

## 4. Geliştirme Akışı

**Dallar (branch)**
- `develop` entegrasyon dalıdır — tüm işler ondan dallanır ve PR ile geri birleşir.
- Dal isimleri `<tip>/<kebab-açıklama>` biçiminde, örn. `feature/fund-dashboard`, `fix/login`,
  `refactor/fund-chat`, `docs/readme`.

**Commit'ler — [Conventional Commits](https://www.conventionalcommits.org/)**
- Biçim: `tip(scope): konu`, örn. `feat(fund-chat): fund dashboard chatbot frontend`.
- Kullanılan tipler: `feat`, `fix`, `refactor`, `chore`, `test`, `docs`.
- Scope ilgili feature/alandır (`fund`, `fund-chat`, `auth`, `dashboard`, `stresstest`, ...).

**Pull request'ler**
- PR'ı `develop` dalına karşı Gitea üzerinde aç (`gitea.infina.com.tr/akademi-26/FirstClass`).
- Build/lint geçmeden merge edilmez.
- Merge için en az **1 onay (review approval)** gerekir. <!-- TODO: takım kesin sayıyı onaylasın -->
- Bir PR tek bir mantıksal değişiklik olsun; ilgisiz bir PR'da başkasının feature'ını refactor etme.

## 5. Lint & Tip Kontrolü

```bash
npm run lint              # ESLint (flat config: eslint.config.js)
npm run build             # tsc -b ile tip kontrolü (build'in parçası)
```

- Otomatik test koşucusu (Jest/Vitest) henüz yapılandırılmadı — kalite kapısı şimdilik **lint +
  tip kontrolü**. Test altyapısı eklenince bu bölüm güncellenecek.

## 6. Backend API Entegrasyonu

- Taban URL, `.env` içindeki `VITE_API_BASE_URL` ile verilir (varsayılan
  `http://localhost:8080/api/v1`).
- İstekler `src/lib/api` altındaki axios istemcisi üzerinden gider; feature'lar kendi API
  modüllerini (`src/features/<alan>/lib/*Api.ts`) bu istemci üzerine kurar.
- Uçların referansı için backend Swagger UI: `http://localhost:8080/swagger-ui.html`.

## 7. Feature Sahipliği

Feature başına birincil sahip (ilk kime sorulur). Şimdilik yalnızca aşağıdaki bölüm dolu;
diğerleri ilgili kişilerce doldurulacak.

| Feature | Sahip |
|---------|-------|
| `features/dashboard` (fon panosu, chatbot widget, grafikler, routing) | Melis Kara |
| `features/auth` | _(boş)_ |
| `features/admin` | _(boş)_ |
| `features/stresstest` | _(boş)_ |
| `features/news` | _(boş)_ |

---

# Katkıda Bulunanlar

Her bölüm ilgili kişi tarafından, kendi geliştirdiği kısımlar anlatılarak doldurulur.

## Melis Kara

Katkı alanlarım — frontend proje iskeleti, giriş ekranının ilk hâli, ve **fon panosu (dashboard)**
ile **fon asistanı (chatbot)** arayüzleri:

### Proje İskeleti
- Vite + React + TypeScript + Tailwind ile feature-first proje yapısının ilk kurulumu.

### Giriş Ekranı (`features/auth`)
- Login sayfasının ilk (placeholder) sürümü — sonradan diğer geliştiriciler tarafından genişletildi.

### Fon Panosu / Dashboard (`features/dashboard`)
- Fon panosu ekranının **canlı** `/funds/{code}/dashboard` ucuyla entegrasyonu.
- Pano menüsünün **react-router** ile `/fund/*` altında routed sayfalara bölünmesi.
- **Recharts** ile benchmark karşılaştırma grafiğinin gerçek backend verisine bağlanması.
- KPI kartlarının sadeleştirilmesi ve **klavye erişilebilirliğinin** kazandırılması.
- Panonun paketlere ayrılması ve sayfanın bileşenlere bölünmesi; `DecisionProvider`'ın aktif fonu
  **tek sefer** çekecek şekilde iyileştirilmesi.
- En güncel AI öneri durumunun karar geçmişinden türetilip panoda gösterilmesi.
- Başlıca bileşenler: `FundDashboardPage`, `FundMetricCard`, `TotalValueFlipCard`,
  `BenchmarkComparisonCard`, `PortfolioDistributionCard`, `LatestAiSuggestionCard`,
  `StockBreakdownModal`.

### Fon Asistanı Widget'ı (Chatbot arayüzü)
- Pano üzerindeki chatbot arayüzü: `FundChatWidget`, `FundChatModal`, `FundChatLogo`.
- `fundChatApi.ts` üzerinden backend chat ucuna (`POST /funds/{code}/chat`) bağlanır; oturum bazlı
  konuşma ve dil seçimi yönetimi.

## Ali Rıza Kaygusuz

_(Bu bölümü kendin doldurabilirsin.)_

## Ece Nisa Uğur

_(Bu bölümü kendin doldurabilirsin.)_

## Mehmet Çavdar

_(Bu bölümü kendin doldurabilirsin.)_

## Beyzanur Yücel

Yapay Zeka (AI) Karar Destek Sayfası, Manuel Senaryo Yönetim Arayüzü, Haberler Bileşeni, Karar KPI paneli ve E-posta Doğrulama arayüzlerini geliştirdim.

### Portföy Karar & AI Öneri Arayüzleri (`features/dashboard`)
- **`AiDecisionPage` & `DashboardShell`** — Kullanıcıların AI tabanlı önerileri ve manuel senaryoları yönetebileceği ana Portföy Karar ekranının tasarımı ve entegrasyonu.
- **`AIRecommendationTab`** — ONNX modelinden dönen makroekonomik girdi parametreleri (enflasyon, faiz, altın, CDS vb.) ve önerilen varlık/hisse dağılımlarının, hisse alt kategorileri (subcategories) kırılımıyla birlikte grafiksel ve tablosal gösterimi.
- **`ManualScenarioTab`** — Kullanıcıların kendi portföy ağırlıklarını ve hisse dağılımlarını girebildiği, anlık doğrulama kurallarının işletildiği (toplam ağırlık doğrulaması, veri girilmediğinde butonun deaktive edilmesi) etkileşimli form ekranı.
- **`dashboardApi.ts`** — Backend AI öneri, manuel senaryo kaydetme ve geçmiş simülasyonları çekme API uçlarının RTK Query entegrasyonları.

### Haber Entegrasyonu & Bileşeni (`features/news`)
- **`NewsHighlights` & `newsService`** — Backend haber servisinden en güncel finans/piyasa haberlerini çeken, giriş ekranında ve pano üzerinde dinamik haber filtreleme ve listeleme sağlayan arayüz.



