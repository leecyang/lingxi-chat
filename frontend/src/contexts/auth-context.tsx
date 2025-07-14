'use client';

import { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import type { User } from '@/lib/types';
import { authService } from '@/services/api-service';

interface AuthContextType {
  user: User | null;
  login: (user: User) => void;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    // 检查localStorage中是否有token和用户信息
    const checkAuth = () => {
      try {
        const token = localStorage.getItem('access_token');
        const userInfo = localStorage.getItem('user_info');
        
        if (token && userInfo) {
          try {
            // 简单恢复用户状态，不进行服务器验证
            const parsedUser = JSON.parse(userInfo);
            setUser(parsedUser);
          } catch (error) {
            // 用户信息解析失败
            // 清除损坏的用户信息
            localStorage.removeItem('access_token');
            localStorage.removeItem('refresh_token');
            localStorage.removeItem('user_info');
          }
        }
      } catch (error) {
        // 认证检查失败
        // 出错时清除可能损坏的数据
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('user_info');
      } finally {
        setLoading(false);
      }
    };
    
    checkAuth();
  }, []);

  const login = (userData: User) => {
    setUser(userData);
    // 存储用户信息到localStorage
    localStorage.setItem('user_info', JSON.stringify(userData));
    
    if (userData.role === 'admin') {
        router.push('/admin');
    } else {
        router.push('/group-chat');
    }
  };

  const logout = () => {
    setUser(null);
    // 清除所有存储的认证信息
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user_info');
    router.push('/login');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
