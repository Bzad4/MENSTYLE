import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getProductBySlug } from '../api/productApi';
import { addCartItem } from '../api/cartApi';
import { useWishlist } from '../context/WishlistContext';
import { useAuth } from '../context/AuthContext';
import ProductGallery from '../components/product/ProductGallery';
import ProductVariants from '../components/product/ProductVariants';
import Loading from '../components/common/Loading';
import ErrorState from '../components/common/ErrorState';

export default function ProductDetailPage() {
  const { slug } = useParams();
  const [state, setState] = useState({ loading: true, product: null, error: '' });
  const { isWishlisted, toggle } = useWishlist();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  useEffect(() => { setState({ loading: true, product: null, error: '' }); getProductBySlug(slug).then((product) => setState({ loading: false, product, error: '' })).catch((error) => setState({ loading: false, product: null, error: error.message })); }, [slug]);
  if (state.loading) return <section className="container detail-page"><Loading /></section>;
  if (state.error || !state.product) return <section className="container detail-page"><ErrorState message={state.error || 'Sản phẩm không tồn tại.'} /><Link className="text-link" to="/products">← Quay lại sản phẩm</Link></section>;
  const product = state.product;
  const addToCart = (variant) => {
    if (!isAuthenticated) { navigate('/login', { state: { from: { pathname: `/products/${slug}` } } }); return; }
    addCartItem({ variantId: variant.id, quantity: 1 }).then(() => window.alert('Đã thêm vào giỏ hàng.')).catch((error) => window.alert(error.message));
  };
  const toggleProductWishlist = () => {
    if (!isAuthenticated) { navigate('/login', { state: { from: { pathname: `/products/${slug}` } } }); return; }
    toggle(product.id).catch(() => {});
  };
  return <section className="container detail-page"><Link className="back-link" to="/products"><span aria-hidden="true">←</span> Tất cả sản phẩm</Link><div className="detail-layout"><ProductGallery images={product.images} name={product.name} /><div className="detail-copy"><p className="eyebrow">{product.brand?.name} · {product.category?.name}</p><div className="detail-title-row"><h1>{product.name}</h1><button className={`heart-button${isWishlisted(product.id) ? ' active' : ''}`} type="button" aria-label={isWishlisted(product.id) ? 'Xóa khỏi yêu thích' : 'Thêm vào yêu thích'} aria-pressed={isWishlisted(product.id)} onClick={toggleProductWishlist}>{isWishlisted(product.id) ? '♥' : '♡'}</button></div><p className="detail-description">{product.description}</p><div className="detail-divider" /><ProductVariants variants={product.variants} onAdd={addToCart} /></div></div></section>;
}
