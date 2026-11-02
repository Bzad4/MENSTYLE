import axiosClient from './axiosClient';

const unwrap = ({ data }) => {
  if (!data?.success) throw new Error(data?.message || 'Yêu cầu không thành công.');
  return data.data;
};

export const getAdminProfile = () => axiosClient.get('/admin/me').then(unwrap);
export const updateAdminProfile = (payload) => axiosClient.put('/admin/me', payload).then(unwrap);
export const changeAdminPassword = (payload) => axiosClient.post('/admin/change-password', payload).then(unwrap);
export const getAdminProducts = (params) => axiosClient.get('/admin/products', { params }).then(unwrap);
export const getAdminProduct = (id) => axiosClient.get(`/admin/products/${id}`).then(unwrap);
export const createAdminProduct = (payload) => axiosClient.post('/admin/products', payload).then(unwrap);
export const updateAdminProduct = (id, payload) => axiosClient.put(`/admin/products/${id}`, payload).then(unwrap);
export const updateAdminProductStatus = (id, value) => axiosClient.patch(`/admin/products/${id}/status`, null, { params: { value } }).then(unwrap);
export const getAdminCategories = (params) => axiosClient.get('/admin/categories', { params }).then(unwrap);
export const createAdminCategory = (payload) => axiosClient.post('/admin/categories', payload).then(unwrap);
export const updateAdminCategory = (id, payload) => axiosClient.put(`/admin/categories/${id}`, payload).then(unwrap);
export const updateAdminCategoryStatus = (id, value) => axiosClient.patch(`/admin/categories/${id}/status`, null, { params: { value } }).then(unwrap);
export const getAdminBrands = (params) => axiosClient.get('/admin/brands', { params }).then(unwrap);
export const createAdminBrand = (payload) => axiosClient.post('/admin/brands', payload).then(unwrap);
export const updateAdminBrand = (id, payload) => axiosClient.put(`/admin/brands/${id}`, payload).then(unwrap);
export const updateAdminBrandStatus = (id, value) => axiosClient.patch(`/admin/brands/${id}/status`, null, { params: { value } }).then(unwrap);
export const createAdminVariant = (productId, payload) => axiosClient.post(`/admin/products/${productId}/variants`, payload).then(unwrap);
export const addAdminImage = (productId, payload) => axiosClient.post(`/admin/products/${productId}/images`, null, { params: payload }).then(unwrap);
export const uploadAdminImage = (productId, file, primary = false, sortOrder = 0) => {
  const form = new FormData(); form.append('file', file);
  return axiosClient.post(`/admin/products/${productId}/images/upload`, form, { params: { primary, sortOrder }, headers: { 'Content-Type': 'multipart/form-data' } }).then(unwrap);
};
export const getAdminSizes = () => axiosClient.get('/admin/sizes').then(unwrap);
export const getAdminColors = () => axiosClient.get('/admin/colors').then(unwrap);
export const getAdminOrders = (params) => axiosClient.get('/admin/orders', { params }).then(unwrap);
export const getAdminOrder = (id) => axiosClient.get(`/admin/orders/${id}`).then(unwrap);
export const updateAdminOrderStatus = (id, value, note) => axiosClient.patch(`/admin/orders/${id}/status`, { note }, { params: { value } }).then(unwrap);
export const getAdminCustomers = (params) => axiosClient.get('/admin/customers', { params }).then(unwrap);
export const updateAdminCustomerStatus = (id, value) => axiosClient.patch(`/admin/customers/${id}/status`, null, { params: { value } }).then(unwrap);
export const getAdminUsers = (params) => axiosClient.get('/admin/users', { params }).then(unwrap);
export const createAdminUser = (payload) => axiosClient.post('/admin/users', payload).then(unwrap);
export const updateAdminUserStatus = (id, value) => axiosClient.patch(`/admin/users/${id}/status`, null, { params: { value } }).then(unwrap);
export const getAdminInventory = (params) => axiosClient.get('/admin/inventory', { params }).then(unwrap);
export const adjustAdminInventory = (id, payload) => axiosClient.put(`/admin/inventory/${id}`, payload).then(unwrap);
