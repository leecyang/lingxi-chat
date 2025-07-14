'use client';

import { useState, useEffect, useRef } from 'react';
import { Button } from '@/components/ui/button';
import { Send, AtSign, RotateCcw } from 'lucide-react';
import { cn } from '@/lib/utils';
import { agentService } from '@/services/api-service';
import type { Agent } from '@/lib/agents';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Badge } from '@/components/ui/badge';
import RichTextInput from './rich-text-input';

interface MessageInputProps {
  onSendMessage: (message: string) => void;
  isLoading: boolean;
  onClearChat?: () => void;
}

export default function MessageInput({ onSendMessage, isLoading, onClearChat }: MessageInputProps) {
  const [input, setInput] = useState('');
  const [agents, setAgents] = useState<Agent[]>([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    fetchActiveAgents();
  }, []);

  const fetchActiveAgents = async () => {
    try {
      const response = await agentService.getActiveAgents();
      if (response.success && response.data && Array.isArray(response.data.agents)) {
        setAgents(response.data.agents);
      }
    } catch (error) {
      // 获取智能体失败
    }
  };

  const handleSend = () => {
    if (input.trim() && !isLoading) {
      onSendMessage(input.trim());
      setInput('');
    }
  };

  const handleKeyDown = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      handleSend();
    }
  };

  const handleAgentSelect = (agent: Agent) => {
    setInput(prev => prev + `@${agent.displayName} `);
    setIsDropdownOpen(false);
    // 自动聚焦到输入框
    setTimeout(() => {
      inputRef.current?.focus();
    }, 100);
  };

  const getAgentStatus = (agent: Agent) => {
    // 简单的在线状态模拟，实际应该从后端获取
    return agent.status === 'APPROVED' && agent.enabled !== false;
  };

  return (
    <div className="w-full">
      <div className="w-full max-w-none px-4 md:px-6 py-3 md:py-4">
        <div className="max-w-4xl mx-auto">
          <div className="relative">
            <div className="absolute left-3 top-1/2 transform -translate-y-1/2 z-10">
              <DropdownMenu open={isDropdownOpen} onOpenChange={setIsDropdownOpen}>
                <DropdownMenuTrigger asChild>
                  <Button 
                    variant="ghost" 
                    size="icon" 
                    className="h-8 w-8 rounded-full hover:bg-muted/80 text-muted-foreground hover:text-foreground"
                    disabled={isLoading}
                  >
                    <AtSign className="h-4 w-4" />
                    <span className="sr-only">选择智能体</span>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent 
                  align="start" 
                  className="w-80 md:w-96 max-h-64 overflow-y-auto"
                  side="top"
                >
                  {agents.map((agent) => (
                    <DropdownMenuItem 
                      key={agent.id} 
                      onClick={() => handleAgentSelect(agent)}
                      className="flex items-center gap-3 p-3"
                    >
                      <div className="flex items-center gap-2 flex-1">
                        {agent.avatar ? (
                          <img src={agent.avatar} alt={agent.displayName} className="w-6 h-6 rounded-full" />
                        ) : (
                          <div className="w-6 h-6 bg-primary/20 rounded-full flex items-center justify-center text-primary font-semibold text-xs">
                            {agent.displayName.charAt(0)}
                          </div>
                        )}
                        <div className="flex-1 min-w-0">
                          <div className="font-medium text-sm truncate">{agent.displayName}</div>
                          <div className="text-xs text-muted-foreground truncate">{agent.description}</div>
                        </div>
                      </div>
                      <Badge 
                        variant={getAgentStatus(agent) ? "default" : "secondary"}
                        className="text-xs"
                      >
                        {getAgentStatus(agent) ? '在线' : '离线'}
                      </Badge>
                    </DropdownMenuItem>
                  ))}
                  {agents.length === 0 && (
                    <div className="p-3 text-center text-sm text-muted-foreground">
                      暂无可用智能体
                    </div>
                  )}
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
            
            <RichTextInput
              ref={inputRef}
              value={input}
              onChange={setInput}
              onKeyDown={handleKeyDown}
              placeholder="向AI助手提问..."
              disabled={isLoading}
              className="pl-14 pr-20 md:pr-24 min-h-[52px] md:min-h-[56px] max-h-[120px] resize-none border-2 border-border/60 rounded-2xl md:rounded-3xl bg-background focus:border-primary/60 focus:ring-0 text-base md:text-lg placeholder:text-muted-foreground/60 leading-relaxed"
              style={{ 
                fontSize: window.innerWidth < 768 ? '16px' : '16px', 
                lineHeight: '1.5', 
                paddingTop: window.innerWidth < 768 ? '14px' : '18px', 
                paddingBottom: window.innerWidth < 768 ? '14px' : '18px'
              }}
            />
            
            <div className="absolute bottom-2 md:bottom-3 right-2 md:right-3 flex items-center gap-1 md:gap-2">
              {onClearChat && (
                <Button 
                  variant="ghost"
                  size="icon" 
                  onClick={onClearChat}
                  disabled={isLoading}
                  className="h-8 w-8 rounded-full hover:bg-muted/80 text-muted-foreground hover:text-foreground"
                  title="清理聊天"
                >
                  <RotateCcw className="h-4 w-4" />
                  <span className="sr-only">清理聊天</span>
                </Button>
              )}
              <Button 
                type="submit" 
                size="icon" 
                onClick={handleSend} 
                disabled={isLoading || !input.trim()}
                className={cn(
                  "h-9 w-9 md:h-10 md:w-10 rounded-full transition-all duration-200",
                  input.trim() && !isLoading 
                    ? "bg-primary hover:bg-primary/90 text-primary-foreground shadow-md" 
                    : "bg-muted text-muted-foreground hover:bg-muted/80"
                )}
              >
                <Send className="h-4 w-4" />
                <span className="sr-only">发送</span>
              </Button>
            </div>
          </div>
          
          {/* Hint text */}
          <div className="flex items-center justify-between mt-2 px-2 md:px-4">
            <p className="text-xs text-muted-foreground/60 hidden sm:block">
              按 Enter 发送，Shift + Enter 换行，点击 @ 选择智能体
            </p>
            <p className="text-xs text-muted-foreground/60 sm:hidden">
              Enter 发送，@ 选择智能体
            </p>
            {isLoading && (
              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                <div className="w-3 h-3 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                <span className="hidden sm:inline">AI正在思考...</span>
                <span className="sm:hidden">思考中...</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
