import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { listCategories } from '@/apis/categories.api';
import { usePosts } from '@/features/boards/hooks/usePosts';
import RecipeCard from '@/features/recipes/components/RecipeCard';
import { useToast } from '@/contexts/ToastContext';

export default function RecipeListPage() {
  const navigate = useNavigate();
  const { show } = useToast();
  const [recipeCategoryId, setRecipeCategoryId] = useState<number | null>(null);

  // 1. 레시피 카테고리 ID 찾기
  useEffect(() => {
    listCategories().then((categories) => {
      const recipeCat = categories.find((c) => c.type === 'RECIPE');
      if (recipeCat) {
        setRecipeCategoryId(recipeCat.id);
      }
    }).catch(() => {
      show('카테고리 정보를 불러오지 못했습니다.', { type: 'error' });
    });
  }, [show]);

  // 2. 해당 카테고리의 게시글(레시피) 목록 조회
  const { data, isLoading } = usePosts({
    boardId: recipeCategoryId ?? undefined,
    size: 12,
    sort: 'createdAt',
  });

  const recipes = data?.content ?? [];

  const handleWriteClick = () => {
    navigate('/recipes/new');
  };

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col sm:flex-row justify-between sm:items-end mb-8 gap-4">
        <div>
          <h1 className="text-3xl font-bold leading-tight text-gray-900">
            레시피
          </h1>
          <p className="mt-2 text-md text-gray-500">
            나만의 레시피, 함께 나누고 즐겨보세요.
          </p>
        </div>
        {/* [수정됨] 버튼 색상 변경 */}
        <button
          onClick={handleWriteClick}
          className="px-4 py-2 bg-[#4E652F] text-white text-sm font-medium rounded-md hover:bg-[#425528] transition-colors flex-shrink-0"
        >
          레시피 작성
        </button>
      </div>

      {isLoading ? (
        <div className="text-center py-20 text-gray-500">레시피를 불러오는 중입니다...</div>
      ) : recipes.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
          {recipes.map((recipe) => (
            <RecipeCard key={recipe.id} recipe={recipe} />
          ))}
        </div>
      ) : (
        <div className="text-center py-20 text-gray-500 bg-gray-50 rounded-lg border border-dashed border-gray-300">
          <p className="mb-4">등록된 레시피가 없습니다.</p>
          <Link to="/recipes/new" className="text-[#4E652F] hover:underline font-medium">
            첫 번째 레시피를 작성해보세요!
          </Link>
        </div>
      )}
    </div>
  );
}