import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ThumbsUpIcon, EyeIcon } from '@/components/ui/Icons';
import { usePosts } from '@/features/boards/hooks/usePosts';
import { listCategories } from '@/apis/categories.api';

const BestRecipesSection: React.FC = () => {
  const [recipeCategoryId, setRecipeCategoryId] = useState<number | undefined>(undefined);

  // 레시피 카테고리 ID 찾기
  useEffect(() => {
    listCategories().then((categories) => {
      const recipeCat = categories.find((c) => c.type === 'RECIPE');
      if (recipeCat) {
        setRecipeCategoryId(recipeCat.id);
      }
    }).catch(console.error);
  }, []);

  // 레시피 카테고리의 글만, 좋아요 순으로 상위 3개 조회
  const { data, isLoading } = usePosts({
    size: 3,
    sort: 'likeCount',
    boardId: recipeCategoryId, // 레시피 카테고리 ID 전달
  });

  if (isLoading) return <div className="py-12 text-center">로딩 중...</div>;

  const bestRecipes = data?.content || [];

  return (
    <section className="py-12 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center mb-8">
          <div className="bg-yellow-100 p-2 rounded-full mr-3">
            <span className="text-2xl">🏆</span>
          </div>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">
              <span className="block sm:inline">지금 가장 핫한 레시피</span>
            </h2>
            <p className="text-gray-500 text-sm mt-1">
              커뮤니티에서 가장 많은 추천을 받은 레시피입니다.
            </p>
          </div>
        </div>

        {bestRecipes.length === 0 ? (
          <div className="text-gray-500 text-center py-10">아직 등록된 레시피가 없습니다.</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {bestRecipes.map((recipe, index) => (
              <Link
                key={recipe.id}
                to={`/boards/${recipe.id}`}
                className="group cursor-pointer relative flex flex-col bg-white rounded-xl shadow-md overflow-hidden hover:shadow-xl hover:-translate-y-1 transition-all duration-300 border border-gray-100"
              >
                {/* Rank Badge */}
                <div className="absolute top-4 left-4 z-10 bg-[#4E652F] text-white text-xs font-bold w-8 h-8 flex items-center justify-center rounded-full shadow-md border-2 border-white">
                  {index + 1}위
                </div>

                {/* Image Area */}
                <div className="relative h-48 overflow-hidden">
                  {recipe.thumbnailUrl ? (
                    <img
                      src={recipe.thumbnailUrl}
                      alt={recipe.title}
                      className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                    />
                  ) : (
                    <div className="w-full h-full bg-gray-200 flex items-center justify-center text-gray-400">
                      이미지 없음
                    </div>
                  )}
                  {/* Overlay Gradient */}
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-60"></div>

                  {/* Stats on Image */}
                  <div className="absolute bottom-3 left-3 right-3 flex justify-between items-center text-white text-xs font-medium">
                    <div className="flex items-center space-x-1 bg-black/30 px-2 py-1 rounded-full backdrop-blur-sm">
                      <ThumbsUpIcon className="w-3 h-3 text-red-400" />
                      <span>{recipe.likeCount}</span>
                    </div>
                    <div className="flex items-center space-x-1 bg-black/30 px-2 py-1 rounded-full backdrop-blur-sm">
                      <EyeIcon className="w-3 h-3" />
                      <span>{recipe.viewCount > 999 ? (recipe.viewCount / 1000).toFixed(1) + 'k' : recipe.viewCount}</span>
                    </div>
                  </div>
                </div>

                {/* Content Area */}
                <div className="p-5 flex flex-col flex-grow">
                  <h3 className="text-lg font-bold text-gray-800 group-hover:text-[#4E652F] transition-colors line-clamp-1 mb-2">
                    {recipe.title}
                  </h3>
                  <div className="mt-auto flex items-center justify-between">
                    <div className="flex items-center">
                      <div className="w-6 h-6 rounded-full bg-gray-300 flex items-center justify-center text-[10px] text-white font-bold mr-2 overflow-hidden">
                        {recipe.authorName ? recipe.authorName.charAt(0) : '?'}
                      </div>
                      <span className="text-sm text-gray-600 truncate max-w-[100px]">{recipe.authorName}</span>
                    </div>
                    <span className="text-xs text-gray-400">{new Date(recipe.createdAt).toLocaleDateString()}</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </section>
  );
};

export default BestRecipesSection;