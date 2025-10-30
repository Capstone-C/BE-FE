import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

export default function ProtectedRoute() {
  const { user, isInitialized } = useAuth();

  // AuthContext의 초기화가 완료될 때까지 기다립니다.
  // 초기화 중 리다이렉트되는 것을 방지하여 UX를 개선합니다.
  if (!isInitialized) {
    return <div>Loading...</div>; // 또는 스피너 컴포넌트
  }

  // 초기화 완료 후, user 상태를 확인하여 리다이렉트 여부를 결정합니다.
  if (!user) {
    // 사용자가 로그인하지 않았으면 로그인 페이지로 리다이렉트합니다.
    // 'replace' 옵션은 히스토리 스택에 현재 경로를 남기지 않습니다.
    return <Navigate to="/login" replace />;
  }

  // 로그인한 사용자는 요청한 페이지를 보여줍니다.
  return <Outlet />;
}
