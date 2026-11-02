import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useWishlist } from '../../context/WishlistContext';
import { resolveImageUrl } from '../../utils/imageUrl';

const money = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);

export default function ProductCard({ product }) {
  const { isWishlisted, toggle } = useWishlist();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const active = isWishlisted(product.id);
  const toggleWishlist = (event) => {
    event.preventDefault();
    event.stopPropagation();
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/products/${product.slug}` } } });
      return;
    }
    toggle(product.id).catch(() => {});
  };
  return (
    <Link className="product-card" to={`/products/${product.slug}`}>
      <div className="product-image-wrap">
        {product.primaryImage ? <img src={resolveImageUrl(product.primaryImage)} alt={product.name} /> : <div className="image-placeholder">MENSTYLE</div>}
        {!product.inStock && <span className="stock-badge">Hết hàng</span>}
        <button className={`card-heart${active ? ' active' : ''}`} type="button" onClick={toggleWishlist} aria-label={active ? 'Xóa khỏi yêu thích' : 'Thêm vào yêu thích'}>{active ? '♥' : '♡'}</button>
      </div>
      <div className="product-card-body">
        <div className="eyebrow">{product.brand?.name} · {product.category?.name}</div>
        <h3>{product.name}</h3>
        <strong>{money(product.basePrice)}</strong>
        <span className={product.inStock ? 'in-stock' : 'out-stock'}>{product.inStock ? 'Còn hàng' : 'Tạm hết hàng'}</span>
      </div>
    </Link>
  );
}
