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
    content: '有几门课程我听不懂，还想准备数学竞赛，感觉时间和精力都不够用，压力很大。',
    sender: 'user',
    timestamp: '2024-01-15 15:00:00'
  },
  {
    id: '2',
    content: '检测到您面临学业理解与竞赛备战双重压力。@学伴智答智能体，请对用户列出困难课程并推送资源包；@竞赛辅导智能体，制定竞赛冲刺方案；@成绩预测智能体，实时预测成绩趋势并反馈。',
    sender: 'ai',
    agentName: '灵犀',
    agentCategory: '统筹',
    timestamp: '2024-01-15 15:00:05'
  },
  {
    id: '3',
    content: '您好，我是@学伴智答智能体。请问您具体遇到困难的课程有哪些？（如高等数学、线性代数、概率论等）\n请回复课程名称，我将为您推送专属学习资源包、每日10题训练任务，并邀请加入互助小组。',
    sender: 'ai',
    agentName: '学伴智答智能体',
    agentCategory: '智',
    timestamp: '2024-01-15 15:00:10'
  },
  {
    id: '4',
    content: '高等数学、线性代数',
    sender: 'user',
    timestamp: '2024-01-15 15:00:15'
  },
  {
    id: '5',
    content: '已为您推送高等数学、线性代数的分层学习资源包、每日训练任务，并邀请您加入互助小组。@竞赛辅导智能体，请根据用户目标制定竞赛备考方案。',
    sender: 'ai',
    agentName: '学伴智答智能体',
    agentCategory: '智',
    timestamp: '2024-01-15 15:00:18'
  },
  {
    id: '6',
    content: '您好，我是@竞赛辅导智能体。根据您的目标（全国大学生数学竞赛），为您定制如下冲刺方案：\n- 每日1小时专项训练，周日复盘错题\n- 阶段目标分三期：基础巩固、技巧提升、模拟实战\n- 推送历年真题题库与解析视频\n- 可加入冲刺辅导营，由国奖学长带队答疑\n是否需要我帮您建立学习打卡计划并设置提醒？',
    sender: 'ai',
    agentName: '竞赛辅导智能体',
    agentCategory: '智',
    timestamp: '2024-01-15 15:00:22'
  },
  {
    id: '7',
    content: '需要，请帮我建立打卡计划。',
    sender: 'user',
    timestamp: '2024-01-15 15:00:25'
  },
  {
    id: '8',
    content: '已为您建立学习打卡计划，并同步成绩预测：预计本学期数学成绩提升12分。后续将根据您的学习进展动态调整方案，若有新需求可随时反馈。',
    sender: 'ai',
    agentName: '成绩预测智能体',
    agentCategory: '智',
    timestamp: '2024-01-15 15:00:28'
  }
];

function processMessageContent(content: string): string {
  return content.replace(/@[\u4e00-\u9fa5a-zA-Z]+智能体/g, '');
}

function highlightMentions(content: string): string {
  return content.replace(/(@[\u4e00-\u9fa5a-zA-Z]+智能体)/g, '<span class="bg-blue-100 text-blue-800 px-1 rounded font-medium">$1</span>');
}

export default function StudyCompetitionDemo() {
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
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-purple-500 to-pink-600 flex items-center justify-center">
                <MessageSquare className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-semibold text-foreground">学业困难与竞赛备战</h1>
                <p className="text-sm text-muted-foreground">演示场景 2</p>
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