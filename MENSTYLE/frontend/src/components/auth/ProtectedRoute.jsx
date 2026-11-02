import { Navigate, Outlet, useLocation } from 'react-router-dom';
import Loading from '../common/Loading';
import { useAuth } from '../../context/AuthContext';

export default function ProtectedRoute() {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();
  if (loading) return <section className="container auth-page"><Loading /></section>;
  if (!isAuthenticated) return <Navigate to="/login" replace state={{ from: location }} />;
  return <Outlet />;
}
