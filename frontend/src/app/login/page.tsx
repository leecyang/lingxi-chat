'use client';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Link from 'next/link';
import { ArrowRight } from 'lucide-react';
import { useAuth } from '@/contexts/auth-context';
import { authService } from '@/services/api-service';

const formSchema = z.object({
  username: z.string().min(1, { message: '请输入您的用户名或邮箱' }),
  password: z.string().min(6, { message: '密码至少为6位' }),
});

export default function LoginPage() {
  const { login } = useAuth();
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  });

  async function onSubmit(values: z.infer<typeof formSchema>) {
    try {
      const response = await authService.login({
        username: values.username,
        password: values.password,
      });

      if (response.success && response.data) {
        // 存储token
        localStorage.setItem('access_token', response.data.accessToken);
        localStorage.setItem('refresh_token', response.data.refreshToken);
        
        // 登录成功，更新用户状态
        login({ 
          id: response.data.user.id,
          name: response.data.user.nickname || response.data.user.username, 
          username: response.data.user.username,
          email: response.data.user.email,
          role: response.data.user.role.toLowerCase(), // 确保角色是小写
          avatar: response.data.user.avatar
        });
      } else {
        alert('登录失败：' + (response.error || '未知错误'));
      }
    } catch (error) {
      // 登录失败
      alert('登录失败，请稍后重试');
    }
  }

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-10rem)] bg-muted/20 py-12">
      <Card className="w-full max-w-md shadow-2xl bg-card border-primary/20">
        <CardHeader className="text-center">
          <CardTitle className="text-3xl font-headline">欢迎回来</CardTitle>
          <CardDescription>登录您的灵犀智学账户，支持用户名或邮箱登录</CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>用户名 / 邮箱</FormLabel>
                    <FormControl>
                      <Input placeholder="请输入您的用户名或邮箱" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <div className="flex justify-between items-center">
                      <FormLabel>密码</FormLabel>
                      <Link href="#" className="text-sm text-primary hover:underline">
                        忘记密码?
                      </Link>
                    </div>
                    <FormControl>
                      <Input type="password" placeholder="请输入密码" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <Button type="submit" className="w-full" size="lg">
                登录 <ArrowRight className="ml-2" />
              </Button>
            </form>
          </Form>
          <div className="mt-6 text-center text-sm">
            还没有账户?{' '}
            <Link href="/register" className="font-semibold text-primary hover:underline">
              立即注册
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
