'use client';

import { useState, useEffect } from 'react';
import { MessageSquare } from 'lucide-react';
import ProtectedRoute from '@/components/auth/protected-route';
import ChatWindow from '@/components/group-chat/chat-window';
import MessageInput from '@/components/group-chat/message-input';
import type { Message } from '@/lib/chat-types';
import type { Agent } from '@/lib/agents';
import { format } from 'date-fns';
import { chatService, agentService } from '@/services/api-service';
import { useSidebar } from '@/contexts/sidebar-context';


export default function GroupChatPage() {
  const { isCollapsed: isLeftSidebarCollapsed } = useSidebar();
  
  // 调试输出
  console.log('Left sidebar collapsed:', isLeftSidebarCollapsed);
  
  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [agents, setAgents] = useState<Agent[]>([]);
  const [sessionId, setSessionId] = useState<string>('');
  const [chatSessions, setChatSessions] = useState<{[key: string]: Message[]}>({});
  const [currentSessionId, setCurrentSessionId] = useState<string>('');

  useEffect(() => {
    // 生成或获取sessionId
    const existingSessionId = localStorage.getItem('chatSessionId');
    if (existingSessionId) {
      setSessionId(existingSessionId);
      setCurrentSessionId(existingSessionId);
    } else {
      const newSessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      setSessionId(newSessionId);
      setCurrentSessionId(newSessionId);
      localStorage.setItem('chatSessionId', newSessionId);
    }
    
    // 加载保存的聊天会话
    const savedSessions = localStorage.getItem('chatSessions');
    if (savedSessions) {
      setChatSessions(JSON.parse(savedSessions));
    }
    
    // 初始化时获取智能体数据
    fetchActiveAgents();
    // 从后端获取历史消息
    fetchChatHistory();
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

  const fetchChatHistory = async () => {
    if (!sessionId) return;
    
    try {
      const response = await chatService.getChatHistory(sessionId);
      if (response.success && response.data) {
        // 后端返回的数据结构是 { history: ChatHistory[], total: number, ... }
        // 需要将 ChatHistory 转换为前端的 Message 格式
        const chatHistories = response.data.history || [];
        const historyMessages: Message[] = chatHistories.map((chat: any) => {
          // 安全地处理时间格式化
          const createdAtDate = chat.createdAt ? new Date(chat.createdAt) : new Date();
          const timestamp = isNaN(createdAtDate.getTime()) ? format(new Date(), 'HH:mm') : format(createdAtDate, 'HH:mm');
          
          return {
            id: chat.id.toString(),
            sender: chat.messageType === 'USER' ? 'user' : (chat.agent?.name || 'assistant'),
            content: chat.content,
            timestamp,
          };
        });
        setMessages(historyMessages);
        // 保存到本地会话
        setChatSessions(prev => {
          const updated = { ...prev, [sessionId]: historyMessages };
          localStorage.setItem('chatSessions', JSON.stringify(updated));
          return updated;
        });
      }
    } catch (error) {
      // 如果获取历史记录失败，设置为空数组确保 messages 是数组
      setMessages([]);
    }
  };

  // 解析消息中的@信息
  const parseAtMentions = (content: string): { mentionedAgent: Agent | null; cleanContent: string } => {
    const atMatch = content.match(/@([^\s@]+)/);
    if (atMatch) {
      const mentionedName = atMatch[1];
      const mentionedAgent = agents.find(agent => 
        agent.displayName === mentionedName || agent.name === mentionedName
      );
      const cleanContent = content.replace(/@([^\s@]+)/, '').trim();
      return {
        mentionedAgent: mentionedAgent || null,
        cleanContent
      };
    }
    return { mentionedAgent: null, cleanContent: content };
  };

  // 选择智能体进行回复
  const selectAgentForReply = (content: string, lastMessage?: Message): Agent | string => {
    const { mentionedAgent } = parseAtMentions(content);
    
    if (mentionedAgent) {
      return mentionedAgent;
    }
    
    // 检查上一条消息是否有@信息，如果有则继续使用该智能体
    if (lastMessage && lastMessage.sender !== 'user') {
      const { mentionedAgent: lastMentionedAgent } = parseAtMentions(lastMessage.content);
      if (lastMentionedAgent) {
        return lastMentionedAgent;
      }
    }
    
    // 默认使用灵犀智能体
    // 优先通过名称查找灵犀智能体（更可靠）
    const defaultAgent = agents.find(agent => 
      agent.name === 'lingxi' || 
      agent.displayName === '灵犀' ||
      agent.displayName.includes('灵犀') ||
      agent.name.toLowerCase().includes('lingxi')
    );
    
    // 如果找到了灵犀智能体，直接返回
    if (defaultAgent) {
      return defaultAgent;
    }
    
    // 如果没有找到灵犀智能体，检查是否有配置了original_id的智能体
    const agentWithOriginalId = agents.find(agent => 
      agent.config && agent.config['original_id'] === '6867fda14c78b04e5ad1b603'
    );
    if (agentWithOriginalId) {
      return agentWithOriginalId;
    }
    
    // 最后的备选方案：使用第一个可用的智能体或返回默认名称
    return agents[0] || '灵犀';
  };

  // 清理聊天
  const handleClearChat = () => {
    const newSessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    // 保存当前会话到历史记录
    if (messages.length > 0) {
      setChatSessions(prev => {
        const updated = { ...prev, [sessionId]: messages };
        localStorage.setItem('chatSessions', JSON.stringify(updated));
        return updated;
      });
    }
    
    // 重置状态
    setMessages([]);
    setSessionId(newSessionId);
    setCurrentSessionId(newSessionId);
    localStorage.setItem('chatSessionId', newSessionId);
  };
  
  // 切换到历史会话
  const handleSwitchToSession = (sessionKey: string) => {
    // 保存当前会话
    if (messages.length > 0) {
      setChatSessions(prev => {
        const updated = { ...prev, [sessionId]: messages };
        localStorage.setItem('chatSessions', JSON.stringify(updated));
        return updated;
      });
    }
    
    // 切换到选中的会话
    setSessionId(sessionKey);
    setCurrentSessionId(sessionKey);
    setMessages(chatSessions[sessionKey] || []);
    localStorage.setItem('chatSessionId', sessionKey);
  };

  const handleSendMessage = async (content: string) => {
    const userMessage: Message = {
      id: `user-${Date.now()}`,
      sender: 'user',
      content,
      timestamp: format(new Date(), 'HH:mm'),
    };
    const updatedMessages = [...messages, userMessage];
    setMessages(updatedMessages);
    
    // 保存到本地会话，并更新会话的最后更新时间
    const updatedSession = {
      id: sessionId,
      messages: updatedMessages,
      lastUpdated: new Date()
    };
    
    setChatSessions(prev => {
      const updated = { ...prev, [sessionId]: updatedSession };
      localStorage.setItem('chatSessions', JSON.stringify(updated));
      return updated;
    });
    
    setIsLoading(true);

    try {
      // 选择要使用的智能体
      const lastMessage = messages[messages.length - 1];
      const selectedAgent = selectAgentForReply(content, lastMessage);
      
      // 发送消息到后端，包含选中的智能体信息
      const agentId = typeof selectedAgent === 'object' && selectedAgent !== null ? selectedAgent.id : null;
      const { cleanContent } = parseAtMentions(content);
      
      // 添加智能体响应占位符
      const agentMessageId = `agent-${Date.now()}`;
      const agentMessage: Message = {
        id: agentMessageId,
        sender: typeof selectedAgent === 'object' && selectedAgent !== null ? selectedAgent : 'assistant',
        content: '',
        timestamp: format(new Date(), 'HH:mm'),
      };
      const messagesWithAgent = [...updatedMessages, agentMessage];
      setMessages(messagesWithAgent);
      
      // 使用流式API
      await chatService.sendMessageStream(
        sessionId,
        cleanContent,
        agentId,
        // onChunk: 处理流式数据块
        (chunk: string) => {
          setMessages(prev => {
            const updatedMessages = prev.map(msg => {
              if (msg.id === agentMessageId) {
                return { ...msg, content: msg.content + chunk };
              }
              return msg;
            });
            return updatedMessages;
          });
        },
        // onComplete: 处理完成
        (response: any) => {
          // 最终保存完整的消息状态
          setMessages(prev => {
            // 保存到本地会话
            setChatSessions(prevSessions => {
              const updatedSession = {
                id: sessionId,
                messages: prev,
                lastUpdated: new Date()
              };
              const updated = { ...prevSessions, [sessionId]: updatedSession };
              localStorage.setItem('chatSessions', JSON.stringify(updated));
              return updated;
            });
            return prev;
          });
        },
        // onError: 处理错误
        (error: string) => {
          // 更新错误消息
          setMessages(prev => {
            const updatedMessages = prev.map(msg => 
              msg.id === agentMessageId 
                ? { ...msg, content: `响应失败: ${error}` }
                : msg
            );
            // 保存错误状态到本地会话
            setChatSessions(prevSessions => {
              const updatedSession = {
                id: sessionId,
                messages: updatedMessages,
                lastUpdated: new Date()
              };
              const updated = { ...prevSessions, [sessionId]: updatedSession };
              localStorage.setItem('chatSessions', JSON.stringify(updated));
              return updated;
            });
            return updatedMessages;
          });
        }
      );
    } catch (error) {
      // 发送失败时，可以添加错误消息
      const errorMessage: Message = {
        id: `error-${Date.now()}`,
        sender: 'system',
        content: '消息发送失败，请重试',
        timestamp: format(new Date(), 'HH:mm'),
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  // 处理智能体之间的对话
  const handleAgentReply = async (triggerContent: string, targetAgent: Agent) => {
    setIsLoading(true);
    
    try {
      // 模拟智能体回复（实际应该调用对应的智能体API）
      const response = await chatService.sendMessage(`回复: ${triggerContent}`);
      if (response.success && response.data && response.data.chatHistory) {
        const chatHistory = response.data.chatHistory;
        // 安全地处理时间格式化
        const createdAtDate = chatHistory.createdAt ? new Date(chatHistory.createdAt) : new Date();
        const timestamp = isNaN(createdAtDate.getTime()) ? format(new Date(), 'HH:mm') : format(createdAtDate, 'HH:mm');
        
        const agentMessage: Message = {
          id: `agent-reply-${Date.now()}`,
          sender: targetAgent,
          content: chatHistory.content,
          timestamp,
        };
        setMessages(prev => [...prev, agentMessage]);
      }
    } catch (error) {
    } finally {
      setIsLoading(false);
    }
  };

  // 侧边栏状态管理
  const [sidebarWidth, setSidebarWidth] = useState(320);
  const [isResizing, setIsResizing] = useState(false);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  
  // 检测移动端
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
      if (window.innerWidth < 768) {
        setIsSidebarCollapsed(true);
      }
    };
    
    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);
  
  // 处理侧边栏拖拽
  const handleMouseDown = (e: React.MouseEvent) => {
    if (isMobile) return;
    setIsResizing(true);
    e.preventDefault();
  };
  
  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!isResizing || isMobile) return;
      
      const newWidth = window.innerWidth - e.clientX;
      if (newWidth >= 250 && newWidth <= 500) {
        setSidebarWidth(newWidth);
      }
    };
    
    const handleMouseUp = () => {
      setIsResizing(false);
    };
    
    if (isResizing) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = 'col-resize';
      document.body.style.userSelect = 'none';
    }
    
    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };
  }, [isResizing, isMobile]);

  return (
    <div className="flex flex-col h-screen bg-gradient-to-br from-background via-background to-muted/20">
        {/* Header */}
        <div className="flex-shrink-0 border-b border-border/40 bg-background/80 backdrop-blur-sm z-10">
          <div className="px-4 md:px-6 py-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-gradient-to-r from-blue-500 to-purple-600 flex items-center justify-center">
                  <MessageSquare className="w-4 h-4 text-white" />
                </div>
                <h1 className="text-xl font-semibold text-foreground">AI 对话</h1>
              </div>
              <div className="flex items-center gap-2">
                <div className="text-sm text-muted-foreground hidden sm:block">
                  {messages.length > 0 && `${messages.length} 条消息`}
                </div>
                {/* 移动端侧边栏切换按钮 */}
                <button
                  onClick={() => setIsSidebarCollapsed(!isSidebarCollapsed)}
                  className="md:hidden p-2 rounded-lg hover:bg-muted/50 transition-colors"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Main content area */}
        <div className="flex-1 flex min-h-0 relative" style={{ paddingRight: !isSidebarCollapsed && !isMobile ? `${sidebarWidth + 4}px` : '0' }}>
          {/* Chat area */}
          <div className="flex-1 flex flex-col min-h-0">
            {/* Messages area with proper scrolling */}
            <div className="flex-1 min-h-0">
              <ChatWindow messages={messages} isGenerating={isLoading} />
            </div>
          </div>
        </div>
        
        {/* Fixed Sidebar */}
        {!isSidebarCollapsed && (
          <>
            {/* Resize handle */}
            {!isMobile && (
              <div
                className="fixed top-16 bottom-0 z-10 w-1 bg-border/40 hover:bg-border cursor-col-resize transition-colors group"
                style={{ right: `${sidebarWidth}px` }}
                onMouseDown={handleMouseDown}
              >
                <div className="absolute inset-y-0 -left-1 -right-1 group-hover:bg-primary/20 transition-colors" />
              </div>
            )}
            
            {/* Sidebar */}
            <div 
              className={`
                fixed top-16 bottom-0 right-0 z-20
                ${isMobile 
                  ? 'w-80 shadow-xl' 
                  : 'flex-shrink-0'
                }
                bg-background/95 backdrop-blur-sm border-l border-border/40 flex flex-col
              `}
              style={!isMobile ? { width: `${sidebarWidth}px` } : {}}
            >
                {/* Sidebar header */}
                <div className="p-4 border-b border-border/40 flex items-center justify-between">
                  <div>
                    <h3 className="font-semibold text-foreground">聊天会话</h3>
                    <p className="text-sm text-muted-foreground mt-1">历史对话记录</p>
                  </div>
                  <button
                    onClick={() => setIsSidebarCollapsed(true)}
                    className="p-1.5 rounded-lg hover:bg-muted/50 transition-colors"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
                
                {/* Sidebar content */}
                <div className="flex-1 overflow-y-auto p-4 space-y-3">
                  {/* 当前会话 */}
                  {messages.length > 0 && (
                    <div className="p-3 rounded-lg bg-primary/10 border border-primary/20">
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-xs font-medium text-primary">当前会话</span>
                        <span className="text-xs text-muted-foreground">{messages.length} 条消息</span>
                      </div>
                      <p className="text-sm text-muted-foreground line-clamp-2">
                        {messages[messages.length - 1]?.content.length > 50 
                          ? messages[messages.length - 1]?.content.substring(0, 50) + '...' 
                          : messages[messages.length - 1]?.content}
                      </p>
                    </div>
                  )}
                  
                  {/* 历史会话 */}
                  {Object.entries(chatSessions)
                    .filter(([sessionKey]) => sessionKey !== currentSessionId)
                    .sort(([,a], [,b]) => {
                      const aTime = a[a.length - 1]?.timestamp || '';
                      const bTime = b[b.length - 1]?.timestamp || '';
                      return bTime.localeCompare(aTime);
                    })
                    .slice(0, 10)
                    .map(([sessionKey, sessionMessages]) => (
                      <div 
                        key={sessionKey} 
                        className="p-3 rounded-lg bg-muted/30 hover:bg-muted/50 transition-colors cursor-pointer"
                        onClick={() => {
                          handleSwitchToSession(sessionKey);
                          if (isMobile) setIsSidebarCollapsed(true);
                        }}
                      >
                        <div className="flex items-center gap-2 mb-1">
                          <span className="text-xs font-medium text-foreground">会话</span>
                          <span className="text-xs text-muted-foreground">{sessionMessages.length} 条消息</span>
                        </div>
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {sessionMessages[sessionMessages.length - 1]?.content.length > 50 
                            ? sessionMessages[sessionMessages.length - 1]?.content.substring(0, 50) + '...' 
                            : sessionMessages[sessionMessages.length - 1]?.content}
                        </p>
                      </div>
                    ))
                  }
                  
                  {Object.keys(chatSessions).length === 0 && messages.length === 0 && (
                    <div className="text-center text-muted-foreground text-sm">
                      暂无对话记录
                    </div>
                  )}
                </div>
              </div>
            </>
          )}
          
          {/* 移动端遮罩层 */}
          {isMobile && !isSidebarCollapsed && (
            <div 
              className="absolute inset-0 bg-black/20 backdrop-blur-sm z-10"
              onClick={() => setIsSidebarCollapsed(true)}
            />
          )}
        
        {/* 桌面端侧边栏展开按钮 */}
        {isSidebarCollapsed && !isMobile && (
          <button
            onClick={() => setIsSidebarCollapsed(false)}
            className="fixed top-1/2 right-4 -translate-y-1/2 p-2 rounded-lg bg-background/80 backdrop-blur-sm border border-border/40 hover:bg-muted/50 transition-colors z-10 shadow-lg"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
        )}
        
        {/* Fixed Input area at bottom */}
        <div 
          className="fixed bottom-0 left-0 right-0 z-30 border-t border-border/40 bg-background/95 backdrop-blur-sm transition-all duration-300 ease-in-out"
          style={{
            marginLeft: isMobile ? '0px' : (isLeftSidebarCollapsed ? '64px' : '256px'),
            marginRight: !isSidebarCollapsed && !isMobile ? `${sidebarWidth}px` : '0px'
          }}
        >
          <MessageInput onSendMessage={handleSendMessage} isLoading={isLoading} onClearChat={handleClearChat} />
        </div>
      </div>
  );
}
