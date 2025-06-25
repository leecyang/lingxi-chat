'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/auth-context';
import ProtectedRoute from '@/components/auth/protected-route';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ShieldCheck, UserCheck, MessageSquare, CheckCircle, XCircle } from 'lucide-react';

// Mock Data
const pendingAgents = [
  { id: 'agent1', name: 'Quantum Physics Simulator', description: 'Simulates quantum physics experiments for advanced students.', apiUrl: 'https://api.example.com/quantum', submittedBy: 'Dr. Evelyn Reed' },
  { id: 'agent2', name: 'Creative Writing Partner', description: 'Helps students brainstorm ideas and overcome writer\'s block.', apiUrl: 'https://api.example.com/writing', submittedBy: 'Alex Chen (Developer)' },
  { id: 'agent3', name: 'Music Theory Tutor', description: 'Explains music theory concepts with interactive examples.', apiUrl: 'https://api.example.com/music', submittedBy: 'Ben Carter (Developer)' },
];

const developerRequests = [
  { id: 'devreq1', name: 'Alice Johnson', studentId: 'S12345', college: 'College of Engineering', class: 'CS 2024' },
  { id: 'devreq2', name: 'Bob Williams', studentId: 'S67890', college: 'College of Science', class: 'Physics 2025' },
];

const conversationLogs = [
    { id: 'log1', timestamp: '10:45 AM', userName: 'Charlie Brown', agentName: '数学解题大师', message: '请解释一下勾股定理。' },
    { id: 'log2', timestamp: '10:47 AM', userName: '数学解题大师', agentName: 'Charlie Brown', message: '当然！在一个直角三角形中，斜边的平方等于两直角边的平方和。' },
    { id: 'log3', timestamp: '11:02 AM', userName: 'Dana Scully', agentName: '历史故事讲解员', message: '@历史故事讲解员 给我讲讲文艺复兴。' },
    { id: 'log4', timestamp: '11:05 AM', userName: '历史故事讲解员', agentName: 'Dana Scully', message: '文艺复兴是14-16世纪欧洲的一场思想文化运动...' },
    { id: 'log5', timestamp: '11:15 AM', userName: 'Eve Polastri', agentName: '通用知识问答', message: '为什么天空是蓝色的？' },
];


export default function AdminDashboardPage() {
  const { user } = useAuth();
  const router = useRouter();

  useEffect(() => {
    // Extra layer of protection: redirect if not an admin.
    if (user && user.role !== 'admin') {
      router.push('/');
    }
  }, [user, router]);

  // Render null or a loading spinner while checking the user role
  if (!user || user.role !== 'admin') {
    return null;
  }

  const handleApprove = (type: string, id: string) => {
    console.log(`Approved ${type} with id: ${id}`);
    // Add logic to update state/backend
  };

  const handleReject = (type: string, id: string) => {
    console.log(`Rejected ${type} with id: ${id}`);
    // Add logic to update state/backend
  };

  return (
    <ProtectedRoute>
      <div className="container mx-auto px-4 py-12">
        <section className="text-center mb-12">
          <h1 className="text-4xl md:text-5xl font-bold font-headline">管理后台</h1>
          <p className="mt-4 max-w-2xl mx-auto text-lg text-muted-foreground">
            管理平台内容与用户权限
          </p>
        </section>

        <Tabs defaultValue="agent-approval" className="w-full">
          <TabsList className="grid w-full grid-cols-3 max-w-2xl mx-auto">
            <TabsTrigger value="agent-approval"><ShieldCheck className="mr-2" />智能体审核</TabsTrigger>
            <TabsTrigger value="developer-requests"><UserCheck className="mr-2" />开发者申请</TabsTrigger>
            <TabsTrigger value="conversation-logs"><MessageSquare className="mr-2" />对话日志</TabsTrigger>
          </TabsList>

          <TabsContent value="agent-approval" className="mt-8">
            <Card className="bg-card/60 backdrop-blur-lg border-border/20">
              <CardHeader>
                <CardTitle>待审核的智能体</CardTitle>
                <CardDescription>审核由开发者提交的新智能体，批准后将对所有用户可见。</CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>名称</TableHead>
                      <TableHead>描述</TableHead>
                      <TableHead>提交者</TableHead>
                      <TableHead className="text-center">状态</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {pendingAgents.map((agent) => (
                      <TableRow key={agent.id}>
                        <TableCell className="font-medium">{agent.name}</TableCell>
                        <TableCell className="text-muted-foreground max-w-sm">{agent.description}</TableCell>
                        <TableCell>{agent.submittedBy}</TableCell>
                        <TableCell className="text-center"><Badge variant="outline" className="text-yellow-400 border-yellow-400/50">待审核</Badge></TableCell>
                        <TableCell className="text-right space-x-2">
                          <Button variant="ghost" size="icon" className="text-green-500 hover:text-green-400" onClick={() => handleApprove('agent', agent.id)}><CheckCircle className="h-5 w-5" /></Button>
                          <Button variant="ghost" size="icon" className="text-red-500 hover:text-red-400" onClick={() => handleReject('agent', agent.id)}><XCircle className="h-5 w-5" /></Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="developer-requests" className="mt-8">
            <Card className="bg-card/60 backdrop-blur-lg border-border/20">
              <CardHeader>
                <CardTitle>开发者权限申请</CardTitle>
                <CardDescription>审核希望成为智能体开发者的学生申请。</CardDescription>
              </CardHeader>
              <CardContent>
                 <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>姓名</TableHead>
                      <TableHead>学号</TableHead>
                      <TableHead>学院</TableHead>
                       <TableHead>班级</TableHead>
                      <TableHead className="text-center">状态</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {developerRequests.map((req) => (
                      <TableRow key={req.id}>
                        <TableCell className="font-medium">{req.name}</TableCell>
                        <TableCell>{req.studentId}</TableCell>
                        <TableCell>{req.college}</TableCell>
                        <TableCell>{req.class}</TableCell>
                        <TableCell className="text-center"><Badge variant="outline" className="text-yellow-400 border-yellow-400/50">待审核</Badge></TableCell>
                        <TableCell className="text-right space-x-2">
                          <Button variant="ghost" size="icon" className="text-green-500 hover:text-green-400" onClick={() => handleApprove('developer', req.id)}><CheckCircle className="h-5 w-5" /></Button>
                          <Button variant="ghost" size="icon" className="text-red-500 hover:text-red-400" onClick={() => handleReject('developer', req.id)}><XCircle className="h-5 w-5" /></Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="conversation-logs" className="mt-8">
            <Card className="bg-card/60 backdrop-blur-lg border-border/20">
              <CardHeader>
                <CardTitle>用户对话日志</CardTitle>
                <CardDescription>查看平台上的用户与智能体之间的对话记录。</CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>时间</TableHead>
                      <TableHead>发送方</TableHead>
                      <TableHead>接收方</TableHead>
                      <TableHead>消息内容</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {conversationLogs.map((log) => (
                      <TableRow key={log.id}>
                        <TableCell className="text-muted-foreground">{log.timestamp}</TableCell>
                        <TableCell className="font-medium">{log.userName}</TableCell>
                        <TableCell className="font-medium text-accent">{log.agentName}</TableCell>
                        <TableCell className="text-muted-foreground">{log.message}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </ProtectedRoute>
  );
}
