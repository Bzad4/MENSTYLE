import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const loginError = (error) => {
  const status = error?.response?.status;
  if (!error?.response) return 'Không thể kết nối đến máy chủ. Vui lòng thử lại.';
  if (status >= 500) return 'Hệ thống đang gặp sự cố. Vui lòng thử lại sau ít phút.';
  if (status === 403) return 'Tài khoản của bạn đã bị khóa. Vui lòng liên hệ bộ phận hỗ trợ.';
  if (status === 401 || status === 400) return 'Email hoặc mật khẩu không chính xác. Vui lòng kiểm tra và thử lại.';
  return 'Không thể đăng nhập. Vui lòng thử lại.';
};

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [remember, setRemember] = useState(true);
  const [state, setState] = useState({ loading: false, error: '' });
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const destination = location.state?.from?.pathname || '/';

  const submit = async (event) => {
    event.preventDefault();
    if (!form.email) return setState({ loading: false, error: 'Vui lòng nhập địa chỉ email.' });
    if (!emailPattern.test(form.email)) return setState({ loading: false, error: 'Địa chỉ email không hợp lệ. Vui lòng nhập lại.' });
    if (!form.password) return setState({ loading: false, error: 'Vui lòng nhập mật khẩu.' });
    setState({ loading: true, error: '' });
    try {
      await login(form, remember);
      navigate(destination, { replace: true });
    } catch (error) {
      setState({ loading: false, error: loginError(error) });
    }
  };

  return <AuthForm title="Chào mừng trở lại" subtitle="Đăng nhập để tiếp tục mua sắm cùng MENSTYLE." onSubmit={submit} error={state.error} loading={state.loading}>
    <label htmlFor="login-email">Email<input id="login-email" type="email" autoComplete="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
    <label htmlFor="login-password">Mật khẩu<span className="password-field"><input id="login-password" type={showPassword ? 'text' : 'password'} autoComplete="current-password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} /><button type="button" onClick={() => setShowPassword(!showPassword)} aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}>{showPassword ? 'Ẩn' : 'Hiện'}</button></span></label>
    <label className="form-check"><input type="checkbox" checked={remember} onChange={(event) => setRemember(event.target.checked)} /> Ghi nhớ đăng nhập</label>
    <button className="primary-button auth-submit" disabled={state.loading}>{state.loading ? 'Đang đăng nhập...' : 'Đăng nhập'}</button>
    <div className="auth-links"><Link to="/forgot-password">Quên mật khẩu?</Link><span>Chưa có tài khoản? <Link to="/register">Đăng ký</Link></span></div>
  </AuthForm>;
}

export function AuthForm({ title, subtitle, onSubmit, error, loading, children, variant = 'default' }) {
  return <section className={`auth-page auth-page-${variant} container`}><div className="auth-card"><p className="eyebrow">MENSTYLE ACCOUNT</p><h1>{title}</h1><p className="auth-subtitle">{subtitle}</p>{error && <div className="form-error" role="alert"><span aria-hidden="true">!</span>{error}</div>}<form onSubmit={onSubmit}>{children}</form></div></section>;
}
