'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/auth-context';
import ProtectedRoute from '@/components/auth/protected-route';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ShieldCheck, UserCheck, MessageSquare, CheckCircle, XCircle, Power, Trash2, Edit } from 'lucide-react';
import { adminService, agentService, developerService } from '@/services/api-service';
import { refreshAgents } from '@/lib/agents';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from '@/components/ui/alert-dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { AgentSubmission, DeveloperApplication, ConversationLog } from '@/lib/types';

// 为了兼容后端返回的数据结构，定义一个别名
type DeveloperRequest = DeveloperApplication;

// 数据接口定义
interface PendingAgent {
  id: string;
  name: string;
  description: string;
  apiUrl?: string;  // AgentSubmission有此字段，Agent没有
  endpoint?: string;  // Agent有此字段，AgentSubmission没有
  submittedBy?: string;  // 用于显示，需要从creator或submitter字段映射
  creator?: { 
    id: number;
    nickname?: string;
    name?: string;
    username?: string;
  };  // Agent实体的创建者
  submitter?: { 
    id: number;
    nickname?: string;
    name?: string;
    username?: string;
  };  // AgentSubmission实体的提交者
  status?: string;  // 状态字段
  displayName?: string;  // Agent实体的显示名称
  modelId?: string;  // Agent实体的模型ID
  avatar?: string;  // Agent实体的头像
  appId?: string;  // 九天平台字段
  apiKey?: string;  // API密钥
  token?: string;  // Token
  category?: string;  // AgentSubmission的类别
  type?: string;  // Agent的类型
  enabled?: boolean;  // Agent是否启用
  priority?: number;  // Agent优先级
  createdAt?: string;
  updatedAt?: string;
}

interface DeveloperRequest {
  id: string;
  name: string;
  studentId: string;
  college: string;
  class: string;
}

// 使用全局类型定义，移除本地接口定义


