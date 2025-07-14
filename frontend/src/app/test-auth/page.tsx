'use client';

import { useAuth } from '@/contexts/auth-context';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function TestAuthPage() {
  const { user, logout, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-lg">加载中...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-8">
      <Card className="max-w-md mx-auto">
        <CardHeader>
          <CardTitle>认证状态测试</CardTitle>
          <CardDescription>测试登录状态和刷新页面后的持久化</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {user ? (
            <div className="space-y-4">
              <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
                <h3 className="font-semibold text-green-800">已登录</h3>
                <p className="text-green-700">用户名: {user.name}</p>
                <p className="text-green-700">角色: {user.role}</p>
              </div>
              
              <div className="space-y-2">
                <p className="text-sm text-gray-600">存储的Token信息:</p>
                <div className="text-xs bg-gray-100 p-2 rounded">
                  <p>Access Token: {localStorage.getItem('access_token') ? '已存储' : '未存储'}</p>
                  <p>Refresh Token: {localStorage.getItem('refresh_token') ? '已存储' : '未存储'}</p>
                  <p>User Info: {localStorage.getItem('user_info') ? '已存储' : '未存储'}</p>
                </div>
              </div>
              
              <Button onClick={logout} variant="destructive" className="w-full">
                退出登录
              </Button>
              
              <div className="text-sm text-gray-600">
                <p>💡 提示: 刷新页面测试登录状态是否保持</p>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                <h3 className="font-semibold text-red-800">未登录</h3>
                <p className="text-red-700">请先登录以测试认证功能</p>
              </div>
              
              <Button 
                onClick={() => window.location.href = '/login'} 
                className="w-full"
              >
                前往登录
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}