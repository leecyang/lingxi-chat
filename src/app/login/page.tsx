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

const formSchema = z.object({
  username: z.string().min(1, { message: '请输入您的学号或姓名' }),
  password: z.string().min(6, { message: '密码至少为6位' }),
});

export default function LoginPage() {
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  });

  function onSubmit(values: z.infer<typeof formSchema>) {
    // Handle login logic here
    console.log(values);
    // On success, redirect to another page
  }

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-10rem)] bg-muted/20 py-12">
      <Card className="w-full max-w-md shadow-2xl bg-card/60 backdrop-blur-lg border-primary/20">
        <CardHeader className="text-center">
          <CardTitle className="text-3xl font-headline">欢迎回来</CardTitle>
          <CardDescription>登录您的灵犀智学账户</CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>学号 / 姓名</FormLabel>
                    <FormControl>
                      <Input placeholder="请输入您的学号或教师姓名" {...field} />
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
