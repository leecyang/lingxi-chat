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
    content: '最近学习压力很大，晚上总是睡不着，感觉焦虑，甚至有时会心跳加速，希望能获得专业的评估和调适建议。',
    sender: 'user',
    timestamp: '2024-01-15 14:30:00'
  },
  {
    id: '2',
    content: '检测到您存在明显的心理压力和睡眠障碍风险。@心理评估智能体，请立即对用户进行压力指数评估；@青藤心桥智能体，准备个性化情绪调适方案，并提供正念冥想资源。',
    sender: 'ai',
    agentName: '灵犀',
    agentCategory: '统筹',
    timestamp: '2024-01-15 14:30:05'
  },
  {
    id: '3',
    content: '您好，我是@心理评估智能体。请您完成以下压力自测问卷：\n1. 近两周是否频繁感到紧张、心慌或注意力难以集中？\n2. 晚上入睡平均需要多长时间？\n3. 是否出现过因压力导致的身体不适（如头痛、胃痛等）？\n4. 您希望获得哪类帮助？A. 情绪疏导 B. 睡眠改善 C. 压力管理训练 D. 预约咨询\n（请回复1-4题答案）',
    sender: 'ai',
    agentName: '心理评估智能体',
    agentCategory: '德育',
    timestamp: '2024-01-15 14:30:10'
  },
  {
    id: '4',
    content: '1. 是 2. 超过30分钟 3. 有 4. A、B、D',
    sender: 'user',
    timestamp: '2024-01-15 14:30:15'
  },
  {
    id: '5',
    content: '评估结果：您的压力指数为78（偏高），睡眠障碍风险较大，预计通过干预可下降20%。建议立即开展情绪疏导与睡眠改善训练。@青藤心桥智能体，请为用户生成专属调适方案，并推送正念冥想音频。',
    sender: 'ai',
    agentName: '心理评估智能体',
    agentCategory: '德育',
    timestamp: '2024-01-15 14:30:18'
  },
  {
    id: '6',
    content: '您好，我是@青藤心桥智能体。为您定制如下方案：\n- 每晚睡前进行10分钟正念冥想（已推送音频链接）\n- 记录每日情绪波动，系统将自动分析压力源并提供反馈\n- 推荐参加“情绪调节工作坊”线上课程，每周三晚开放\n- 已为您预约下周一15:00线上心理疏导\n预计三周后压力指数下降20%，如需调整或添加提醒，请随时告知。',
    sender: 'ai',
    agentName: '青藤心桥智能体',
    agentCategory: '德育',
    timestamp: '2024-01-15 14:30:23'
  },
  {
    id: '7',
    content: '谢谢，能否调整预约时间到周二？',
    sender: 'user',
    timestamp: '2024-01-15 14:30:28'
  },
  {
    id: '8',
    content: '预约已调整至周二15:00，系统将发送提醒。持续跟踪您的情绪变化，如有其他需求可随时告知。',
    sender: 'ai',
    agentName: '青藤心桥智能体',
    agentCategory: '德育',
    timestamp: '2024-01-15 14:30:33'
  }
];

function processMessageContent(content: string): string {
  // 移除 @智能体名称 的显示
  return content.replace(/@[\u4e00-\u9fa5a-zA-Z]+智能体/g, '');
}

function highlightMentions(content: string): string {
  // 高亮 @智能体名称
  return content.replace(/(@[\u4e00-\u9fa5a-zA-Z]+智能体)/g, '<span class="bg-blue-100 text-blue-800 px-1 rounded font-medium">$1</span>');
}

export default function PsychologyDemo() {
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
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-green-500 to-blue-600 flex items-center justify-center">
                <MessageSquare className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-semibold text-foreground">心理压力咨询</h1>
                <p className="text-sm text-muted-foreground">演示场景 1</p>
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