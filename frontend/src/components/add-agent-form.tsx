'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Bot, Loader2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from './ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { useState } from 'react';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/auth-context';
import { agentService } from '@/services/api-service';

const formSchema = z.object({
  name: z.string().min(2, { message: '智能体名称至少需要2个字。' }),
  description: z.string().min(10, { message: '描述信息至少需要10个字。' }),
  apiUrl: z.string().url({ message: '请输入一个有效的API URL。' }),
  appId: z.string().min(1, { message: '应用ID不能为空。' }),
  apiKey: z.string().min(1, { message: 'API密钥不能为空。' }),
  token: z.string().min(1, { message: 'Token不能为空。' }),
  category: z.string().min(1, { message: '请选择智能体类别。' }),
});

export default function AddAgentForm() {
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();
  const { user } = useAuth();
  
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: '',
      description: '',
      apiUrl: 'https://jiutian.10086.cn/largemodel/api/v1/completions',
      appId: '',
      apiKey: '',
      token: '',
      category: '',
    },
  });

  async function onSubmit(data: z.infer<typeof formSchema>) {
    setIsLoading(true);
    
    try {
      const response = await agentService.submitAgent({
        ...data,
        submitterId: user?.id,
        submitterRole: user?.role,
        status: 'pending' // 待审核状态
      });

      if (response.success) {
        toast({
          title: 'API提交成功',
          description: `智能体API "${data.name}" 已提交，等待管理员审核。`,
        });
        form.reset();
      } else {
        throw new Error(response.message || '提交失败');
      }
    } catch (error) {
      toast({
        title: '提交失败',
        description: error instanceof Error ? error.message : '请检查网络连接或稍后重试。',
        variant: 'destructive',
      });
    }
    
    setIsLoading(false);
  }

  return (
    <Card className="shadow-lg bg-card border-border/20">
      <CardHeader>
        <CardTitle className="font-headline text-xl">智能体API信息</CardTitle>
        <CardDescription>填写智能体API的详细信息，提交后需要管理员审核。</CardDescription>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>智能体名称</FormLabel>
                  <FormControl>
                    <Input placeholder="例如：物理实验模拟器" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
             <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>智能体描述</FormLabel>
                  <FormControl>
                    <Textarea placeholder="描述一下这个智能体的功能和用途。" {...field} rows={3} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
             <FormField
              control={form.control}
              name="category"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>智能体类别</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="选择智能体类别" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value="MORAL_EDUCATION">德育引导</SelectItem>
                      <SelectItem value="INTELLECTUAL_EDUCATION">智育助力</SelectItem>
                      <SelectItem value="PHYSICAL_EDUCATION">体育赋能</SelectItem>
                      <SelectItem value="AESTHETIC_EDUCATION">美育熏陶</SelectItem>
                      <SelectItem value="LABOR_EDUCATION">劳育实践</SelectItem>
                      <SelectItem value="LIFE_SUPPORT">生活护航</SelectItem>
                      <SelectItem value="COORDINATION_AGENT">统筹智能体</SelectItem>
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
             <FormField
              control={form.control}
              name="apiUrl"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>API 地址</FormLabel>
                  <FormControl>
                    <Input placeholder="https://jiutian.10086.cn/largemodel/api/v2/completions" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
             <FormField
              control={form.control}
              name="appId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>应用ID</FormLabel>
                  <FormControl>
                    <Input placeholder="例如：684fc27c1d7a1436f74c8bc4" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
             <FormField
              control={form.control}
              name="apiKey"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>API 密钥</FormLabel>
                  <FormControl>
                    <Input type="password" placeholder="例如：686b39bc4c78b04e5adeb050.KJ75HafB8ppylHX2Ivnde8TnAFu0Fshs" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
             <FormField
              control={form.control}
              name="token"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Token</FormLabel>
                  <FormControl>
                    <Input type="password" placeholder="例如：eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..." {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" disabled={isLoading} className="w-full">
              {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Bot className="mr-2 h-4 w-4" />}
              {isLoading ? '正在提交...' : '提交API审核'}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}
