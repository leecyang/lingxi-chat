'use client';

import { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { MessageSquare, ArrowLeft, User, Bot } from 'lucide-react';
import Link from 'next/link';

interface Message {
  id: string;
  content: string;
  sender: 'user' | 'ai';
  agentName?: string;
  agentCategory?: string;
  timestamp: string;
}

const messages: Message[] = [
  {
    id: '1',
    content: '我想参加第二课堂活动，提升综合素质，也希望能结识志同道合的朋友，有什么推荐？',
    sender: 'user',
    timestamp: '2024-01-15 19:30:00'
  },
  {
    id: '2',
    content: '检测到您有素质提升与社交拓展双重需求。@素质评估智能体，请对用户进行兴趣偏好问卷分析；@智慧二课荐智能体，推荐个性化第二课堂活动并推送报名入口。',
    sender: 'ai',
    agentName: '灵犀',
    agentCategory: '统筹',
    timestamp: '2024-01-15 19:30:05'
  },
  {
    id: '3',
    content: '您好，我是@素质评估智能体。请填写兴趣问卷：\n1. 更偏好哪类活动？A. 志愿服务 B. 科技创新 C. 文体艺术\n2. 每周可参与活动时长？\n3. 是否希望参与团队协作类项目？（请回复1-3题答案）',
    sender: 'ai',
    agentName: '素质评估智能体',
    agentCategory: '成长',
    timestamp: '2024-01-15 19:30:10'
  },
  {
    id: '4',
    content: '1. B、C 2. 4小时 3. 希望有团队协作',
    sender: 'user',
    timestamp: '2024-01-15 19:30:15'
  },
  {
    id: '5',
    content: '评估结果：您适合参与科技创新与文体艺术类团队活动，预计本学期综合素质积分提升10%。@智慧二课荐智能体，请为用户推送精选活动并同步报名入口。',
    sender: 'ai',
    agentName: '素质评估智能体',
    agentCategory: '成长',
    timestamp: '2024-01-15 19:30:18'
  },
  {
    id: '6',
    content: '您好，我是@智慧二课荐智能体。为您推荐以下第二课堂活动：\n- 校园科技创新挑战赛（团队协作）\n- 校合唱团体验营（艺术交流）\n- 志愿服务嘉年华（跨领域合作）\n已同步报名入口，后续将推送活动提醒与成长趋势预测。如需调整活动类型或获取更多推荐，欢迎随时告知。',
    sender: 'ai',
    agentName: '智慧二课荐智能体',
    agentCategory: '成长',
    timestamp: '2024-01-15 19:30:23'
  }
];

function processMessageContent(content: string): string {
  return content.replace(/@[\u4e00-\u9fa5a-zA-Z]+智能体/g, '');
}

function highlightMentions(content: string): string {
  return content.replace(/(@[\u4e00-\u9fa5a-zA-Z]+智能体)/g, '<span class="bg-blue-100 text-blue-800 px-1 rounded font-medium">$1</span>');
}

export default function SecondClassroomDemo() {
  return (
    <div className="flex flex-col h-screen">
      {/* Header */}
      <div className="flex-shrink-0 border-b border-border/40 bg-background/80 backdrop-blur-sm">
        <div className="px-4 md:px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Link href="/demo" className="p-2 rounded-lg hover:bg-muted/50 transition-colors">
                <ArrowLeft className="w-4 h-4" />
              </Link>
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-emerald-500 to-teal-600 flex items-center justify-center">
                <MessageSquare className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-semibold text-foreground">第二课堂活动推荐</h1>
                <p className="text-sm text-muted-foreground">演示场景 7</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((message) => (
          <div key={message.id} className={`flex gap-3 ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
            {/* AI头像 */}
            {message.sender === 'ai' && (
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-blue-500 to-purple-600 flex items-center justify-center flex-shrink-0">
                <Bot className="w-4 h-4 text-white" />
              </div>
            )}
            
            <div className={`max-w-[80%] ${message.sender === 'user' ? 'order-1' : 'order-1'}`}>
              {/* 智能体名称显示 */}
              {message.sender === 'ai' && message.agentName && (
                <div className="mb-2 flex items-center gap-2">
                  <span className="text-sm font-medium text-primary">
                    {message.agentCategory}·{message.agentName}
                  </span>
                  <span className="text-xs text-muted-foreground">{message.timestamp}</span>
                </div>
              )}
              
              {/* 消息内容 */}
              <div className={`rounded-lg px-4 py-3 ${
                message.sender === 'user'
                  ? 'bg-primary text-primary-foreground'
                  : 'bg-muted'
              }`}>
                {message.sender === 'ai' ? (
                  <div 
                    className="prose prose-sm max-w-none dark:prose-invert"
                    dangerouslySetInnerHTML={{ 
                      __html: highlightMentions(message.content)
                        .replace(/\n/g, '<br>')
                        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                    }}
                  />
                ) : (
                  <div className="prose prose-sm max-w-none dark:prose-invert text-primary-foreground">
                    <ReactMarkdown>
                      {message.content}
                    </ReactMarkdown>
                  </div>
                )}
              </div>
              
              {/* 用户消息时间戳 */}
              {message.sender === 'user' && (
                <div className="mt-1 text-xs text-muted-foreground text-right">
                  {message.timestamp}
                </div>
              )}
            </div>
            
            {/* 用户头像 */}
            {message.sender === 'user' && (
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-green-500 to-blue-500 flex items-center justify-center flex-shrink-0 order-2">
                <User className="w-4 h-4 text-white" />
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}