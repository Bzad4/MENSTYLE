export default function ErrorState({ message = 'Đã có lỗi xảy ra.' }) {
  return <div className="state-card error-state"><strong>Không thể tải dữ liệu</strong><span>{message}</span></div>;
}
