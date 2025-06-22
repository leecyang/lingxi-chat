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
import { useState } from 'react';
import { useToast } from '@/hooks/use-toast';

const formSchema = z.object({
  name: z.string().min(2, { message: '智能体名称至少需要2个字。' }),
  description: z.string().min(10, { message: '描述信息至少需要10个字。' }),
  apiUrl: z.string().url({ message: '请输入一个有效的API URL。' }),
});

export default function AddAgentForm() {
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();
  
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: '',
      description: '',
      apiUrl: '',
    },
  });

  async function onSubmit(data: z.infer<typeof formSchema>) {
    setIsLoading(true);
    console.log('Adding new agent:', data);
    // Mock API call
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    toast({
      title: '智能体创建成功',
      description: `智能体 "${data.name}" 已成功添加。`,
    });

    form.reset();
    setIsLoading(false);
  }

  return (
    <Card className="shadow-lg">
      <CardHeader>
        <CardTitle className="font-headline text-xl">智能体信息</CardTitle>
        <CardDescription>填写新智能体的详细信息并提供API端点。</CardDescription>
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
              name="apiUrl"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>API 地址</FormLabel>
                  <FormControl>
                    <Input placeholder="https://api.example.com/agent" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" disabled={isLoading} className="w-full">
              {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Bot className="mr-2 h-4 w-4" />}
              {isLoading ? '正在添加...' : '添加智能体'}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}
