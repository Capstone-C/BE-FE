import { useNavigate } from 'react-router-dom';
import { createPost, type UpsertPostDto } from '@/apis/boards.api';
import { useToast } from '@/contexts/ToastContext';
import RecipeForm from '@/features/recipes/components/RecipeForm';

export default function RecipeCreatePage() {
  const nav = useNavigate();
  const { show } = useToast();

  const handleCreate = async (dto: UpsertPostDto) => {
    try {
      const newId = await createPost(dto);
      show('레시피가 성공적으로 등록되었습니다.', { type: 'success' });

      if (typeof newId === 'number') {
        nav(`/boards/${newId}`);
      } else {
        nav('/boards');
      }
    } catch (err: any) {
      if (err?.response?.status === 401) {
        show('로그인이 필요한 서비스입니다.', { type: 'error' });
        nav('/login');
        return;
      }
      show(err?.response?.data?.message ?? '등록 중 오류가 발생했습니다.', { type: 'error' });
    }
  };

  return (
    <div className="p-6">
      <RecipeForm onSubmit={handleCreate} />
    </div>
  );
}