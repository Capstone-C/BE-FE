import { useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getDiaryByDate, type DiaryEntryResponse, type MealType, deleteDiary } from '@/apis/diary.api';

const MEAL_ORDER: MealType[] = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'];
const MEAL_LABEL: Record<MealType, string> = {
  BREAKFAST: '아침',
  LUNCH: '점심',
  DINNER: '저녁',
  SNACK: '간식',
};

function formatKoreanDate(ymd: string | undefined) {
  if (!ymd) return '';
  const [y, m, d] = ymd.split('-').map((s) => Number(s));
  return `${y}년 ${m}월 ${d}일`;
}

function groupByMeal(entries: DiaryEntryResponse[]) {
  const groups: Record<MealType, DiaryEntryResponse[]> = {
    BREAKFAST: [],
    LUNCH: [],
    DINNER: [],
    SNACK: [],
  };
  for (const e of entries) groups[e.mealType].push(e);
  return groups;
}

export default function DiaryDayPage() {
  const { date } = useParams(); // YYYY-MM-DD
  const navigate = useNavigate();
  const qc = useQueryClient();

  const { data, isLoading, isError, refetch, isFetching } = useQuery<DiaryEntryResponse[]>({
    queryKey: ['diary-day', date],
    queryFn: () => getDiaryByDate(date!),
    enabled: !!date,
    staleTime: 1000 * 15,
  });

  const delMut = useMutation({
    mutationFn: (id: number) => deleteDiary(id),
    onSuccess: async () => {
      await Promise.all([
        qc.invalidateQueries({ queryKey: ['diary-day', date] }),
        qc.invalidateQueries({ queryKey: ['monthly-diary'] }),
      ]);
    },
  });

  const grouped = useMemo(() => groupByMeal(data ?? []), [data]);

  const onClose = () => navigate('/diary');
  const onAdd = () => navigate(`/diary/${date}/new`);

  const onEdit = (id: number) => navigate(`/diary/${date}/edit/${id}`);
  const onDelete = async (id: number) => {
    if (confirm(`정말로 이 기록(#${id})을 삭제하시겠습니까?`)) {
      try {
        await delMut.mutateAsync(id);
        alert('기록이 삭제되었습니다.');
      } catch {
        alert('삭제 중 오류가 발생했습니다.');
      }
    }
  };
  const onRecipeClick = (recipeId: number) => navigate(`/boards/${recipeId}`);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />

      {/* Modal */}
      <div className="relative bg-white rounded-lg shadow-xl w-full max-w-3xl mx-4 max-h-[90vh] overflow-hidden">
        <div className="flex items-center justify-between border-b px-5 py-3">
          <h2 className="text-xl font-semibold">{formatKoreanDate(date)}</h2>
          <div className="flex gap-2">
            <button className="px-3 py-1 border rounded" onClick={() => refetch()} disabled={isFetching}>
              새로고침
            </button>
            {/* [수정됨] 식단 추가 버튼 색상 변경 */}
            <button
              className="px-3 py-1 border rounded bg-[#4E652F] text-white hover:bg-[#425528]"
              onClick={onAdd}
            >
              식단 추가
            </button>
            <button className="px-3 py-1 border rounded" onClick={onClose}>
              닫기
            </button>
          </div>
        </div>

        <div className="p-5 overflow-y-auto">
          {isLoading ? (
            <div className="py-8 text-center">불러오는 중…</div>
          ) : isError ? (
            <div className="py-8 text-center text-red-600">해당 날짜의 식단을 불러오지 못했습니다.</div>
          ) : !data || data.length === 0 ? (
            <div className="py-8 text-center text-gray-600">
              <p className="mb-3">기록된 식단이 없습니다. 첫 식단을 추가해보세요.</p>
              {/* [수정됨] 식단 추가 버튼 색상 변경 */}
              <button
                className="px-3 py-1 border rounded bg-[#4E652F] text-white hover:bg-[#425528]"
                onClick={onAdd}
              >
                식단 추가
              </button>
            </div>
          ) : (
            <div className="flex flex-col gap-6">
              {MEAL_ORDER.map((mt) => (
                <div key={mt}>
                  <h3 className="text-lg font-bold mb-3">{MEAL_LABEL[mt]}</h3>
                  {grouped[mt].length === 0 ? (
                    <div className="text-sm text-gray-500">기록 없음</div>
                  ) : (
                    <ul className="space-y-3">
                      {grouped[mt].map((entry) => (
                        <li key={entry.id} className="border rounded p-3 flex gap-3">
                          {entry.imageUrl ? (
                            <img
                              src={entry.imageUrl}
                              alt="식단 이미지"
                              className="w-20 h-20 object-cover rounded"
                              onError={(e) => (e.currentTarget.style.display = 'none')}
                            />
                          ) : (
                            <div className="w-20 h-20 bg-gray-100 rounded flex items-center justify-center text-gray-400 text-xs">
                              No Image
                            </div>
                          )}
                          <div className="flex-1">
                            <div className="font-medium">{entry.content}</div>
                            {entry.recipeId ? (
                              <button
                                type="button"
                                className="text-xs text-blue-600 mt-1 underline"
                                onClick={() => onRecipeClick(entry.recipeId!)}
                              >
                                원본 레시피 보기 #{entry.recipeId}
                              </button>
                            ) : null}
                          </div>
                          <div className="flex flex-col gap-2">
                            <button className="px-2 py-1 border rounded text-sm" onClick={() => onEdit(entry.id)}>
                              수정
                            </button>
                            <button className="px-2 py-1 border rounded text-sm" onClick={() => onDelete(entry.id)}>
                              삭제
                            </button>
                          </div>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}