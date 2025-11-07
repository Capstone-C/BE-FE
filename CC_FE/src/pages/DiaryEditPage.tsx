// src/pages/DiaryEditPage.tsx
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getDiaryByDate,
  type DiaryEntryResponse,
  type MealType,
  updateDiary,
  type UpdateDiaryRequest,
} from '@/apis/diary';

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

  // 간단히 현재 날짜의 항목들을 받아서 해당 id를 찾는다 (실무에서는 단건 조회 API 권장)
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

  useEffect(() => {
    if (current) {
      setForm({
        mealType: current.mealType,
        content: current.content,
        imageUrl: current.imageUrl ?? '',
        recipeId: current.recipeId ?? undefined,
      });
    }
  }, [current]);

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
      if (name === 'recipeId') {
        next.recipeId = value === '' ? undefined : Number(value);
      } else if (name === 'imageUrl') {
        next.imageUrl = value;
      } else if (name === 'content') {
        next.content = value;
      } else if (name === 'mealType') {
        next.mealType = value as MealType;
      }
      return next;
    });
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
            <label className="block text-sm font-medium mb-1">이미지 URL (선택)</label>
            <input
              type="url"
              name="imageUrl"
              value={form.imageUrl ?? ''}
              onChange={handleChange}
              className="w-full border rounded px-3 py-2"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">레시피 ID (선택)</label>
            <input
              type="number"
              name="recipeId"
              value={form.recipeId ?? ''}
              onChange={handleChange}
              className="w-full border rounded px-3 py-2"
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
              {isPending ? '수정 중…' : '수정 완료'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
