import { Link } from 'react-router-dom';
export default function NotFoundPage() { return <section className="container not-found"><p className="eyebrow">404</p><h1>Trang bạn tìm không tồn tại.</h1><Link className="primary-button" to="/">Về trang chủ</Link></section>; }
