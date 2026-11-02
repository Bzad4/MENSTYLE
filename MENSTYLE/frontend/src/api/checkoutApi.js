import axiosClient from './axiosClient';

const unwrap = ({ data }) => {
  if (!data?.success) throw new Error(data?.message || 'Không thể tạo đơn hàng.');
  return data.data;
};

export const checkout = (payload) => axiosClient.post('/checkout', payload).then(unwrap);
