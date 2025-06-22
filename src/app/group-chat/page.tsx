'use client';

import { useState } from 'react';
import ChatWindow from '@/components/group-chat/chat-window';
import MessageInput from '@/components/group-chat/message-input';
import AgentBar from '@/components/group-chat/agent-bar';
import { availableAgents } from '@/lib/agents';
import type { Message } from '@/lib/chat-types';
import { format } from 'date-fns';

const initialMessage: Message = {
  id: 'system-initial',
  sender: availableAgents.find(a => a.name === '通用知识问答')!,
  content: '大家好！我是你们的AI助教团队。有什么问题可以随时 @我 或者其他智能体。我们随时准备提供帮助！',
  timestamp: format(new Date(), 'HH:mm'),
};


export default function GroupChatPage() {
  const [messages, setMessages] = useState<Message[]>([initialMessage]);
  const [isLoading, setIsLoading] = useState(false);

  const handleSendMessage = (content: string) => {
    const userMessage: Message = {
      id: `user-${Date.now()}`,
      sender: 'user',
      content,
      timestamp: format(new Date(), 'HH:mm'),
    };
    setMessages(prev => [...prev, userMessage]);
    setIsLoading(true);

    // Mock AI response
    const mentionedAgent = availableAgents.find(agent => content.includes(`@${agent.name}`)) || 
                           availableAgents[Math.floor(Math.random() * availableAgents.length)];

    const typingMessage: Message = {
        id: `typing-${Date.now()}`,
        sender: mentionedAgent,
        content: '',
        isTyping: true,
        timestamp: format(new Date(), 'HH:mm'),
    }
    setMessages(prev => [...prev, typingMessage]);


    setTimeout(() => {
        const aiResponse: Message = {
            id: typingMessage.id, // Replace typing message with actual response
            sender: mentionedAgent,
            content: `你好！我是${mentionedAgent.name}。关于 “${content.substring(0, 20)}...” 的问题，我正在思考。如果需要更专业的解答，可以尝试 @历史故事讲解员 或者 @数学解题大师。`,
            timestamp: format(new Date(), 'HH:mm'),
        };
      
        setMessages(prev => prev.map(m => m.id === typingMessage.id ? aiResponse : m));
        setIsLoading(false);
    }, 2500);
  };

  return (
    <div className="flex flex-col h-[calc(100vh-3.5rem)] bg-background">
      {/* The header is h-14 (3.5rem), so we subtract that from viewport height */}
      <ChatWindow messages={messages} />
      <div className="flex-shrink-0">
        <AgentBar />
        <MessageInput onSendMessage={handleSendMessage} isLoading={isLoading} />
      </div>
    </div>
  );
}
