import { useNavigate, useParams } from 'react-router-dom';
import { usePost } from '@/features/boards/hooks/usePosts';
import { updatePost, type UpsertPostDto } from '@/apis/boards.api';
import { useToast } from '@/contexts/ToastContext';
import RecipeForm from '@/features/recipes/components/RecipeForm';
import { parseRecipeContent } from '@/features/recipes/utils/recipeContent';

export default function RecipeEditPage() {
  const { postId } = useParams();
  const nav = useNavigate();
  const { show } = useToast();
  const { data: post, isLoading, error } = usePost(Number(postId));

  if (isLoading) return <div className="p-6 text-center text-gray-500">레시피를 불러오는 중입니다...</div>;
  if (error || !post) return <div className="p-6 text-center text-red-500">레시피를 불러올 수 없습니다.</div>;

  // 기존 HTML 콘텐츠에서 요약과 조리 순서 파싱
  const { summary, steps } = parseRecipeContent(post.content);

  // 초기 데이터 구성
  const initialData = {
    categoryId: post.categoryId,
    title: post.title,
    status: post.status,
    dietType: post.dietType,
    cookTimeInMinutes: post.cookTimeInMinutes,
    servings: post.servings,
    difficulty: post.difficulty,
    ingredients: post.ingredients?.map((i) => ({
      name: i.name,
      quantity: i.quantity,
      unit: i.unit,
      memo: i.memo,
    })),
    thumbnailUrl: undefined, // 현재 BE 미지원으로 undefined 처리 (필요 시 post.thumbnailUrl 사용)
    summary,
    steps,
  };

  const handleUpdate = async (dto: UpsertPostDto) => {
    try {
      await updatePost(Number(postId), dto);
      show('레시피가 성공적으로 수정되었습니다.', { type: 'success' });
      nav(`/boards/${postId}`);
    } catch (err: any) {
      show(err?.response?.data?.message ?? '수정 중 오류가 발생했습니다.', { type: 'error' });
    }
  };

  return (
    <div className="p-6">
      <RecipeForm initialData={initialData} onSubmit={handleUpdate} isEdit />
    </div>
  );
}
