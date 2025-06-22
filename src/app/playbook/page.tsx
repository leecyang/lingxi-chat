import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { availableAgents } from '@/lib/agents';
import AgentRecommender from '@/components/agent-recommender';

export default function PlaybookPage() {
  return (
    <div className="container mx-auto px-4 py-12">
      <section className="text-center">
        <h1 className="text-4xl md:text-5xl font-bold font-headline">智能体 Playbook</h1>
        <p className="mt-4 max-w-2xl mx-auto text-lg text-muted-foreground">
          探索我们强大的智能体阵容，它们能协同工作，为您提供全方位的智能服务。
        </p>
      </section>

      <section className="mt-12 max-w-3xl mx-auto">
        <AgentRecommender />
      </section>

      <section className="mt-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {availableAgents.map((agent) => {
            const Icon = agent.icon;
            return (
              <Card key={agent.name} className="flex flex-col hover:shadow-lg hover:-translate-y-1 transition-transform duration-300">
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
