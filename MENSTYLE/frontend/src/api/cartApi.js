import axiosClient from './axiosClient';

const unwrap = ({ data }) => {
  if (!data?.success) throw new Error(data?.message || 'Không thể cập nhật giỏ hàng.');
  return data.data;
};
export const getCart = () => axiosClient.get('/cart').then(unwrap);
export const addCartItem = (payload) => axiosClient.post('/cart/items', payload).then(unwrap);
export const updateCartItem = (id, payload) => axiosClient.patch(`/cart/items/${id}`, payload).then(unwrap);
export const removeCartItem = (id) => axiosClient.delete(`/cart/items/${id}`).then(unwrap);
export const clearCart = () => axiosClient.delete('/cart').then(unwrap);
