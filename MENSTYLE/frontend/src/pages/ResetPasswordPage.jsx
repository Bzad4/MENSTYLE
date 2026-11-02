import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { resetPassword } from '../api/authApi';
import { AuthForm } from './LoginPage';

export default function ResetPasswordPage() {
  const [params] = useSearchParams();
  const [form, setForm] = useState({ newPassword: '', confirmPassword: '' });
  const [state, setState] = useState({ loading: false, error: '', success: '' });
  const token = params.get('token') || '';
  const submit = async (event) => {
    event.preventDefault();
    if (!token) return setState({ loading: false, error: 'Liên kết đặt lại mật khẩu không có token hợp lệ.', success: '' });
    if (form.newPassword.length < 8 || form.newPassword !== form.confirmPassword) return setState({ loading: false, error: form.newPassword.length < 8 ? 'Mật khẩu phải có ít nhất 8 ký tự.' : 'Mật khẩu xác nhận không trùng khớp.', success: '' });
    setState({ loading: true, error: '', success: '' });
    try {
      await resetPassword({ token, ...form });
      setState({ loading: false, error: '', success: 'Đặt lại mật khẩu thành công.' });
    } catch (error) {
      setState({ loading: false, error: error.response?.data?.message || 'Token không hợp lệ hoặc đã hết hạn.', success: '' });
    }
  };
  return <AuthForm title="Đặt lại mật khẩu" subtitle="Tạo mật khẩu mới cho tài khoản của bạn." onSubmit={submit} error={state.error} loading={state.loading}><label htmlFor="reset-password">Mật khẩu mới<input id="reset-password" type="password" value={form.newPassword} onChange={(event) => setForm({ ...form, newPassword: event.target.value })} /></label><label htmlFor="reset-confirm-password">Xác nhận mật khẩu<input id="reset-confirm-password" type="password" value={form.confirmPassword} onChange={(event) => setForm({ ...form, confirmPassword: event.target.value })} /></label>{state.success ? <div className="form-success" role="status">{state.success} <Link to="/login">Đăng nhập</Link></div> : <button className="primary-button auth-submit" disabled={state.loading}>{state.loading ? 'Đang cập nhật...' : 'Đặt lại mật khẩu'}</button>}</AuthForm>;
}
