'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { UserCheck, Loader2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from './ui/input';
import { useState } from 'react';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/auth-context';
import { developerService } from '@/services/api-service';

const formSchema = z.object({
  reason: z.string().min(50, { message: '申请理由至少需要50个字。' }),
  experience: z.string().min(20, { message: '技术经验描述至少需要20个字。' }),
  portfolio: z.string().url({ message: '请输入有效的作品集URL。' }).optional().or(z.literal('')),
  github: z.string().url({ message: '请输入有效的GitHub URL。' }).optional().or(z.literal('')),
  contact: z.string().min(5, { message: '联系方式至少需要5个字符。' }),
});

export default function DeveloperApplicationForm() {
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();
  const { user } = useAuth();
  
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      reason: '',
      experience: '',
      portfolio: '',
      github: '',
      contact: '',
    },
  });

  async function onSubmit(data: z.infer<typeof formSchema>) {
    setIsLoading(true);
    
    try {
      const response = await developerService.applyForDeveloper({
        ...data,
        applicantId: user?.id,
        applicantName: user?.name,
        status: 'pending' // 待审核状态
      });

      if (response.success) {
        toast({
          title: '申请提交成功',
          description: '您的开发者申请已提交，请等待管理员审核。',
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
        <CardTitle className="font-headline text-xl">申请成为开发者</CardTitle>
        <CardDescription>填写申请信息，通过审核后您将获得提交智能体API的权限。</CardDescription>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="reason"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>申请理由</FormLabel>
                  <FormControl>
                    <Textarea placeholder="请详细说明您申请成为开发者的理由和目标..." {...field} rows={4} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="experience"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>技术经验</FormLabel>
                  <FormControl>
                    <Textarea placeholder="请描述您的编程经验、技术栈和相关项目经历..." {...field} rows={3} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="portfolio"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>作品集链接（可选）</FormLabel>
                  <FormControl>
                    <Input placeholder="https://your-portfolio.com" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="github"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>GitHub链接（可选）</FormLabel>
                  <FormControl>
                    <Input placeholder="https://github.com/yourusername" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="contact"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>联系方式</FormLabel>
                  <FormControl>
                    <Input placeholder="邮箱、QQ或微信等联系方式" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" disabled={isLoading} className="w-full">
              {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <UserCheck className="mr-2 h-4 w-4" />}
              {isLoading ? '正在提交...' : '提交申请'}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}