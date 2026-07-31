# FINSIGHT — Frontend Getting started

```bash
npm install
npm run dev      # start the dev server
```

## Project structure

Code is organized **feature-first**: each product area owns its screens and logic under `src/features/<area>`. Cross-cutting building blocks shared across pages — the design-system primitives and icons — live in `src/components`. The `@/` alias maps to `src/`.

```
src/
├── main.tsx                     # App entry — mounts <App> into the DOM
├── App.tsx                      # Root component / routing shell
├── index.css                    # Global styles + Tailwind theme tokens
│
├── components/                  # Shared across pages/features
│   └── ui/                      # Reusable design-system primitives
│       ├── TextField.tsx            # Labeled input with optional icon/trailing slot
│       ├── PasswordField.tsx        # Password input with show/hide toggle
│       ├── Checkbox.tsx             # Labeled checkbox
│       ├── Button.tsx               # Primary full-width action button
│       └── index.ts                 # Barrel → import from `@/components/ui`
│
├── features/                    # Product features (self-contained)
│   ├── auth/
│   │   ├── LoginPage.tsx        # Login screen composition
│   │   └── components/          # Page sections specific to auth
│   │       ├── BrandPanel.tsx       # Left hero panel (photo + headline)
│   │       ├── LanguageSwitcher.tsx # TR | EN language toggle
│   │       └── LoginForm.tsx        # Email + password sign-in form
│   └── news/
│       ├── NewsHighlights.tsx   # "Today's highlights" list
│       └── newsService.ts       # News data access (placeholder → API)
│
├── i18n/
│   └── translations.ts          # TR/EN copy + Lang / Translations types
│
└── lib/
    └── utils.ts                 # Generic helpers

public/                          # Static assets served as-is
├── favicon.svg
├── hero-bg.png                  # Login hero background
└── logo.png                     # FINSIGHT logo
```