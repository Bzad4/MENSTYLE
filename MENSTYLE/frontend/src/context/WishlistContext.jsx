import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { addWishlistItem, getWishlist, removeWishlistItem } from '../api/wishlistApi';
import { useAuth } from './AuthContext';

const WishlistContext = createContext(null);
const token = () => localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
const apiError = (error) => error?.response?.status === 401
  ? 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
  : error?.message || 'Không thể cập nhật danh sách yêu thích.';

export function WishlistProvider({ children }) {
  const { isAuthenticated, loading: authLoading } = useAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const applyWishlist = (wishlist) => {
    const nextItems = Array.isArray(wishlist?.items) ? wishlist.items : [];
    setItems(nextItems);
    return nextItems;
  };

  const refreshWishlist = async () => {
    if (!token()) {
      setItems([]);
      setError('');
      return [];
    }
    setLoading(true);
    setError('');
    try {
      return applyWishlist(await getWishlist());
    } catch (requestError) {
      setError(apiError(requestError));
      setItems([]);
      return [];
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (authLoading) return;
    if (isAuthenticated) refreshWishlist();
    else {
      setItems([]);
      setError('');
      setLoading(false);
    }
  }, [authLoading, isAuthenticated]);

  const add = async (productId) => {
    setError('');
    try {
      return applyWishlist(await addWishlistItem(productId));
    } catch (requestError) {
      setError(apiError(requestError));
      throw requestError;
    }
  };

  const remove = async (productId) => {
    setError('');
    try {
      return applyWishlist(await removeWishlistItem(productId));
    } catch (requestError) {
      setError(apiError(requestError));
      throw requestError;
    }
  };

  const value = useMemo(() => ({
    items,
    wishlistIds: new Set(items.map((item) => item.productId)),
    loading,
    error,
    refreshWishlist,
    isWishlisted: (productId) => items.some((item) => item.productId === productId),
    add,
    remove,
    toggle: (productId) => items.some((item) => item.productId === productId)
      ? remove(productId)
      : add(productId),
  }), [items, loading, error]);

  return <WishlistContext.Provider value={value}>{children}</WishlistContext.Provider>;
}

export function useWishlist() {
  const context = useContext(WishlistContext);
  if (!context) throw new Error('useWishlist must be used within WishlistProvider');
  return context;
}
