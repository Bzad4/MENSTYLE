import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getOrder } from '../api/orderApi';
import Loading from '../components/common/Loading';
import ErrorState from '../components/common/ErrorState';

const money = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);
export default function OrderSuccessPage() {
  const { orderId } = useParams();
  const [state, setState] = useState({ loading: true, order: null, error: '' });
  useEffect(() => {
    let active = true;
    const load = async () => {
      try {
        const order = await getOrder(orderId);
        if (active) setState({ loading: false, order, error: '' });
      } catch (error) {
        if (active) setState({ loading: false, order: null, error: error.response?.data?.message || 'Không thể tải thông tin đơn hàng.' });
      }
    };
    load();
    return () => { active = false; };
  }, [orderId]);
  if (state.loading) return <section className="container order-success-page"><Loading /></section>;
  if (state.error || !state.order) return <section className="container order-success-page"><ErrorState message={state.error} /><Link className="text-link" to="/products">Tiếp tục mua sắm →</Link></section>;
  const { order } = state;
  return <section className="container order-success-page"><div className="success-mark" aria-hidden="true">✓</div><p className="eyebrow">ORDER CONFIRMED</p><h1>Đặt hàng thành công</h1><p className="success-lead">Cảm ơn bạn đã mua sắm cùng MENSTYLE.</p><div className="order-summary-card"><div><span>Mã đơn hàng</span><strong>{order.orderCode}</strong></div><div><span>Trạng thái</span><strong>{order.status}</strong></div><div><span>Thanh toán</span><strong>COD · {order.paymentStatus}</strong></div><div><span>Tổng tiền</span><strong>{money(order.totalAmount)}</strong></div><div className="order-address"><span>Địa chỉ nhận hàng</span><strong>{order.address?.receiverName} · {order.address?.receiverPhone}</strong><p>{order.address?.addressLine}, {order.address?.ward}, {order.address?.district}, {order.address?.province}</p></div></div><div className="success-actions"><Link className="primary-button" to={`/account/orders/${order.id}`}>Xem đơn hàng</Link><Link className="secondary-button" to="/products">Tiếp tục mua sắm</Link></div></section>;
}
