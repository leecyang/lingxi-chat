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
    content: '我想查一下第二课堂积分，最好能快速核查并获得操作指引，有什么便捷方法？',
    sender: 'user',
    timestamp: '2024-01-15 20:30:00'
  },
  {
    id: '2',
    content: '检测到您有信息核查与操作指引双重需求。@积分核查智能体，请对用户进行积分数据校验；@问答回廊智能体，推送详细查询路径与注意事项。',
    sender: 'ai',
    agentName: '灵犀',
    agentCategory: '统筹',
    timestamp: '2024-01-15 20:30:05'
  },
  {
    id: '3',
    content: '您好，我是@积分核查智能体。请填写核查问卷：\n1. 是否已绑定学号？\n2. 是否近期有新活动积分未到账？\n3. 是否遇到积分异常或争议？（请回复1-3题答案）',
    sender: 'ai',
    agentName: '积分核查智能体',
    agentCategory: '信息',
    timestamp: '2024-01-15 20:30:10'
  },
  {
    id: '4',
    content: '1. 已绑定 2. 有新活动 3. 暂无异常',
    sender: 'user',
    timestamp: '2024-01-15 20:30:15'
  },
  {
    id: '5',
    content: '核查结果：部分新活动积分需1-2个工作日到账，暂无异常。预计本学期积分增长8%。@问答回廊智能体，请为用户推送详细查询路径及注意事项。',
    sender: 'ai',
    agentName: '积分核查智能体',
    agentCategory: '信息',
    timestamp: '2024-01-15 20:30:18'
  },
  {
    id: '6',
    content: '您好，我是@问答回廊智能体。第二课堂积分查询指引如下：\n- 登录“智慧校园”App，进入“第二课堂”模块\n- 点击“我的积分”查看明细\n- 如有疑问可在线提交工单，系统将自动跟进处理进度\n后续将推送积分变动提醒与常见问题解答，如需人工协助请随时告知。',
    sender: 'ai',
    agentName: '问答回廊智能体',
    agentCategory: '信息',
    timestamp: '2024-01-15 20:30:23'
  }
];

function processMessageContent(content: string): string {
  return content.replace(/@[\u4e00-\u9fa5a-zA-Z]+智能体/g, '');
}

function highlightMentions(content: string): string {
  return content.replace(/(@[\u4e00-\u9fa5a-zA-Z]+智能体)/g, '<span class="bg-blue-100 text-blue-800 px-1 rounded font-medium">$1</span>');
}

export default function InfoQueryDemo() {
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
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-slate-500 to-gray-600 flex items-center justify-center">
                <MessageSquare className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-semibold text-foreground">一般信息查询</h1>
                <p className="text-sm text-muted-foreground">演示场景 10</p>
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