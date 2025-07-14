'use client';

import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { useState, useEffect } from 'react';
import { agentService } from '@/services/api-service';
import { Agent } from '@/lib/agents';
import AddAgentForm from '@/components/add-agent-form';
import DeveloperApplicationForm from '@/components/developer-application-form';
import { Separator } from '@/components/ui/separator';
import { PlusCircle, UserPlus, Grid3X3, Sparkles } from 'lucide-react';
import { useAuth } from '@/contexts/auth-context';

// 智能体分类配置
const AGENT_CATEGORIES = {
  MORAL_EDUCATION: {
    name: '德育引导',
    description: '点亮成长中的品格星光',
    icon: '🌟',
    color: 'bg-gradient-to-br from-purple-500 to-pink-500'
  },
  INTELLECTUAL_EDUCATION: {
    name: '智育助力', 
    description: '你的专属学习规划伙伴/定制你的学霸进阶路',
    icon: '📚',
    color: 'bg-gradient-to-br from-blue-500 to-cyan-500'
  },
  PHYSICAL_EDUCATION: {
    name: '体育赋能',
    description: '活力计划与暖心提醒/运动打卡，活力满格！',
    icon: '💪',
    color: 'bg-gradient-to-br from-green-500 to-emerald-500'
  },
  AESTHETIC_EDUCATION: {
    name: '美育熏陶',
    description: '创意路上的温暖同行者/画出你的专属艺术范儿',
    icon: '🎨',
    color: 'bg-gradient-to-br from-orange-500 to-red-500'
  },
  LABOR_EDUCATION: {
    name: '劳育实践',
    description: '劳务协作，轻松搞定！',
    icon: '🔧',
    color: 'bg-gradient-to-br from-yellow-500 to-orange-500'
  },
  LIFE_SUPPORT: {
    name: '生活护航',
    description: '全天候贴心守护/你的24小时智能陪伴',
    icon: '🏠',
    color: 'bg-gradient-to-br from-indigo-500 to-purple-500'
  },
  COORDINATION_AGENT: {
    name: '统筹智能体',
    description: '灵犀智学团队通过九天大模型平台设计的专门为解决学生生活的全方位学习助手',
    icon: '🎯',
    color: 'bg-gradient-to-br from-rose-500 to-pink-600'
  }
} as const;

