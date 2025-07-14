'use client';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Link from 'next/link';
import { ArrowRight } from 'lucide-react';
import { useAuth } from '@/contexts/auth-context';
import { useRouter } from 'next/navigation';
import { authService } from '@/services/api-service';

const studentSchema = z.object({
  username: z.string().min(3, { message: '用户名至少需要3位' }),
  email: z.string().email({ message: '请输入有效的邮箱地址' }),
  password: z.string().min(6, { message: '密码至少需要6位' }),
  confirmPassword: z.string().min(6, { message: '确认密码至少需要6位' }),
  nickname: z.string().min(2, { message: '请输入真实姓名' }),
  studentId: z.string().min(5, { message: '请输入有效的学号' }),
  college: z.string().min(2, { message: '请输入学院名称' }),
  class: z.string().min(2, { message: '请输入班级名称' }),
  isDeveloper: z.boolean().default(false),
  agreeToTerms: z.boolean().refine(val => val === true, { message: '必须同意服务条款' }),
}).refine(data => data.password === data.confirmPassword, {
  message: '两次输入的密码不一致',
  path: ['confirmPassword'],
});

const teacherSchema = z.object({
  username: z.string().min(3, { message: '用户名至少需要3位' }),
  email: z.string().email({ message: '请输入有效的邮箱地址' }),
  password: z.string().min(6, { message: '密码至少需要6位' }),
  confirmPassword: z.string().min(6, { message: '确认密码至少需要6位' }),
  nickname: z.string().min(2, { message: '请输入真实姓名' }),
  institution: z.string().min(2, { message: '请输入单位名称' }),
  agreeToTerms: z.boolean().refine(val => val === true, { message: '必须同意服务条款' }),
}).refine(data => data.password === data.confirmPassword, {
  message: '两次输入的密码不一致',
  path: ['confirmPassword'],
});

