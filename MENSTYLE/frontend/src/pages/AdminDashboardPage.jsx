import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function AdminDashboardPage() {
  const { user } = useAuth();
  return <section className="admin-page">
    <div className="admin-breadcrumb">Admin / Tổng quan</div>
    <div className="admin-page-heading"><div><p className="admin-kicker">OVERVIEW</p><h1>Tổng quan</h1><p>Không gian điều hành MENSTYLE, được chuẩn bị cho các module quản trị tiếp theo.</p></div><span className="admin-status-chip">● Đang hoạt động</span></div>
    <div className="admin-welcome"><div><span className="admin-kicker">PHIÊN QUẢN TRỊ</span><h2>Xin chào, {user?.fullName || user?.email}</h2><p>Quyền hiện tại: <strong>{user?.role}</strong>. Các chỉ số kinh doanh sẽ được kết nối ở Phase 15.</p></div><Link to="/admin/profile">Quản lý hồ sơ →</Link></div>
    <div className="admin-placeholder-grid"><article><span>01</span><h3>KPI & doanh thu</h3><p>Khu vực dành cho số liệu thực tế, không hiển thị dữ liệu giả.</p></article><article><span>02</span><h3>Đơn hàng & khách hàng</h3><p>Đang chờ module quản lý tương ứng trong roadmap.</p></article><article><span>03</span><h3>Biểu đồ vận hành</h3><p>Dashboard thống kê chi tiết sẽ triển khai ở Phase 15.</p></article></div>
  </section>;
}
