import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyOrders } from '../api/orderApi';
import Loading from '../components/common/Loading';
import ErrorState from '../components/common/ErrorState';

const money = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);
const labels = { PENDING: 'Chờ xác nhận', CONFIRMED: 'Đã xác nhận', PACKING: 'Đang chuẩn bị', SHIPPING: 'Đang giao', COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy' };
const date = (value) => value ? new Date(value).toLocaleDateString('vi-VN') : '—';

export default function OrderListPage() {
  const [filter, setFilter] = useState('');
  const [state, setState] = useState({ loading: true, data: null, error: '' });
  const load = async () => {
    setState((old) => ({ ...old, loading: true, error: '' }));
    try {
      const data = await getMyOrders({ page: 0, size: 20, ...(filter ? { status: filter } : {}) });
      setState({ loading: false, data, error: '' });
    } catch (error) {
      setState({ loading: false, data: null, error: error.response?.data?.message || error.message || 'Không thể tải đơn hàng.' });
    }
  };
  useEffect(() => {
    let active = true;
    const run = async () => {
      setState((old) => ({ ...old, loading: true, error: '' }));
      try {
        const data = await getMyOrders({ page: 0, size: 20, ...(filter ? { status: filter } : {}) });
        if (active) setState({ loading: false, data, error: '' });
      } catch (error) {
        if (active) setState({ loading: false, data: null, error: error.response?.data?.message || error.message || 'Không thể tải đơn hàng.' });
      }
    };
    run();
    return () => { active = false; };
  }, [filter]);
  if (state.loading) return <section className="container orders-page"><Loading /></section>;
  if (state.error) return <section className="container orders-page"><ErrorState message={state.error} /><button className="secondary-button" type="button" onClick={load}>Thử lại</button></section>;
  const orders = state.data?.content ?? [];
  return <section className="container orders-page">
    <div className="page-intro"><p className="eyebrow">YOUR ORDERS</p><h1>Đơn hàng của tôi</h1><p>Theo dõi các đơn hàng MENSTYLE của bạn.</p></div>
    <div className="order-filters"><label htmlFor="order-status">Lọc trạng thái</label><select id="order-status" value={filter} onChange={(event) => setFilter(event.target.value)}><option value="">Tất cả</option>{Object.entries(labels).map(([key, value]) => <option key={key} value={key}>{value}</option>)}</select></div>
    {!orders.length ? <div className="state-card order-empty"><strong>Chưa có đơn hàng</strong><span>Đơn hàng sau khi thanh toán sẽ xuất hiện tại đây.</span><Link className="primary-button" to="/products">Khám phá sản phẩm</Link></div> :
      <div className="orders-list">{orders.map((order) => <article className="order-card" key={order.id}><div><span className="eyebrow">MÃ ĐƠN</span><h2>{order.orderCode}</h2><p>{date(order.createdAt)} · {order.itemCount} sản phẩm</p></div><div><span className={`order-status status-${order.orderStatus.toLowerCase()}`}>{labels[order.orderStatus] || order.orderStatus}</span><strong>{money(order.totalAmount)}</strong><small>{order.paymentMethod} · {order.paymentStatus}</small></div><Link className="secondary-button" to={`/account/orders/${order.id}`}>Xem chi tiết →</Link></article>)}</div>}
  </section>;
}
