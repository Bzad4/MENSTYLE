import { useMemo, useState } from 'react';

const money = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value);

export default function ProductVariants({ variants = [], onAdd }) {
  const sizes = [...new Set(variants.map((item) => item.size))];
  const colors = [...new Set(variants.map((item) => item.color))];
  const [size, setSize] = useState(sizes[0] || '');
  const [color, setColor] = useState(colors[0] || '');
  const selected = useMemo(() => variants.find((item) => item.size === size && item.color === color), [variants, size, color]);
  return <div className="variant-selector"><div><label>Kích cỡ</label><div className="option-row">{sizes.map((item) => <button type="button" className={item === size ? 'selected' : ''} aria-pressed={item === size} key={item} onClick={() => setSize(item)}>{item}</button>)}</div></div><div><label>Màu sắc</label><div className="option-row">{colors.map((item) => <button type="button" className={item === color ? 'selected' : ''} aria-pressed={item === color} key={item} onClick={() => setColor(item)}>{item}</button>)}</div></div>{selected ? <div className="selected-variant"><strong>{money(selected.price)}</strong><span>SKU: {selected.sku}</span><span className={selected.inStock ? 'in-stock' : 'out-stock'}>{selected.inStock ? `Còn ${selected.stockQuantity} sản phẩm` : 'Hết hàng'}</span></div> : <p className="variant-warning">Không có biến thể phù hợp.</p>}<button className="primary-button" type="button" disabled={!selected?.inStock} onClick={() => onAdd?.(selected)}>Thêm vào giỏ hàng</button></div>;
}
