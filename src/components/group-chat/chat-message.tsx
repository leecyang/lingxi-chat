'use client';
import type { Message } from '@/lib/chat-types';
import { cn } from '@/lib/utils';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Bot, User } from 'lucide-react';

const TypingIndicator = () => (
    <div className="flex items-center space-x-1 p-2">
      <span className="h-2 w-2 bg-muted-foreground rounded-full animate-bounce [animation-delay:-0.3s]"></span>
      <span className="h-2 w-2 bg-muted-foreground rounded-full animate-bounce [animation-delay:-0.15s]"></span>
      <span className="h-2 w-2 bg-muted-foreground rounded-full animate-bounce"></span>
    </div>
  );
  
const ChatMessageContent = ({ content }: { content: string }) => {
    const parts = content.split(/(@[^\s@]+)/g);
    return (
      <p className="leading-relaxed break-words whitespace-pre-wrap">
        {parts.map((part, index) =>
          part.startsWith('@') ? (
            <span key={index} className="font-semibold text-primary bg-primary/10 px-1 py-0.5 rounded-sm">
              {part}
            </span>
          ) : (
            part
          )
        )}
      </p>
    );
};

export default function ChatMessage({ message }: { message: Message }) {
  const isUser = message.sender === 'user';
  const agent = isUser ? null : message.sender;

  return (
    <div className={cn('flex items-end gap-3 my-4', isUser ? 'justify-end' : 'justify-start')}>
      {!isUser && (
        <Avatar className="w-10 h-10 border self-start">
            <AvatarImage src={agent?.avatar} alt={agent?.name} data-ai-hint={agent?.dataAiHint} />
            <AvatarFallback>
                <Bot />
            </AvatarFallback>
        </Avatar>
      )}

      <div className={cn('max-w-md lg:max-w-xl rounded-2xl px-4 py-3 shadow-md flex flex-col', 
        isUser ? 'bg-primary text-primary-foreground rounded-br-none' : 'bg-card text-card-foreground rounded-bl-none'
      )}>
        {!isUser && (
            <p className="text-sm font-bold text-accent mb-1">{agent?.name}</p>
        )}
        
        {message.isTyping ? <TypingIndicator /> : <ChatMessageContent content={message.content} />}

        <p className={cn("text-xs mt-2 self-end", isUser ? "text-primary-foreground/70" : "text-muted-foreground/80")}>
            {message.timestamp}
        </p>
      </div>

      {isUser && (
        <Avatar className="w-10 h-10 border self-start">
            <AvatarFallback>
                <User />
            </AvatarFallback>
        </Avatar>
      )}
    </div>
  );
}
