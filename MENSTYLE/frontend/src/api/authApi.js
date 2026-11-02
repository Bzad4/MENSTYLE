import axiosClient from './axiosClient';

const unwrap = ({ data }) => {
  if (!data?.success) throw new Error(data?.message || 'Yêu cầu không thành công.');
  return data.data;
};

export const register = (payload) => axiosClient.post('/auth/register', payload).then(unwrap);
export const login = (payload) => axiosClient.post('/auth/login', payload).then(unwrap);
export const getCurrentUser = () => axiosClient.get('/auth/me').then(unwrap);
export const forgotPassword = (payload) => axiosClient.post('/auth/forgot-password', payload).then(unwrap);
export const resetPassword = (payload) => axiosClient.post('/auth/reset-password', payload).then(unwrap);
