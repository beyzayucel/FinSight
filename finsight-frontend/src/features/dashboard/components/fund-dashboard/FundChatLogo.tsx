import { useState } from 'react'
import { IoChatbubbleEllipses } from 'react-icons/io5'

const LOGO_SRC = '/chat-logo.png'

type FundChatLogoProps = {
  className?: string
}

/** Chatbot logo açılmazsa. Yedek*/
export default function FundChatLogo({ className = '' }: FundChatLogoProps) {
  const [failed, setFailed] = useState(false)

  if (failed) {
    return (
      <span
        className={`flex items-center justify-center bg-[#12161F] text-[#7ea2ff] ${className}`}
        aria-hidden="true"
      >
        <IoChatbubbleEllipses className="h-1/2 w-1/2" />
      </span>
    )
  }

  return (
    <span className={`block overflow-hidden ${className}`} aria-hidden="true">
      <img
        src={LOGO_SRC}
        alt=""
        draggable={false}
        onError={() => setFailed(true)}
        className="h-full w-full translate-y-[2.7%] scale-[2.1] object-cover select-none"
      />
    </span>
  )
}
