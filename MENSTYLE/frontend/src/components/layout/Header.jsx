import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Header() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const signOut = () => { logout(); navigate('/', { replace: true }); };
  return (
    <header className="site-header">
      <div className="container header-inner">
        <Link className="brand-mark" to="/">MEN<span>STYLE</span></Link>
        <nav className="main-nav">
          <NavLink to="/">Trang chủ</NavLink>
          <NavLink to="/products">Sản phẩm</NavLink>
        </nav>
        <div className="header-actions">
          <Link className="header-tool header-search" to="/products" aria-label="Tìm kiếm"><span className="header-tool-icon" aria-hidden="true">⌕</span><span>Tìm kiếm</span></Link>
          {isAuthenticated ? <div className="account-actions"><Link className="account-name" to="/account/orders"><span className="header-tool-icon" aria-hidden="true">♙</span>{user?.fullName || 'Tài khoản'}</Link><Link className="header-orders-link" to="/account/orders">Đơn hàng</Link><button type="button" className="ghost-action" onClick={signOut}>Đăng xuất</button></div> : <span className="account-actions"><Link to="/login">Đăng nhập</Link><Link to="/register" className="header-register">Đăng ký</Link></span>}
          <Link to="/wishlist" className="header-tool header-icon-link" title="Danh sách yêu thích"><span aria-hidden="true">♡</span><span className="header-tool-label">Yêu thích</span></Link>
          <Link to="/cart" className="header-tool header-icon-link" title="Giỏ hàng"><span aria-hidden="true">▢</span><span className="header-tool-label">Giỏ hàng</span></Link>
        </div>
      </div>
    </header>
  );
}
