import { useCallback, useEffect, useRef, useState } from 'react'
import FundChatLogo from './FundChatLogo'
import FundChatModal from './FundChatModal'
import type { FundChatMessage } from './FundChatModal'
import { askFundChat, resetFundChat } from '../../lib/fund-dashboard/fundChatApi'
import { getApiError } from '@/lib/api/apiError'
import { getTranslations } from '@/i18n/translations'

type FundChatWidgetProps = {
  fundCode: string
}

/** Tanıtım baloncuğu süre */
const BUBBLE_DELAY_MS = 800
const BUBBLE_DURATION_MS = 7000
const BUBBLE_MAX_SHOWS = 2

export default function FundChatWidget({ fundCode }: FundChatWidgetProps) {
  const t = getTranslations()

  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState<FundChatMessage[]>(() => [
    { id: 'greeting', role: 'assistant', text: getTranslations().fundChatGreeting },
  ])
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const sessionIdRef = useRef<string | null>(null)
  const messageIdRef = useRef(0)

  const [bubbleVisible, setBubbleVisible] = useState(false)
  const bubbleRef = useRef({ shows: 0, visible: false, chatOpened: false })

  const nextId = () => `m-${++messageIdRef.current}`

  useEffect(() => {
    let hideTimer = 0

    function showBubble() {
      const bubble = bubbleRef.current
      if (bubble.chatOpened || bubble.visible || bubble.shows >= BUBBLE_MAX_SHOWS) return
      bubble.shows += 1
      bubble.visible = true
      setBubbleVisible(true)
      hideTimer = window.setTimeout(() => {
        bubbleRef.current.visible = false
        setBubbleVisible(false)
      }, BUBBLE_DURATION_MS)
    }

    const showTimer = window.setTimeout(showBubble, BUBBLE_DELAY_MS)
    document.addEventListener('click', showBubble)
    return () => {
      window.clearTimeout(showTimer)
      window.clearTimeout(hideTimer)
      document.removeEventListener('click', showBubble)
    }
  }, [])

  function openChat() {
    bubbleRef.current.chatOpened = true
    bubbleRef.current.visible = false
    setBubbleVisible(false)
    setOpen(true)
  }

  const handleSend = useCallback(
    async (message: string) => {
      if (sending) return
      setError(null)
      setMessages((prev) => [...prev, { id: nextId(), role: 'user', text: message }])
      setSending(true)
      try {
        const answer = await askFundChat(fundCode, message, sessionIdRef.current)
        sessionIdRef.current = answer.sessionId
        setMessages((prev) => [...prev, { id: nextId(), role: 'assistant', text: answer.reply }])
      } catch (err) {
        const apiError = getApiError(err)
        setError(
          apiError.status === 404
            ? t.fundChatNotAvailable
            : apiError.status === 0
              ? t.fundChatError
              : apiError.message
        )
      } finally {
        setSending(false)
      }
    },
    [fundCode, sending, t.fundChatError, t.fundChatNotAvailable]
  )

  const handleReset = useCallback(() => {
    const previousSession = sessionIdRef.current
    sessionIdRef.current = null
    setMessages([{ id: 'greeting', role: 'assistant', text: t.fundChatGreeting }])
    setError(null)
    if (previousSession) {
      resetFundChat(fundCode, previousSession).catch(() => {})
    }
  }, [fundCode, t.fundChatGreeting])

  return (
    <div className="relative flex shrink-0 items-center">
      <button
        onClick={openChat}
        aria-label={t.fundChatOpen}
        aria-haspopup="dialog"
        title={t.fundChatOpen}
        className="h-14 w-14 shrink-0 overflow-hidden rounded-2xl shadow-sm ring-1 ring-slate-200 transition-all hover:-translate-y-0.5 hover:shadow-md hover:ring-[#c89834] outline-none"
      >
        <FundChatLogo className="h-full w-full" />
      </button>

      {/* ---------- TANITIM BALONCUĞU ---------- */}
      <div
        aria-hidden={!bubbleVisible}
        className="absolute right-0 top-full z-30 mt-2 sm:right-full sm:top-1/2 sm:mt-0 sm:mr-3 sm:-translate-y-1/2"
      >
        <button
          type="button"
          tabIndex={-1}
          onClick={openChat}
          className={`relative block max-w-[60vw] rounded-2xl bg-[#12161F] px-3.5 py-2 text-left text-xs font-medium text-white shadow-lg origin-top-right transition-all duration-300 sm:max-w-none sm:origin-right sm:whitespace-nowrap ${
            bubbleVisible ? 'opacity-100 scale-100' : 'pointer-events-none opacity-0 scale-95'
          }`}
        >
          {t.fundChatBubble}
          <span className="hidden sm:block absolute left-full top-1/2 -translate-y-1/2 border-[5px] border-transparent border-l-[#12161F]" />
        </button>
      </div>

      {open && (
        <FundChatModal
          messages={messages}
          sending={sending}
          error={error}
          onSend={handleSend}
          onReset={handleReset}
          onClose={() => setOpen(false)}
        />
      )}
    </div>
  )
}
