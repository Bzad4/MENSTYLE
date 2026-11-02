import AppRoutes from './routes/AppRoutes';
import './styles/global.css';
import { AuthProvider } from './context/AuthContext';

function App() {
  return <AuthProvider><AppRoutes /></AuthProvider>;
}

export default App;
