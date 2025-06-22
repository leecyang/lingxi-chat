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

const studentSchema = z.object({
  name: z.string().min(2, { message: '请输入真实姓名' }),
  studentId: z.string().min(5, { message: '请输入有效的学号' }),
  college: z.string().min(2, { message: '请输入学院名称' }),
  class: z.string().min(2, { message: '请输入班级名称' }),
  password: z.string().min(6, { message: '密码至少需要6位' }),
  isDeveloper: z.boolean().default(false),
});

const teacherSchema = z.object({
  name: z.string().min(2, { message: '请输入真实姓名' }),
  institution: z.string().min(2, { message: '请输入单位名称' }),
  password: z.string().min(6, { message: '密码至少需要6位' }),
});

export default function RegisterPage() {
  const { login } = useAuth();
  
  const studentForm = useForm<z.infer<typeof studentSchema>>({
    resolver: zodResolver(studentSchema),
    defaultValues: {
      name: '',
      studentId: '',
      college: '',
      class: '',
      password: '',
      isDeveloper: false,
    },
  });

  const teacherForm = useForm<z.infer<typeof teacherSchema>>({
    resolver: zodResolver(teacherSchema),
    defaultValues: {
      name: '',
      institution: '',
      password: '',
    },
  });

  function onStudentSubmit(values: z.infer<typeof studentSchema>) {
    console.log('Student registration:', values);
    login({
      name: values.name,
      role: values.isDeveloper ? 'developer' : 'student'
    });
  }

  function onTeacherSubmit(values: z.infer<typeof teacherSchema>) {
    console.log('Teacher registration:', values);
    login({
      name: values.name,
      role: 'teacher'
    });
  }

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-10rem)] bg-muted/20 py-12">
      <Card className="w-full max-w-lg shadow-2xl bg-card/60 backdrop-blur-lg border-primary/20">
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
                  <FormField control={studentForm.control} name="name" render={({ field }) => (
                    <FormItem>
                      <FormLabel>姓名</FormLabel>
                      <FormControl><Input placeholder="请输入您的姓名" {...field} /></FormControl>
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
                  <Button type="submit" className="w-full" size="lg">注册学生账户</Button>
                </form>
              </Form>
            </TabsContent>
            <TabsContent value="teacher">
              <Form {...teacherForm}>
                <form onSubmit={teacherForm.handleSubmit(onTeacherSubmit)} className="space-y-6 pt-4">
                   <FormField control={teacherForm.control} name="name" render={({ field }) => (
                    <FormItem>
                      <FormLabel>姓名</FormLabel>
                      <FormControl><Input placeholder="请输入您的姓名" {...field} /></FormControl>
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
