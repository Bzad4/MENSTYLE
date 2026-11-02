import { Link } from 'react-router-dom';

export default function AdminForbiddenPage() {
  return <section className="admin-error-page"><p className="admin-kicker">403 / FORBIDDEN</p><h1>Không có quyền truy cập</h1><p>Tài khoản hiện tại không được cấp quyền quản trị.</p><Link className="admin-submit" to="/">Về cửa hàng</Link></section>;
}
