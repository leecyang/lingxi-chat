import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Code, Shield, User, ArrowRight } from 'lucide-react';
import Link from 'next/link';

export default function Home() {
  return (
    <div className="container mx-auto px-4 py-12 sm:py-24">
      <section className="text-center">
        <h1 className="text-4xl md:text-6xl font-bold font-headline bg-gradient-to-r from-primary to-accent text-transparent bg-clip-text">
          灵犀智学 Lite
        </h1>
        <p className="mt-4 max-w-2xl mx-auto text-lg text-muted-foreground">
          一个融合AI多智能体、AR互动及数据分析的智能教学辅助平台。
        </p>
        <div className="mt-8 flex justify-center gap-4">
          <Button asChild size="lg">
            <Link href="/playbook">
              探索智能体 Playbook <ArrowRight className="ml-2" />
            </Link>
          </Button>
          <Button asChild size="lg" variant="outline">
            <Link href="/ar-dialogue">体验AR语音对话</Link>
          </Button>
        </div>
      </section>

      <section className="mt-20">
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold font-headline">为不同角色量身打造</h2>
          <p className="text-muted-foreground mt-2">无论你是学生、开发者还是管理员，都能找到适合你的工具。</p>
        </div>
        <div className="grid md:grid-cols-3 gap-8">
          <Card className="hover:shadow-lg hover:-translate-y-1 transition-transform duration-300 bg-card/60 backdrop-blur-lg border-border/20">
            <CardHeader className="items-center text-center">
              <div className="p-4 bg-accent/10 rounded-full">
                <User className="w-10 h-10 text-accent" />
              </div>
              <CardTitle className="mt-4 font-headline">学生用户</CardTitle>
              <CardDescription>沉浸式学习体验</CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="space-y-2 text-muted-foreground text-center">
                <li>与AI智能体群聊互动</li>
                <li>AR虚拟教师语音对话</li>
                <li>个性化学习路径推荐</li>
              </ul>
            </CardContent>
          </Card>

          <Card className="hover:shadow-lg hover:-translate-y-1 transition-transform duration-300 bg-card/60 backdrop-blur-lg border-border/20">
            <CardHeader className="items-center text-center">
              <div className="p-4 bg-accent/10 rounded-full">
                <Code className="w-10 h-10 text-accent" />
              </div>
              <CardTitle className="mt-4 font-headline">智能体开发者</CardTitle>
              <CardDescription>灵活扩展与接入</CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="space-y-2 text-muted-foreground text-center">
                <li>浏览与调试智能体</li>
                <li>开放的API接口</li>
                <li>便捷的智能体管理</li>
              </ul>
            </CardContent>
          </Card>

          <Card className="hover:shadow-lg hover:-translate-y-1 transition-transform duration-300 bg-card/60 backdrop-blur-lg border-border/20">
            <CardHeader className="items-center text-center">
              <div className="p-4 bg-accent/10 rounded-full">
                <Shield className="w-10 h-10 text-accent" />
              </div>
              <CardTitle className="mt-4 font-headline">系统管理员</CardTitle>
              <CardDescription>教学数据洞察</CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="space-y-2 text-muted-foreground text-center">
                <li>可视化数据分析面板</li>
                <li>学生学习情况追踪</li>
                <li>系统状态监控与管理</li>
              </ul>
            </CardContent>
          </Card>
        </div>
      </section>
    </div>
  );
}
