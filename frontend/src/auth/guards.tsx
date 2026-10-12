import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import type { Role } from '../types';
import { hasRole } from './roles';

export function FullScreenSpinner() {
  return (
    <div className="grid min-h-screen place-items-center bg-paper text-sm text-ink-soft">
      Loading the desk…
    </div>
  );
}

export function ProtectedRoute({ roles, children }: { roles?: Role[]; children: ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) {
    return <FullScreenSpinner />;
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  if (roles && !hasRole(user.role, roles)) {
    return <Navigate to="/" replace />;
  }
  return children;
}

export function GuestRoute({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) {
    return <FullScreenSpinner />;
  }
  if (user) {
    return <Navigate to="/" replace />;
  }
  return children;
}
