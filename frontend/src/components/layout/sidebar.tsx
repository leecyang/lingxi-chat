'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LogOut, Shield, MessageSquare, BookOpen, Headphones, User, UserPlus, Settings, ChevronLeft, ChevronRight, Menu, Play } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/contexts/auth-context';
import { useSidebar } from '@/contexts/sidebar-context';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { cn } from '@/lib/utils';
import Image from 'next/image';

export default function Sidebar() {
  const { user, logout } = useAuth();
  const { isCollapsed, toggleSidebar } = useSidebar();
  const pathname = usePathname();

  const getRoleName = (role: string | undefined) => {
    switch (role) {
      case 'admin': return '管理员';
      case 'teacher': return '教师';
      case 'developer': return '开发者';
      case 'student': return '学生';
      default: return '';
    }
  };

  const navigationItems = [
    {
      href: '/demo',
      label: '演示场景',
      icon: Play,
      requireAuth: false
    },
    {
      href: '/group-chat',
      label: 'AI对话',
      icon: MessageSquare,
      requireAuth: true
    },
    {
      href: '/ar-dialogue',
      label: 'AR对话',
      icon: Headphones,
      requireAuth: true
    },
    {
      href: '/playbook',
      label: '智能体矩阵',
      icon: BookOpen,
      requireAuth: false
    }
  ];

  return (
    <TooltipProvider>
      <div className={cn(
        "fixed left-0 top-0 z-50 h-full bg-background border-r transition-all duration-300 ease-in-out",
        isCollapsed ? "w-16" : "w-64"
      )}>
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b">
          {!isCollapsed && (
            <Link href="/" className="flex items-center gap-2">
              <Image 
                src="/logo.png" 
                alt="灵犀智学" 
                width={24} 
                height={24} 
                className="flex-shrink-0"
              />
              <span className="font-bold font-headline text-lg whitespace-nowrap">灵犀智学</span>
            </Link>
          )}
          {isCollapsed && (
            <Link href="/" className="flex items-center justify-center w-full">
              <Image 
                src="/logo.png" 
                alt="灵犀智学" 
                width={24} 
                height={24} 
                className="flex-shrink-0"
              />
            </Link>
          )}
          <Button
            variant="ghost"
            size="sm"
            onClick={toggleSidebar}
            className={cn(
              "h-8 w-8 p-0 hover:bg-accent",
              isCollapsed && "absolute -right-3 top-4 bg-background border shadow-md"
            )}
          >
            {isCollapsed ? (
              <ChevronRight className="h-4 w-4" />
            ) : (
              <ChevronLeft className="h-4 w-4" />
            )}
          </Button>
        </div>

        {/* Navigation */}
        <nav className="flex flex-col gap-2 p-4">
          {navigationItems.map((item) => {
            if (item.requireAuth && !user) return null;
            
            const Icon = item.icon;
            const linkContent = (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors hover:bg-accent hover:text-accent-foreground",
                  pathname === item.href ? "bg-primary text-primary-foreground hover:bg-primary/90" : "text-muted-foreground",
                  isCollapsed && "justify-center"
                )}
              >
                <Icon className={cn(
                  "h-4 w-4 flex-shrink-0",
                  pathname === item.href && "text-white"
                )} />
                {!isCollapsed && (
                  <span className={cn(
                    "whitespace-nowrap",
                    pathname === item.href && "text-white"
                  )}>
                    {item.label}
                  </span>
                )}
              </Link>
            );

            if (isCollapsed) {
              return (
                <Tooltip key={item.href}>
                  <TooltipTrigger asChild>
                    {linkContent}
                  </TooltipTrigger>
                  <TooltipContent side="right">
                    <p>{item.label}</p>
                  </TooltipContent>
                </Tooltip>
              );
            }

            return linkContent;
          })}
        
          {user && user.role === 'admin' && (() => {
            const adminLinkContent = (
              <Link
                href="/admin"
                className={cn(
                  "flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors hover:bg-accent hover:text-accent-foreground",
                  pathname === '/admin' ? "bg-primary text-primary-foreground hover:bg-primary/90" : "text-muted-foreground",
                  isCollapsed && "justify-center"
                )}
              >
                <Shield className={cn(
                  "h-4 w-4 flex-shrink-0",
                  pathname === '/admin' && "text-white"
                )} />
                {!isCollapsed && (
                  <span className={cn(
                    "whitespace-nowrap",
                    pathname === '/admin' && "text-white"
                  )}>
                    管理后台
                  </span>
                )}
              </Link>
            );

            if (isCollapsed) {
              return (
                <Tooltip key="admin">
                  <TooltipTrigger asChild>
                    {adminLinkContent}
                  </TooltipTrigger>
                  <TooltipContent side="right">
                    <p>管理后台</p>
                  </TooltipContent>
                </Tooltip>
              );
            }

            return adminLinkContent;
          })()}
      </nav>

        {/* User Section */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t">
          {user ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className={cn(
                  "w-full h-auto p-3 transition-all",
                  isCollapsed ? "justify-center" : "justify-start gap-3"
                )}>
                  <div className={cn(
                    "flex items-center",
                    isCollapsed ? "justify-center" : "gap-3"
                  )}>
                    <Avatar className="h-8 w-8 flex-shrink-0">
                      <AvatarFallback className="bg-primary text-primary-foreground">
                        {user.name.charAt(0).toUpperCase()}
                      </AvatarFallback>
                    </Avatar>
                    {!isCollapsed && (
                      <div className="flex flex-col items-start">
                        <span className="text-sm font-medium">{user.name}</span>
                        <span className="text-xs text-muted-foreground">{getRoleName(user.role)}</span>
                      </div>
                    )}
                  </div>
                </Button>
              </DropdownMenuTrigger>
            <DropdownMenuContent className="w-56" align="end" forceMount>
              <DropdownMenuLabel className="font-normal">
                <div className="flex flex-col space-y-1">
                  <p className="text-sm font-medium leading-none">{user.name}</p>
                  <p className="text-xs leading-none text-muted-foreground">
                    {getRoleName(user.role)}
                  </p>
                </div>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem asChild>
                <Link href="/settings">
                  <Settings className="mr-2 h-4 w-4" />
                  <span>账户设置</span>
                </Link>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={logout}>
                <LogOut className="mr-2 h-4 w-4" />
                <span>登出</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          ) : (
            <div className="flex flex-col gap-2">
              {isCollapsed ? (
                <>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button variant="ghost" asChild className="w-full justify-center p-3">
                        <Link href="/login">
                          <User className="h-4 w-4" />
                        </Link>
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent side="right">
                      <p>登录</p>
                    </TooltipContent>
                  </Tooltip>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button asChild className="w-full justify-center p-3">
                        <Link href="/register">
                          <UserPlus className="h-4 w-4" />
                        </Link>
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent side="right">
                      <p>注册</p>
                    </TooltipContent>
                  </Tooltip>
                </>
              ) : (
                <>
                  <Button variant="ghost" asChild className="w-full justify-start gap-2">
                    <Link href="/login">
                      <User className="h-4 w-4" />
                      <span>登录</span>
                    </Link>
                  </Button>
                  <Button asChild className="w-full justify-start gap-2">
                    <Link href="/register">
                      <UserPlus className="h-4 w-4" />
                      <span>注册</span>
                    </Link>
                  </Button>
                </>
              )}
            </div>
          )}</div>
      </div>
    </TooltipProvider>
  );
}