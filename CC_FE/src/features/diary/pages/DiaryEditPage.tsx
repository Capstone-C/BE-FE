import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getDiaryByDate,
  type DiaryEntryResponse,
  type MealType,
  updateDiary,
  type UpdateDiaryRequest,
} from '@/apis/diary.api';
import ImageUploader from '@/components/ui/ImageUploader';
import { getPost } from '@/apis/boards.api';
import { parseRecipeUrlToId } from '@/utils/recipe';

const MEAL_OPTIONS: { value: MealType; label: string }[] = [
  { value: 'BREAKFAST', label: '아침' },
  { value: 'LUNCH', label: '점심' },
  { value: 'DINNER', label: '저녁' },
  { value: 'SNACK', label: '간식' },
];

export default function DiaryEditPage() {
  const { date, id } = useParams();
  const navigate = useNavigate();
  const qc = useQueryClient();

  const { data } = useQuery<DiaryEntryResponse[]>({
    queryKey: ['diary-day', date],
    queryFn: () => getDiaryByDate(date!),
    enabled: !!date,
  });

  const current = useMemo(() => data?.find((e) => e.id === Number(id)), [data, id]);

  const [form, setForm] = useState<UpdateDiaryRequest>({
    mealType: current?.mealType,
    content: current?.content ?? '',
    imageUrl: current?.imageUrl ?? '',
    recipeId: current?.recipeId ?? undefined,
  });
  const [recipeUrlInput, setRecipeUrlInput] = useState<string>(current?.recipeId ? String(current.recipeId) : '');
  const [recipeTitle, setRecipeTitle] = useState<string | null>(null);

  useEffect(() => {
    if (current) {
      setForm({
        mealType: current.mealType,
        content: current.content,
        imageUrl: current.imageUrl ?? '',
        recipeId: current.recipeId ?? undefined,
      });
      setRecipeUrlInput(current.recipeId ? String(current.recipeId) : '');
    }
  }, [current]);

  useEffect(() => {
    async function loadTitle() {
      if (form.recipeId) {
        try {
          const post = await getPost(form.recipeId);
          setRecipeTitle(post.title);
        } catch {
          setRecipeTitle(null);
        }
      } else {
        setRecipeTitle(null);
      }
    }
    void loadTitle();
  }, [form.recipeId]);

  const { mutateAsync, isPending } = useMutation({
    mutationFn: (payload: UpdateDiaryRequest) => updateDiary(Number(id), payload),
    onSuccess: async () => {
      alert('기록이 수정되었습니다.');
      await Promise.all([
        qc.invalidateQueries({ queryKey: ['diary-day', date] }),
        qc.invalidateQueries({ queryKey: ['monthly-diary'] }),
      ]);
      navigate(`/diary/${date}`);
    },
  });

  const onClose = () => navigate(`/diary/${date}`);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm((prev) => {
      const next = { ...prev } as UpdateDiaryRequest;
      if (name === 'content') {
        next.content = value;
      } else if (name === 'mealType') {
        next.mealType = value as MealType;
      }
      return next;
    });
  };

  const handleRecipeUrlChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setRecipeUrlInput(val);
    const parsed = parseRecipeUrlToId(val);
    setForm((prev) => ({ ...prev, recipeId: parsed }));
  };

  const handleImageChange = (url: string) => {
    setForm((prev) => ({ ...prev, imageUrl: url }));
  };

  const validate = useMemo(() => {
    return () => {
      const errors: { mealType?: string; content?: string } = {};
      if (!form.mealType) errors.mealType = '식사 타입을 선택해주세요.';
      if (!form.content || form.content.trim().length === 0) errors.content = '메뉴를 입력해주세요.';
      if (Object.keys(errors).length > 0) {
        alert(Object.values(errors).join('\n'));
        return false;
      }
      return true;
    };
  }, [form.mealType, form.content]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    await mutateAsync({
      mealType: form.mealType,
      content: form.content?.trim(),
      imageUrl: form.imageUrl ? form.imageUrl : undefined,
      recipeId: form.recipeId ?? undefined,
    });
  };

  if (!current) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center">
        <div className="absolute inset-0 bg-black/40" onClick={onClose} />
        <div className="relative bg-white rounded-lg shadow-xl w-full max-w-xl mx-4 p-6">
          <div className="text-center">존재하지 않는 식단 기록이거나 데이터를 불러오는 중입니다.</div>
          <div className="text-center mt-4">
            <button className="px-3 py-1 border rounded" onClick={onClose}>
              닫기
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="relative bg-white rounded-lg shadow-xl w-full max-w-xl mx-4 max-h-[90vh] overflow-hidden">
        <div className="flex items-center justify-between border-b px-5 py-3">
          <h2 className="text-xl font-semibold">식단 수정</h2>
          <button className="px-3 py-1 border rounded" onClick={onClose}>
            닫기
          </button>
        </div>

        <form onSubmit={onSubmit} className="p-5 space-y-4 overflow-y-auto">
          <div>
            <label className="block text-sm font-medium mb-1">식사 타입</label>
            <select
              name="mealType"
              value={form.mealType}
              onChange={handleChange}
              className="w-full border rounded px-3 py-2"
              required
            >
              {MEAL_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">메뉴/내용</label>
            <textarea
              name="content"
              value={form.content}
              onChange={handleChange}
              maxLength={500}
              className="w-full border rounded px-3 py-2 h-28"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">사진 (선택)</label>
            <ImageUploader value={form.imageUrl} onChange={handleImageChange} placeholder="식단 사진 업로드" />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">레시피 URL 또는 ID (선택)</label>
            <input
              type="text"
              name="recipeUrl"
              value={recipeUrlInput}
              onChange={handleRecipeUrlChange}
              placeholder="예: /boards/123 또는 전체 URL"
              className="w-full border rounded px-3 py-2"
            />
            {form.recipeId && (
              <p className="text-xs text-gray-600 mt-1">
                연결된 레시피: {recipeTitle ? recipeTitle : `#${form.recipeId}`}
              </p>
            )}
          </div>

          <div className="pt-2 flex gap-2 justify-end">
            <button type="button" className="px-3 py-2 border rounded" onClick={onClose}>
              취소
            </button>
            {/* [수정됨] 수정 완료 버튼 색상 변경: bg-blue-600 -> bg-[#4E652F] */}
            <button
              type="submit"
              className="px-3 py-2 border rounded bg-[#4E652F] text-white disabled:opacity-50 hover:bg-[#425528]"
              disabled={isPending}
            >
              {isPending ? '수정 중…' : '수정 완료'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}