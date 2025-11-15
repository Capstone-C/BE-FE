import { Outlet } from 'react-router-dom';
import { HeaderNav } from '@/features/common/components/HeaderNav';
import { Footer } from '@/features/common/components/Footer';

export function RootLayout() {
  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      <HeaderNav />
      <main className="flex-grow">
        <div className="py-6 container mx-auto px-4">
          <Outlet />
        </div>
      </main>
      <Footer />
    </div>
  );
}
