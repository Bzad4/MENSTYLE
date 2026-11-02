import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { isAdminUser } from '../components/auth/AdminProtectedRoute';

const errorMessage = (error) => {
  const status = error?.response?.status;
  if (status === 403) return 'Tài khoản đang bị khóa hoặc vô hiệu hóa.';
  if (status === 401 || status === 400) return 'Email hoặc mật khẩu không chính xác.';
  return error?.response?.data?.message || 'Không thể đăng nhập vào khu vực quản trị.';
};

export default function AdminLoginPage() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [state, setState] = useState({ loading: false, error: '' });
  const { login, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const submit = async (event) => {
    event.preventDefault();
    if (!form.email || !form.password) {
      setState({ loading: false, error: 'Vui lòng nhập email và mật khẩu.' });
      return;
    }
    setState({ loading: true, error: '' });
    try {
      const user = await login(form, true);
      if (!isAdminUser(user)) {
        logout();
        setState({ loading: false, error: 'Tài khoản này không có quyền quản trị.' });
        return;
      }
      navigate(location.state?.from?.pathname || '/admin/dashboard', { replace: true });
    } catch (error) {
      setState({ loading: false, error: errorMessage(error) });
    }
  };

  return <section className="admin-login-page">
    <div className="admin-login-card">
      <Link to="/" className="admin-login-brand">MEN<span>STYLE</span></Link>
      <p className="admin-kicker">SECURE ADMIN ACCESS</p>
      <h1>Chào mừng trở lại</h1>
      <p className="admin-login-subtitle">Đăng nhập để quản trị hệ thống MENSTYLE.</p>
      {state.error && <div className="admin-form-error" role="alert">{state.error}</div>}
      <form onSubmit={submit}>
        <label>Email quản trị<input type="email" autoComplete="username" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
        <label>Mật khẩu<span className="admin-password-field"><input type={showPassword ? 'text' : 'password'} autoComplete="current-password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} /><button type="button" aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'} onClick={() => setShowPassword(!showPassword)}>{showPassword ? 'Ẩn' : 'Hiện'}</button></span></label>
        <button className="admin-submit" disabled={state.loading}>{state.loading ? 'Đang xác thực...' : 'Đăng nhập quản trị'}</button>
      </form>
      <Link to="/" className="admin-login-return">← Quay lại cửa hàng</Link>
    </div>
  </section>;
}
