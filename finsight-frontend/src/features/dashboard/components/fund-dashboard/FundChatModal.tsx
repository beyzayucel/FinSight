import { useEffect, useRef, useState } from 'react'
import type { KeyboardEvent as ReactKeyboardEvent } from 'react'
import { IoClose, IoRefresh, IoSend } from 'react-icons/io5'
import FundChatLogo from './FundChatLogo'
import { FUND_CHAT_MAX_MESSAGE } from '../../lib/fund-dashboard/fundChatApi'
import { getTranslations } from '@/i18n/translations'

export type FundChatMessage = {
  id: string
  role: 'user' | 'assistant'
  text: string
}

type FundChatModalProps = {
  messages: FundChatMessage[]
  sending: boolean
  error: string | null
  onSend: (message: string) => void
  onReset: () => void
  onClose: () => void
}

export default function FundChatModal({
  messages,
  sending,
  error,
  onSend,
  onReset,
  onClose,
}: FundChatModalProps) {
  const t = getTranslations()
  const [input, setInput] = useState('')
  const inputRef = useRef<HTMLTextAreaElement>(null)
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  useEffect(() => {
    inputRef.current?.focus()
  }, [])

  useEffect(() => {
    const list = scrollRef.current
    if (list) list.scrollTop = list.scrollHeight
  }, [messages, sending])

  useEffect(() => {
    const el = inputRef.current
    if (!el) return
    el.style.height = 'auto'
    el.style.height = `${Math.min(el.scrollHeight, 96)}px`
  }, [input])

  function submit() {
    const message = input.trim()
    if (!message || sending) return
    onSend(message.slice(0, FUND_CHAT_MAX_MESSAGE))
    setInput('')
  }

  function onInputKeyDown(event: ReactKeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      submit()
    }
  }

  const showSuggestions = messages.length <= 1 && !sending

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="fund-chat-title"
        className="flex h-[min(36rem,calc(100dvh-2rem))] w-full max-w-lg flex-col overflow-hidden bg-white rounded-2xl shadow-2xl animate-fade-in"
        onClick={(e) => e.stopPropagation()}
      >
        {/* ---------- BAŞLIK ---------- */}
        <div className="flex items-start justify-between gap-3 border-b border-slate-100 px-5 py-4">
          <div className="flex items-center gap-3 min-w-0">
            <FundChatLogo className="h-11 w-11 shrink-0 rounded-xl" />
            <div className="min-w-0">
              <h3 id="fund-chat-title" className="text-lg font-bold text-slate-800 truncate">
                {t.fundChatTitle}
              </h3>
              <p className="text-[11px] text-slate-500 font-medium leading-relaxed">
                {t.fundChatSubtitle}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-1 shrink-0">
            <button
              onClick={onReset}
              disabled={sending || messages.length <= 1}
              className="text-slate-400 hover:text-slate-600 disabled:opacity-40 disabled:hover:text-slate-400 transition-colors outline-none"
              aria-label={t.fundChatReset}
              title={t.fundChatReset}
            >
              <IoRefresh size={19} />
            </button>
            <button
              onClick={onClose}
              className="text-slate-400 hover:text-slate-600 transition-colors outline-none"
              aria-label={t.close}
            >
              <IoClose size={22} />
            </button>
          </div>
        </div>

        {/* ---------- MESAJLAR ---------- */}
        <div
          ref={scrollRef}
          className="flex-1 overflow-y-auto px-5 py-4 space-y-3"
          aria-live="polite"
        >
          {messages.map((message) => (
            <div
              key={message.id}
              className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`max-w-[85%] whitespace-pre-wrap break-words rounded-2xl px-3.5 py-2.5 text-sm leading-relaxed ${
                  message.role === 'user'
                    ? 'rounded-br-sm bg-[#c89834] text-white font-medium'
                    : 'rounded-bl-sm bg-slate-100 text-slate-700'
                }`}
              >
                {message.text}
              </div>
            </div>
          ))}

          {sending && (
            <div className="flex justify-start">
              <div className="rounded-2xl rounded-bl-sm bg-slate-100 px-3.5 py-3">
                <span className="flex gap-1">
                  {[0, 150, 300].map((delay) => (
                    <span
                      key={delay}
                      className="h-1.5 w-1.5 rounded-full bg-slate-400 animate-bounce"
                      style={{ animationDelay: `${delay}ms` }}
                    />
                  ))}
                </span>
                <span className="sr-only">{t.fundChatThinking}</span>
              </div>
            </div>
          )}

          {showSuggestions && (
            <div className="pt-1 space-y-2">
              <p className="text-[11px] font-semibold uppercase tracking-wide text-slate-400">
                {t.fundChatSuggestionsLabel}
              </p>
              <div className="flex flex-wrap gap-2">
                {t.fundChatSuggestions.map((suggestion) => (
                  <button
                    key={suggestion}
                    onClick={() => onSend(suggestion)}
                    className="rounded-full border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-600 hover:border-[#c89834] hover:text-[#c89834] transition-colors outline-none text-left"
                  >
                    {suggestion}
                  </button>
                ))}
              </div>
            </div>
          )}

          {error && (
            <p className="rounded-xl bg-rose-50 border border-rose-100 px-3.5 py-2.5 text-xs font-medium text-rose-700">
              {error}
            </p>
          )}
        </div>

        {/* ---------- MESAJ YAZMA ---------- */}
        <form
          onSubmit={(e) => {
            e.preventDefault()
            submit()
          }}
          className="border-t border-slate-100 p-3"
        >
          <div className="flex items-end gap-2 rounded-2xl bg-slate-50 border border-slate-200 focus-within:border-[#c89834] transition-colors px-3 py-2">
            <textarea
              ref={inputRef}
              rows={1}
              value={input}
              maxLength={FUND_CHAT_MAX_MESSAGE}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={onInputKeyDown}
              placeholder={t.fundChatPlaceholder}
              aria-label={t.fundChatPlaceholder}
              className="flex-1 resize-none bg-transparent text-sm text-slate-700 placeholder:text-slate-400 outline-none leading-relaxed max-h-24"
            />
            <button
              type="submit"
              disabled={!input.trim() || sending}
              className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#c89834] text-white transition-all hover:bg-[#a87e2a] disabled:opacity-40 disabled:hover:bg-[#c89834] outline-none"
              aria-label={t.fundChatSend}
              title={t.fundChatSend}
            >
              <IoSend size={14} />
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
