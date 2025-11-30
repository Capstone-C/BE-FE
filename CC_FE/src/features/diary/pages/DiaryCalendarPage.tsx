import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getMonthlyDiary, type MonthlyDiaryResponse } from '@/apis/diary.api';

function getDaysInMonth(year: number, month: number) {
  const firstDay = new Date(year, month - 1, 1);
  const lastDay = new Date(year, month, 0);
  return { firstDay, lastDay, count: lastDay.getDate() };
}

function addMonths(year: number, month: number, delta: number) {
  const d = new Date(year, month - 1 + delta, 1);
  return { year: d.getFullYear(), month: d.getMonth() + 1 };
}

function formatYmdLocal(d: Date) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export default function DiaryCalendarPage() {
  const today = useMemo(() => new Date(), []);
  const [cursor, setCursor] = useState(() => ({ year: today.getFullYear(), month: today.getMonth() + 1 }));
  const navigate = useNavigate();

  const { data, isLoading, isError, refetch, isFetching } = useQuery<MonthlyDiaryResponse>({
    queryKey: ['monthly-diary', cursor.year, cursor.month],
    queryFn: () => getMonthlyDiary(cursor.year, cursor.month),
    staleTime: 1000 * 30,
  });

  useEffect(() => {
    void refetch();
  }, [cursor.year, cursor.month, refetch]);

  const entriesByDate = useMemo(() => {
    const map = new Map<string, MonthlyDiaryResponse['dailyEntries'][number]>();
    data?.dailyEntries.forEach((e) => map.set(e.date, e));
    return map;
  }, [data]);

  const handlePrev = () => setCursor((c) => addMonths(c.year, c.month, -1));
  const handleNext = () => setCursor((c) => addMonths(c.year, c.month, +1));
  const handleToday = () => setCursor({ year: today.getFullYear(), month: today.getMonth() + 1 });

  const { count, firstDay } = getDaysInMonth(cursor.year, cursor.month);

  // [수정] 미사용 변수 startWeekday 제거
  // const startWeekday = (firstDay.getDay() + 6) % 7;

  const days: Array<{ date: Date; ymd: string } | null> = [];
  // 달력 앞쪽 빈칸 (일요일=0)
  for (let i = 0; i < firstDay.getDay(); i++) days.push(null);

  for (let d = 1; d <= count; d++) {
    const date = new Date(cursor.year, cursor.month - 1, d);
    const ymd = formatYmdLocal(date);
    days.push({ date, ymd });
  }

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col sm:flex-row justify-between sm:items-center mb-6 gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-800">내 식단 다이어리 캘린더</h1>
          <p className="text-4xl font-extrabold text-[#4E652F] mt-1">{`${cursor.year}년 ${cursor.month}월`}</p>
        </div>
        <div className="flex items-center space-x-2">
          <button onClick={handlePrev} className="px-3 py-2 rounded-md bg-white border border-gray-300 hover:bg-gray-100 transition-colors" aria-label="이전 달">
            &lt;
          </button>
          <button onClick={handleToday} className="px-4 py-2 rounded-md bg-white border border-gray-300 text-sm font-semibold text-gray-700 hover:bg-gray-100 transition-colors">
            오늘
          </button>
          <button onClick={handleNext} className="px-3 py-2 rounded-md bg-white border border-gray-300 hover:bg-gray-100 transition-colors" aria-label="다음 달">
            &gt;
          </button>
        </div>
      </div>

      {isLoading || isFetching ? (
        <div className="p-6 text-center">불러오는 중…</div>
      ) : isError ? (
        <div className="p-6 text-center text-red-600">캘린더 데이터를 불러오지 못했습니다.</div>
      ) : (
        <div className="bg-white p-4 sm:p-6 rounded-lg shadow-lg">
          <div className="grid grid-cols-7 gap-0">
            {['일', '월', '화', '수', '목', '금', '토'].map((w, i) => (
              <div key={w} className={`p-2 text-sm text-center font-semibold border-b-2 ${i === 0 ? 'text-red-500' : i === 6 ? 'text-blue-500' : 'text-gray-600'}`}>
                {w}
              </div>
            ))}
            {days.map((cell, idx) => {
              if (!cell) return <div key={`blank-${idx}`} className="p-2 h-24 sm:h-32 border border-gray-100 bg-gray-50" />;

              const entry = entriesByDate.get(cell.ymd);
              const isToday = today.getDate() === cell.date.getDate() && today.getMonth() === cell.date.getMonth() && today.getFullYear() === cell.date.getFullYear();

              const onClickDate = () => navigate(`/diary/${cell.ymd}`);
              const onClickAdd = (e: React.MouseEvent) => {
                e.stopPropagation();
                navigate(`/diary/${cell.ymd}/new`);
              };

              return (
                <div
                  key={cell.ymd}
                  onClick={onClickDate}
                  className="p-1.5 h-24 sm:h-32 border border-gray-100 align-top relative group hover:bg-lime-50 transition-colors duration-200 cursor-pointer"
                >
                  <div className="flex justify-between items-start">
                    <span className={`text-sm font-medium ${isToday ? 'bg-[#4E652F] text-white rounded-full flex items-center justify-center w-6 h-6' : 'text-gray-700'}`}>
                      {cell.date.getDate()}
                    </span>
                    <button
                      onClick={onClickAdd}
                      className="opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-white rounded-full p-1 shadow-sm hover:bg-gray-100 text-gray-500 hover:text-[#4E652F]"
                      aria-label="기록 추가"
                    >
                      <span className="text-xs font-bold px-1">+</span>
                    </button>
                  </div>

                  {entry && (
                    <div className="mt-1 flex flex-col gap-1 overflow-hidden">
                      {entry.thumbnailUrl && (
                        <div className="w-full h-12 overflow-hidden rounded">
                          <img
                            src={entry.thumbnailUrl}
                            alt="대표 이미지"
                            className="w-full h-full object-cover"
                          />
                        </div>
                      )}
                      <div className="flex flex-wrap gap-1 text-xs">
                        {entry.hasBreakfast && <span title="아침">☀️</span>}
                        {entry.hasLunch && <span title="점심">🌇</span>}
                        {entry.hasDinner && <span title="저녁">🌙</span>}
                        {entry.hasSnack && <span title="간식">🍪</span>}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}