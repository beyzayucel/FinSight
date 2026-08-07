interface LLMCommentSectionProps {
  comment: string;
}

/** Renders the model's scenario commentary. The final paragraph (disclaimer)
 * is styled as muted footnote text, matching the reference design. */
export function LLMCommentSection({ comment }: LLMCommentSectionProps) {
  const paragraphs = comment.split("\n").filter(Boolean);

  return (
    <div>
      <h3 className="text-base font-semibold text-neutral-900">LLM Yorumu</h3>
      <div className="mt-2 space-y-3 text-sm leading-relaxed">
        {paragraphs.map((paragraph, index) => {
          const isLast = index === paragraphs.length - 1;
          return (
            <p key={index} className={isLast ? "text-neutral-400" : "text-neutral-700"}>
              {paragraph}
            </p>
          );
        })}
      </div>
    </div>
  );
}
