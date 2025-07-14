'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { RefreshCw, CheckCircle, XCircle, AlertTriangle } from 'lucide-react';
import { chatService, authService } from '@/services/api-service';
import ProtectedRoute from '@/components/auth/protected-route';
import { toast } from 'sonner';

export default function TestTokenPage() {
  const [testResults, setTestResults] = useState<{
    tokenValidation?: { success: boolean; message: string };
    apiCall?: { success: boolean; message: string };
    tokenRefresh?: { success: boolean; message: string };
  }>({});
  const [isLoading, setIsLoading] = useState(false);

  const testTokenValidation = async () => {
    setIsLoading(true);
    try {
      const response = await authService.validateToken();
      const result = {
        success: response.success,
        message: response.success ? 'Token验证成功' : (response.message || 'Token验证失败')
      };
      setTestResults(prev => ({ ...prev, tokenValidation: result }));
      toast(result.success ? '✅ Token验证成功' : '❌ Token验证失败');
    } catch (error) {
      const result = { success: false, message: 'Token验证异常: ' + (error as Error).message };
      setTestResults(prev => ({ ...prev, tokenValidation: result }));
      toast('❌ Token验证异常');
    } finally {
      setIsLoading(false);
    }
  };

  const testApiCall = async () => {
    setIsLoading(true);
    try {
      const response = await chatService.sendMessage('测试消息 - 验证Token自动刷新功能');
      const result = {
        success: response.success,
        message: response.success ? 'API调用成功' : (response.message || 'API调用失败')
      };
      setTestResults(prev => ({ ...prev, apiCall: result }));
      toast(result.success ? '✅ API调用成功' : '❌ API调用失败');
    } catch (error) {
      const result = { success: false, message: 'API调用异常: ' + (error as Error).message };
      setTestResults(prev => ({ ...prev, apiCall: result }));
      toast('❌ API调用异常');
    } finally {
      setIsLoading(false);
    }
  };

  const testTokenRefresh = async () => {
    setIsLoading(true);
    try {
      const response = await authService.refreshToken();
      const result = {
        success: response.success,
        message: response.success ? 'Token刷新成功' : (response.message || 'Token刷新失败')
      };
      setTestResults(prev => ({ ...prev, tokenRefresh: result }));
      toast(result.success ? '✅ Token刷新成功' : '❌ Token刷新失败');
    } catch (error) {
      const result = { success: false, message: 'Token刷新异常: ' + (error as Error).message };
      setTestResults(prev => ({ ...prev, tokenRefresh: result }));
      toast('❌ Token刷新异常');
    } finally {
      setIsLoading(false);
    }
  };

  const clearResults = () => {
    setTestResults({});
  };

  const getStatusIcon = (success?: boolean) => {
    if (success === undefined) return <AlertTriangle className="w-4 h-4 text-muted-foreground" />;
    return success ? <CheckCircle className="w-4 h-4 text-green-500" /> : <XCircle className="w-4 h-4 text-red-500" />;
  };

  const getStatusBadge = (success?: boolean) => {
    if (success === undefined) return <Badge variant="secondary">未测试</Badge>;
    return success ? <Badge variant="default" className="bg-green-500">成功</Badge> : <Badge variant="destructive">失败</Badge>;
  };

  return (
    <ProtectedRoute>
      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold mb-2">Token自动刷新功能测试</h1>
          <p className="text-muted-foreground">
            测试前端Token过期自动刷新机制是否正常工作
          </p>
        </div>

        <div className="grid gap-6 md:grid-cols-2">
          {/* 测试操作卡片 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <RefreshCw className="w-5 h-5" />
                测试操作
              </CardTitle>
              <CardDescription>
                点击下方按钮执行不同的测试
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Button 
                onClick={testTokenValidation} 
                disabled={isLoading}
                className="w-full"
                variant="outline"
              >
                {isLoading ? '测试中...' : '测试Token验证'}
              </Button>
              
              <Button 
                onClick={testApiCall} 
                disabled={isLoading}
                className="w-full"
                variant="outline"
              >
                {isLoading ? '测试中...' : '测试API调用'}
              </Button>
              
              <Button 
                onClick={testTokenRefresh} 
                disabled={isLoading}
                className="w-full"
                variant="outline"
              >
                {isLoading ? '测试中...' : '测试Token刷新'}
              </Button>
              
              <Separator />
              
              <Button 
                onClick={clearResults} 
                disabled={isLoading}
                className="w-full"
                variant="secondary"
              >
                清除结果
              </Button>
            </CardContent>
          </Card>

          {/* 测试结果卡片 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <CheckCircle className="w-5 h-5" />
                测试结果
              </CardTitle>
              <CardDescription>
                各项测试的执行结果
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* Token验证结果 */}
              <div className="flex items-center justify-between p-3 rounded-lg border">
                <div className="flex items-center gap-2">
                  {getStatusIcon(testResults.tokenValidation?.success)}
                  <span className="font-medium">Token验证</span>
                </div>
                <div className="flex items-center gap-2">
                  {getStatusBadge(testResults.tokenValidation?.success)}
                </div>
              </div>
              {testResults.tokenValidation && (
                <p className="text-sm text-muted-foreground ml-6">
                  {testResults.tokenValidation.message}
                </p>
              )}

              {/* API调用结果 */}
              <div className="flex items-center justify-between p-3 rounded-lg border">
                <div className="flex items-center gap-2">
                  {getStatusIcon(testResults.apiCall?.success)}
                  <span className="font-medium">API调用</span>
                </div>
                <div className="flex items-center gap-2">
                  {getStatusBadge(testResults.apiCall?.success)}
                </div>
              </div>
              {testResults.apiCall && (
                <p className="text-sm text-muted-foreground ml-6">
                  {testResults.apiCall.message}
                </p>
              )}

              {/* Token刷新结果 */}
              <div className="flex items-center justify-between p-3 rounded-lg border">
                <div className="flex items-center gap-2">
                  {getStatusIcon(testResults.tokenRefresh?.success)}
                  <span className="font-medium">Token刷新</span>
                </div>
                <div className="flex items-center gap-2">
                  {getStatusBadge(testResults.tokenRefresh?.success)}
                </div>
              </div>
              {testResults.tokenRefresh && (
                <p className="text-sm text-muted-foreground ml-6">
                  {testResults.tokenRefresh.message}
                </p>
              )}
            </CardContent>
          </Card>
        </div>

        {/* 说明信息 */}
        <Card className="mt-6">
          <CardHeader>
            <CardTitle>测试说明</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm text-muted-foreground">
            <p><strong>Token验证:</strong> 验证当前Token是否有效</p>
            <p><strong>API调用:</strong> 发送一条测试消息，验证API调用和Token自动刷新机制</p>
            <p><strong>Token刷新:</strong> 手动触发Token刷新操作</p>
            <p className="mt-4 p-3 bg-blue-50 dark:bg-blue-950 rounded-lg border border-blue-200 dark:border-blue-800">
              <strong>注意:</strong> 如果Token过期，系统会自动尝试刷新Token并重试请求。如果刷新失败，会自动跳转到登录页面。
            </p>
          </CardContent>
        </Card>
      </div>
    </ProtectedRoute>
  );
}