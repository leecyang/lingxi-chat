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
    content: '我想找一份和专业相关的实习，最好能提升职业技能并有发展空间，有什么推荐吗？',
    sender: 'user',
    timestamp: '2024-01-15 19:00:00'
  },
  {
    id: '2',
    content: '检测到您有专业实习与职业成长双重需求。@实习意向评估智能体，请对用户进行岗位意向问卷分析；@职途领航智能体，推荐个性化实习岗位并推送简历优化建议。',
    sender: 'ai',
    agentName: '灵犀',
    agentCategory: '统筹',
    timestamp: '2024-01-15 19:00:05'
  },
  {
    id: '3',
    content: '您好，我是@实习意向评估智能体。请填写岗位意向问卷：\n1. 目标行业？\n2. 是否有相关项目经验？\n3. 期望实习城市？\n4. 对实习内容的特殊要求？（请回复1-4题答案）',
    sender: 'ai',
    agentName: '实习意向评估智能体',
    agentCategory: '职业',
    timestamp: '2024-01-15 19:00:10'
  },
  {
    id: '4',
    content: '1. 金融 2. 有 3. 上海 4. 希望有导师指导和轮岗机会',
    sender: 'user',
    timestamp: '2024-01-15 19:00:15'
  },
  {
    id: '5',
    content: '评估结果：您适合金融分析、风险管理等岗位，建议优先选择有导师培养机制的企业。预计本轮投递通过率提升20%。@职途领航智能体，请为用户推送精选实习岗位并同步简历优化建议。',
    sender: 'ai',
    agentName: '实习意向评估智能体',
    agentCategory: '职业',
    timestamp: '2024-01-15 19:00:18'
  },
  {
    id: '6',
    content: '您好，我是@职途领航智能体。为您推荐以下实习岗位：\n- 招商银行金融分析实习生（上海，含导师带教）\n- 平安信托风险管理助理（上海，轮岗机会丰富）\n- 国泰君安证券研究助理（上海，项目制培养）\n简历优化建议：突出数据分析能力、项目经验及团队协作。后续可推送面试技巧与行业趋势分析，如需调整岗位方向请随时告知。',
    sender: 'ai',
    agentName: '职途领航智能体',
    agentCategory: '职业',
    timestamp: '2024-01-15 19:00:23'
  }
];

function processMessageContent(content: string): string {
  return content.replace(/@[\u4e00-\u9fa5a-zA-Z]+智能体/g, '');
}

function highlightMentions(content: string): string {
  return content.replace(/(@[\u4e00-\u9fa5a-zA-Z]+智能体)/g, '<span class="bg-blue-100 text-blue-800 px-1 rounded font-medium">$1</span>');
}

export default function InternshipDemo() {
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
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-amber-500 to-orange-600 flex items-center justify-center">
                <MessageSquare className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-semibold text-foreground">专业相关实习推荐</h1>
                <p className="text-sm text-muted-foreground">演示场景 8</p>
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