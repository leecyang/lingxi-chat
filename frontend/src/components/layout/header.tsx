'use client';

import Link from 'next/link';
import { BrainCircuit, LogOut, Shield } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/contexts/auth-context';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import Image from 'next/image';

export default function Header() {
  const { user, logout } = useAuth();

  const getRoleName = (role: string | undefined) => {
    switch (role) {
      case 'admin': return '管理员';
      case 'teacher': return '教师';
      case 'developer': return '开发者';
      case 'student': return '学生';
      default: return '';
    }
  }

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 items-center">
        <Link href="/" className="flex items-center gap-2 mr-6">
          <Image 
            src="/logo.png" 
            alt="灵犀智学" 
            width={24} 
            height={24}
          />
          <span className="font-bold font-headline text-lg">灵犀智学</span>
        </Link>
        <nav className="flex items-center gap-4 text-sm">
          <Link href="/playbook" className="text-muted-foreground transition-colors hover:text-foreground">
            智能体矩阵
          </Link>
          {user && (
            <>
              <Link href="/group-chat" className="text-muted-foreground transition-colors hover:text-foreground">
                群聊
              </Link>
              <Link href="/ar-dialogue" className="text-muted-foreground transition-colors hover:text-foreground">
                AR 对话
              </Link>
              {user.role === 'admin' && (
                  <Link href="/admin" className="text-muted-foreground transition-colors hover:text-foreground flex items-center gap-1">
                      <Shield className="h-4 w-4" />
                      管理后台
                  </Link>
              )}
            </>
          )}
        </nav>
        <div className="flex flex-1 items-center justify-end gap-4">
          {user ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="relative h-9 w-9 rounded-full">
                  <Avatar className="h-9 w-9">
                    <AvatarFallback>{user.name.charAt(0).toUpperCase()}</AvatarFallback>
                  </Avatar>
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
                <DropdownMenuItem onClick={logout}>
                  <LogOut className="mr-2 h-4 w-4" />
                  <span>登出</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <>
              <Button variant="ghost" asChild>
                <Link href="/login">登录</Link>
              </Button>
              <Button asChild>
                <Link href="/register">注册</Link>
              </Button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
