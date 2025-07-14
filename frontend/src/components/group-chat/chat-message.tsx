'use client';
import type { Message } from '@/lib/chat-types';
import { cn } from '@/lib/utils';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Bot, User } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import 'highlight.js/styles/github.css';

const TypingIndicator = () => (
    <div className="flex items-center space-x-1 p-2">
      <span className="h-2 w-2 bg-muted-foreground rounded-full animate-bounce [animation-delay:-0.3s]"></span>
      <span className="h-2 w-2 bg-muted-foreground rounded-full animate-bounce [animation-delay:-0.15s]"></span>
      <span className="h-2 w-2 bg-muted-foreground rounded-full animate-bounce"></span>
    </div>
  );
  
const ChatMessageContent = ({ content, isUser }: { content: string; isUser?: boolean }) => {
    // 检查内容是否包含@提及
    const hasMentions = /@[^\s@]+/.test(content);
    
    // 如果有@提及，需要特殊处理
    if (hasMentions) {
        const parts = content.split(/(@[^\s@]+)/g);
        return (
            <div className="leading-relaxed break-words">
                <ReactMarkdown
                    remarkPlugins={[remarkGfm]}
                    rehypePlugins={[rehypeHighlight]}
                    components={{
                        // 自定义代码块样式
                        code: ({ node, inline, className, children, ...props }) => {
                            const match = /language-(\w+)/.exec(className || '');
                            return !inline && match ? (
                                <pre className="bg-gray-100 dark:bg-gray-800 rounded-lg p-4 overflow-x-auto my-2">
                                    <code className={className} {...props}>
                                        {children}
                                    </code>
                                </pre>
                            ) : (
                                <code className="bg-gray-100 dark:bg-gray-800 px-1 py-0.5 rounded text-sm" {...props}>
                                    {children}
                                </code>
                            );
                        },
                        // 自定义段落样式 - 处理@提及
                        p: ({ children }) => {
                            if (typeof children === 'string') {
                                const textParts = children.split(/(@[^\s@]+)/g);
                                return (
                                    <p className="mb-2 last:mb-0">
                                        {textParts.map((part, index) =>
                                            part.startsWith('@') ? (
                                                <span 
                                                    key={index} 
                                                    className={cn(
                                                        "font-semibold px-2 py-1 rounded-md border shadow-sm inline-block mx-1",
                                                        isUser 
                                                            ? "bg-white/20 text-white border-white/30 backdrop-blur-sm" 
                                                            : "bg-blue-500 text-white border-blue-600"
                                                    )}
                                                >
                                                    {part}
                                                </span>
                                            ) : (
                                                part
                                            )
                                        )}
                                    </p>
                                );
                            }
                            return <p className="mb-2 last:mb-0">{children}</p>;
                        },
                        // 自定义列表样式
                        ul: ({ children }) => (
                            <ul className="list-disc list-inside mb-2 space-y-1">{children}</ul>
                        ),
                        ol: ({ children }) => (
                            <ol className="list-decimal list-inside mb-2 space-y-1">{children}</ol>
                        ),
                        // 自定义链接样式
                        a: ({ href, children }) => (
                            <a href={href} className="text-blue-500 hover:text-blue-700 underline" target="_blank" rel="noopener noreferrer">
                                {children}
                            </a>
                        ),
                        // 自定义表格样式
                        table: ({ children }) => (
                            <div className="overflow-x-auto my-2">
                                <table className="min-w-full border border-gray-300 dark:border-gray-600">
                                    {children}
                                </table>
                            </div>
                        ),
                        th: ({ children }) => (
                            <th className="border border-gray-300 dark:border-gray-600 px-4 py-2 bg-gray-100 dark:bg-gray-700 font-semibold">
                                {children}
                            </th>
                        ),
                        td: ({ children }) => (
                            <td className="border border-gray-300 dark:border-gray-600 px-4 py-2">
                                {children}
                            </td>
                        ),
                        // 自定义引用块样式
                        blockquote: ({ children }) => (
                            <blockquote className="border-l-4 border-gray-300 dark:border-gray-600 pl-4 italic my-2">
                                {children}
                            </blockquote>
                        ),
                    }}
                >
                    {content}
                </ReactMarkdown>
            </div>
        );
    }
    
    // 没有@提及的情况，直接渲染markdown
    return (
        <div className="leading-relaxed break-words">
            <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                rehypePlugins={[rehypeHighlight]}
                components={{
                    // 自定义代码块样式
                    code: ({ node, inline, className, children, ...props }) => {
                        const match = /language-(\w+)/.exec(className || '');
                        return !inline && match ? (
                            <pre className="bg-gray-100 dark:bg-gray-800 rounded-lg p-4 overflow-x-auto my-2">
                                <code className={className} {...props}>
                                    {children}
                                </code>
                            </pre>
                        ) : (
                            <code className="bg-gray-100 dark:bg-gray-800 px-1 py-0.5 rounded text-sm" {...props}>
                                {children}
                            </code>
                        );
                    },
                    // 自定义段落样式
                    p: ({ children }) => (
                        <p className="mb-2 last:mb-0">{children}</p>
                    ),
                    // 自定义列表样式
                    ul: ({ children }) => (
                        <ul className="list-disc list-inside mb-2 space-y-1">{children}</ul>
                    ),
                    ol: ({ children }) => (
                        <ol className="list-decimal list-inside mb-2 space-y-1">{children}</ol>
                    ),
                    // 自定义链接样式
                    a: ({ href, children }) => (
                        <a href={href} className="text-blue-500 hover:text-blue-700 underline" target="_blank" rel="noopener noreferrer">
                            {children}
                        </a>
                    ),
                    // 自定义表格样式
                    table: ({ children }) => (
                        <div className="overflow-x-auto my-2">
                            <table className="min-w-full border border-gray-300 dark:border-gray-600">
                                {children}
                            </table>
                        </div>
                    ),
                    th: ({ children }) => (
                        <th className="border border-gray-300 dark:border-gray-600 px-4 py-2 bg-gray-100 dark:bg-gray-700 font-semibold">
                            {children}
                        </th>
                    ),
                    td: ({ children }) => (
                        <td className="border border-gray-300 dark:border-gray-600 px-4 py-2">
                            {children}
                        </td>
                    ),
                    // 自定义引用块样式
                    blockquote: ({ children }) => (
                        <blockquote className="border-l-4 border-gray-300 dark:border-gray-600 pl-4 italic my-2">
                            {children}
                        </blockquote>
                    ),
                }}
            >
                {content}
            </ReactMarkdown>
        </div>
    );
};