export default function RegisterPage() {
  const { login } = useAuth();
  const router = useRouter();
  
  const studentForm = useForm<z.infer<typeof studentSchema>>({
    resolver: zodResolver(studentSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      nickname: '',
      studentId: '',
      college: '',
      class: '',
      isDeveloper: false,
      agreeToTerms: false,
    },
  });

  const teacherForm = useForm<z.infer<typeof teacherSchema>>({
    resolver: zodResolver(teacherSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      nickname: '',
      institution: '',
      agreeToTerms: false,
    },
  });

  async function onStudentSubmit(values: z.infer<typeof studentSchema>) {
    try {
      const registerData = {
        username: values.username,
        email: values.email,
        password: values.password,
        confirmPassword: values.confirmPassword,
        nickname: values.nickname,
        organization: `${values.college} ${values.class}`,
        roleRequest: values.isDeveloper ? 'developer' : 'user',
        agreeToTerms: values.agreeToTerms,
      };

      const response = await authService.register(registerData);

      if (response.success && response.data) {
        // 存储token
        localStorage.setItem('access_token', response.data.accessToken);
        localStorage.setItem('refresh_token', response.data.refreshToken);
        
        alert('注册成功！即将跳转到登录页面');
        
        // 跳转到登录页面
        router.push('/login');
      } else {
        alert('注册失败：' + (response.error || '注册响应格式错误'));
      }
    } catch (error) {
      // 注册失败
      alert('注册失败，请稍后重试');
    }
  }

  async function onTeacherSubmit(values: z.infer<typeof teacherSchema>) {
    try {
      const registerData = {
        username: values.username,
        email: values.email,
        password: values.password,
        confirmPassword: values.confirmPassword,
        nickname: values.nickname,
        organization: values.institution,
        roleRequest: 'user',
        agreeToTerms: values.agreeToTerms,
      };

      const response = await authService.register(registerData);

      if (response.success && response.data) {
        // 存储token
        localStorage.setItem('access_token', response.data.accessToken);
        localStorage.setItem('refresh_token', response.data.refreshToken);
        
        alert('注册成功！即将跳转到登录页面');
        
        // 跳转到登录页面
        router.push('/login');
      } else {
        alert('注册失败：' + (response.error || '注册响应格式错误'));
      }
    } catch (error) {
      console.error('注册错误:', error);
      alert('注册失败，请稍后重试');
    }
  }

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-10rem)] bg-muted/20 py-12">
      <Card className="w-full max-w-lg shadow-2xl bg-card border-primary/20">
        <CardHeader className="text-center">
          <CardTitle className="text-3xl font-headline">创建您的账户</CardTitle>
          <CardDescription>加入灵犀智学，开启智能学习之旅</CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="student" className="w-full">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="student">我是学生</TabsTrigger>
              <TabsTrigger value="teacher">我是教师</TabsTrigger>
            </TabsList>
            <TabsContent value="student">
              <Form {...studentForm}>
                <form onSubmit={studentForm.handleSubmit(onStudentSubmit)} className="space-y-4 pt-4">
                  <FormField control={studentForm.control} name="username" render={({ field }) => (
                    <FormItem>
                      <FormLabel>用户名</FormLabel>
                      <FormControl><Input placeholder="请输入用户名" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <FormField control={studentForm.control} name="email" render={({ field }) => (
                    <FormItem>
                      <FormLabel>邮箱</FormLabel>
                      <FormControl><Input type="email" placeholder="请输入您的邮箱" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <FormField control={studentForm.control} name="nickname" render={({ field }) => (
                    <FormItem>
                      <FormLabel>姓名</FormLabel>
                      <FormControl><Input placeholder="请输入您的真实姓名" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <FormField control={studentForm.control} name="studentId" render={({ field }) => (
                    <FormItem>
                      <FormLabel>学号</FormLabel>
                      <FormControl><Input placeholder="请输入您的学号" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <div className="grid grid-cols-2 gap-4">
                     <FormField control={studentForm.control} name="college" render={({ field }) => (
                        <FormItem>
                        <FormLabel>学院</FormLabel>
                        <FormControl><Input placeholder="例如：计算机学院" {...field} /></FormControl>
                        <FormMessage />
                        </FormItem>
                    )} />
                    <FormField control={studentForm.control} name="class" render={({ field }) => (
                        <FormItem>
                        <FormLabel>班级</FormLabel>
                        <FormControl><Input placeholder="例如：软件工程2101" {...field} /></FormControl>
                        <FormMessage />
                        </FormItem>
                    )} />
                  </div>
                  <FormField control={studentForm.control} name="password" render={({ field }) => (
                    <FormItem>
                      <FormLabel>密码</FormLabel>
                      <FormControl><Input type="password" placeholder="请设置您的密码" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <FormField control={studentForm.control} name="confirmPassword" render={({ field }) => (
                    <FormItem>
                      <FormLabel>确认密码</FormLabel>
                      <FormControl><Input type="password" placeholder="请再次输入密码" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <FormField control={studentForm.control} name="isDeveloper" render={({ field }) => (
                    <FormItem className="flex flex-row items-start space-x-3 space-y-0 rounded-md border p-4 shadow-sm">
                      <FormControl><Checkbox checked={field.value} onCheckedChange={field.onChange} /></FormControl>
                      <div className="space-y-1 leading-none">
                        <FormLabel>申请成为开发者</FormLabel>
                        <FormDescription>
                          开发者可以创建和接入自定义智能体。
                        </FormDescription>
                      </div>
                    </FormItem>
                  )} />
                  <FormField control={studentForm.control} name="agreeToTerms" render={({ field }) => (
                    <FormItem className="flex flex-row items-start space-x-3 space-y-0">
                      <FormControl><Checkbox checked={field.value} onCheckedChange={field.onChange} /></FormControl>
                      <div className="space-y-1 leading-none">
                        <FormLabel>我同意服务条款和隐私政策</FormLabel>
                      </div>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <Button type="submit" className="w-full" size="lg">注册学生账户</Button>
                </form>
              </Form>
            </TabsContent>
            <TabsContent value="teacher">
              <Form {...teacherForm}>
                <form onSubmit={teacherForm.handleSubmit(onTeacherSubmit)} className="space-y-4 pt-4">
                   <FormField control={teacherForm.control} name="username" render={({ field }) => (
                    <FormItem>
                      <FormLabel>用户名</FormLabel>
                      <FormControl><Input placeholder="请输入用户名" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                   <FormField control={teacherForm.control} name="email" render={({ field }) => (
                    <FormItem>
                      <FormLabel>邮箱</FormLabel>
                      <FormControl><Input type="email" placeholder="请输入您的邮箱" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                   <FormField control={teacherForm.control} name="nickname" render={({ field }) => (
                    <FormItem>
                      <FormLabel>姓名</FormLabel>
                      <FormControl><Input placeholder="请输入您的真实姓名" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                   <FormField control={teacherForm.control} name="institution" render={({ field }) => (
                    <FormItem>
                      <FormLabel>单位</FormLabel>
                      <FormControl><Input placeholder="请输入您的单位名称" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <FormField control={teacherForm.control} name="password" render={({ field }) => (
                    <FormItem>
                      <FormLabel>密码</FormLabel>
                      <FormControl><Input type="password" placeholder="请设置您的密码" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <FormField control={teacherForm.control} name="confirmPassword" render={({ field }) => (
                    <FormItem>
                      <FormLabel>确认密码</FormLabel>
                      <FormControl><Input type="password" placeholder="请再次输入密码" {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <FormField control={teacherForm.control} name="agreeToTerms" render={({ field }) => (
                    <FormItem className="flex flex-row items-start space-x-3 space-y-0">
                      <FormControl><Checkbox checked={field.value} onCheckedChange={field.onChange} /></FormControl>
                      <div className="space-y-1 leading-none">
                        <FormLabel>我同意服务条款和隐私政策</FormLabel>
                      </div>
                      <FormMessage />
                    </FormItem>
                  )} />
                  <Button type="submit" className="w-full" size="lg">注册教师账户</Button>
                </form>
              </Form>
            </TabsContent>
          </Tabs>

          <div className="mt-6 text-center text-sm">
            已有账户?{' '}
            <Link href="/login" className="font-semibold text-primary hover:underline">
              直接登录 <ArrowRight className="inline h-4 w-4" />
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
