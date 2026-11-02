import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { changeAdminPassword, updateAdminProfile } from '../api/adminApi';

const messageFor = (error, fallback) => error?.response?.data?.message || error?.message || fallback;

export default function AdminProfilePage() {
  const { user, refreshUser } = useAuth();
  const [profile, setProfile] = useState({ fullName: user?.fullName || '', phone: user?.phone || '' });
  const [password, setPassword] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [state, setState] = useState({ profile: '', password: '', error: '' });

  const saveProfile = async (event) => {
    event.preventDefault();
    try {
      await updateAdminProfile(profile);
      await refreshUser();
      setState({ ...state, profile: 'Hồ sơ đã được cập nhật.', error: '' });
    } catch (error) {
      setState({ ...state, error: messageFor(error, 'Không thể cập nhật hồ sơ.') });
    }
  };
  const savePassword = async (event) => {
    event.preventDefault();
    try {
      await changeAdminPassword(password);
      setPassword({ currentPassword: '', newPassword: '', confirmPassword: '' });
      setState({ ...state, password: 'Đổi mật khẩu thành công. Phiên hiện tại vẫn được giữ nguyên.', error: '' });
    } catch (error) {
      setState({ ...state, error: messageFor(error, 'Không thể đổi mật khẩu.') });
    }
  };

  return <section className="admin-page"><div className="admin-breadcrumb">Admin / Tài khoản</div><div className="admin-page-heading"><div><p className="admin-kicker">ACCOUNT SECURITY</p><h1>Hồ sơ quản trị</h1><p>Thông tin tài khoản và bảo mật của phiên quản trị hiện tại.</p></div></div>
    {state.error && <div className="admin-form-error" role="alert">{state.error}</div>}
    <div className="admin-profile-grid"><form className="admin-panel" onSubmit={saveProfile}><h2>Thông tin cá nhân</h2><label>Họ và tên<input value={profile.fullName} onChange={(event) => setProfile({ ...profile, fullName: event.target.value })} required /></label><label>Email<input value={user?.email || ''} readOnly /></label><label>Số điện thoại<input value={profile.phone} onChange={(event) => setProfile({ ...profile, phone: event.target.value })} /></label><label>Vai trò<input value={user?.role || ''} readOnly /></label><label>Trạng thái<input value={user?.status || ''} readOnly /></label><button className="admin-submit">Lưu hồ sơ</button>{state.profile && <p className="admin-success">{state.profile}</p>}</form>
      <form className="admin-panel" onSubmit={savePassword}><h2>Đổi mật khẩu</h2><p className="admin-panel-note">Mật khẩu mới tối thiểu 8 ký tự và không được trùng mật khẩu hiện tại.</p><label>Mật khẩu hiện tại<input type="password" autoComplete="current-password" value={password.currentPassword} onChange={(event) => setPassword({ ...password, currentPassword: event.target.value })} required /></label><label>Mật khẩu mới<input type="password" autoComplete="new-password" value={password.newPassword} onChange={(event) => setPassword({ ...password, newPassword: event.target.value })} required /></label><label>Xác nhận mật khẩu mới<input type="password" autoComplete="new-password" value={password.confirmPassword} onChange={(event) => setPassword({ ...password, confirmPassword: event.target.value })} required /></label><button className="admin-submit">Cập nhật mật khẩu</button>{state.password && <p className="admin-success">{state.password}</p>}</form></div>
  </section>;
}
