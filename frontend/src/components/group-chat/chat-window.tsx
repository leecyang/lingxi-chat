'use client';

import { useEffect, useRef, useState } from 'react';
import type { Message } from '@/lib/chat-types';
import ChatMessage from './chat-message';
import { ScrollArea } from '@/components/ui/scroll-area';
import { MessageSquare, ChevronDown } from 'lucide-react';

interface ChatWindowProps {
  messages: Message[];
  isGenerating?: boolean;
}

export default function ChatWindow({ messages, isGenerating = false }: ChatWindowProps) {
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const scrollAreaRef = useRef<HTMLDivElement>(null);
  const [isUserScrolling, setIsUserScrolling] = useState(false);
  const [shouldAutoScroll, setShouldAutoScroll] = useState(true);
  const [showScrollButton, setShowScrollButton] = useState(false);

  // 检测用户是否在手动滚动
  useEffect(() => {
    const scrollContainer = scrollAreaRef.current?.querySelector('[data-radix-scroll-area-viewport]');
    if (!scrollContainer) return;

    let scrollTimeout: NodeJS.Timeout;
    
    const handleScroll = () => {
      setIsUserScrolling(true);
      clearTimeout(scrollTimeout);
      
      // 检查是否滚动到底部附近（容差30px）
      const { scrollTop, scrollHeight, clientHeight } = scrollContainer;
      const isNearBottom = scrollHeight - scrollTop - clientHeight < 30;
      setShouldAutoScroll(isNearBottom);
      setShowScrollButton(!isNearBottom && messages.length > 0);
      
      scrollTimeout = setTimeout(() => {
        setIsUserScrolling(false);
      }, 150);
    };

    scrollContainer.addEventListener('scroll', handleScroll, { passive: true });
    return () => {
      scrollContainer.removeEventListener('scroll', handleScroll);
      clearTimeout(scrollTimeout);
    };
  }, [messages.length]);

  // 自动滚动到底部
  const scrollToBottom = (smooth = true) => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ 
        behavior: smooth ? 'smooth' : 'auto',
        block: 'end'
      });
    }
  };

  // 当有新消息时自动滚动
  useEffect(() => {
    if (shouldAutoScroll) {
      // 使用 requestAnimationFrame 确保 DOM 更新完成后再滚动
      requestAnimationFrame(() => {
        scrollToBottom(!isUserScrolling);
      });
    }
  }, [messages, isGenerating, shouldAutoScroll, isUserScrolling]);

  // 当窗口大小改变时重新滚动到底部
  useEffect(() => {
    const handleResize = () => {
      if (shouldAutoScroll) {
        setTimeout(() => scrollToBottom(false), 100);
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [shouldAutoScroll]);

  const handleScrollToBottom = () => {
    setShouldAutoScroll(true);
    setShowScrollButton(false);
    scrollToBottom(true);
  };

  return (
    <div className="flex-1 flex flex-col min-h-0 relative">
      <ScrollArea ref={scrollAreaRef} className="flex-1">
        <div className="min-h-full flex flex-col pb-24">
          {messages.length === 0 ? (
            <div className="flex-1 flex items-center justify-center">
              <div className="text-center max-w-md mx-auto px-6">
                <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-gradient-to-r from-blue-500 to-purple-600 flex items-center justify-center">
                  <MessageSquare className="w-8 h-8 text-white" />
                </div>
                <h3 className="text-lg font-semibold text-foreground mb-2">开始对话</h3>
                <p className="text-muted-foreground text-sm leading-relaxed">
                  向AI助手提问任何问题，或者使用 @ 符号来指定特定的智能体回复
                </p>
              </div>
            </div>
          ) : (
            <div className="flex-1 space-y-6 p-4 md:p-6">
              <div className="max-w-4xl mx-auto w-full space-y-6">
                {messages.map((message, index) => (
                  <ChatMessage 
                    key={`${message.id}-${index}`} 
                    message={message} 
                    isGenerating={isGenerating && index === messages.length - 1}
                  />
                ))}
                {isGenerating && (
                  <div className="flex items-center gap-3 px-4 py-3 rounded-lg bg-muted/30">
                    <div className="flex space-x-1">
                      <div className="w-2 h-2 bg-primary rounded-full animate-bounce [animation-delay:-0.3s]"></div>
                      <div className="w-2 h-2 bg-primary rounded-full animate-bounce [animation-delay:-0.15s]"></div>
                      <div className="w-2 h-2 bg-primary rounded-full animate-bounce"></div>
                    </div>
                    <span className="text-sm text-muted-foreground">AI 正在思考...</span>
                  </div>
                )}
              </div>
              {/* 滚动锚点 */}
              <div ref={messagesEndRef} className="h-1" />
            </div>
          )}
        </div>
      </ScrollArea>
      
      {/* 滚动到底部按钮 */}
      {showScrollButton && (
        <button
          onClick={handleScrollToBottom}
          className="absolute bottom-4 right-4 p-3 rounded-full bg-primary text-primary-foreground shadow-lg hover:bg-primary/90 transition-all duration-200 z-10 animate-in slide-in-from-bottom-2"
          aria-label="滚动到底部"
        >
          <ChevronDown className="w-5 h-5" />
        </button>
      )}
    </div>
  );
}
