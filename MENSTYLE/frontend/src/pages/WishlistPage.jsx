import { useState } from 'react';
import { Link } from 'react-router-dom';
import Loading from '../components/common/Loading';
import ErrorState from '../components/common/ErrorState';
import { useWishlist } from '../context/WishlistContext';
import { resolveImageUrl } from '../utils/imageUrl';

const money = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);
function WishlistImage({ src, alt }) {
  const [failed, setFailed] = useState(false);
  return failed || !src
    ? <div className="image-placeholder">MENSTYLE</div>
    : <img src={resolveImageUrl(src)} alt={alt} onError={() => setFailed(true)} />;
}
export default function WishlistPage() {
  const { items = [], loading, error, remove } = useWishlist();
  if (loading) return <section className="container wishlist-page"><Loading /></section>;
  if (error) return <section className="container wishlist-page"><ErrorState message={`${error} Hãy đăng nhập để sử dụng danh sách yêu thích.`} /></section>;
  return <section className="container wishlist-page"><div className="page-intro"><p className="eyebrow">SAVED FOR LATER</p><h1>Yêu thích</h1><p>Sản phẩm bạn muốn giữ lại cho lần mua sắm tiếp theo.</p></div>{!items.length ? <div className="state-card"><strong>Chưa có sản phẩm yêu thích</strong><Link className="text-link" to="/products">Khám phá sản phẩm →</Link></div> : <div className="wishlist-grid">{items.map((item) => <article className="wishlist-card" key={item.productId}><Link to={`/products/${item.slug}`}><div className="product-image-wrap"><WishlistImage src={item.primaryImage} alt={item.name} /></div><div className="product-card-body"><h3>{item.name}</h3><strong>{money(item.basePrice)}</strong><span className={item.inStock ? 'in-stock' : 'out-stock'}>{item.inStock ? 'Còn hàng' : 'Tạm hết hàng'}</span></div></Link><button className="wishlist-remove" type="button" onClick={() => remove(item.productId).catch(() => {})}><span aria-hidden="true">×</span> Xóa khỏi yêu thích</button></article>)}</div>}</section>;
}
