import { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../api/authApi';
import { AuthForm } from './LoginPage';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [state, setState] = useState({ loading: false, error: '', success: '' });
  const submit = async (event) => {
    event.preventDefault();
    if (!email) return setState({ loading: false, error: 'Vui lòng nhập email.', success: '' });
    setState({ loading: true, error: '', success: '' });
    try {
      await forgotPassword({ email });
      setState({ loading: false, error: '', success: 'Nếu email tồn tại, hệ thống sẽ xử lý yêu cầu khôi phục. Backend hiện chưa tích hợp gửi email tự động.' });
    } catch (error) {
      setState({ loading: false, error: error.response?.data?.message || 'Không thể gửi yêu cầu. Vui lòng thử lại.', success: '' });
    }
  };
  return <AuthForm title="Quên mật khẩu" subtitle="Nhập email để yêu cầu khôi phục mật khẩu." onSubmit={submit} error={state.error} loading={state.loading}><label htmlFor="forgot-email">Email<input id="forgot-email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} /></label>{state.success && <div className="form-success" role="status">{state.success}</div>}<button className="primary-button auth-submit" disabled={state.loading}>{state.loading ? 'Đang gửi...' : 'Gửi yêu cầu'}</button><div className="auth-links"><Link to="/login">← Quay lại đăng nhập</Link></div></AuthForm>;
}
