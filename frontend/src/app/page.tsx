'use client';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Code, Shield, User, ArrowRight, Sparkles, Brain, Zap, Globe, Heart, Star, Rocket, BookOpen, Users, Target } from 'lucide-react';
import Link from 'next/link';
import { useAuth } from '@/contexts/auth-context';
import { useRouter } from 'next/navigation';

export default function Home() {
  const { user } = useAuth();
  const router = useRouter();

  const handleStartExperience = () => {
    if (user) {
      router.push('/group-chat');
    } else {
      router.push('/login');
    }
  };
  return (
    <div className="min-h-screen">
      {/* Hero Section with Enhanced Visual Impact */}
      <section className="relative overflow-hidden">
        {/* Animated Background Elements */}
        <div className="absolute inset-0 -z-10">
          <div className="absolute top-20 left-10 w-72 h-72 bg-gradient-to-r from-blue-400/20 to-purple-600/20 rounded-full blur-3xl animate-pulse"></div>
          <div className="absolute top-40 right-20 w-96 h-96 bg-gradient-to-r from-green-400/20 to-blue-600/20 rounded-full blur-3xl animate-pulse delay-1000"></div>
          <div className="absolute bottom-20 left-1/3 w-80 h-80 bg-gradient-to-r from-purple-400/20 to-pink-600/20 rounded-full blur-3xl animate-pulse delay-2000"></div>
        </div>
        
        {/* Background Text - 让教育再次伟大 */}
        <div className="absolute inset-0 flex items-center justify-center z-0 -translate-y-16">
          <div className="text-6xl md:text-8xl lg:text-9xl xl:text-[8rem] font-black text-gray-800/10 select-none pointer-events-none leading-none tracking-tighter">
            让教育再次伟大
          </div>
        </div>
        
        <div className="container mx-auto px-4 py-24 sm:py-40 text-center relative">

          
          {/* Main Title with Enhanced Animation */}
          <h1 className="text-6xl md:text-8xl lg:text-9xl font-bold font-headline rainbow-gradient mb-6 animate-fade-in select-none pointer-events-none transform hover:scale-110 transition-transform duration-500 relative z-20">
            灵犀智学
          </h1>
          
          {/* Subtitle with Typewriter Effect */}
          <p className="mt-6 max-w-4xl mx-auto text-lg md:text-xl lg:text-2xl text-gray-600 leading-relaxed select-none pointer-events-none">
            融合AI多智能体、AR互动及数据分析的
            <br className="hidden md:block" />
            下一代智能教学辅助平台
          </p>
          
          {/* Enhanced CTA Buttons */}
          <div className="mt-12 flex flex-col sm:flex-row justify-center gap-6">
            <Button asChild size="lg" className="px-8 py-4 text-lg font-semibold bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white shadow-xl hover:shadow-2xl transform hover:scale-105 transition-all duration-300">
              <Link href="/demo">
                <Rocket className="mr-3 w-5 h-5" />
                体验演示场景
                <ArrowRight className="ml-3 w-5 h-5" />
              </Link>
            </Button>
            <Button asChild size="lg" variant="outline" className="px-8 py-4 text-lg font-semibold border-4 border-blue-600 text-blue-800 bg-white hover:bg-blue-600 hover:text-white hover:border-blue-700 shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300">
              <Link href="/playbook">
                <Sparkles className="mr-3 w-5 h-5" />
                探索智能体矩阵
              </Link>
            </Button>
          </div>
          
          {/* Stats Section */}
          <div className="mt-16 grid grid-cols-2 md:grid-cols-4 gap-8 max-w-4xl mx-auto">
            <div className="text-center">
              <div className="text-3xl md:text-4xl font-bold rainbow-gradient">10+</div>
              <div className="text-sm text-muted-foreground mt-1">AI智能体</div>
            </div>
            <div className="text-center">
              <div className="text-3xl md:text-4xl font-bold rainbow-gradient">100%</div>
              <div className="text-sm text-muted-foreground mt-1">开源免费</div>
            </div>
            <div className="text-center">
              <div className="text-3xl md:text-4xl font-bold rainbow-gradient">24/7</div>
              <div className="text-sm text-muted-foreground mt-1">智能服务</div>
            </div>
            <div className="text-center">
              <div className="text-3xl md:text-4xl font-bold rainbow-gradient">∞</div>
              <div className="text-sm text-muted-foreground mt-1">学习可能</div>
            </div>
          </div>
        </div>
      </section>

      {/* Innovation Highlights Section */}
      <section className="py-32 bg-gradient-to-b from-transparent to-blue-50/30">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-5xl md:text-6xl lg:text-7xl font-bold font-headline rainbow-gradient mb-4 select-none pointer-events-none transform hover:scale-105 transition-transform duration-300">突破性技术创新</h2>
            <p className="text-xl md:text-2xl text-gray-600 max-w-3xl mx-auto select-none pointer-events-none leading-relaxed">
              融合前沿AI技术，重新定义教育体验的边界
            </p>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 mb-16">
            <Card className="group hover:shadow-2xl hover:-translate-y-2 transition-all duration-500 glass-effect border-2 hover:border-blue-200">
              <CardHeader className="text-center">
                <div className="mx-auto w-16 h-16 bg-gradient-to-r from-blue-500 to-purple-600 rounded-2xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform duration-300">
                  <Brain className="w-8 h-8 text-white" />
                </div>
                <CardTitle className="text-xl font-bold">多智能体协作</CardTitle>
                <CardDescription>首创AI智能体群体协作学习模式</CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="space-y-3 text-sm">
                  <li className="flex items-center"><Star className="w-4 h-4 mr-2 text-yellow-500" />智能体间实时协作对话</li>
                  <li className="flex items-center"><Star className="w-4 h-4 mr-2 text-yellow-500" />个性化学习路径生成</li>
                  <li className="flex items-center"><Star className="w-4 h-4 mr-2 text-yellow-500" />知识图谱动态构建</li>
                </ul>
              </CardContent>
            </Card>
            
            <Card className="group hover:shadow-2xl hover:-translate-y-2 transition-all duration-500 glass-effect border-2 hover:border-purple-200">
              <CardHeader className="text-center">
                <div className="mx-auto w-16 h-16 bg-gradient-to-r from-purple-500 to-pink-600 rounded-2xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform duration-300">
                  <Globe className="w-8 h-8 text-white" />
                </div>
                <CardTitle className="text-xl font-bold">沉浸式AR教学</CardTitle>
                <CardDescription>突破传统教学空间限制</CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="space-y-3 text-sm">
                  <li className="flex items-center"><Star className="w-4 h-4 mr-2 text-yellow-500" />3D虚拟教师互动</li>
                  <li className="flex items-center"><Star className="w-4 h-4 mr-2 text-yellow-500" />空间化学习环境</li>
                  <li className="flex items-center"><Star className="w-4 h-4 mr-2 text-yellow-500" />手势语音双重交互</li>
                </ul>
              </CardContent>
            </Card>
            
            <Card className="group hover:shadow-2xl hover:-translate-y-2 transition-all duration-500 glass-effect border-2 hover:border-green-200">
              <CardHeader className="text-center">
                <div className="mx-auto w-16 h-16 bg-gradient-to-r from-green-500 to-teal-600 rounded-2xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform duration-300">
                  <Target className="w-8 h-8 text-white" />
                </div>
                <CardTitle className="text-xl font-bold">智能数据洞察</CardTitle>
                <CardDescription>精准把握学习成效</CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="space-y-3 text-sm">
                  <li className="flex items-center"><Star className="w-4 h-4 mr-2 text-yellow-500" />学习行为实时分析</li>
                  <li className="flex items-center"><Star className="w-4 h-4 mr-2 text-yellow-500" />知识掌握度评估</li>
                  <li className="flex items-center"><Star className="w-4 h-4 mr-2 text-yellow-500" />个性化改进建议</li>
                </ul>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>
      
      {/* Mission Statement Section */}
      <section className="py-32 bg-gradient-to-r from-blue-600/10 via-purple-600/10 to-pink-600/10">
        <div className="container mx-auto px-4 text-center">
          <div className="max-w-4xl mx-auto">
            <div className="text-center mb-16">
              <h2 className="text-5xl md:text-6xl lg:text-7xl font-bold font-headline rainbow-gradient mb-4 select-none pointer-events-none transform hover:scale-105 transition-transform duration-300">让教育再次伟大</h2>
              <p className="text-xl md:text-2xl text-gray-600 max-w-3xl mx-auto select-none pointer-events-none leading-relaxed">
                重塑教育未来，让每个学习者都能发挥无限潜能
              </p>
            </div>
            <p className="text-lg md:text-xl lg:text-2xl text-gray-600 leading-relaxed mb-12 select-none pointer-events-none">
               我们相信，每一个学习者都拥有无限潜能。通过AI技术的力量，
               <br className="hidden md:block" />
               我们要打破传统教育的壁垒，让个性化学习成为现实，
               <br className="hidden md:block" />
               让每个人都能在知识的海洋中自由翱翔。
             </p>
            
            <div className="grid md:grid-cols-3 gap-8 mt-16">
              <div className="text-center">
                <div className="w-20 h-20 mx-auto mb-4 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center">
                  <BookOpen className="w-10 h-10 text-white" />
                </div>
                <h3 className="text-xl font-bold mb-2">普惠教育</h3>
                <p className="text-muted-foreground">让优质教育资源触手可及，消除地域和经济差距</p>
              </div>
              <div className="text-center">
                <div className="w-20 h-20 mx-auto mb-4 bg-gradient-to-r from-purple-500 to-pink-600 rounded-full flex items-center justify-center">
                  <Users className="w-10 h-10 text-white" />
                </div>
                <h3 className="text-xl font-bold mb-2">个性化学习</h3>
                <p className="text-muted-foreground">因材施教，为每个学习者量身定制专属学习路径</p>
              </div>
              <div className="text-center">
                <div className="w-20 h-20 mx-auto mb-4 bg-gradient-to-r from-green-500 to-teal-600 rounded-full flex items-center justify-center">
                  <Rocket className="w-10 h-10 text-white" />
                </div>
                <h3 className="text-xl font-bold mb-2">未来就绪</h3>
                <p className="text-muted-foreground">培养面向未来的核心素养和创新思维能力</p>
              </div>
            </div>
          </div>
        </div>
      </section>
      
      {/* User Roles Section - Enhanced */}
      <section className="py-32">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-5xl md:text-6xl lg:text-7xl font-bold font-headline rainbow-gradient mb-4 select-none pointer-events-none transform hover:scale-105 transition-transform duration-300">为不同角色量身打造</h2>
            <p className="text-lg md:text-xl text-gray-600 max-w-3xl mx-auto select-none pointer-events-none leading-relaxed">
               无论你是学生、开发者还是管理员，都能找到适合你的专属工具和体验
             </p>
          </div>
          
          <div className="grid md:grid-cols-3 gap-8">
            <Card className="group hover:shadow-2xl hover:-translate-y-3 transition-all duration-500 glass-effect border-2 hover:border-blue-300 relative overflow-hidden">
              <div className="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-purple-500/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
              <CardHeader className="items-center text-center relative z-10">
                <div className="p-6 bg-gradient-to-r from-blue-500 to-purple-600 rounded-2xl mb-4 group-hover:scale-110 transition-transform duration-300">
                  <User className="w-12 h-12 text-white" />
                </div>
                <CardTitle className="text-2xl font-bold font-headline">学生用户</CardTitle>
                <CardDescription className="text-lg">沉浸式个性化学习体验</CardDescription>
              </CardHeader>
              <CardContent className="relative z-10">
                <ul className="space-y-4 text-center">
                  <li className="flex items-center justify-center"><Sparkles className="w-5 h-5 mr-3 text-blue-500" />与AI智能体群聊互动</li>
                  <li className="flex items-center justify-center"><Globe className="w-5 h-5 mr-3 text-purple-500" />AR虚拟教师语音对话</li>
                  <li className="flex items-center justify-center"><Target className="w-5 h-5 mr-3 text-green-500" />个性化学习路径推荐</li>
                  <li className="flex items-center justify-center"><Brain className="w-5 h-5 mr-3 text-orange-500" />智能学习进度跟踪</li>
                </ul>
              </CardContent>
            </Card>

            <Card className="group hover:shadow-2xl hover:-translate-y-3 transition-all duration-500 glass-effect border-2 hover:border-purple-300 relative overflow-hidden">
              <div className="absolute inset-0 bg-gradient-to-br from-purple-500/5 to-pink-500/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
              <CardHeader className="items-center text-center relative z-10">
                <div className="p-6 bg-gradient-to-r from-purple-500 to-pink-600 rounded-2xl mb-4 group-hover:scale-110 transition-transform duration-300">
                  <Code className="w-12 h-12 text-white" />
                </div>
                <CardTitle className="text-2xl font-bold font-headline">智能体开发者</CardTitle>
                <CardDescription className="text-lg">灵活扩展与无缝接入</CardDescription>
              </CardHeader>
              <CardContent className="relative z-10">
                <ul className="space-y-4 text-center">
                  <li className="flex items-center justify-center"><Code className="w-5 h-5 mr-3 text-purple-500" />浏览与调试智能体</li>
                  <li className="flex items-center justify-center"><Zap className="w-5 h-5 mr-3 text-blue-500" />开放的API接口</li>
                  <li className="flex items-center justify-center"><Rocket className="w-5 h-5 mr-3 text-green-500" />便捷的智能体管理</li>
                  <li className="flex items-center justify-center"><Star className="w-5 h-5 mr-3 text-orange-500" />社区协作开发</li>
                </ul>
              </CardContent>
            </Card>

            <Card className="group hover:shadow-2xl hover:-translate-y-3 transition-all duration-500 glass-effect border-2 hover:border-green-300 relative overflow-hidden">
              <div className="absolute inset-0 bg-gradient-to-br from-green-500/5 to-teal-500/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
              <CardHeader className="items-center text-center relative z-10">
                <div className="p-6 bg-gradient-to-r from-green-500 to-teal-600 rounded-2xl mb-4 group-hover:scale-110 transition-transform duration-300">
                  <Shield className="w-12 h-12 text-white" />
                </div>
                <CardTitle className="text-2xl font-bold font-headline">系统管理员</CardTitle>
                <CardDescription className="text-lg">全方位教学数据洞察</CardDescription>
              </CardHeader>
              <CardContent className="relative z-10">
                <ul className="space-y-4 text-center">
                  <li className="flex items-center justify-center"><Target className="w-5 h-5 mr-3 text-green-500" />可视化数据分析面板</li>
                  <li className="flex items-center justify-center"><Users className="w-5 h-5 mr-3 text-blue-500" />学生学习情况追踪</li>
                  <li className="flex items-center justify-center"><Shield className="w-5 h-5 mr-3 text-purple-500" />系统状态监控与管理</li>
                  <li className="flex items-center justify-center"><Brain className="w-5 h-5 mr-3 text-orange-500" />智能决策支持系统</li>
                </ul>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>
      
      {/* Call to Action Section */}
      <section className="py-32 bg-gradient-to-r from-blue-600 to-purple-600 text-white">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-5xl md:text-6xl lg:text-7xl font-bold font-headline mb-6 select-none pointer-events-none transform hover:scale-105 transition-transform duration-300">
             开启智能教育新时代
           </h2>
           <p className="text-2xl md:text-3xl lg:text-4xl mb-12 max-w-3xl mx-auto opacity-90 select-none pointer-events-none font-bold">
             加入我们，共同构建更美好的教育未来
           </p>
          <div className="flex flex-col sm:flex-row justify-center gap-6">
            <Button onClick={handleStartExperience} size="lg" className="px-10 py-4 text-lg font-semibold bg-white text-blue-800 hover:bg-blue-50 hover:text-blue-900 border-2 border-blue-200 hover:border-blue-400 shadow-xl hover:shadow-2xl transform hover:scale-105 transition-all duration-300">
              <Star className="mr-3 w-5 h-5" />
              立即开始体验
            </Button>
             <Button asChild size="lg" variant="outline" className="px-10 py-4 text-lg font-semibold border-4 border-white text-white bg-transparent hover:bg-white hover:text-blue-800 hover:border-blue-200 shadow-xl hover:shadow-2xl transform hover:scale-105 transition-all duration-300">
               <Link href="/docs">
                 <BookOpen className="mr-3 w-5 h-5" />
                 查看文档
               </Link>
             </Button>
          </div>
        </div>
      </section>
    </div>
  );
}
