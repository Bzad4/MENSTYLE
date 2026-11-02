import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { getCurrentUser, login as loginRequest, register as registerRequest } from '../api/authApi';

const AuthContext = createContext(null);
const TOKEN_KEY = 'accessToken';
const getToken = () => localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(TOKEN_KEY);
};
const messageFor = (error, fallback) => {
  if (!error?.response) return 'Không thể kết nối máy chủ. Vui lòng thử lại sau.';
  return error.response.data?.message || error.message || fallback;
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const refreshUser = useCallback(async () => {
    if (!getToken()) {
      setUser(null);
      setLoading(false);
      return null;
    }
    setLoading(true);
    try {
      const currentUser = await getCurrentUser();
      setUser(currentUser);
      setError('');
      return currentUser;
    } catch (requestError) {
      if (requestError.response?.status === 401) {
        clearToken();
        setUser(null);
      } else {
        setError(messageFor(requestError, 'Không thể khôi phục phiên đăng nhập.'));
      }
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshUser();
  }, [refreshUser]);

  const login = useCallback(async (credentials, remember = true) => {
    setError('');
    const response = await loginRequest(credentials);
    if (!response?.accessToken || !response.user) throw new Error('Phản hồi đăng nhập không hợp lệ.');
    clearToken();
    const storage = remember ? localStorage : sessionStorage;
    storage.setItem(TOKEN_KEY, response.accessToken);
    setUser(response.user);
    return response.user;
  }, []);

  const register = useCallback((payload) => registerRequest(payload), []);

  const logout = useCallback(() => {
    clearToken();
    setUser(null);
    setError('');
  }, []);

  const value = useMemo(() => ({
    user,
    isAuthenticated: Boolean(user),
    loading,
    error,
    login,
    register,
    logout,
    refreshUser,
  }), [user, loading, error, login, register, logout, refreshUser]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
