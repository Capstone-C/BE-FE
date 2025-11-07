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
  const startWeekday = (firstDay.getDay() + 6) % 7;
  const days: Array<{ date: Date; ymd: string } | null> = [];
  for (let i = 0; i < startWeekday; i++) days.push(null);
  for (let d = 1; d <= count; d++) {
    const date = new Date(cursor.year, cursor.month - 1, d);
    const ymd = formatYmdLocal(date);
    days.push({ date, ymd });
  }

  return (
    <div className="max-w-5xl mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">내 식단 다이어리 캘린더</h1>

      <div className="flex items-center justify-between mb-4">
        <div className="text-lg font-semibold">
          {cursor.year}년 {cursor.month}월
        </div>
        <div className="flex gap-2">
          <button className="px-3 py-1 border rounded" onClick={handlePrev} aria-label="이전 달">
            {'<'}
          </button>
          <button className="px-3 py-1 border rounded" onClick={handleToday}>
            오늘
          </button>
          <button className="px-3 py-1 border rounded" onClick={handleNext} aria-label="다음 달">
            {'>'}
          </button>
        </div>
      </div>

      {isLoading || isFetching ? (
        <div className="p-6 text-center">불러오는 중…</div>
      ) : isError ? (
        <div className="p-6 text-center text-red-600">캘린더 데이터를 불러오지 못했습니다.</div>
      ) : (
        <>
          {data && data.dailyEntries.length === 0 && (
            <div className="mb-2 text-sm text-gray-600">이번 달 첫 식단을 기록해보세요!</div>
          )}
          <div className="grid grid-cols-7 gap-2">
            {['월', '화', '수', '목', '금', '토', '일'].map((w) => (
              <div key={w} className="text-center text-sm font-medium text-gray-500">
                {w}
              </div>
            ))}
            {days.map((cell, idx) => {
              if (!cell) return <div key={idx} className="p-2" />;
              const entry = entriesByDate.get(cell.ymd);
              const hasAny = entry && (entry.hasBreakfast || entry.hasLunch || entry.hasDinner);

              const onClickDate = () => navigate(`/diary/${cell.ymd}`);
              const onClickAdd = (e: React.MouseEvent) => {
                e.stopPropagation();
                navigate(`/diary/${cell.ymd}/new`);
              };

              return (
                <div
                  key={cell.ymd}
                  onClick={onClickDate}
                  className={`border rounded p-2 min-h-24 cursor-pointer hover:bg-gray-50 flex flex-col gap-2 ${hasAny ? 'border-blue-300' : ''}`}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-semibold">{cell.date.getDate()}</span>
                    {!hasAny && (
                      <button
                        onClick={onClickAdd}
                        className="text-xs px-1 py-0.5 border rounded hover:bg-gray-100"
                        aria-label="기록 추가"
                      >
                        +
                      </button>
                    )}
                  </div>

                  {entry && (
                    <div className="flex items-center gap-2">
                      {entry.thumbnailUrl ? (
                        <img
                          src={entry.thumbnailUrl}
                          alt="대표 이미지"
                          className="w-10 h-10 object-cover rounded"
                          onError={(e) => (e.currentTarget.style.display = 'none')}
                        />
                      ) : null}
                      <div className="text-lg" aria-label="식사 아이콘">
                        {entry.hasBreakfast && <span title="아침">☀️</span>}
                        {entry.hasLunch && (
                          <span className="ml-1" title="점심">
                            🌇
                          </span>
                        )}
                        {entry.hasDinner && (
                          <span className="ml-1" title="저녁">
                            🌙
                          </span>
                        )}
                        {entry.hasSnack && (
                          <span className="ml-1" title="간식">
                            🍪
                          </span>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
}