export default function AdminDashboardPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [pendingAgents, setPendingAgents] = useState<PendingAgent[]>([]);
  const [approvedAgents, setApprovedAgents] = useState<PendingAgent[]>([]);
  const [developerRequests, setDeveloperRequests] = useState<DeveloperRequest[]>([]);
  const [conversationLogs, setConversationLogs] = useState<ConversationLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [agentToDelete, setAgentToDelete] = useState<string | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [agentToEdit, setAgentToEdit] = useState<PendingAgent | null>(null);
  const [editForm, setEditForm] = useState({ name: '', description: '' });

  useEffect(() => {
    // Extra layer of protection: redirect if not an admin.
    if (user && user.role !== 'admin') {
      router.push('/');
    } else if (user && user.role === 'admin') {
      fetchAdminData();
    }
  }, [user, router]);

  const fetchAdminData = async () => {
    try {
      setLoading(true);
      // 并行获取所有管理数据
      const [agentsRes, submissionsRes, devsRes, logsRes, approvedAgentsRes] = await Promise.all([
        adminService.getPendingAgents(),
        adminService.getPendingSubmissions(),
        adminService.getDeveloperRequests(),
        adminService.getConversationLogs(),
        adminService.getApprovedAgents()
      ]);

      // 获取智能体和提交数据

      // 处理待审核智能体数据（现在统一使用ApiResponse格式）
      let allPendingAgents: PendingAgent[] = [];
      
      if (agentsRes && agentsRes.success && agentsRes.data) {
        // AgentController现在也返回ApiResponse<Page<Agent>>格式
        const agents = agentsRes.data.content || agentsRes.data || [];
        const mappedAgents = agents.map((agent: any) => ({
          id: agent.id,
          name: agent.name,
          description: agent.description,
          endpoint: agent.endpoint,
          submittedBy: agent.creator?.nickname || agent.creator?.name || '未知用户',
          creator: agent.creator,
          status: agent.status
        }));
        allPendingAgents = [...mappedAgents];
      }

      // 处理智能体提交申请数据（AgentSubmissionController返回ApiResponse格式）
      if (submissionsRes && submissionsRes.success && submissionsRes.data) {
        // AgentSubmissionController返回的是ApiResponse<Page<AgentSubmission>>格式
        const submissions = submissionsRes.data.content || submissionsRes.data || [];
        const mappedSubmissions = submissions.map((submission: any) => ({
          id: submission.id,
          name: submission.name,
          description: submission.description,
          apiUrl: submission.apiUrl,
          submittedBy: submission.submitter?.nickname || submission.submitter?.name || '未知用户',
          submitter: submission.submitter,
          status: submission.status
        }));
        allPendingAgents = [...allPendingAgents, ...mappedSubmissions];
      }
      
      setPendingAgents(allPendingAgents);

      if (devsRes.success && devsRes.data) {
        // 处理开发者申请的分页数据
        const devsData = devsRes.data.content || devsRes.data || [];
        // 映射后端字段到前端接口
        const mappedDevsData = devsData.map((dev: any) => ({
          ...dev,
          skills: dev.skills,
          experience: dev.experience,
          contactInfo: dev.contactInfo,
          // 保持兼容性
          contact: dev.contactInfo,
          applicantName: dev.user?.nickname || dev.user?.name || dev.user?.username || '未知用户'
        }));
        setDeveloperRequests(mappedDevsData);
      }

      if (logsRes.success && logsRes.data) {
        // 处理对话日志的分页数据
        const logsData = logsRes.data.content || logsRes.data || [];
        setConversationLogs(logsData);
      }

      if (approvedAgentsRes.success && approvedAgentsRes.data) {
        const approvedAgentsData = approvedAgentsRes.data.content || approvedAgentsRes.data || [];
        setApprovedAgents(approvedAgentsData);
      }
    } catch (error) {
      // 获取管理员数据失败
    } finally {
      setLoading(false);
    }
  };

  // Render null or a loading spinner while checking the user role
  if (!user || user.role !== 'admin') {
    return null;
  }

  const handleApprove = async (type: string, id: string, itemData?: PendingAgent) => {
    try {
      let response;
      if (type === 'agent') {
        // 判断是Agent还是AgentSubmission
        if (itemData?.creator) {
          // 这是Agent实体，调用Agent审核API
          response = await fetch(`/api/agents/${id}/review`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${localStorage.getItem('access_token')}`
            },
            body: JSON.stringify({
              status: 'APPROVED',
              comment: '管理员批准'
            })
          }).then(res => res.json());
        } else if (itemData?.submitter) {
          // 这是AgentSubmission实体，调用AgentSubmission审核API
          response = await agentService.reviewSubmission(id, {
            reviewerId: user?.id,
            status: 'APPROVED',
            notes: '管理员批准'
          });
        }
      } else if (type === 'developer') {
        // 对于开发者申请，调用开发者审核API
        response = await developerService.reviewRequest(id, {
          approved: true,
          notes: '管理员批准'
        });
      }
      
      if (response && (response.success || response.message)) {
        // 重新获取数据以更新UI
        fetchAdminData();
      }
    } catch (error) {
      // 审批失败
    }
  };

  const handleReject = async (type: string, id: string, itemData?: PendingAgent) => {
    try {
      let response;
      if (type === 'agent') {
        // 判断是Agent还是AgentSubmission
        if (itemData?.creator) {
          // 这是Agent实体，调用Agent审核API
          response = await fetch(`/api/agents/${id}/review`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${localStorage.getItem('access_token')}`
            },
            body: JSON.stringify({
              status: 'REJECTED',
              comment: '管理员拒绝'
            })
          }).then(res => res.json());
        } else if (itemData?.submitter) {
          // 这是AgentSubmission实体，调用AgentSubmission审核API
          response = await agentService.reviewSubmission(id, {
            reviewerId: user?.id,
            status: 'REJECTED',
            notes: '管理员拒绝'
          });
        }
      } else if (type === 'developer') {
        // 对于开发者申请，调用开发者审核API
        response = await developerService.reviewRequest(id, {
          approved: false,
          notes: '管理员拒绝'
        });
      }
      
      if (response && (response.success || response.message)) {
        // 重新获取数据以更新UI
        fetchAdminData();
      }
    } catch (error) {
      // 拒绝失败
    }
  };

  const handleDeactivate = async (id: string) => {
    try {
      const response = await agentService.deactivateAgent(id);
      
      if (response.success) {
        fetchAdminData();
        // 刷新全局智能体缓存
        await refreshAgents();
      } else {
        // 停用智能体失败
        alert(`下线失败: ${response.message || '未知错误'}`);
      }
    } catch (error) {
      // 停用智能体异常
      alert('网络错误，请稍后重试');
    }
  };

  const handleActivate = async (id: string) => {
    try {
      const response = await agentService.activateAgent(id);
      
      if (response.success) {
        fetchAdminData();
        // 刷新全局智能体缓存
        await refreshAgents();
      } else {
        // 激活智能体失败
        alert(`上线失败: ${response.message || '未知错误'}`);
      }
    } catch (error) {
      // 激活智能体异常
      alert('网络错误，请稍后重试');
    }
  };

  const handleDeleteAgent = async () => {
    if (!agentToDelete) return;
    
    try {
      const response = await agentService.deleteAgent(agentToDelete);
      
      if (response.success) {
        fetchAdminData();
        setDeleteDialogOpen(false);
        setAgentToDelete(null);
        // 刷新全局智能体缓存
        await refreshAgents();
      } else {
        // 删除智能体失败
        alert(`删除失败: ${response.message || '未知错误'}`);
      }
    } catch (error) {
      // 删除智能体异常
      alert('网络错误，请稍后重试');
    }
  };

  const openDeleteDialog = (id: string) => {
    setAgentToDelete(id);
    setDeleteDialogOpen(true);
  };

  const openEditDialog = (agent: PendingAgent) => {
    setAgentToEdit(agent);
    setEditForm({ name: agent.name, description: agent.description });
    setEditDialogOpen(true);
  };

  const handleEditAgent = async () => {
    if (!agentToEdit) return;
    
    try {
      // 构建完整的Agent对象，保留原有字段并更新编辑的字段
      const updatedAgent = {
        ...agentToEdit,
        name: editForm.name,
        displayName: editForm.name, // 同步更新displayName
        description: editForm.description,
        // 确保必需字段存在
        modelId: agentToEdit.modelId || 'default-model',
        endpoint: agentToEdit.endpoint || 'http://localhost:8080/api/chat',
        type: agentToEdit.type || 'JIUTIAN',
        status: agentToEdit.status || 'APPROVED',
        enabled: agentToEdit.enabled !== undefined ? agentToEdit.enabled : true,
        priority: agentToEdit.priority || 0
      };
      
      const response = await agentService.updateAgent(agentToEdit.id, updatedAgent);
      
      if (response.success) {
        fetchAdminData();
        setEditDialogOpen(false);
        setAgentToEdit(null);
        setEditForm({ name: '', description: '' });
        // 刷新全局智能体缓存
        await refreshAgents();
      } else {
        // 编辑智能体失败
        alert(`编辑失败: ${response.message || '未知错误'}`);
      }
    } catch (error: any) {
      // 编辑智能体异常
      alert('网络错误，请稍后重试');
    }
  };

  if (loading) {
    return (
      <ProtectedRoute>
        <div className="container mx-auto px-4 py-12 text-center">
          <p>加载中...</p>
        </div>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute>
      <div className="container mx-auto px-4 py-12">
        <section className="text-center mb-12">
          <h1 className="text-4xl md:text-5xl font-bold font-headline">管理后台</h1>
          <p className="mt-4 max-w-2xl mx-auto text-lg text-muted-foreground">
            管理平台内容与用户权限
          </p>
        </section>

        <Tabs defaultValue="approved-agents" className="w-full">
          <TabsList className="grid w-full grid-cols-4 max-w-3xl mx-auto">
            <TabsTrigger value="approved-agents"><CheckCircle className="mr-2" />已上线智能体</TabsTrigger>
            <TabsTrigger value="agent-approval"><ShieldCheck className="mr-2" />智能体审核</TabsTrigger>
            <TabsTrigger value="developer-requests"><UserCheck className="mr-2" />开发者申请</TabsTrigger>
            <TabsTrigger value="conversation-logs"><MessageSquare className="mr-2" />对话日志</TabsTrigger>
          </TabsList>

          <TabsContent value="approved-agents" className="mt-8">
            <Card className="bg-card/60 backdrop-blur-lg border-border/20">
              <CardHeader>
                <CardTitle>已上线智能体</CardTitle>
                <CardDescription>管理已上线的智能体，可执行下线操作。</CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>名称</TableHead>
                      <TableHead>描述</TableHead>
                      <TableHead>创建者</TableHead>
                      <TableHead className="text-center">审核状态</TableHead>
                      <TableHead className="text-center">运行状态</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {approvedAgents.map((agent) => (
                      <TableRow key={agent.id}>
                        <TableCell className="font-medium">{agent.name}</TableCell>
                        <TableCell className="text-muted-foreground max-w-sm">{agent.description}</TableCell>
                        <TableCell>{agent.creator?.nickname}</TableCell>
                        <TableCell className="text-center"><Badge variant="default">{agent.status}</Badge></TableCell>
                        <TableCell className="text-center">
                          <Badge variant={agent.enabled ? "default" : "secondary"}>
                            {agent.enabled ? "运行中" : "已下线"}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right space-x-2">
                          <Button variant="outline" size="sm" onClick={() => openEditDialog(agent)}>
                            <Edit className="h-4 w-4 mr-1" />
                            编辑
                          </Button>
                          {agent.enabled ? (
                            <Button variant="destructive" size="sm" onClick={() => handleDeactivate(agent.id)}>
                              <Power className="h-4 w-4 mr-1" />
                              下线
                            </Button>
                          ) : (
                            <>
                              <Button variant="default" size="sm" onClick={() => handleActivate(agent.id)}>
                                <Power className="h-4 w-4 mr-1" />
                                上线
                              </Button>
                              <Button variant="destructive" size="sm" onClick={() => openDeleteDialog(agent.id)}>
                                <Trash2 className="h-4 w-4 mr-1" />
                                删除
                              </Button>
                            </>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>

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
                          <Button variant="ghost" size="icon" className="text-green-500 hover:text-green-400" onClick={() => handleApprove('agent', agent.id, agent)}><CheckCircle className="h-5 w-5" /></Button>
                          <Button variant="ghost" size="icon" className="text-red-500 hover:text-red-400" onClick={() => handleReject('agent', agent.id, agent)}><XCircle className="h-5 w-5" /></Button>
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
                      <TableHead>会话ID</TableHead>
                      <TableHead>用户</TableHead>
                      <TableHead>智能体</TableHead>
                      <TableHead>消息数量</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead>创建时间</TableHead>
                      <TableHead>操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {conversationLogs.map((log) => (
                      <TableRow key={log.id}>
                        <TableCell className="font-mono text-sm">{log.sessionId}</TableCell>
                        <TableCell className="font-medium">{log.user.name}</TableCell>
                        <TableCell className="font-medium text-accent">{log.agent.name}</TableCell>
                        <TableCell className="text-center">{log.messageCount}</TableCell>
                        <TableCell>
                          <Badge 
                            variant={log.status === 'ACTIVE' ? 'default' : log.status === 'COMPLETED' ? 'secondary' : 'outline'}
                            className={log.isFlagged ? 'border-red-500 text-red-500' : ''}
                          >
                            {log.isFlagged ? '已标记' : log.status}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-muted-foreground">
                          {new Date(log.createdAt).toLocaleString('zh-CN')}
                        </TableCell>
                        <TableCell>
                          {log.isFlagged ? (
                            <Button variant="ghost" size="sm" className="text-green-500 hover:text-green-400">
                              取消标记
                            </Button>
                          ) : (
                            <Button variant="ghost" size="sm" className="text-red-500 hover:text-red-400">
                              标记
                            </Button>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
      
      {/* 删除确认对话框 */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除</AlertDialogTitle>
            <AlertDialogDescription>
              您确定要删除这个智能体吗？此操作无法撤销，智能体的所有数据将被永久删除。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setDeleteDialogOpen(false)}>取消</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteAgent} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
              删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* 编辑智能体对话框 */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>编辑智能体</DialogTitle>
            <DialogDescription>
              修改智能体的名称和描述信息。
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="name" className="text-right">
                名称
              </Label>
              <Input
                id="name"
                value={editForm.name}
                onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                className="col-span-3"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="description" className="text-right">
                描述
              </Label>
              <Textarea
                id="description"
                value={editForm.description}
                onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
                className="col-span-3"
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditDialogOpen(false)}>
              取消
            </Button>
            <Button onClick={handleEditAgent}>
              保存更改
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </ProtectedRoute>
  );
}
