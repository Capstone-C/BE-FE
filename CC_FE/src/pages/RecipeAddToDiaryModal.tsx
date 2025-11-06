// src/pages/RecipeAddToDiaryModal.tsx
import { useMemo, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createDiary, type MealType } from '@/apis/diary';

const MEAL_OPTIONS: { value: MealType; label: string }[] = [
  { value: 'BREAKFAST', label: '아침' },
  { value: 'LUNCH', label: '점심' },
  { value: 'DINNER', label: '저녁' },
  { value: 'SNACK', label: '간식' },
];

function todayYmdLocal() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export default function RecipeAddToDiaryModal() {
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { recipeId } = useParams();
  const [sp] = useSearchParams();

  const defaultTitle = sp.get('title') ?? '';
  const defaultImage = sp.get('imageUrl') ?? '';

  const [date, setDate] = useState<string>(todayYmdLocal());
  const [mealType, setMealType] = useState<MealType | ''>('');
  const [content, setContent] = useState<string>(defaultTitle);
  const [imageUrl, setImageUrl] = useState<string>(defaultImage);

  const validate = useMemo(() => {
    return () => {
      if (!date) {
        alert('날짜를 선택해주세요.');
        return false;
      }
      if (!mealType) {
        alert('식사 타입을 선택해주세요.');
        return false;
      }
      if (!content.trim()) {
        alert('메뉴를 입력해주세요.');
        return false;
      }
      return true;
    };
  }, [date, mealType, content]);

  const { mutateAsync, isPending } = useMutation({
    mutationFn: () =>
      createDiary({
        date,
        mealType: mealType as MealType,
        content: content.trim(),
        imageUrl: imageUrl || undefined,
        recipeId: recipeId ? Number(recipeId) : undefined,
      }),
    onSuccess: async () => {
      alert('다이어리에 추가되었습니다.');
      await Promise.all([
        qc.invalidateQueries({ queryKey: ['diary-day', date] }),
        qc.invalidateQueries({ queryKey: ['monthly-diary'] }),
      ]);
      // 원래 페이지로 돌아가거나 다이어리로 이동
      if (window.history.length > 1) navigate(-1);
      else navigate('/diary');
    },
  });

  const onClose = () => {
    if (window.history.length > 1) navigate(-1);
    else navigate('/diary');
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    await mutateAsync();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="relative bg-white rounded-lg shadow-xl w-full max-w-xl mx-4 max-h-[90vh] overflow-hidden">
        <div className="flex items-center justify-between border-b px-5 py-3">
          <h2 className="text-xl font-semibold">다이어리에 추가하기</h2>
          <button className="px-3 py-1 border rounded" onClick={onClose}>
            닫기
          </button>
        </div>
        <form onSubmit={onSubmit} className="p-5 space-y-4 overflow-y-auto">
          <div>
            <label className="block text-sm font-medium mb-1">날짜</label>
            <input
              type="date"
              className="w-full border rounded px-3 py-2"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">식사 타입</label>
            <select
              className="w-full border rounded px-3 py-2"
              value={mealType}
              onChange={(e) => setMealType(e.target.value as MealType | '')}
              required
            >
              <option value="" disabled>
                선택하세요
              </option>
              {MEAL_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">메뉴/내용</label>
            <input
              type="text"
              className="w-full border rounded px-3 py-2"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">이미지 URL (선택)</label>
            <input
              type="url"
              className="w-full border rounded px-3 py-2"
              value={imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
              placeholder="https://..."
            />
          </div>
          <div className="pt-2 flex gap-2 justify-end">
            <button type="button" className="px-3 py-2 border rounded" onClick={onClose}>
              취소
            </button>
            <button
              type="submit"
              className="px-3 py-2 border rounded bg-blue-600 text-white disabled:opacity-50"
              disabled={isPending}
            >
              {isPending ? '추가 중…' : '추가하기'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
