import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getBrands, getCategories, getProducts } from '../api/productApi';
import ProductFilters from '../components/product/ProductFilters';
import ProductGrid from '../components/product/ProductGrid';
import Loading from '../components/common/Loading';
import ErrorState from '../components/common/ErrorState';

const initialFilters = { keyword: '', categoryId: '', brandId: '', minPrice: '', maxPrice: '', sizeFilter: '', color: '', inStock: false, sort: 'newest' };
export default function ProductListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState({ ...initialFilters, categoryId: searchParams.get('categoryId') || '' });
  const [meta, setMeta] = useState({ products: [], page: 0, totalPages: 0, loading: true, error: '', categories: [], brands: [] });
  const load = (page = 0, values = filters) => { setMeta((old) => ({ ...old, loading: true, error: '' })); const params = { page, pageSize: 12, sort: values.sort }; Object.entries(values).forEach(([key, value]) => { if (value !== '' && value !== false) params[key] = value; }); getProducts(params).then((data) => setMeta((old) => ({ ...old, loading: false, products: data.content || [], page: data.page || 0, totalPages: data.totalPages || 0 }))).catch((error) => setMeta((old) => ({ ...old, loading: false, error: error.message }))); };
  useEffect(() => { Promise.all([getCategories(), getBrands()]).then(([categories, brands]) => setMeta((old) => ({ ...old, categories, brands }))).catch(() => {}); }, []);
  useEffect(() => { load(0, filters); }, [filters]);
  const change = (key, value) => { setFilters((old) => ({ ...old, [key]: value })); setSearchParams((old) => { const next = new URLSearchParams(old); if (value) next.set(key, value); else next.delete(key); return next; }, { replace: true }); };
  const reset = () => setFilters(initialFilters);
  return <section className="container catalog-page"><div className="page-intro"><p className="eyebrow">BỘ SƯU TẬP MENSTYLE</p><h1>Tất cả sản phẩm</h1><p>Khám phá những thiết kế dành cho phong cách nam hiện đại.</p></div><div className="catalog-layout"><ProductFilters filters={filters} categories={meta.categories} brands={meta.brands} onChange={change} onReset={reset} /><div className="catalog-results"><div className="results-toolbar"><span>{meta.loading ? 'Đang cập nhật...' : `${meta.products.length} sản phẩm`}</span><select value={filters.sort} onChange={(event) => change('sort', event.target.value)}><option value="newest">Mới nhất</option><option value="priceAsc">Giá thấp đến cao</option><option value="priceDesc">Giá cao đến thấp</option><option value="nameAsc">Tên A-Z</option><option value="nameDesc">Tên Z-A</option></select></div>{meta.loading ? <Loading /> : meta.error ? <ErrorState message={meta.error} /> : <ProductGrid products={meta.products} />}{meta.totalPages > 1 && <div className="pagination"><button disabled={meta.page <= 0} onClick={() => { load(meta.page - 1); window.scrollTo({ top: 0, behavior: 'smooth' }); }}>← Trước</button><span>Trang {meta.page + 1} / {meta.totalPages}</span><button disabled={meta.page + 1 >= meta.totalPages} onClick={() => { load(meta.page + 1); window.scrollTo({ top: 0, behavior: 'smooth' }); }}>Sau →</button></div>}</div></div></section>;
}
