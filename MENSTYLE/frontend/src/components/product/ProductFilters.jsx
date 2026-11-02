export default function ProductFilters({ filters, categories, brands, onChange, onReset }) {
  const update = (key) => (event) => onChange(key, event.target.type === 'checkbox' ? event.target.checked : event.target.value);
  return (
    <aside className="filters">
      <div className="filter-heading"><h2>Bộ lọc</h2><button className="filter-reset" type="button" onClick={onReset}><span aria-hidden="true">↺</span> Đặt lại</button></div>
      <label>Từ khóa<input value={filters.keyword} onChange={update('keyword')} placeholder="Tìm sản phẩm..." /></label>
      <label>Danh mục<select value={filters.categoryId} onChange={update('categoryId')}><option value="">Tất cả danh mục</option>{categories.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
      <label>Thương hiệu<select value={filters.brandId} onChange={update('brandId')}><option value="">Tất cả thương hiệu</option>{brands.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
      <div className="price-fields"><label>Giá từ<input type="number" min="0" value={filters.minPrice} onChange={update('minPrice')} placeholder="0" /></label><label>Đến<input type="number" min="0" value={filters.maxPrice} onChange={update('maxPrice')} placeholder="9999999" /></label></div>
      <label>Kích cỡ<select value={filters.sizeFilter} onChange={update('sizeFilter')}><option value="">Tất cả size</option>{['S', 'M', 'L', 'XL', 'XXL'].map((size) => <option key={size}>{size}</option>)}</select></label>
      <label>Màu sắc<select value={filters.color} onChange={update('color')}><option value="">Tất cả màu</option>{['Black', 'White', 'Gray', 'Navy', 'Beige'].map((color) => <option key={color} value={color}>{color}</option>)}</select></label>
      <label className="check-label"><input type="checkbox" checked={filters.inStock} onChange={update('inStock')} /> Chỉ xem sản phẩm còn hàng</label>
    </aside>
  );
}
