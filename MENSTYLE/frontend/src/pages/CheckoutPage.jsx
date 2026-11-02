import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getCart } from '../api/cartApi';
import { checkout } from '../api/checkoutApi';
import Loading from '../components/common/Loading';
import { resolveImageUrl } from '../utils/imageUrl';

const money = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);
const initialForm = { receiverName: '', receiverPhone: '', province: '', district: '', ward: '', addressLine: '', note: '' };

export default function CheckoutPage() {
  const [cart, setCart] = useState(null);
  const [form, setForm] = useState(initialForm);
  const [state, setState] = useState({ loading: true, submitting: false, error: '' });
  const [idempotencyKey, setIdempotencyKey] = useState(() => `web-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`);
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;
    const load = async () => {
      try {
        const data = await getCart();
        if (active) setCart(data);
      } catch (error) {
        if (active) setState({ loading: false, submitting: false, error: error.response?.data?.message || 'Không thể tải giỏ hàng.' });
      } finally {
        if (active) setState((old) => ({ ...old, loading: false }));
      }
    };
    load();
    return () => { active = false; };
  }, []);

  const update = (key) => (event) => setForm((old) => ({ ...old, [key]: event.target.value }));
  const submit = async (event) => {
    event.preventDefault();
    if (!form.receiverName || !form.receiverPhone || !form.province || !form.district || !form.ward || !form.addressLine) {
      setState((old) => ({ ...old, error: 'Vui lòng điền đầy đủ thông tin nhận hàng.' }));
      return;
    }
    setState((old) => ({ ...old, submitting: true, error: '' }));
    try {
      const result = await checkout({ ...form, paymentMethod: 'COD', idempotencyKey });
      navigate(`/order-success/${result.id}`, { replace: true });
    } catch (error) {
      if (error.response?.status === 400 && error.response?.data?.message?.includes('Idempotency key')) {
        setIdempotencyKey(`web-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`);
      }
      setState((old) => ({ ...old, submitting: false, error: error.response?.data?.message || error.message || 'Không thể tạo đơn hàng. Vui lòng thử lại.' }));
    }
  };

  if (state.loading) return <section className="container checkout-page"><Loading /></section>;
  if (!cart?.items?.length) return <section className="container checkout-page"><div className="state-card"><strong>Giỏ hàng đang trống</strong><p>Nếu lần đặt hàng trước chưa hiển thị kết quả, hãy thử gửi lại cùng thông tin để kiểm tra trạng thái đơn.</p><button className="primary-button" disabled={state.submitting} onClick={submit}>{state.submitting ? 'Đang kiểm tra...' : 'Thử gửi lại yêu cầu trước'}</button><Link className="text-link" to="/products">Tiếp tục mua sắm →</Link></div></section>;

  return <section className="container checkout-page"><div className="page-intro"><p className="eyebrow">SECURE CHECKOUT</p><h1>Thông tin đặt hàng</h1><p>Kiểm tra thông tin trước khi đặt hàng bằng hình thức COD.</p></div><div className="checkout-layout"><form className="checkout-form" onSubmit={submit}><div className="checkout-card"><h2>Thông tin nhận hàng</h2><div className="checkout-fields"><label>Họ và tên người nhận<input value={form.receiverName} onChange={update('receiverName')} autoComplete="name" /></label><label>Số điện thoại<input value={form.receiverPhone} onChange={update('receiverPhone')} autoComplete="tel" /></label><label>Tỉnh / thành phố<input value={form.province} onChange={update('province')} /></label><label>Quận / huyện<input value={form.district} onChange={update('district')} /></label><label>Phường / xã<input value={form.ward} onChange={update('ward')} /></label><label className="field-wide">Địa chỉ cụ thể<input value={form.addressLine} onChange={update('addressLine')} /></label><label className="field-wide">Ghi chú đơn hàng <textarea value={form.note} onChange={update('note')} rows="3" /></label></div></div><div className="checkout-card"><h2>Phương thức thanh toán</h2><label className="payment-option"><input type="radio" checked readOnly /> <span><strong>Thanh toán khi nhận hàng (COD)</strong><small>Thanh toán trực tiếp khi nhận được sản phẩm.</small></span></label></div>{state.error && <div className="form-error" role="alert"><span aria-hidden="true">!</span>{state.error}</div>}<button className="primary-button checkout-submit" disabled={state.submitting}>{state.submitting ? 'Đang tạo đơn hàng...' : 'Xác nhận đặt hàng'}</button></form><aside className="checkout-summary checkout-card"><h2>Đơn hàng của bạn</h2><div className="checkout-items">{cart.items.map((item) => <div className="checkout-item" key={item.id}><div className="checkout-item-image">{item.imageUrl ? <img src={resolveImageUrl(item.imageUrl)} alt="" /> : <span>MENSTYLE</span>}</div><div><strong>{item.productName}</strong><small>{item.size} · {item.color} · SL {item.quantity}</small><span>{money(item.unitPrice)}</span></div></div>)}</div><div className="summary-row"><span>Tạm tính</span><strong>{money(cart.subtotal)}</strong></div><div className="summary-row"><span>Phí vận chuyển</span><span>Chưa tính</span></div><div className="summary-row summary-total"><strong>Tổng thanh toán</strong><strong>{money(cart.subtotal)}</strong></div></aside></div></section>;
}
