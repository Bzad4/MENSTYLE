import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthForm } from './LoginPage';
import { useAuth } from '../context/AuthContext';

const initial = { fullName: '', email: '', password: '', confirmPassword: '', phone: '' };
export default function RegisterPage() {
  const [form, setForm] = useState(initial);
  const [show, setShow] = useState({ password: false, confirmPassword: false });
  const [state, setState] = useState({ loading: false, error: '', success: '' });
  const { register } = useAuth();
  const navigate = useNavigate();
  const submit = async (event) => {
    event.preventDefault();
    if (!form.fullName || !form.email || !form.password || !form.confirmPassword) return setState({ loading: false, error: 'Vui lòng điền đầy đủ các trường bắt buộc.', success: '' });
    if (form.password.length < 8) return setState({ loading: false, error: 'Mật khẩu phải có ít nhất 8 ký tự.', success: '' });
    if (form.password !== form.confirmPassword) return setState({ loading: false, error: 'Mật khẩu xác nhận không trùng khớp.', success: '' });
    setState({ loading: true, error: '', success: '' });
    try {
      await register(form);
      setState({ loading: false, error: '', success: 'Đăng ký thành công. Đang chuyển đến trang đăng nhập...' });
      setTimeout(() => navigate('/login', { replace: true }), 900);
    } catch (error) {
      setState({ loading: false, error: error.response?.data?.message || error.message || 'Không thể đăng ký tài khoản.', success: '' });
    }
  };
  const field = (name, label, type = 'text') => <label htmlFor={`register-${name}`}>{label}{name === 'password' || name === 'confirmPassword' ? <span className="password-field"><input id={`register-${name}`} type={show[name] ? 'text' : 'password'} value={form[name]} onChange={(event) => setForm({ ...form, [name]: event.target.value })} autoComplete="new-password" /><button type="button" onClick={() => setShow({ ...show, [name]: !show[name] })} aria-label={show[name] ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}>{show[name] ? 'Ẩn' : 'Hiện'}</button></span> : <input id={`register-${name}`} type={type} value={form[name]} onChange={(event) => setForm({ ...form, [name]: event.target.value })} />}</label>;
  return <AuthForm variant="register" title="Tạo tài khoản" subtitle="Đăng ký để lưu sản phẩm và quản lý giỏ hàng của bạn." onSubmit={submit} error={state.error} loading={state.loading}><div className="form-grid">{field('fullName', 'Họ và tên *')}{field('email', 'Email *', 'email')}{field('password', 'Mật khẩu *')}{field('confirmPassword', 'Xác nhận mật khẩu *')}{field('phone', 'Số điện thoại')}</div>{state.success && <div className="form-success">{state.success}</div>}<button className="primary-button auth-submit" disabled={state.loading}>{state.loading ? 'Đang tạo tài khoản...' : 'Đăng ký'}</button><div className="auth-links"><span>Đã có tài khoản? <Link to="/login">Đăng nhập</Link></span></div></AuthForm>;
}
