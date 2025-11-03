import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createDiary, type CreateDiaryRequest, type MealType } from '@/apis/diary';

const MEAL_OPTIONS: { value: MealType; label: string }[] = [
  { value: 'BREAKFAST', label: '아침' },
  { value: 'LUNCH', label: '점심' },
  { value: 'DINNER', label: '저녁' },
  { value: 'SNACK', label: '간식' },
];

function DiaryCreatePage() {
  const { date } = useParams();
  const navigate = useNavigate();
  const qc = useQueryClient();

  const [form, setForm] = useState<CreateDiaryRequest>({
    date: date ?? '',
    mealType: 'BREAKFAST',
    content: '',
    imageUrl: '',
    recipeId: undefined,
  });
  const [errors, setErrors] = useState<{ mealType?: string; content?: string } | null>(null);

  useEffect(() => {
    if (date) setForm((f) => ({ ...f, date }));
  }, [date]);

  const { mutateAsync, isPending } = useMutation({
    mutationFn: createDiary,
    onSuccess: async () => {
      alert('식단이 기록되었습니다.');
      // invalidate day list and monthly calendar
      await Promise.all([
        qc.invalidateQueries({ queryKey: ['diary-day', form.date] }),
        qc.invalidateQueries({ queryKey: ['monthly-diary'] }), // key has year/month; broad invalidation
      ]);
      navigate(`/diary/${form.date}`); // return to day modal
    },
  });

  const onClose = () => navigate(`/diary/${form.date}`);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: name === 'recipeId' && value !== '' ? Number(value) : value }) as any);
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

  const onSubmit = async (e: React.FormEvent) => {
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
    } catch (err: any) {
      if (err?.response?.status === 409) {
        alert('이미 해당 시간에 식단이 기록되어 있습니다.');
      } else {
        alert('식단 기록 중 오류가 발생했습니다.');
      }
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />

      {/* Modal */}
      <div className="relative bg-white rounded-lg shadow-xl w-full max-w-xl mx-4 max-h-[90vh] overflow-hidden">
        <div className="flex items-center justify-between border-b px-5 py-3">
          <h2 className="text-xl font-semibold">식단 추가</h2>
          <button className="px-3 py-1 border rounded" onClick={onClose}>
            닫기
          </button>
        </div>

        <form onSubmit={onSubmit} className="p-5 space-y-4 overflow-y-auto">
          <div>
            <label className="block text-sm font-medium mb-1">날짜</label>
            <input
              type="date"
              name="date"
              value={form.date}
              onChange={handleChange}
              className="w-full border rounded px-3 py-2"
              required
            />
          </div>

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
            {errors?.mealType && <p className="text-sm text-red-600 mt-1">{errors.mealType}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">메뉴/내용</label>
            <textarea
              name="content"
              value={form.content}
              onChange={handleChange}
              maxLength={500}
              placeholder="예) 닭가슴살 샐러드와 현미밥"
              className="w-full border rounded px-3 py-2 h-28"
              required
            />
            {errors?.content && <p className="text-sm text-red-600 mt-1">{errors.content}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">이미지 URL (선택)</label>
            <input
              type="url"
              name="imageUrl"
              value={form.imageUrl ?? ''}
              onChange={handleChange}
              placeholder="https://..."
              className="w-full border rounded px-3 py-2"
            />
            <p className="text-xs text-gray-500 mt-1">현재 버전은 URL 입력을 지원합니다. 파일 업로드는 추후 확장.</p>
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">레시피 ID (선택)</label>
            <input
              type="number"
              name="recipeId"
              value={form.recipeId ?? ''}
              onChange={handleChange}
              className="w-full border rounded px-3 py-2"
              min={1}
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
              {isPending ? '저장 중…' : '저장'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default DiaryCreatePage;
export { DiaryCreatePage };
