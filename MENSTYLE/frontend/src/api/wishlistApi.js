import axiosClient from './axiosClient';

const unwrap = ({ data }) => {
  if (!data?.success) throw new Error(data?.message || 'Không thể cập nhật danh sách yêu thích.');
  return data.data;
};
export const getWishlist = () => axiosClient.get('/wishlist').then(unwrap);
export const addWishlistItem = (productId) => axiosClient.post(`/wishlist/items/${productId}`).then(unwrap);
export const removeWishlistItem = (productId) => axiosClient.delete(`/wishlist/items/${productId}`).then(unwrap);
