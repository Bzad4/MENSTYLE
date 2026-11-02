import axiosClient from './axiosClient';

const unwrap = ({ data }) => {
  if (!data?.success) throw new Error(data?.message || 'Không thể tải đơn hàng.');
  return data.data;
};

export const getOrder = (orderId) => axiosClient.get(`/orders/${orderId}`).then(unwrap);
export const getOrderById = getOrder;
export const getMyOrders = (params) => axiosClient.get('/orders', { params }).then(unwrap);
export const cancelOrder = (orderId, reason) => axiosClient.post(`/orders/${orderId}/cancel`, { reason }).then(unwrap);
