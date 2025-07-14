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
    content: '最近饮食很不规律，肠胃经常不舒服，想改善饮食习惯并获得科学建议，有什么好办法吗？',
    sender: 'user',
    timestamp: '2024-01-15 20:00:00'
  },
  {
    id: '2',
    content: '检测到您有饮食健康与肠胃调理双重需求。@健康评估智能体，请对用户进行饮食习惯问卷分析；@膳食管理智能体，制定个性化饮食方案并推送健康打卡功能。',
    sender: 'ai',
    agentName: '灵犀',
    agentCategory: '统筹',
    timestamp: '2024-01-15 20:00:05'
  },
  {
    id: '3',
    content: '您好，我是@健康评估智能体。请填写饮食习惯问卷：\n1. 每天三餐是否规律？\n2. 是否经常吃夜宵？\n3. 每周蔬果摄入频率？\n4. 是否有乳糖不耐或特殊饮食需求？（请回复1-4题答案）',
    sender: 'ai',
    agentName: '健康评估智能体',
    agentCategory: '健康',
    timestamp: '2024-01-15 20:00:10'
  },
  {
    id: '4',
    content: '1. 不规律 2. 偶尔 3. 2次 4. 无特殊需求',
    sender: 'user',
    timestamp: '2024-01-15 20:00:15'
  },
  {
    id: '5',
    content: '评估结果：饮食不规律、蔬果摄入偏少，建议规律三餐、减少夜宵、增加蔬果摄入。预计三周后肠胃舒适度提升15%。@膳食管理智能体，请为用户生成饮食健康方案并同步健康打卡功能。',
    sender: 'ai',
    agentName: '健康评估智能体',
    agentCategory: '健康',
    timestamp: '2024-01-15 20:00:18'
  },
  {
    id: '6',
    content: '您好，我是@膳食管理智能体。为您定制的饮食健康方案如下：\n- 早餐：7:30前，主食+蛋白+水果\n- 午餐：多样化搭配，减少油炸食品\n- 晚餐：清淡为主，避免过饱\n- 每日蔬果不少于300g\n- 健康饮食打卡功能已开启，系统将自动追踪饮食习惯并推送激励反馈\n如需调整方案或添加特殊饮食建议，请随时告知。',
    sender: 'ai',
    agentName: '膳食管理智能体',
    agentCategory: '健康',
    timestamp: '2024-01-15 20:00:23'
  }
];

function processMessageContent(content: string): string {
  return content.replace(/@[\u4e00-\u9fa5a-zA-Z]+智能体/g, '');
}

function highlightMentions(content: string): string {
  return content.replace(/(@[\u4e00-\u9fa5a-zA-Z]+智能体)/g, '<span class="bg-blue-100 text-blue-800 px-1 rounded font-medium">$1</span>');
}

export default function DietHealthDemo() {
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
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-lime-500 to-green-600 flex items-center justify-center">
                <MessageSquare className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-semibold text-foreground">饮食不规律</h1>
                <p className="text-sm text-muted-foreground">演示场景 9</p>
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