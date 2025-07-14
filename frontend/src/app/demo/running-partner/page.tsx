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
    content: '我想每天早上跑步，但一个人坚持不下来，有没有可以一起的伙伴和科学的锻炼建议？',
    sender: 'user',
    timestamp: '2024-01-15 16:00:00'
  },
  {
    id: '2',
    content: '检测到您有健康锻炼与社交陪伴双重需求。@健康评估智能体，请对用户进行晨跑健康评估；@陆吾训策智能体，制定个性化晨跑方案；@搭子匹配智能体，匹配合适晨跑搭子并推送邀请。',
    sender: 'ai',
    agentName: '灵犀',
    agentCategory: '统筹',
    timestamp: '2024-01-15 16:00:05'
  },
  {
    id: '3',
    content: '您好，我是@健康评估智能体。请填写晨跑健康问卷：\n1. 近半年有无心血管疾病史？\n2. 平均每周运动次数？\n3. 晨跑目标距离？\n4. 是否有晨练低血糖、头晕等特殊情况？（请回复1-4题答案）',
    sender: 'ai',
    agentName: '健康评估智能体',
    agentCategory: '体',
    timestamp: '2024-01-15 16:00:10'
  },
  {
    id: '4',
    content: '1. 无 2. 2次 3. 5公里 4. 无',
    sender: 'user',
    timestamp: '2024-01-15 16:00:15'
  },
  {
    id: '5',
    content: '健康评估通过，您的身体状况适合晨跑。预计三周后耐力提升15%。@陆吾训策智能体，请为用户生成科学晨跑计划并推送校园最佳路线。',
    sender: 'ai',
    agentName: '健康评估智能体',
    agentCategory: '体',
    timestamp: '2024-01-15 16:00:18'
  },
  {
    id: '6',
    content: '您好，我是@陆吾训策智能体。为您定制“晨跑赋能计划”：\n- 每周晨跑3～5天，每次30分钟，推荐慢跑配速7-8min/km\n- 训练目标：3周内从2公里→5公里过渡\n- 校园晨跑最佳路线图已推送\n- 晨跑前后建议深呼吸2次，关注心率变化\n已同步跑步打卡功能，欢迎每日更新。@搭子匹配智能体将为您推荐跑步搭子。',
    sender: 'ai',
    agentName: '陆吾训策智能体',
    agentCategory: '体',
    timestamp: '2024-01-15 16:00:22'
  },
  {
    id: '7',
    content: '您好，我是@搭子匹配智能体。根据您的“晨跑+6:30起床+音乐耳机”偏好，推荐以下3位同学：\n- 小杨（大三，体育学院）\n- 阿杰（大一，喜听轻音乐）\n- 薇薇（大二，居住同宿舍区）\n是否要向其中某位发出结伴邀请？',
    sender: 'ai',
    agentName: '搭子匹配智能体',
    agentCategory: '生活',
    timestamp: '2024-01-15 16:00:25'
  },
  {
    id: '8',
    content: '邀请小杨',
    sender: 'user',
    timestamp: '2024-01-15 16:00:28'
  },
  {
    id: '9',
    content: '已为您与小杨建立晨跑搭子关系，后续将跟踪您的运动数据并预测健康趋势。预计三周后晨跑坚持率提升30%。如需更换搭子或调整计划可随时告知。',
    sender: 'ai',
    agentName: '搭子匹配智能体',
    agentCategory: '生活',
    timestamp: '2024-01-15 16:00:32'
  }
];

function processMessageContent(content: string): string {
  return content.replace(/@[\u4e00-\u9fa5a-zA-Z]+智能体/g, '');
}

function highlightMentions(content: string): string {
  return content.replace(/(@[\u4e00-\u9fa5a-zA-Z]+智能体)/g, '<span class="bg-blue-100 text-blue-800 px-1 rounded font-medium">$1</span>');
}

export default function RunningPartnerDemo() {
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
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-orange-500 to-red-600 flex items-center justify-center">
                <MessageSquare className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-semibold text-foreground">找晨跑搭子</h1>
                <p className="text-sm text-muted-foreground">演示场景 3</p>
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