type ComingSoonPlaceholderProps = {
  title: string
  description: string
}

/**
 * Henüz canlı veriye bağlanmamış sidebar ekranları için ortak boş durum.
 * Markup, tek sayfalık DashboardPage içindeki renderPlaceholder()'dan
 * olduğu gibi taşındı.
 */
export default function ComingSoonPlaceholder({ title, description }: ComingSoonPlaceholderProps) {
  return (
    <div className="space-y-6 select-none animate-fade-in">
      {/* Breadcrumb / Küçük Başlık */}
      <div>
        <span className="text-[10px] font-bold tracking-wider text-[#c89834] uppercase block">
          Finsight · Karar Destek Platformu
        </span>
        <h2 className="text-3xl font-extrabold text-slate-800 mt-1">{title}</h2>
        <p className="text-xs text-slate-500 font-medium mt-1.5">{description}</p>
      </div>

      {/* Placeholder Görsel / Premium Card */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm p-12 flex flex-col items-center justify-center text-center space-y-4">
        <div className="w-16 h-16 rounded-2xl bg-amber-50 border border-amber-200 flex items-center justify-center text-amber-600 shadow-sm">
          <span className="text-2xl font-bold font-serif">FI</span>
        </div>
        <div className="max-w-md">
          <h3 className="text-lg font-bold text-slate-800">
            Bu Panel Çok Yakında Hizmetinizde!
          </h3>
          <p className="text-sm text-slate-500 mt-2 leading-relaxed">
            Finsight Karar Destek Platformu'nun gelişmiş analitik modülleri üzerinde çalışmaya devam ediyoruz. Bu ekran kısa bir süre içinde canlı verilerle kullanıma açılacaktır.
          </p>
        </div>
      </div>
    </div>
  )
}
