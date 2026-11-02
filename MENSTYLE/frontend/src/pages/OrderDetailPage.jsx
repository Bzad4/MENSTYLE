import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { cancelOrder, getOrderById } from '../api/orderApi';
import Loading from '../components/common/Loading';
import ErrorState from '../components/common/ErrorState';

const money = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);
const labels = { PENDING: 'Chờ xác nhận', CONFIRMED: 'Đã xác nhận', PACKING: 'Đang chuẩn bị', SHIPPING: 'Đang giao', COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy' };
const date = (value) => value ? new Date(value).toLocaleString('vi-VN') : '—';

export default function OrderDetailPage() {
  const { orderId } = useParams();
  const [state, setState] = useState({ loading: true, order: null, error: '' });
  const [reason, setReason] = useState('');
  const [cancelState, setCancelState] = useState({ open: false, submitting: false, error: '' });
  useEffect(() => {
    let active = true;
    setState({ loading: true, order: null, error: '' });
    const load = async () => {
      try {
        const order = await getOrderById(orderId);
        if (active) setState({ loading: false, order, error: '' });
      } catch (error) {
        if (active) setState({ loading: false, order: null, error: error.response?.data?.message || 'Không thể tải đơn hàng.' });
      }
    };
    load();
    return () => { active = false; };
  }, [orderId]);
  if (state.loading) return <section className="container order-detail-page"><Loading /></section>;
  if (state.error || !state.order) return <section className="container order-detail-page"><ErrorState message={state.error} /><Link className="text-link" to="/account/orders">← Đơn hàng của tôi</Link></section>;
  const order = state.order;
  const submitCancel = (event) => {
    event.preventDefault();
    setCancelState({ open: true, submitting: true, error: '' });
    cancelOrder(order.id, reason).then((updated) => { setState({ loading: false, order: updated, error: '' }); setReason(''); setCancelState({ open: false, submitting: false, error: '' }); })
      .catch((error) => setCancelState((old) => ({ ...old, submitting: false, error: error.response?.data?.message || 'Không thể hủy đơn hàng.' })));
  };
  return <section className="container order-detail-page">
    <Link className="back-link" to="/account/orders">← Đơn hàng của tôi</Link>
    <div className="order-detail-heading"><div><p className="eyebrow">ORDER DETAIL</p><h1>{order.orderCode}</h1><p>Đặt ngày {date(order.createdAt)}</p></div><span className={`order-status status-${order.status.toLowerCase()}`}>{labels[order.status] || order.status}</span></div>
    <div className="order-detail-layout"><main>
      <section className="order-panel"><h2>Sản phẩm</h2>{(order.items || []).map((item) => <div className="order-line" key={`${item.sku}-${item.size}-${item.color}`}><div><strong>{item.productName}</strong><p>{item.size} · {item.color} · {item.sku}</p></div><span>{item.quantity} × {money(item.unitPrice)}</span><strong>{money(item.lineTotal)}</strong></div>)}</section>
      <section className="order-panel"><h2>Lịch sử trạng thái</h2><div className="order-timeline">{(order.history || []).map((item, index) => <div className="timeline-item" key={`${item.createdAt}-${index}`}><span className="timeline-dot" /><div><strong>{labels[item.newStatus] || item.newStatus}</strong><p>{item.note || 'Cập nhật trạng thái'} · {date(item.createdAt)}</p></div></div>)}</div></section>
    </main><aside>
      <section className="order-panel"><h2>Thông tin đơn hàng</h2><div className="summary-row"><span>Tạm tính</span><strong>{money(order.subtotal)}</strong></div><div className="summary-row"><span>Phí vận chuyển</span><strong>{money(order.shippingFee)}</strong></div><div className="summary-row summary-total"><span>Tổng tiền</span><strong>{money(order.totalAmount)}</strong></div><p className="order-payment">Thanh toán: {order.paymentMethod} · {order.paymentStatus}</p></section>
      {order.address && <section className="order-panel"><h2>Nhận hàng</h2><strong>{order.address.receiverName}</strong><p>{order.address.receiverPhone}</p><p>{order.address.addressLine}, {order.address.ward}, {order.address.district}, {order.address.province}</p></section>}
      {order.status === 'PENDING' && <section className="order-cancel-box"><h2>Hủy đơn hàng</h2><p>Thao tác này sẽ hủy toàn bộ đơn và hoàn lại tồn kho.</p><form onSubmit={submitCancel}><textarea value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Lý do hủy đơn hàng" maxLength={1000} required /><button className="danger-button" type="submit" disabled={cancelState.submitting || !reason.trim()}>{cancelState.submitting ? 'Đang xử lý…' : 'Xác nhận hủy đơn'}</button>{cancelState.error && <p className="form-error" role="alert">{cancelState.error}</p>}</form></section>}
    </aside></div>
  </section>;
}
