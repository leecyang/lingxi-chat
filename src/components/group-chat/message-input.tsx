'use client';

import { useState } from 'react';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { Send, CornerDownLeft } from 'lucide-react';

interface MessageInputProps {
  onSendMessage: (message: string) => void;
  isLoading: boolean;
}

export default function MessageInput({ onSendMessage, isLoading }: MessageInputProps) {
  const [input, setInput] = useState('');

  const handleSend = () => {
    if (input.trim() && !isLoading) {
      onSendMessage(input.trim());
      setInput('');
    }
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="w-full p-4 border-t bg-background/80 backdrop-blur-sm">
      <div className="relative max-w-4xl mx-auto">
        <Textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="输入 @智能体 + 问题，按 Enter 发送..."
          className="pr-24 min-h-[52px] resize-none"
          rows={1}
          disabled={isLoading}
        />
        <div className="absolute bottom-3 right-3 flex items-center gap-2">
            <p className="text-xs text-muted-foreground hidden sm:block">
              <kbd className="pointer-events-none inline-flex h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground opacity-100">
                <span className="text-xs">Shift +</span><CornerDownLeft className="h-3 w-3" />
              </kbd>
               换行
            </p>
            <Button type="submit" size="icon" onClick={handleSend} disabled={isLoading || !input.trim()}>
              <Send className="h-5 w-5" />
              <span className="sr-only">发送</span>
            </Button>
        </div>
      </div>
    </div>
  );
}
