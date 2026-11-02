export default function EmptyState({ title = 'Không tìm thấy sản phẩm', message = 'Hãy thử thay đổi bộ lọc hoặc từ khóa.' }) {
  return <div className="state-card"><strong>{title}</strong><span>{message}</span></div>;
}
