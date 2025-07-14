'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { useAuth } from '@/contexts/auth-context';
import ProtectedRoute from '@/components/auth/protected-route';
import { toast } from 'sonner';
import { Settings, Lock, User } from 'lucide-react';
import { Separator } from '@/components/ui/separator';
import { userService } from '@/services/api-service';

const passwordSchema = z.object({
  oldPassword: z.string().min(1, { message: '请输入当前密码' }),
  newPassword: z.string().min(6, { message: '新密码至少为6位' }),
  confirmPassword: z.string().min(6, { message: '请确认新密码' }),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: '两次输入的密码不一致',
  path: ['confirmPassword'],
});

type PasswordFormData = z.infer<typeof passwordSchema>;

export default function SettingsPage() {
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(false);

  const passwordForm = useForm<PasswordFormData>({
    resolver: zodResolver(passwordSchema),
    defaultValues: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const onPasswordSubmit = async (data: PasswordFormData) => {
    setIsLoading(true);
    try {
      const response = await userService.changePassword({
        oldPassword: data.oldPassword,
        newPassword: data.newPassword,
      });

      if (response.success) {
        toast.success('密码修改成功');
        passwordForm.reset();
      } else {
        toast.error(response.error || '密码修改失败');
      }
    } catch (error) {
      // 密码修改失败
      toast.error('网络错误，请稍后重试');
    } finally {
      setIsLoading(false);
    }
  };

  const getRoleName = (role: string | undefined) => {
    switch (role) {
      case 'admin': return '管理员';
      case 'teacher': return '教师';
      case 'developer': return '开发者';
      case 'student': return '学生';
      default: return '';
    }
  };

  return (
    <ProtectedRoute>
      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <div className="flex items-center gap-3 mb-8">
          <Settings className="w-8 h-8 text-primary" />
          <h1 className="text-3xl font-bold font-headline">账户设置</h1>
        </div>

        <div className="grid gap-6">
          {/* 用户信息卡片 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="w-5 h-5" />
                个人信息
              </CardTitle>
              <CardDescription>
                查看您的账户基本信息
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-muted-foreground">用户名</label>
                  <p className="text-sm font-mono bg-muted px-3 py-2 rounded-md">{user?.name}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">角色</label>
                  <p className="text-sm bg-muted px-3 py-2 rounded-md">{getRoleName(user?.role)}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* 修改密码卡片 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Lock className="w-5 h-5" />
                修改密码
              </CardTitle>
              <CardDescription>
                为了账户安全，建议定期更换密码
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Form {...passwordForm}>
                <form onSubmit={passwordForm.handleSubmit(onPasswordSubmit)} className="space-y-4">
                  <FormField
                    control={passwordForm.control}
                    name="oldPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>当前密码</FormLabel>
                        <FormControl>
                          <Input
                            type="password"
                            placeholder="请输入当前密码"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  
                  <Separator />
                  
                  <FormField
                    control={passwordForm.control}
                    name="newPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>新密码</FormLabel>
                        <FormControl>
                          <Input
                            type="password"
                            placeholder="请输入新密码（至少6位）"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  
                  <FormField
                    control={passwordForm.control}
                    name="confirmPassword"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>确认新密码</FormLabel>
                        <FormControl>
                          <Input
                            type="password"
                            placeholder="请再次输入新密码"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  
                  <Button type="submit" disabled={isLoading} className="w-full">
                    {isLoading ? '修改中...' : '修改密码'}
                  </Button>
                </form>
              </Form>
            </CardContent>
          </Card>
        </div>
      </div>
    </ProtectedRoute>
  );
}