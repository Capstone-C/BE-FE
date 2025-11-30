import Container from '@/components/ui/Container';
import Button from '@/components/ui/Button';
import { Link } from 'react-router-dom';

export default function HomePage() {
  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-purple-50 via-white to-indigo-50 -z-10"></div>
        <Container className="py-32 px-8">
          <div className="grid md:grid-cols-2 gap-16 items-center">
            <div className="space-y-8 animate-fade-in">
              <div className="inline-block px-6 py-3 bg-gradient-to-r from-purple-100 to-indigo-100 rounded-full text-base font-medium text-purple-700 mb-6">
                ✨ 스마트한 레시피 관리 플랫폼
              </div>
              <h1 className="text-5xl md:text-6xl font-bold text-gray-900 leading-tight">
                나만의 레시피와<br />
                <span className="gradient-text">식생활을 한 곳에서</span>
              </h1>
              <p className="text-xl text-gray-600 leading-relaxed">
                레시피 작성·공유, 내 냉장고 재료 비교, 식단 다이어리까지.<br />
                간편하게 시작하세요.
              </p>
              <div className="flex flex-wrap gap-4 pt-6">
                <Link to="/boards">
                  <Button size="lg">🔍 레시피 둘러보기</Button>
                </Link>
                <Link to="/recipes/new">
                  <Button variant="outline" size="lg">✍️ 레시피 작성</Button>
                </Link>
              </div>
            </div>
            <div className="hidden md:block">
              <div className="relative">
                <div className="aspect-video rounded-3xl bg-gradient-to-br from-purple-200 via-indigo-200 to-blue-200 shadow-2xl transform rotate-2 hover:rotate-0 transition-transform duration-300"></div>
                <div className="absolute inset-0 aspect-video rounded-3xl bg-white shadow-2xl flex items-center justify-center text-6xl">
                  🍳
                </div>
              </div>
            </div>
          </div>
        </Container>
      </section>

      {/* Features Section */}
      <section className="py-20">
        <Container className="px-8">
          <div className="text-center mb-20">
            <h2 className="text-5xl font-bold text-gray-900 mb-8">주요 기능</h2>
            <p className="text-2xl text-gray-600">Capstone이 제공하는 다양한 기능을 만나보세요</p>
          </div>
          <div className="grid md:grid-cols-3 gap-12 max-w-7xl mx-auto">
            <Link to="/refrigerator" className="group">
              <div className="relative h-full bg-white rounded-2xl p-10 shadow-lg hover:shadow-2xl transition-all duration-300 hover:-translate-y-2 border border-gray-100">
                <div className="absolute inset-0 bg-gradient-to-br from-purple-50 to-transparent rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity"></div>
                <div className="relative">
                  <div className="w-16 h-16 bg-gradient-to-br from-purple-500 to-indigo-500 rounded-2xl flex items-center justify-center text-3xl mb-6 shadow-lg">
                    🧊
                  </div>
                  <h3 className="text-2xl font-bold text-gray-900 mb-4">내 냉장고</h3>
                  <p className="text-base text-gray-600 leading-relaxed">보유 재료를 관리하고 레시피와 비교해 부족한 재료를 확인하세요.</p>
                </div>
              </div>
            </Link>
            <Link to="/diary" className="group">
              <div className="relative h-full bg-white rounded-2xl p-10 shadow-lg hover:shadow-2xl transition-all duration-300 hover:-translate-y-2 border border-gray-100">
                <div className="absolute inset-0 bg-gradient-to-br from-indigo-50 to-transparent rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity"></div>
                <div className="relative">
                  <div className="w-16 h-16 bg-gradient-to-br from-indigo-500 to-blue-500 rounded-2xl flex items-center justify-center text-3xl mb-6 shadow-lg">
                    📖
                  </div>
                  <h3 className="text-2xl font-bold text-gray-900 mb-4">식단 다이어리</h3>
                  <p className="text-base text-gray-600 leading-relaxed">하루 식단을 기록하고 레시피를 연동해 더 편하게 관리하세요.</p>
                </div>
              </div>
            </Link>
            <Link to="/community" className="group">
              <div className="relative h-full bg-white rounded-2xl p-10 shadow-lg hover:shadow-2xl transition-all duration-300 hover:-translate-y-2 border border-gray-100">
                <div className="absolute inset-0 bg-gradient-to-br from-blue-50 to-transparent rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity"></div>
                <div className="relative">
                  <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-2xl flex items-center justify-center text-3xl mb-6 shadow-lg">
                    💬
                  </div>
                  <h3 className="text-2xl font-bold text-gray-900 mb-4">커뮤니티</h3>
                  <p className="text-base text-gray-600 leading-relaxed">자유롭게 소통하고 질문/팁을 나누세요.</p>
                </div>
              </div>
            </Link>
          </div>
        </Container>
      </section>
    </div>
  );
}
