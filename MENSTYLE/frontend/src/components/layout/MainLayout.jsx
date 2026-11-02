import { Outlet } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';
import { WishlistProvider } from '../../context/WishlistContext';

export default function MainLayout() {
  return <WishlistProvider><div className="app-shell"><Header /><main className="site-main"><Outlet /></main><Footer /></div></WishlistProvider>;
}
