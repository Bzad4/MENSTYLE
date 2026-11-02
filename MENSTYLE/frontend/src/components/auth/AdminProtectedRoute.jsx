import { Navigate, Outlet, useLocation } from 'react-router-dom';
import Loading from '../common/Loading';
import { useAuth } from '../../context/AuthContext';

const adminRoles = new Set(['ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_STAFF']);

export const isAdminUser = (user) => adminRoles.has(user?.role);

export default function AdminProtectedRoute() {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) return <section className="admin-loading"><Loading /></section>;
  if (!user) return <Navigate to="/admin/login" replace state={{ from: location }} />;
  if (!isAdminUser(user)) return <Navigate to="/admin/403" replace />;
  return <Outlet />;
}
