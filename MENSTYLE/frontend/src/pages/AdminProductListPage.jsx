import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAdminProducts, updateAdminProductStatus } from '../api/adminApi';

export default function AdminProductListPage() {
  const [state, setState] = useState({ loading: true, error: '', data: { content: [], totalPages: 0, page: 0 } });
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const load = async () => {
    const searchKeyword = keyword.trim();
    setState((s) => ({ ...s, loading: true, error: '' }));
    try { const data = await getAdminProducts({ keyword: searchKeyword || undefined, page, size: 12 }); setState({ loading: false, error: '', data }); }
    catch (error) { setState((s) => ({ ...s, loading: false, error: error.response?.data?.message || 'Không thể tải sản phẩm.' })); }
  };
  useEffect(() => { load(); }, [page]);
  const search = (event) => { event.preventDefault(); if (page === 0) load(); else setPage(0); };
  const toggle = async (product) => { try { await updateAdminProductStatus(product.id, product.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'); await load(); } catch (error) { setState((s) => ({ ...s, error: error.response?.data?.message || 'Không thể cập nhật trạng thái.' })); } };
  return <section className="admin-page"><div className="admin-breadcrumb">Admin / Sản phẩm</div><div className="admin-page-heading"><div><p className="admin-kicker">CATALOG</p><h1>Sản phẩm</h1><p>Quản lý danh mục sản phẩm và trạng thái kinh doanh.</p></div><Link className="admin-submit" to="/admin/products/new">+ Thêm sản phẩm</Link></div>
    <form className="admin-toolbar" onSubmit={search}><input aria-label="Tìm sản phẩm" placeholder="Tìm theo tên, slug hoặc SKU..." value={keyword} onChange={(e) => setKeyword(e.target.value)} /><button className="admin-submit">Tìm kiếm</button></form>
    {state.error && <div className="admin-form-error">{state.error}</div>}{state.loading ? <div className="admin-empty">Đang tải...</div> : state.data.content.length === 0 ? <div className="admin-empty">Chưa có sản phẩm phù hợp.</div> : <div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>Sản phẩm</th><th>Danh mục</th><th>Giá</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>{state.data.content.map((p) => <tr key={p.id}><td><strong>{p.name}</strong><small>{p.slug}</small></td><td>{p.categoryName}</td><td>{Number(p.basePrice).toLocaleString('vi-VN')} đ</td><td><span className={`admin-status ${p.status.toLowerCase()}`}>{p.status}</span></td><td className="admin-table-actions"><Link to={`/admin/products/${p.id}/edit`}>Sửa</Link><button type="button" onClick={() => toggle(p)}>{p.status === 'ACTIVE' ? 'Ngừng bán' : 'Kích hoạt'}</button></td></tr>)}</tbody></table></div>}
    <div className="admin-pagination"><button disabled={page === 0} onClick={() => setPage(page - 1)}>← Trước</button><span>Trang {page + 1} / {Math.max(1, state.data.totalPages)}</span><button disabled={page + 1 >= state.data.totalPages} onClick={() => setPage(page + 1)}>Sau →</button></div>
  </section>;
}
