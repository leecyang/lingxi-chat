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
    content: '本学期课程安排太满了，感觉压力很大，想科学减负并兼顾社团和兼职，有什么建议？',
    sender: 'user',
    timestamp: '2024-01-15 18:30:00'
  },
  {
    id: '2',
    content: '检测到您有学业减负与时间统筹双重需求。@学业评估智能体，请对用户进行学业压力问卷分析；@选课推荐智能体，分析课程密度并制定调整建议。',
    sender: 'ai',
    agentName: '灵犀',
    agentCategory: '统筹',
    timestamp: '2024-01-15 18:30:05'
  },
  {
    id: '3',
    content: '您好，我是@学业评估智能体。请填写学业压力问卷：\n1. 每周课程总数？\n2. 课余时间平均每天几小时？\n3. 是否有社团/兼职等额外任务？\n4. 目前最困扰您的压力来源？（请回复1-4题答案）',
    sender: 'ai',
    agentName: '学业评估智能体',
    agentCategory: '学业',
    timestamp: '2024-01-15 18:30:10'
  },
  {
    id: '4',
    content: '1. 10门 2. 2小时 3. 有兼职 4. 选修课太多，休息时间少',
    sender: 'user',
    timestamp: '2024-01-15 18:30:15'
  },
  {
    id: '5',
    content: '分析结果：课程密度偏高，选修课占比大，课余时间有限。建议优先保留主修课程，适当减少选修课，预留休息时段。预计本学期学业压力指数下降15%。@选课推荐智能体，请为用户生成课程调整方案并推送讲座报名入口。',
    sender: 'ai',
    agentName: '学业评估智能体',
    agentCategory: '学业',
    timestamp: '2024-01-15 18:30:18'
  },
  {
    id: '6',
    content: '您好，我是@选课推荐智能体。为您推荐的课程调整方案如下：\n- 保留主修课程8门，建议减少2门选修课\n- 每周至少预留3次课余休息时段\n- 可报名“学业减负”主题讲座，提升时间管理能力\n如需进一步优化课表或获取压力调适资源，请随时告知。',
    sender: 'ai',
    agentName: '选课推荐智能体',
    agentCategory: '学业',
    timestamp: '2024-01-15 18:30:23'
  }
];

function processMessageContent(content: string): string {
  return content.replace(/@[\u4e00-\u9fa5a-zA-Z]+智能体/g, '');
}

function highlightMentions(content: string): string {
  return content.replace(/(@[\u4e00-\u9fa5a-zA-Z]+智能体)/g, '<span class="bg-blue-100 text-blue-800 px-1 rounded font-medium">$1</span>');
}

export default function CourseArrangementDemo() {
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
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-cyan-500 to-blue-600 flex items-center justify-center">
                <MessageSquare className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-semibold text-foreground">课程安排合理性</h1>
                <p className="text-sm text-muted-foreground">演示场景 6</p>
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