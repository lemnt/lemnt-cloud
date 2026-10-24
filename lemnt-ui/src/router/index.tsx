import { Route, Routes } from 'react-router-dom';
import LoginPage from '../modules/auth/pages/LoginPage';
import AuthenticatedLayout from '../layouts/AuthenticatedLayout';

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="*" element={<AuthenticatedLayout />} />
    </Routes>
  );
}
