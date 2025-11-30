import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
// Add search params usage
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createDiary, type CreateDiaryRequest, type MealType } from '@/apis/diary.api';
import ImageUploader from '@/components/ui/ImageUploader';
import { getPost } from '@/apis/boards.api';
import { parseRecipeUrlToId } from '@/utils/recipe';

const MEAL_OPTIONS: { value: MealType; label: string }[] = [
  { value: 'BREAKFAST', label: '아침' },
  { value: 'LUNCH', label: '점심' },
  { value: 'DINNER', label: '저녁' },
  { value: 'SNACK', label: '간식' },
];

export default function DiaryCreatePage() {
  const { date } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = (location.state as { recipeId?: number } | undefined) || undefined;
  // Parse mealType from query string
  const searchParams = new URLSearchParams(location.search);
  const mealTypeFromQuery = searchParams.get('mealType') as MealType | null;
  const qc = useQueryClient();

  const [form, setForm] = useState<CreateDiaryRequest>({
    date: date ?? '',
    mealType: mealTypeFromQuery ?? 'BREAKFAST',
    content: '',
    imageUrl: '',
    recipeId: undefined,
  });
  const [recipeUrlInput, setRecipeUrlInput] = useState<string>('');
  const [recipeTitle, setRecipeTitle] = useState<string | null>(null);
  const [errors, setErrors] = useState<{ mealType?: string; content?: string }>({});

  useEffect(() => {
    if (date) setForm((f) => ({ ...f, date }));
  }, [date]);

  useEffect(() => {
    if (locationState?.recipeId) {
      setForm((f) => ({ ...f, recipeId: locationState.recipeId }));
      setRecipeUrlInput(locationState.recipeId.toString());
    }
  }, [locationState?.recipeId]);

  useEffect(() => {
    if (mealTypeFromQuery) {
      setForm((f) => ({ ...f, mealType: mealTypeFromQuery }));
    }
  }, [mealTypeFromQuery]);

  useEffect(() => {
    // When recipeId changes, attempt to fetch its title
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
    mutationFn: createDiary,
    onSuccess: async () => {
      alert('식단이 기록되었습니다.');
      await Promise.all([
        qc.invalidateQueries({ queryKey: ['diary-day', form.date] }),
        qc.invalidateQueries({ queryKey: ['monthly-diary'] }),
      ]);
      navigate(`/diary/${form.date}`);
    },
  });

  const onClose = () => navigate(`/diary/${form.date}`);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm((prev) => {
      const next = { ...prev };
      if (name === 'date') next.date = value;
      else if (name === 'mealType') next.mealType = value as MealType;
      else if (name === 'content') next.content = value;
      return next;
    });
  };

  const handleRecipeUrlChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setRecipeUrlInput(value);
    const parsed = parseRecipeUrlToId(value);
    setForm((prev) => ({ ...prev, recipeId: parsed }));
  };

  const handleImageChange = (url: string) => {
    setForm((prev) => ({ ...prev, imageUrl: url }));
  };

  const validate = useMemo(() => {
    return () => {
      const next: { mealType?: string; content?: string } = {};
      if (!form.mealType) next.mealType = '식사 타입을 선택해주세요.';
      if (!form.content || form.content.trim().length === 0) next.content = '메뉴를 입력해주세요.';
      setErrors(next);
      return Object.keys(next).length === 0;
    };
  }, [form.mealType, form.content]);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      await mutateAsync({
        date: form.date,
        mealType: form.mealType,
        content: form.content.trim(),
        imageUrl: form.imageUrl ? form.imageUrl : undefined,
        recipeId: form.recipeId ?? undefined,
      });
    } catch (err: unknown) {
      const anyErr = err as { response?: { status?: number } };
      if (anyErr?.response?.status === 409) {
        alert('이미 해당 시간에 식단이 기록되어 있습니다.');
      } else {
        alert('식단 기록 중 오류가 발생했습니다.');
      }
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-xl mx-4 max-h-[90vh] overflow-hidden">
        <div className="flex items-center justify-between border-b-2 border-gray-100 px-8 py-5 bg-gradient-to-r from-purple-50 to-indigo-50">
          <h2 className="text-3xl font-bold gradient-text">➕ 식단 추가</h2>
          <button 
            className="px-4 py-2.5 border-2 border-gray-200 rounded-xl hover:bg-white transition-all text-base" 
            onClick={onClose}
          >
            ✕
          </button>
        </div>

        <form onSubmit={onSubmit} className="p-8 space-y-6 overflow-y-auto">
          <div>
            <label className="block text-base font-semibold mb-2 text-gray-700">📅 날짜</label>
            <input
              type="date"
              name="date"
              value={form.date}
              onChange={handleChange}
              className="w-full border-2 border-gray-200 rounded-xl px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all text-base"
              required
            />
          </div>

          <div>
            <label className="block text-base font-semibold mb-2 text-gray-700">🍽️ 식사 타입</label>
            <select
              name="mealType"
              value={form.mealType}
              onChange={handleChange}
              className="w-full border-2 border-gray-200 rounded-xl px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all text-base"
              required
            >
              {MEAL_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            {errors?.mealType && <p className="text-sm text-red-600 mt-1">⚠️ {errors.mealType}</p>}
          </div>

          <div>
            <label className="block text-base font-semibold mb-2 text-gray-700">📝 메뉴/내용</label>
            <textarea
              name="content"
              value={form.content}
              onChange={handleChange}
              maxLength={500}
              placeholder="예) 닭가슴살 샐러드와 현미밥"
              className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 h-28 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all resize-none text-base"
              required
            />
            {errors?.content && <p className="text-sm text-red-600 mt-1">⚠️ {errors.content}</p>}
          </div>

          <div>
            <label className="block text-base font-semibold mb-2 text-gray-700">📸 사진 (선택)</label>
            <ImageUploader value={form.imageUrl} onChange={handleImageChange} placeholder="식단 사진 업로드" />
          </div>

          <div>
            <label className="block text-base font-semibold mb-2 text-gray-700">📖 레시피 URL 또는 ID (선택)</label>
            <input
              type="text"
              name="recipeUrl"
              value={recipeUrlInput}
              onChange={handleRecipeUrlChange}
              placeholder="예: /boards/123 또는 전체 URL"
              className="w-full border-2 border-gray-200 rounded-xl px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all text-base"
            />
            {form.recipeId && (
              <p className="text-sm text-purple-600 mt-2 font-medium">
                🔗 연결된 레시피: {recipeTitle ? recipeTitle : `#${form.recipeId}`}
              </p>
            )}
          </div>

          <div className="pt-3 flex gap-3 justify-end">
            <button 
              type="button" 
              className="px-6 py-2.5 border-2 border-gray-200 rounded-xl text-gray-600 font-medium hover:bg-gray-50 hover:border-gray-300 transition-all text-base" 
              onClick={onClose}
            >
              취소
            </button>
            <button
              type="submit"
              className="px-7 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-xl font-semibold hover:shadow-lg hover:scale-105 disabled:opacity-50 disabled:hover:scale-100 transition-all text-base"
              disabled={isPending}
            >
              {isPending ? '⏳ 저장 중…' : '🎉 저장'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
