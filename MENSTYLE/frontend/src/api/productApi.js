import axiosClient from './axiosClient';

const unwrap = ({ data }) => {
  if (!data?.success) throw new Error(data?.message || 'Không thể tải dữ liệu.');
  return data.data;
};

export const getProducts = (params) => axiosClient.get('/public/products', { params }).then(unwrap);
export const getProductBySlug = (slug) => axiosClient.get(`/public/products/slug/${encodeURIComponent(slug)}`).then(unwrap);
export const getCategories = () => axiosClient.get('/public/categories').then(unwrap);
export const getBrands = () => axiosClient.get('/public/brands').then(unwrap);
