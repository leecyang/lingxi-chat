'use client';

import { usePathname } from 'next/navigation';
import Sidebar from '@/components/layout/sidebar';
import { useSidebar } from '@/contexts/sidebar-context';
import { cn } from '@/lib/utils';

export default function LayoutContent({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { isCollapsed } = useSidebar();
  // 显示侧边栏的条件：不是登录页和注册页
  const shouldShowSidebar = !pathname.startsWith('/login') && !pathname.startsWith('/register');

  return (
    <>
      {shouldShowSidebar && <Sidebar />}
      <main 
        className={cn(
          "transition-all duration-300 ease-in-out",
          shouldShowSidebar ? (isCollapsed ? "ml-16" : "ml-64") : "ml-0"
        )}
      >
        {children}
      </main>
    </>
  );
}