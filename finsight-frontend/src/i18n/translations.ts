export type Lang = 'tr' | 'en'

export const translations = {
  tr: {
    tagline: 'Veriyle Analiz Edin, Güvenle Karar Verin',
    heroTitle1: 'AI DESTEKLİ',
    heroTitle2: 'PORTFÖY',
    heroTitle3: 'YÖNETİMİ',
    highlightsTitle: 'GÜNÜN ÖNE ÇIKANLARI:',
    loginTitle: 'Giriş Yap',
    loginSubtitle: 'Sisteme erişmek için kimlik bilgilerinizi doğrulayın.',
    emailLabel: 'E-POSTA ADRESİ',
    emailPlaceholder: 'ornek@kurum.com',
    passwordLabel: 'ŞİFRE',
    passwordPlaceholder: 'Şifrenizi girin',
    forgotPassword: 'Şifremi Unuttum',
    rememberMe: 'Beni Hatırla',
    loginButton: 'Giriş Yap',
    ago: (s: string) => s,
    noNews: 'Güncel bildirim yok.',
  },
  en: {
    tagline: 'Analyze with Data, Decide with Confidence',
    heroTitle1: 'AI-POWERED',
    heroTitle2: 'PORTFOLIO',
    heroTitle3: 'MANAGEMENT',
    highlightsTitle: "TODAY'S HIGHLIGHTS:",
    loginTitle: 'Sign In',
    loginSubtitle: 'Verify your credentials to access the system.',
    emailLabel: 'EMAIL ADDRESS',
    emailPlaceholder: 'example@company.com',
    passwordLabel: 'PASSWORD',
    passwordPlaceholder: 'Enter your password',
    forgotPassword: 'Forgot Password',
    rememberMe: 'Remember Me',
    loginButton: 'Sign In',
    ago: (s: string) => s,
    noNews: 'No current notifications.',
  },
} satisfies Record<Lang, Record<string, unknown>>

export type Translations = (typeof translations)['tr']
