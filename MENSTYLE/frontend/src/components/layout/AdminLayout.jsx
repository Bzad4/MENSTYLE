import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const navigation = [
  { label: 'Tổng quan', to: '/admin/dashboard', icon: '⌂' },
  { label: 'Sản phẩm', to: '/admin/products', icon: '◇' },
  { label: 'Danh mục', to: '/admin/categories', icon: '▦' },
  { label: 'Thương hiệu', to: '/admin/brands', icon: '◆' },
  { label: 'Đơn hàng', to: '/admin/orders', icon: '▤' },
  { label: 'Khách hàng', to: '/admin/customers', icon: '○' },
  { label: 'Tài khoản', to: '/admin/users', icon: '◎' },
  { label: 'Tồn kho', to: '/admin/inventory', icon: '▥' },
  { label: 'Báo cáo', to: '#', icon: '◫', soon: true },
  { label: 'Mã giảm giá', to: '#', icon: '◇', soon: true },
  { label: 'Đánh giá', to: '#', icon: '☆', soon: true },
  { label: 'Nhật ký', to: '#', icon: '≡', soon: true },
  { label: 'Cài đặt', to: '#', icon: '⚙', soon: true },
];

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const signOut = () => {
    logout();
    navigate('/admin/login', { replace: true });
  };

  return <div className="admin-shell">
    <aside className="admin-sidebar">
      <NavLink to="/admin/dashboard" className="admin-brand">MEN<span>STYLE</span><small>ADMIN CONSOLE</small></NavLink>
      <nav className="admin-nav" aria-label="Điều hướng quản trị">
        {navigation.map((item) => item.soon
          ? <span className="admin-nav-item admin-nav-disabled" key={item.label}><b>{item.icon}</b>{item.label}<small>Sắp phát triển</small></span>
          : <NavLink className={({ isActive }) => `admin-nav-item${isActive ? ' active' : ''}`} to={item.to} key={item.label}><b>{item.icon}</b>{item.label}</NavLink>)}
      </nav>
      <button className="admin-back-link" onClick={() => navigate('/')}>← Về cửa hàng</button>
    </aside>
    <div className="admin-content">
      <header className="admin-topbar">
        <div className="admin-topbar-heading"><span className="admin-kicker">MENSTYLE / ADMIN</span><strong>Quản trị hệ thống</strong></div>
        <div className="admin-user-menu"><span>{user?.fullName || user?.email}<small>{user?.role?.replace('ROLE_', '')}</small></span><button onClick={signOut}>Đăng xuất</button></div>
      </header>
      <main className="admin-main"><Outlet /></main>
    </div>
  </div>;
}
