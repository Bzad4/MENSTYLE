import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getCategories, getProducts } from '../api/productApi';
import ProductGrid from '../components/product/ProductGrid';
import Loading from '../components/common/Loading';
import ErrorState from '../components/common/ErrorState';

export default function HomePage() {
  const [state, setState] = useState({ loading: true, products: [], categories: [], error: '' });
  useEffect(() => { Promise.all([getProducts({ page: 0, size: 4, sort: 'newest' }), getCategories()]).then(([products, categories]) => setState({ loading: false, products: products.content || [], categories, error: '' })).catch((error) => setState((old) => ({ ...old, loading: false, error: error.message }))); }, []);
  return <><section className="hero"><div className="container hero-content"><p className="eyebrow light">BỘ SƯU TẬP 2026</p><h1>Phong cách của<br /><em>người đàn ông hiện đại.</em></h1><p>Những thiết kế tinh giản, chất liệu chọn lọc và vừa vặn cho mọi khoảnh khắc.</p><Link className="primary-button" to="/products">Mua sắm ngay →</Link></div><div className="hero-shape" /></section><section className="container home-section"><div className="section-heading"><div><p className="eyebrow">MENSTYLE EDIT</p><h2>Sản phẩm nổi bật</h2></div><Link to="/products">Xem tất cả →</Link></div>{state.loading ? <Loading /> : state.error ? <ErrorState message={state.error} /> : <ProductGrid products={state.products} />}</section><section className="container categories-section"><p className="eyebrow">KHÁM PHÁ PHONG CÁCH</p><h2>Chọn theo danh mục</h2><div className="category-links">{state.categories.slice(0, 6).map((item) => <Link key={item.id} to={`/products?categoryId=${item.id}`}>{item.name}<span>→</span></Link>)}</div></section></>;
}
