import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { availableAgents } from '@/lib/agents';
import AgentRecommender from '@/components/agent-recommender';
import AddAgentForm from '@/components/add-agent-form';
import { Separator } from '@/components/ui/separator';
import { PlusCircle } from 'lucide-react';

export default function PlaybookPage() {
  // TODO: Add logic to check if user is a teacher or developer
  const isPrivilegedUser = true; // Mocked value

  return (
    <div className="container mx-auto px-4 py-12">
      <section className="text-center">
        <h1 className="text-4xl md:text-5xl font-bold font-headline">智能体 Playbook</h1>
        <p className="mt-4 max-w-2xl mx-auto text-lg text-muted-foreground">
          探索我们强大的智能体阵容，并为开发者和教师提供创建自定义智能体的能力。
        </p>
      </section>

      <section className="mt-12 max-w-3xl mx-auto">
        <AgentRecommender />
      </section>

      {isPrivilegedUser && (
        <>
          <Separator className="my-16" />
          <section className="max-w-3xl mx-auto">
            <div className="text-center mb-8">
                <h2 className="text-3xl font-bold font-headline flex items-center justify-center gap-3">
                    <PlusCircle className="w-8 h-8 text-primary" />
                    创建自定义智能体
                </h2>
                <p className="text-muted-foreground mt-2">为平台添加新的能力（仅限教师和开发者）。</p>
            </div>
            <AddAgentForm />
          </section>
        </>
      )}

      <Separator className="my-16" />

      <section>
        <div className="text-center mb-8">
            <h2 className="text-3xl font-bold font-headline">可用智能体列表</h2>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {availableAgents.map((agent) => {
            const Icon = agent.icon;
            return (
              <Card key={agent.name} className="flex flex-col hover:shadow-lg hover:-translate-y-1 transition-transform duration-300 bg-card/60 backdrop-blur-lg border-border/20">
                <CardHeader className="flex-row items-center gap-4">
                  <div className="p-3 bg-primary/10 rounded-lg">
                    <Icon className="w-8 h-8 text-primary" />
                  </div>
                  <div>
                    <CardTitle className="font-headline">{agent.name}</CardTitle>
                  </div>
                </CardHeader>
                <CardContent className="flex-grow">
                  <CardDescription>{agent.description}</CardDescription>
                </CardContent>
              </Card>
            );
          })}
        </div>
      </section>
    </div>
  );
}
