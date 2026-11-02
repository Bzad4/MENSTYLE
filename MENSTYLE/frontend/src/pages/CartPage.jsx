import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getCart, removeCartItem, updateCartItem } from '../api/cartApi';
import Loading from '../components/common/Loading';
import ErrorState from '../components/common/ErrorState';
import { resolveImageUrl } from '../utils/imageUrl';

const money = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);

function CartImage({ src, alt }) {
  const [failed, setFailed] = useState(false);
  if (failed || !src) return <div className="cart-image-placeholder" aria-label="Ảnh sản phẩm không khả dụng">MENSTYLE</div>;
  return <img src={resolveImageUrl(src)} alt={alt} onError={() => setFailed(true)} />;
}

export default function CartPage() {
  const [state, setState] = useState({ loading: true, cart: null, error: '' });
  const [pendingId, setPendingId] = useState(null);
  const load = async () => {
    setState((old) => ({ ...old, loading: true, error: '' }));
    try {
      const cart = await getCart();
      setState({ loading: false, cart, error: '' });
    } catch (error) {
      setState({ loading: false, cart: null, error: error.response?.data?.message || error.message || 'Không thể tải giỏ hàng.' });
    }
  };
  useEffect(() => {
    let active = true;
    const run = async () => {
      setState((old) => ({ ...old, loading: true, error: '' }));
      try {
        const cart = await getCart();
        if (active) setState({ loading: false, cart, error: '' });
      } catch (error) {
        if (active) setState({ loading: false, cart: null, error: error.response?.data?.message || error.message || 'Không thể tải giỏ hàng.' });
      }
    };
    run();
    return () => { active = false; };
  }, []);

  const changeQuantity = (id, quantity) => {
    setPendingId(id);
    updateCartItem(id, { quantity })
      .then(load)
      .catch((error) => setState((old) => ({ ...old, error: error.response?.data?.message || 'Không thể cập nhật số lượng.' })))
      .finally(() => setPendingId(null));
  };
  const removeItem = (id) => {
    setPendingId(id);
    removeCartItem(id)
      .then(load)
      .catch((error) => setState((old) => ({ ...old, error: error.response?.data?.message || 'Không thể xóa sản phẩm.' })))
      .finally(() => setPendingId(null));
  };

  if (state.loading) return <section className="container cart-page"><Loading /></section>;
  if (state.error && !state.cart) return <section className="container cart-page"><ErrorState message={state.error} /><Link className="text-link" to="/products">Tiếp tục mua sắm →</Link></section>;
  const cart = state.cart;
  if (!cart?.items?.length) return <section className="container cart-page"><div className="page-intro"><p className="eyebrow">YOUR SELECTION</p><h1>Giỏ hàng</h1></div><div className="cart-empty"><span className="cart-empty-icon" aria-hidden="true">▢</span><h2>Giỏ hàng của bạn đang trống</h2><p>Khám phá những sản phẩm mới nhất từ MENSTYLE.</p><Link className="primary-button" to="/products">Tiếp tục mua sắm</Link></div></section>;

  return <section className="container cart-page">
    <div className="page-intro"><p className="eyebrow">YOUR SELECTION</p><h1>Giỏ hàng</h1><p>{cart.totalQuantity} sản phẩm trong giỏ hàng</p></div>
    {state.error && <div className="form-error cart-alert" role="alert"><span aria-hidden="true">!</span>{state.error}</div>}
    <div className="cart-layout">
      <div className="cart-items">{cart.items.map((item) => <article className="cart-item" key={item.id}>
        <Link className="cart-item-image" to={`/products/${item.productSlug}`}><CartImage src={item.imageUrl} alt={item.productName} /></Link>
        <div className="cart-item-copy"><Link to={`/products/${item.productSlug}`}><h2>{item.productName}</h2></Link><p className="cart-meta">{item.size || '—'} <span>·</span> {item.color || '—'} <span>·</span> {item.sku || 'Không có SKU'}</p><p className="cart-unit-price">{money(item.unitPrice)}</p><div className="quantity-row"><span className="quantity-label">Số lượng</span><div className="quantity-control"><button type="button" aria-label={`Giảm số lượng ${item.productName}`} disabled={pendingId === item.id || item.quantity <= 1} onClick={() => changeQuantity(item.id, item.quantity - 1)}>−</button><span>{pendingId === item.id ? '…' : item.quantity}</span><button type="button" aria-label={`Tăng số lượng ${item.productName}`} disabled={pendingId === item.id || item.quantity >= item.stockQuantity} onClick={() => changeQuantity(item.id, item.quantity + 1)}>+</button></div><button type="button" className="remove-button" disabled={pendingId === item.id} onClick={() => removeItem(item.id)}>Xóa</button></div></div>
        <strong className="line-total">{money(item.lineTotal)}</strong>
      </article>)}</div>
      <aside className="cart-summary"><h2>Tóm tắt đơn hàng</h2><div className="summary-row"><span>Tổng sản phẩm</span><strong>{cart.totalQuantity}</strong></div><div className="summary-row summary-total"><span>Tạm tính</span><strong>{money(cart.subtotal)}</strong></div><Link className="primary-button cart-checkout-button" to="/checkout">Tiến hành thanh toán</Link><p className="summary-note">Phí vận chuyển và ưu đãi sẽ được xác nhận ở bước thanh toán.</p></aside>
    </div>
  </section>;
}