export default function AgentMatrixPage() {
  const { user } = useAuth();
  const [availableAgents, setAvailableAgents] = useState<Agent[]>([]);

  useEffect(() => {
    const fetchActiveAgents = async () => {
      try {
        const response = await agentService.getActiveAgents();
        if (response.success && response.data && Array.isArray(response.data.agents)) {
          setAvailableAgents(response.data.agents);
        }
      } catch (error) {
        // 获取智能体失败
      }
    };

    fetchActiveAgents();
  }, []);
  
  // 管理员、教师和开发者可以添加API
  const canAddApi = user?.role === 'admin' || user?.role === 'teacher' || user?.role === 'developer';
  
  // 学生可以申请成为开发者
  const canApplyDeveloper = user?.role === 'student';

  // 按分类组织智能体
  const agentsByCategory = availableAgents.reduce((acc, agent) => {
    const category = agent.category || 'UNCATEGORIZED';
    if (!acc[category]) {
      acc[category] = [];
    }
    acc[category].push(agent);
    return acc;
  }, {} as Record<string, Agent[]>);

  return (
    <div className="container mx-auto px-4 py-12">
      <section className="text-center">
        <h1 className="text-4xl md:text-5xl font-bold font-headline flex items-center justify-center gap-3">
          <Grid3X3 className="w-12 h-12 text-primary" />
          智能体矩阵
        </h1>
        <p className="mt-4 max-w-2xl mx-auto text-lg text-muted-foreground">
          探索我们强大的智能体生态系统，按教育领域精心分类，为您的学习和生活提供全方位支持。
        </p>
      </section>

      <Separator className="my-16" />

      {/* 智能体分类展示 */}
      <section>
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold font-headline flex items-center justify-center gap-3">
            <Sparkles className="w-8 h-8 text-primary" />
            智能体生态矩阵
          </h2>
          <p className="text-muted-foreground mt-2">按教育领域分类的智能体，为您的全面发展保驾护航</p>
        </div>

        {Object.entries(AGENT_CATEGORIES).map(([categoryKey, categoryInfo]) => {
          const categoryAgents = agentsByCategory[categoryKey] || [];
          
          if (categoryAgents.length === 0) return null;
          
          return (
            <div key={categoryKey} className="mb-16">
              {/* 分类标题 */}
              <div className={`${categoryInfo.color} rounded-2xl p-6 mb-8 text-white`}>
                <div className="flex items-center gap-4 mb-2">
                  <span className="text-4xl">{categoryInfo.icon}</span>
                  <div>
                    <h3 className="text-2xl font-bold">{categoryInfo.name}</h3>
                    <p className="text-white/90 text-lg">{categoryInfo.description}</p>
                  </div>
                </div>
              </div>
              
              {/* 该分类下的智能体 */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {categoryAgents.map((agent) => (
                  <Card key={agent.id} className="flex flex-col hover:shadow-lg hover:-translate-y-1 transition-all duration-300 bg-card/60 backdrop-blur-lg border-border/20 hover:border-primary/30">
                    <CardHeader className="flex-row items-center gap-4">
                      <div className="p-3 bg-primary/10 rounded-lg">
                        {agent.avatar ? (
                          <img src={agent.avatar} alt={agent.displayName} className="w-8 h-8 rounded" />
                        ) : (
                          <div className="w-8 h-8 bg-primary/20 rounded flex items-center justify-center text-primary font-semibold">
                            {agent.displayName.charAt(0)}
                          </div>
                        )}
                      </div>
                      <div>
                        <CardTitle className="font-headline">{agent.displayName}</CardTitle>
                      </div>
                    </CardHeader>
                    <CardContent className="flex-grow">
                      <CardDescription>{agent.description}</CardDescription>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          );
        })}

        {/* 未分类的智能体 */}
        {agentsByCategory.UNCATEGORIZED && agentsByCategory.UNCATEGORIZED.length > 0 && (
          <div className="mb-16">
            <div className="bg-gradient-to-br from-gray-500 to-slate-600 rounded-2xl p-6 mb-8 text-white">
              <div className="flex items-center gap-4 mb-2">
                <span className="text-4xl">🔍</span>
                <div>
                  <h3 className="text-2xl font-bold">其他智能体</h3>
                  <p className="text-white/90 text-lg">暂未分类的智能体</p>
                </div>
              </div>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {agentsByCategory.UNCATEGORIZED.map((agent) => (
                <Card key={agent.id} className="flex flex-col hover:shadow-lg hover:-translate-y-1 transition-all duration-300 bg-card/60 backdrop-blur-lg border-border/20 hover:border-primary/30">
                  <CardHeader className="flex-row items-center gap-4">
                    <div className="p-3 bg-primary/10 rounded-lg">
                      {agent.avatar ? (
                        <img src={agent.avatar} alt={agent.displayName} className="w-8 h-8 rounded" />
                      ) : (
                        <div className="w-8 h-8 bg-primary/20 rounded flex items-center justify-center text-primary font-semibold">
                          {agent.displayName.charAt(0)}
                        </div>
                      )}
                    </div>
                    <div>
                      <CardTitle className="font-headline">{agent.displayName}</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent className="flex-grow">
                    <CardDescription>{agent.description}</CardDescription>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        )}
      </section>

      {/* 提交智能体API表单 */}
      {canAddApi && (
        <>
          <Separator className="my-16" />
          <section className="max-w-3xl mx-auto">
            <div className="text-center mb-8">
                <h2 className="text-3xl font-bold font-headline flex items-center justify-center gap-3">
                    <PlusCircle className="w-8 h-8 text-primary" />
                    提交智能体API
                </h2>
                <p className="text-muted-foreground mt-2">为平台添加新的智能体API（仅限管理员、教师和开发者）。</p>
            </div>
            <AddAgentForm />
          </section>
        </>
      )}

      {/* 申请成为开发者表单 */}
      {canApplyDeveloper && (
        <>
          <Separator className="my-16" />
          <section className="max-w-3xl mx-auto">
            <div className="text-center mb-8">
                <h2 className="text-3xl font-bold font-headline flex items-center justify-center gap-3">
                    <UserPlus className="w-8 h-8 text-primary" />
                    申请成为开发者
                </h2>
                <p className="text-muted-foreground mt-2">提交申请成为平台开发者，获得提交智能体API的权限。</p>
            </div>
            <DeveloperApplicationForm />
          </section>
        </>
      )}
    </div>
  );
}