export default function ChatMessage({ message, isGenerating = false }: { message: Message; isGenerating?: boolean }) {
  const isUser = message.sender === 'user';
  const isSystem = message.sender === 'system';
  
  // 获取发送者信息
  const getSenderInfo = () => {
    if (isUser) return { name: '你', avatar: null };
    if (isSystem) return { name: '系统', avatar: null };
    
    if (typeof message.sender === 'object' && message.sender !== null) {
      // Agent对象
      return {
        name: message.sender.displayName || message.sender.name,
        avatar: message.sender.avatar
      };
    }
    
    // 字符串类型的sender
    return { name: message.sender || 'AI助手', avatar: null };
  };
  
  const senderInfo = getSenderInfo();

  return (
    <div className={cn(
      "flex gap-4 group",
      isUser ? "flex-row-reverse" : "flex-row"
    )}>
      {/* Avatar */}
      <div className="flex-shrink-0">
        {isUser ? (
          <div className="w-8 h-8 rounded-full bg-gradient-to-r from-green-500 to-emerald-600 flex items-center justify-center">
            <User className="w-4 h-4 text-white" />
          </div>
        ) : isSystem ? (
          <div className="w-8 h-8 rounded-full bg-gradient-to-r from-gray-500 to-gray-600 flex items-center justify-center">
            <Bot className="w-4 h-4 text-white" />
          </div>
        ) : senderInfo.avatar ? (
          <img src={senderInfo.avatar} alt={senderInfo.name} className="w-8 h-8 rounded-full" />
        ) : (
          <div className="w-8 h-8 rounded-full bg-gradient-to-r from-blue-500 to-purple-600 flex items-center justify-center">
            <Bot className="w-4 h-4 text-white" />
          </div>
        )}
      </div>

      {/* Message content */}
      <div className="flex-1 min-w-0">
        {/* Sender name */}
        <div className={cn(
          "flex items-center gap-2 mb-1",
          isUser ? "justify-end" : "justify-start"
        )}>
          <span className="text-sm font-medium text-foreground">
            {senderInfo.name}
          </span>
          <span className="text-xs text-muted-foreground">
            {message.timestamp}
          </span>
        </div>

        {/* Message bubble */}
        <div className={cn(
          "rounded-2xl px-4 py-3 max-w-none",
          isUser 
            ? "bg-primary text-primary-foreground ml-0" 
            : isSystem
            ? "bg-yellow-100 text-yellow-800 border border-yellow-200"
            : "bg-muted/50 text-foreground"
        )}>
          {message.isTyping ? (
            <TypingIndicator />
          ) : isGenerating && !isUser ? (
            <div className="flex items-center space-x-2">
              <TypingIndicator />
              <span className="text-sm text-muted-foreground">正在生成回复...</span>
            </div>
          ) : (
            <ChatMessageContent content={message.content} isUser={isUser} />
          )}
        </div>
      </div>
    </div>
  );
}
