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
    content: '最近时间安排很混乱，学习和娱乐总是冲突，效率低下，想要科学规划时间并提升自律力，有什么建议？',
    sender: 'user',
    timestamp: '2024-01-15 18:00:00'
  },
  {
    id: '2',
    content: '检测到您有时间管理与自律提升双重需求。@时间评估智能体，请对用户进行时间使用结构问卷分析；@时间管理智能体，制定个性化时间管理方案并推送每日打卡功能。',
    sender: 'ai',
    agentName: '灵犀',
    agentCategory: '统筹',
    timestamp: '2024-01-15 18:00:05'
  },
  {
    id: '3',
    content: '您好，我是@时间评估智能体。请填写时间使用问卷：\n1. 每天学习时长？\n2. 娱乐时长？\n3. 是否有固定作息？\n4. 最难坚持的时间段？（请回复1-4题答案）',
    sender: 'ai',
    agentName: '时间评估智能体',
    agentCategory: '成长',
    timestamp: '2024-01-15 18:00:10'
  },
  {
    id: '4',
    content: '1. 4小时 2. 3小时 3. 没有 4. 晚上21:00后容易分心',
    sender: 'user',
    timestamp: '2024-01-15 18:00:15'
  },
  {
    id: '5',
    content: '分析结果：您在夜间自律性较弱，建议尝试“番茄钟”法，分时段安排学习与娱乐，逐步建立作息习惯。预计三周后学习效率提升12%。@时间管理智能体，请为用户生成专属时间安排方案并推送打卡提醒。',
    sender: 'ai',
    agentName: '时间评估智能体',
    agentCategory: '成长',
    timestamp: '2024-01-15 18:00:18'
  },
  {
    id: '6',
    content: '您好，我是@时间管理智能体。为您定制的时间管理方案如下：\n- 7:30 起床，8:00-11:00 学习，11:00-12:00 娱乐\n- 午休后14:00-17:00 学习，17:00-18:00 娱乐\n- 晚上21:00后建议远离电子设备，21:30 前准备休息\n- 每日打卡功能已开启，系统将自动追踪执行情况并推送激励反馈\n如需调整方案或添加自定义提醒，请随时告知。',
    sender: 'ai',
    agentName: '时间管理智能体',
    agentCategory: '成长',
    timestamp: '2024-01-15 18:00:23'
  }
];

function processMessageContent(content: string): string {
  return content.replace(/@[\u4e00-\u9fa5a-zA-Z]+智能体/g, '');
}

function highlightMentions(content: string): string {
  return content.replace(/(@[\u4e00-\u9fa5a-zA-Z]+智能体)/g, '<span class="bg-blue-100 text-blue-800 px-1 rounded font-medium">$1</span>');
}

export default function TimeManagementDemo() {
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
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-indigo-500 to-purple-600 flex items-center justify-center">
                <MessageSquare className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-semibold text-foreground">时间管理问题</h1>
                <p className="text-sm text-muted-foreground">演示场景 5</p>
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