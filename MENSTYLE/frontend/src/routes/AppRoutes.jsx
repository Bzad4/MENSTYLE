import { Route, Routes } from 'react-router-dom';
import MainLayout from '../components/layout/MainLayout';
import HomePage from '../pages/HomePage';
import ProductListPage from '../pages/ProductListPage';
import ProductDetailPage from '../pages/ProductDetailPage';
import NotFoundPage from '../pages/NotFoundPage';
import CartPage from '../pages/CartPage';
import WishlistPage from '../pages/WishlistPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import ForgotPasswordPage from '../pages/ForgotPasswordPage';
import ResetPasswordPage from '../pages/ResetPasswordPage';
import ProtectedRoute from '../components/auth/ProtectedRoute';
import CheckoutPage from '../pages/CheckoutPage';
import OrderSuccessPage from '../pages/OrderSuccessPage';
import OrderListPage from '../pages/OrderListPage';
import OrderDetailPage from '../pages/OrderDetailPage';
import AdminLoginPage from '../pages/AdminLoginPage';
import AdminDashboardPage from '../pages/AdminDashboardPage';
import AdminProfilePage from '../pages/AdminProfilePage';
import AdminForbiddenPage from '../pages/AdminForbiddenPage';
import AdminProductListPage from '../pages/AdminProductListPage';
import AdminProductFormPage from '../pages/AdminProductFormPage';
import AdminTaxonomyPage from '../pages/AdminTaxonomyPage';
import AdminProtectedRoute from '../components/auth/AdminProtectedRoute';
import AdminLayout from '../components/layout/AdminLayout';
import AdminOrdersPage from '../pages/AdminOrdersPage';
import AdminCustomersPage from '../pages/AdminCustomersPage';
import AdminUsersPage from '../pages/AdminUsersPage';
import AdminInventoryPage from '../pages/AdminInventoryPage';

export default function AppRoutes() {
  return <Routes>
    <Route element={<MainLayout />}>
      <Route path="/" element={<HomePage />} />
      <Route path="/products" element={<ProductListPage />} />
      <Route path="/products/:slug" element={<ProductDetailPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route element={<ProtectedRoute />}><Route path="/cart" element={<CartPage />} /><Route path="/wishlist" element={<WishlistPage />} /><Route path="/checkout" element={<CheckoutPage />} /><Route path="/order-success/:orderId" element={<OrderSuccessPage />} /><Route path="/account/orders" element={<OrderListPage />} /><Route path="/account/orders/:orderId" element={<OrderDetailPage />} /></Route>
      <Route path="*" element={<NotFoundPage />} />
    </Route>
    <Route path="/admin/login" element={<AdminLoginPage />} />
    <Route path="/admin/403" element={<AdminForbiddenPage />} />
    <Route element={<AdminProtectedRoute />}>
      <Route element={<AdminLayout />}>
        <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
        <Route path="/admin/profile" element={<AdminProfilePage />} />
        <Route path="/admin/products" element={<AdminProductListPage />} />
        <Route path="/admin/products/new" element={<AdminProductFormPage />} />
        <Route path="/admin/products/:id" element={<AdminProductFormPage />} />
        <Route path="/admin/products/:id/edit" element={<AdminProductFormPage />} />
        <Route path="/admin/categories" element={<AdminTaxonomyPage type="categories" />} />
        <Route path="/admin/brands" element={<AdminTaxonomyPage type="brands" />} />
        <Route path="/admin/orders" element={<AdminOrdersPage />} />
        <Route path="/admin/orders/:id" element={<AdminOrdersPage />} />
        <Route path="/admin/customers" element={<AdminCustomersPage />} />
        <Route path="/admin/users" element={<AdminUsersPage />} />
        <Route path="/admin/inventory" element={<AdminInventoryPage />} />
        <Route path="/admin/*" element={<NotFoundPage />} />
      </Route>
    </Route>
  </Routes>;
}
