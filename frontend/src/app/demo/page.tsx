'use client';

import Link from 'next/link';
import { MessageSquare, ArrowLeft, Users, Brain, Heart, Palette, Dumbbell, Coffee, BookOpen, Calendar, MapPin, HelpCircle } from 'lucide-react';

interface DemoItem {
  id: string;
  title: string;
  description: string;
  category: string;
  path: string;
  icon: React.ReactNode;
  color: string;
}

const demoItems: DemoItem[] = [
  {
    id: '1',
    title: '心理压力咨询',
    description: '学业压力与情绪管理指导',
    category: '德育',
    path: '/demo/psychology',
    icon: <Heart className="w-5 h-5" />,
    color: 'from-pink-500 to-rose-600'
  },
  {
    id: '2',
    title: '学业困难与竞赛备战',
    description: '学习方法优化与竞赛准备',
    category: '智育',
    path: '/demo/study-competition',
    icon: <Brain className="w-5 h-5" />,
    color: 'from-blue-500 to-indigo-600'
  },
  {
    id: '3',
    title: '找晨跑搭子',
    description: '运动伙伴匹配与健身计划',
    category: '体育',
    path: '/demo/running-partner',
    icon: <Dumbbell className="w-5 h-5" />,
    color: 'from-orange-500 to-red-600'
  },
  {
    id: '4',
    title: '艺术兴趣探索',
    description: '艺术活动推荐与技能培养',
    category: '美育',
    path: '/demo/art-interest',
    icon: <Palette className="w-5 h-5" />,
    color: 'from-purple-500 to-violet-600'
  },
  {
    id: '5',
    title: '时间管理问题',
    description: '日程规划与效率提升',
    category: '劳育',
    path: '/demo/time-management',
    icon: <Calendar className="w-5 h-5" />,
    color: 'from-green-500 to-emerald-600'
  },
  {
    id: '6',
    title: '课程安排合理性',
    description: '选课建议与学业规划',
    category: '智育',
    path: '/demo/course-arrangement',
    icon: <BookOpen className="w-5 h-5" />,
    color: 'from-cyan-500 to-blue-600'
  },
  {
    id: '7',
    title: '第二课堂活动推荐',
    description: '课外活动与素质拓展',
    category: '德育',
    path: '/demo/second-classroom',
    icon: <Users className="w-5 h-5" />,
    color: 'from-teal-500 to-green-600'
  },
  {
    id: '8',
    title: '专业相关实习推荐',
    description: '实习机会与职业规划',
    category: '劳育',
    path: '/demo/internship',
    icon: <MapPin className="w-5 h-5" />,
    color: 'from-amber-500 to-orange-600'
  },
  {
    id: '9',
    title: '饮食不规律',
    description: '营养健康与生活习惯',
    category: '生活',
    path: '/demo/diet-health',
    icon: <Coffee className="w-5 h-5" />,
    color: 'from-lime-500 to-green-600'
  },
  {
    id: '10',
    title: '一般信息查询',
    description: '校园服务与信息获取',
    category: '生活',
    path: '/demo/info-query',
    icon: <HelpCircle className="w-5 h-5" />,
    color: 'from-slate-500 to-gray-600'
  }
];

const categoryColors: Record<string, string> = {
  '德育': 'bg-pink-100 text-pink-800 dark:bg-pink-900 dark:text-pink-200',
  '智育': 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
  '体育': 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200',
  '美育': 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
  '劳育': 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
  '生活': 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
};

export default function DemoPage() {
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <div className="border-b border-border/40 bg-background/80 backdrop-blur-sm">
        <div className="px-4 md:px-6 py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Link href="/" className="p-2 rounded-lg hover:bg-muted/50 transition-colors">
                <ArrowLeft className="w-4 h-4" />
              </Link>
              <div className="w-10 h-10 rounded-full bg-gradient-to-r from-blue-500 to-purple-600 flex items-center justify-center">
                <MessageSquare className="w-5 h-5 text-white" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-foreground">灵犀智能体演示</h1>
                <p className="text-muted-foreground">体验多场景智能对话服务</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="px-4 md:px-6 py-8">
        <div className="max-w-6xl mx-auto">
          <div className="mb-8">
            <h2 className="text-xl font-semibold mb-2">演示场景</h2>
            <p className="text-muted-foreground">
              以下是基于真实校园场景设计的十个演示案例，展示灵犀智能体在不同领域的应用效果。
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {demoItems.map((item) => (
              <Link
                key={item.id}
                href={item.path}
                className="group block p-6 rounded-xl border border-border/50 bg-card hover:bg-muted/50 transition-all duration-200 hover:shadow-lg hover:scale-[1.02]"
              >
                <div className="flex items-start gap-4">
                  <div className={`w-12 h-12 rounded-lg bg-gradient-to-r ${item.color} flex items-center justify-center text-white flex-shrink-0`}>
                    {item.icon}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-2">
                      <h3 className="font-semibold text-foreground group-hover:text-primary transition-colors">
                        {item.title}
                      </h3>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${categoryColors[item.category]}`}>
                        {item.category}
                      </span>
                    </div>
                    <p className="text-sm text-muted-foreground line-clamp-2">
                      {item.description}
                    </p>
                  </div>
                </div>
              </Link>
            ))}
          </div>


        </div>
      </div>
    </div>
  );
}