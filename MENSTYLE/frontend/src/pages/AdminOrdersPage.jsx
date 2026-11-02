import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getAdminOrder, getAdminOrders, updateAdminOrderStatus } from '../api/adminApi';

const statuses = ['PENDING', 'CONFIRMED', 'PACKING', 'SHIPPING', 'COMPLETED', 'CANCELLED'];
export default function AdminOrdersPage() {
  const { id } = useParams();
  const [state, setState] = useState({ loading: true, data: null, error: '' });
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [note, setNote] = useState('');
  const load = () => { setState((s) => ({ ...s, loading: true })); const request = id ? getAdminOrder(id) : getAdminOrders({ keyword: keyword || undefined, status: status || undefined, page: 0, size: 20 }); request.then((data) => setState({ loading: false, data, error: '' })).catch((e) => setState({ loading: false, data: null, error: e.response?.data?.message || 'Không thể tải đơn hàng.' })); };
  useEffect(load, [id]);
  const transition = async (value) => { try { await updateAdminOrderStatus(id, value, note || `Admin chuyển trạng thái sang ${value}`); setNote(''); load(); } catch (e) { setState((s) => ({ ...s, error: e.response?.data?.message || 'Không thể cập nhật trạng thái.' })); } };
  if (id) return <section className="admin-page"><Link className="text-link" to="/admin/orders">← Đơn hàng</Link>{state.error && <div className="admin-form-error">{state.error}</div>}{state.loading ? <div className="admin-empty">Đang tải...</div> : state.data && <><div className="admin-page-heading"><div><p className="admin-kicker">ORDER</p><h1>{state.data.orderCode}</h1><p>{state.data.customerName} · {state.data.customerEmail}</p></div><span className="admin-status active">{state.data.status}</span></div><div className="admin-panel"><h2>Trạng thái</h2><textarea value={note} onChange={(e) => setNote(e.target.value)} placeholder="Lý do thao tác" /><div className="admin-table-actions">{statuses.filter((s) => s !== state.data.status).map((s) => <button key={s} onClick={() => transition(s)}>{s}</button>)}</div><h2>Sản phẩm</h2>{state.data.items.map((item) => <p key={item.sku}>{item.productName} · {item.sku} · {item.quantity} · {Number(item.lineTotal).toLocaleString('vi-VN')} đ</p>)}<h2>Lịch sử</h2>{state.data.history.map((h, i) => <p key={i}>{h.newStatus} · {h.note}</p>)}</div></>}</section>;
  const rows = state.data?.content || [];
  return <section className="admin-page"><div className="admin-page-heading"><div><p className="admin-kicker">OPERATIONS</p><h1>Đơn hàng</h1></div></div><form className="admin-toolbar" onSubmit={(e) => { e.preventDefault(); load(); }}><input placeholder="Mã đơn hàng" value={keyword} onChange={(e) => setKeyword(e.target.value)} /><select value={status} onChange={(e) => setStatus(e.target.value)}><option value="">Tất cả trạng thái</option>{statuses.map((s) => <option key={s}>{s}</option>)}</select><button className="admin-submit">Tìm</button></form>{state.error && <div className="admin-form-error">{state.error}</div>}{state.loading ? <div className="admin-empty">Đang tải...</div> : <div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>Mã đơn</th><th>Khách hàng</th><th>Trạng thái</th><th>Tổng tiền</th><th /></tr></thead><tbody>{rows.map((o) => <tr key={o.id}><td>{o.orderCode}</td><td>{o.customerName}<br />{o.customerEmail}</td><td>{o.status}</td><td>{Number(o.totalAmount).toLocaleString('vi-VN')} đ</td><td><Link to={`/admin/orders/${o.id}`}>Chi tiết</Link></td></tr>)}</tbody></table></div>}</section>;
}
