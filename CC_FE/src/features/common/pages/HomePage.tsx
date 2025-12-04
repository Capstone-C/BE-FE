import React from 'react';
import { Link } from 'react-router-dom';
// [수정] 파일명 변경 반영 (BestRecipesSection -> BestRecipeSection)
import BestRecipeSection from '@/features/common/components/BestRecipeSection';
import RecentPostsSection from '@/features/common/components/RecentPostsSection';

const HomePage: React.FC = () => {
  return (
    <div className="flex flex-col min-h-screen">
      {/* Hero Section */}
      <section className="relative bg-[#F0F5E5] py-20 sm:py-32 overflow-hidden">
        {/* Background Decoration */}
        <div className="absolute inset-0 overflow-hidden">
          <img
            src="/images/hero-bg-pattern.svg"
            alt=""
            className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-full h-full object-cover opacity-10"
            onError={(e) => (e.currentTarget.style.display = 'none')}
          />
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="lg:grid lg:grid-cols-12 lg:gap-8 items-center">
            {/* Text Content */}
            <div className="lg:col-span-6 sm:text-center lg:text-left">
              <h1 className="text-4xl tracking-tight font-extrabold text-gray-900 sm:text-5xl md:text-6xl lg:text-5xl xl:text-6xl">
                {/* [수정] 'block' 클래스를 사용하여 항상 줄바꿈 되도록 설정 */}
                <span className="block">건강한 식단,</span>
                <span className="block text-[#4E652F] mt-1">
                  즐거운 커뮤니티
                </span>
              </h1>
              <p className="mt-3 text-base text-gray-500 sm:mt-5 sm:text-lg sm:max-w-xl sm:mx-auto md:mt-5 md:text-xl lg:mx-0">
                다양한 식단 정보를 공유하고, 나에게 맞는 건강한 라이프스타일을
                찾아보세요. 함께하면 더 즐겁습니다.
              </p>
              <div className="mt-8 sm:mt-12 sm:flex sm:justify-center lg:justify-start">
                <div className="rounded-md shadow">
                  <Link
                    to="/boards"
                    className="w-full flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-white bg-[#4E652F] hover:bg-[#425528] transition-colors duration-300 md:py-4 md:text-lg md:px-10"
                  >
                    커뮤니티 시작하기
                  </Link>
                </div>
                <div className="mt-3 sm:mt-0 sm:ml-3">
                  <Link
                    to="/recipes"
                    className="w-full flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-[#4E652F] bg-[#F0F5E5] hover:bg-[#E4E9D9] transition-colors duration-300 md:py-4 md:text-lg md:px-10"
                  >
                    레시피 보기
                  </Link>
                </div>
              </div>
            </div>

            {/* Hero Image */}
            <div className="mt-12 lg:mt-0 lg:col-span-6 relative">
              <div className="mx-auto max-w-md px-4 sm:max-w-2xl sm:px-6 lg:max-w-none lg:px-0 relative">
                {/* Decorative blob shape */}
                <div className="absolute top-0 left-0 w-full h-full bg-[#4E652F] opacity-10 rounded-full filter blur-3xl transform -translate-x-1/2 -translate-y-1/4 lg:translate-x-0 lg:translate-y-0 lg:origin-top-right lg:scale-150"></div>

                <img
                  className="relative w-full rounded-2xl shadow-xl ring-1 ring-gray-900/10 z-10 transform hover:scale-105 transition-transform duration-500 object-cover"
                  src="/images/yachaewa-kong-gwa-sigmul-bucheonim-geuleus-yoli-pyeongmyeondo.jpg"
                  alt="건강한 식단 이미지"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Best Recipe Section */}
      <BestRecipeSection />

      {/* Recent Posts Section */}
      <RecentPostsSection />
    </div>
  );
};

export default HomePage;