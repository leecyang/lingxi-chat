'use client';

import { useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';

export default function RouteGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    // 检查是否是页面刷新（通过检查 performance.navigation.type）
    if (typeof window !== 'undefined') {
      const navigationType = (performance as any).navigation?.type || performance.getEntriesByType('navigation')[0]?.type;
      
      // 如果是页面刷新且不在首页，则重定向到首页
      if (navigationType === 'reload' && pathname !== '/') {
        router.replace('/');
      }
    }
  }, [pathname, router]);

  return <>{children}</>;
}