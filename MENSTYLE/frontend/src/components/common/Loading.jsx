export default function Loading({ label = 'Đang tải dữ liệu...' }) {
  return <div className="state-card loading-state"><span className="spinner" />{label}</div>;
}
