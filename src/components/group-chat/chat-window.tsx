'use client';

import { useEffect, useRef } from 'react';
import type { Message } from '@/lib/chat-types';
import ChatMessage from './chat-message';
import { ScrollArea } from '@/components/ui/scroll-area';

interface ChatWindowProps {
  messages: Message[];
}

export default function ChatWindow({ messages }: ChatWindowProps) {
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages]);

  return (
    <ScrollArea className="flex-1 bg-muted/20">
        <div className="max-w-4xl mx-auto p-4">
          {messages.map((message) => (
            <ChatMessage key={message.id} message={message} />
          ))}
          <div ref={messagesEndRef} />
        </div>
    </ScrollArea>
  );
}
