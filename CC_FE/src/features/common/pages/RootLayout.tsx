import { Outlet } from 'react-router-dom';
import { HeaderNav } from '@/features/common/components/HeaderNav';
import { Footer } from '@/features/common/components/Footer';

export function RootLayout() {
  return (
    <div className="flex flex-col min-h-screen">
      <HeaderNav />
      <main className="flex-grow">
        <div className="py-8 container mx-auto px-4">
          <Outlet />
        </div>
      </main>
      <Footer />
    </div>
  );
}
