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
    content: '我最近对吉他和画画感兴趣，但不知道如何系统提升，也想结识同好，有什么建议或活动推荐？',
    sender: 'user',
    timestamp: '2024-01-15 17:00:00'
  },
  {
    id: '2',
    content: '检测到您有艺术素养提升与社交拓展双重需求。@兴趣评估智能体，请对用户进行兴趣倾向问卷分析；@养趣工坊智能体，推荐个性化课程与活动并推送报名入口。',
    sender: 'ai',
    agentName: '灵犀',
    agentCategory: '统筹',
    timestamp: '2024-01-15 17:00:05'
  },
  {
    id: '3',
    content: '您好，我是@兴趣评估智能体。请填写兴趣问卷：\n1. 您更喜欢哪类艺术活动？A. 乐器演奏 B. 绘画创作 C. 摄影采风\n2. 是否有过相关基础？\n3. 希望每周参与几次艺术活动？（请回复1-3题答案）',
    sender: 'ai',
    agentName: '兴趣评估智能体',
    agentCategory: '美',
    timestamp: '2024-01-15 17:00:10'
  },
  {
    id: '4',
    content: '1. A、B 2. 有一点吉他基础，绘画零基础 3. 2次',
    sender: 'user',
    timestamp: '2024-01-15 17:00:15'
  },
  {
    id: '5',
    content: '评估结果：您偏好乐器演奏和绘画，适合初阶提升。预计本学期艺术素养提升8%。@养趣工坊智能体，请为用户推送吉他和绘画相关课程、活动及同好社群。',
    sender: 'ai',
    agentName: '兴趣评估智能体',
    agentCategory: '美',
    timestamp: '2024-01-15 17:00:18'
  },
  {
    id: '6',
    content: '您好，我是@养趣工坊智能体。为您推荐：\n- 吉他初阶体验课（每周二晚，含乐队合奏体验）\n- “色彩与情绪”水彩班（周六上午，零基础友好）\n- 校园艺术展评报名通道已开放\n- 艺术兴趣交流群已邀请加入\n是否需要报名课程或预约试听？',
    sender: 'ai',
    agentName: '养趣工坊智能体',
    agentCategory: '美',
    timestamp: '2024-01-15 17:00:22'
  },
  {
    id: '7',
    content: '请帮我报名吉他课和水彩班，并加入兴趣群。',
    sender: 'user',
    timestamp: '2024-01-15 17:00:25'
  },
  {
    id: '8',
    content: '已为您报名吉他初阶体验课和水彩班，并邀请加入艺术兴趣交流群。后续将推送活动提醒、成果展示机会及成长趋势预测。如需调整课程或推荐更多活动，欢迎随时告知。',
    sender: 'ai',
    agentName: '养趣工坊智能体',
    agentCategory: '美',
    timestamp: '2024-01-15 17:00:30'
  }
];

function processMessageContent(content: string): string {
  return content.replace(/@[\u4e00-\u9fa5a-zA-Z]+智能体/g, '');
}

function highlightMentions(content: string): string {
  return content.replace(/(@[\u4e00-\u9fa5a-zA-Z]+智能体)/g, '<span class="bg-blue-100 text-blue-800 px-1 rounded font-medium">$1</span>');
}

export default function ArtInterestDemo() {
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
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-pink-500 to-rose-600 flex items-center justify-center">
                <MessageSquare className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-semibold text-foreground">艺术兴趣探索</h1>
                <p className="text-sm text-muted-foreground">演示场景 4</p>
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