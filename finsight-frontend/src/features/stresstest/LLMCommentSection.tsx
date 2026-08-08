interface LLMCommentSectionProps {
  comment: string
}

export function LLMCommentSection({ comment }: LLMCommentSectionProps) {
  const paragraphs = comment.split('\n').filter(Boolean)

  return (
    <div className="space-y-2">
      <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wide">LLM Yorumu</h3>
      <div className="space-y-2 text-xs leading-relaxed">
        {paragraphs.map((paragraph, index) => {
          const isLast = index === paragraphs.length - 1
          return (
            <p key={index} className={isLast ? 'text-slate-400 italic text-[11px]' : 'text-slate-700'}>
              {paragraph}
            </p>
          )
        })}
      </div>
    </div>
  )
}