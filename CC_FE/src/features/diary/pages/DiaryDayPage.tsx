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
      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-3xl mx-4 max-h-[90vh] overflow-hidden">
        <div className="flex items-center justify-between border-b-2 border-gray-100 px-8 py-5 bg-gradient-to-r from-purple-50 to-indigo-50">
          <h2 className="text-3xl font-bold gradient-text">📅 {formatKoreanDate(date)}</h2>
          <div className="flex gap-3">
            <button 
              className="px-4 py-2.5 border-2 border-gray-200 rounded-xl hover:bg-white transition-all font-medium text-base" 
              onClick={() => refetch()} 
              disabled={isFetching}
            >
              {isFetching ? '⏳' : '🔄'} 새로고침
            </button>
            <button 
              className="px-5 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-xl hover:shadow-lg hover:scale-105 transition-all font-semibold text-base" 
              onClick={onAdd}
            >
              ➕ 식단 추가
            </button>
            <button 
              className="px-4 py-2.5 border-2 border-gray-200 rounded-xl hover:bg-gray-50 transition-all text-base" 
              onClick={onClose}
            >
              ✕
            </button>
          </div>
        </div>

        <div className="p-8 overflow-y-auto">
          {isLoading ? (
            <div className="py-12 text-center text-lg">불러오는 중…</div>
          ) : isError ? (
            <div className="py-12 text-center text-red-600 text-lg">해당 날짜의 식단을 불러오지 못했습니다.</div>
          ) : !data || data.length === 0 ? (
            <div className="py-12 text-center text-gray-600">
              <p className="mb-4 text-lg">기록된 식단이 없습니다. 첫 식단을 추가해보세요.</p>
              <button className="px-4 py-2 border rounded text-base" onClick={onAdd}>
                식단 추가
              </button>
            </div>
          ) : (
            <div className="flex flex-col gap-8">
              {MEAL_ORDER.map((mt) => (
                <div key={mt}>
                  <h3 className="text-xl font-bold mb-4">{MEAL_LABEL[mt]}</h3>
                  {grouped[mt].length === 0 ? (
                    <div className="text-base text-gray-500 italic">📭 기록 없음</div>
                  ) : (
                    <ul className="space-y-4">
                      {grouped[mt].map((entry) => (
                        <li key={entry.id} className="border-2 border-gray-100 rounded-xl p-5 flex gap-5 bg-gradient-to-r from-white to-gray-50 hover:shadow-md transition-all">
                          {entry.imageUrl ? (
                            <img
                              src={entry.imageUrl}
                              alt="식단 이미지"
                              className="w-28 h-28 object-cover rounded-xl shadow-sm"
                              onError={(e) => (e.currentTarget.style.display = 'none')}
                            />
                          ) : (
                            <div className="w-28 h-28 bg-gradient-to-br from-gray-100 to-gray-200 rounded-xl flex items-center justify-center text-gray-400 text-sm font-medium">
                              🍽️<br/>No Image
                            </div>
                          )}
                          <div className="flex-1">
                            <div className="font-semibold text-gray-800 text-base">{entry.content}</div>
                            {entry.recipeId ? (
                              <button
                                type="button"
                                className="text-sm text-purple-600 mt-2 hover:text-purple-700 font-medium flex items-center gap-1"
                                onClick={() => onRecipeClick(entry.recipeId!)}
                              >
                                📖 {entry.recipeTitle
                                  ? `레시피: ${entry.recipeTitle}`
                                  : `원본 레시피 보기 #${entry.recipeId}`}
                              </button>
                            ) : null}
                          </div>
                          <div className="flex flex-col gap-2">
                            <button 
                              className="px-4 py-2 border-2 border-purple-200 rounded-lg text-sm text-purple-600 hover:bg-purple-50 transition-all font-medium" 
                              onClick={() => onEdit(entry.id)}
                            >
                              ✏️ 수정
                            </button>
                            <button 
                              className="px-4 py-2 border-2 border-red-200 rounded-lg text-sm text-red-600 hover:bg-red-50 transition-all font-medium" 
                              onClick={() => onDelete(entry.id)}
                            >
                              🗑️ 삭제
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