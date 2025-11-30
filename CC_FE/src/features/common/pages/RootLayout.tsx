import { Outlet } from 'react-router-dom';
import Header from '@/features/common/components/Header';
import NavigationBar from '@/features/common/components/NavigationBar';
import { Footer } from '@/features/common/components/Footer';

export function RootLayout() {
  return (
    <div className="flex flex-col min-h-screen bg-[#F7F9F2] font-sans">
      <Header />
      <NavigationBar />
      <main className="flex-grow w-full">
        {/* max-w-7xl과 padding은 각 페이지에서 필요에 따라 적용하거나 여기서 공통 적용 */}
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}