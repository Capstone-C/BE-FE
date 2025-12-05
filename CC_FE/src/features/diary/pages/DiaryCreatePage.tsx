import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <div
        className="absolute inset-0"
        onClick={onClose}
        aria-label="모달 닫기"
      />

      {/* 모달 컨테이너: flex-col로 설정하여 헤더/바디/푸터 분리 */}
      <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-xl max-h-[90vh] flex flex-col overflow-hidden z-10">

        {/* 1. 헤더 (고정) */}
        <div className="flex items-center justify-between border-b px-6 py-4 bg-white shrink-0">
          <h2 className="text-xl font-bold text-gray-800">식단 추가</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors p-1 rounded-full hover:bg-gray-100"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* 2. 내용 (스크롤 영역) */}
        <div className="flex-1 overflow-y-auto p-6">
          <form id="create-diary-form" onSubmit={onSubmit} className="space-y-5">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1.5">날짜</label>
                <input
                  type="date"
                  name="date"
                  value={form.date}
                  onChange={handleChange}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2.5 focus:ring-2 focus:ring-[#4E652F] focus:border-transparent outline-none transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1.5">식사 타입</label>
                <select
                  name="mealType"
                  value={form.mealType}
                  onChange={handleChange}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2.5 focus:ring-2 focus:ring-[#4E652F] focus:border-transparent outline-none transition-all bg-white"
                  required
                >
                  {MEAL_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
                {errors?.mealType && <p className="text-xs text-red-500 mt-1 ml-1">{errors.mealType}</p>}
              </div>
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">메뉴/내용</label>
              <textarea
                name="content"
                value={form.content}
                onChange={handleChange}
                maxLength={500}
                placeholder="예) 닭가슴살 샐러드와 현미밥"
                className="w-full border border-gray-300 rounded-lg px-4 py-3 h-32 resize-none focus:ring-2 focus:ring-[#4E652F] focus:border-transparent outline-none transition-all placeholder:text-gray-400"
                required
              />
              {errors?.content && <p className="text-xs text-red-500 mt-1 ml-1">{errors.content}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">사진 <span className="text-gray-400 font-normal text-xs">(선택)</span></label>
              <div className="border border-dashed border-gray-300 rounded-lg p-4 bg-gray-50 hover:bg-gray-100 transition-colors">
                <ImageUploader
                  value={form.imageUrl}
                  onChange={handleImageChange}
                  placeholder="클릭하여 식단 사진 업로드"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">레시피 연결 <span className="text-gray-400 font-normal text-xs">(선택)</span></label>
              <input
                type="text"
                name="recipeUrl"
                value={recipeUrlInput}
                onChange={handleRecipeUrlChange}
                placeholder="레시피 URL 또는 ID 입력"
                className="w-full border border-gray-300 rounded-lg px-3 py-2.5 focus:ring-2 focus:ring-[#4E652F] focus:border-transparent outline-none transition-all placeholder:text-gray-400"
              />
              {form.recipeId && (
                <div className="mt-2 text-sm text-[#4E652F] bg-[#4E652F]/10 px-3 py-2 rounded-md flex items-center gap-2">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M12.586 4.586a2 2 0 112.828 2.828l-3 3a2 2 0 01-2.828 0 1 1 0 00-1.414 1.414 4 4 0 005.656 0l3-3a4 4 0 00-5.656-5.656l-1.5 1.5a1 1 0 101.414 1.414l1.5-1.5zm-5 5a2 2 0 012.828 0 1 1 0 101.414-1.414 4 4 0 00-5.656 0l-3 3a4 4 0 105.656 5.656l1.5-1.5a1 1 0 10-1.414-1.414l-1.5 1.5a2 2 0 11-2.828-2.828l3-3z" clipRule="evenodd" />
                  </svg>
                  <span>연결됨: <strong>{recipeTitle ? recipeTitle : `#${form.recipeId}`}</strong></span>
                </div>
              )}
            </div>
          </form>
        </div>

        {/* 3. 푸터 (버튼 고정) */}
        <div className="border-t px-6 py-4 bg-gray-50 shrink-0 flex justify-end gap-3">
          <button
            type="button"
            className="px-4 py-2.5 border border-gray-300 rounded-lg text-gray-700 bg-white hover:bg-gray-50 transition-colors font-medium text-sm"
            onClick={onClose}
          >
            취소
          </button>
          <button
            type="submit"
            form="create-diary-form" // 폼 ID와 연결
            className="px-6 py-2.5 border border-transparent rounded-lg bg-[#4E652F] text-white text-sm font-medium hover:bg-[#3d5024] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#4E652F] disabled:opacity-50 disabled:cursor-not-allowed shadow-sm transition-all"
            disabled={isPending}
          >
            {isPending ? '저장 중...' : '저장하기'}
          </button>
        </div>

      </div>
    </div>
  );
}