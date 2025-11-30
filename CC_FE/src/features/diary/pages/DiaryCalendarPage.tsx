import { useEffect, useMemo, useState, useCallback, useRef } from 'react';
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

// Meal badge meta
const MEALS: Array<{
  key: keyof MonthlyDiaryResponse['dailyEntries'][number];
  label: string;
  code: string;
  color: string;
  mealType: string;
}> = [
  { key: 'hasBreakfast', label: '아침', code: 'B', color: 'bg-gradient-to-r from-yellow-50 to-orange-50 text-orange-700 border border-orange-200', mealType: 'BREAKFAST' },
  { key: 'hasLunch', label: '점심', code: 'L', color: 'bg-gradient-to-r from-orange-50 to-red-50 text-red-700 border border-red-200', mealType: 'LUNCH' },
  { key: 'hasDinner', label: '저녁', code: 'D', color: 'bg-gradient-to-r from-purple-50 to-indigo-50 text-indigo-700 border border-indigo-200', mealType: 'DINNER' },
  { key: 'hasSnack', label: '간식', code: 'S', color: 'bg-gradient-to-r from-pink-50 to-purple-50 text-purple-700 border border-purple-200', mealType: 'SNACK' },
];

interface DayCellProps {
  date: Date;
  ymd: string;
  entry?: MonthlyDiaryResponse['dailyEntries'][number];
  isToday: boolean;
  pulseToday: boolean; // new prop to animate highlight when jumping to today
  onOpenDay: (_ymd: string) => void; // underscore to silence unused param lint in type
  index: number;
  focused: boolean;
  setFocusedIndex: (_i: number) => void; // underscore
}

const DayCell = ({
  date,
  ymd,
  entry,
  isToday,
  pulseToday,
  onOpenDay,
  index,
  focused,
  setFocusedIndex,
}: DayCellProps) => {
  const navigateToDay = () => onOpenDay(ymd);
  const any = !!entry && (entry.hasBreakfast || entry.hasLunch || entry.hasDinner || entry.hasSnack);

  return (
    <div
      role="gridcell"
      aria-selected={focused}
      tabIndex={focused ? 0 : -1}
      onFocus={() => setFocusedIndex(index)}
      onClick={navigateToDay}
      className={`border rounded-xl p-4 min-h-32 cursor-pointer flex flex-col gap-3 relative outline-none transition-all duration-200 ${
        any ? 'border-purple-300 bg-purple-50/30' : 'border-gray-200'
      } ${focused ? 'ring-2 ring-purple-400 shadow-lg' : ''} ${
        isToday ? `bg-gradient-to-br from-purple-50 to-indigo-50 border-purple-400 ${pulseToday ? 'ring-2 ring-purple-500 animate-pulse' : ''}` : 'hover:bg-gray-50 hover:shadow-md hover:-translate-y-0.5'
      }`}
      aria-label={`${date.getMonth() + 1}월 ${date.getDate()}일: ${any ? '기록 있음' : '기록 없음'} ${
        entry
          ? MEALS.filter((m) => entry[m.key])
              .map((m) => m.label)
              .join(', ')
          : ''
      }`}
    >
      <div className="flex items-center justify-between">
        <span className={`text-base font-semibold ${isToday ? 'text-blue-600' : ''}`}>{date.getDate()}</span>
      </div>

      {entry && (
        <div className="flex flex-wrap gap-1 items-center">
          {entry.thumbnailUrl ? (
            <img
              loading="lazy"
              src={entry.thumbnailUrl}
              alt="대표 이미지"
              className="w-10 h-10 object-cover rounded"
              onError={(e) => (e.currentTarget.style.display = 'none')}
            />
          ) : null}
          {MEALS.filter((m) => entry[m.key]).map((m) => (
            <span
              key={m.key}
              className={`inline-flex items-center justify-center text-xs font-semibold px-2 py-1 rounded ${m.color}`}
              title={m.label}
              aria-label={`${m.label} 기록됨`}
            >
              {m.code}
            </span>
          ))}
        </div>
      )}
    </div>
  );
};

// Main component
export default function DiaryCalendarPage() {
  const today = useMemo(() => new Date(), []);
  const [cursor, setCursor] = useState(() => ({ year: today.getFullYear(), month: today.getMonth() + 1 }));
  const navigate = useNavigate();
  const [focusedIndex, setFocusedIndex] = useState<number>(-1);
  const [jumpToToday, setJumpToToday] = useState(false);

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
  const handleToday = () => {
    setCursor({ year: today.getFullYear(), month: today.getMonth() + 1 });
    setJumpToToday(true);
  };

  // Month/year picker change handlers
  const onChangeYear = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const year = Number(e.target.value);
    setCursor((c) => ({ ...c, year }));
  };
  const onChangeMonth = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const month = Number(e.target.value);
    setCursor((c) => ({ ...c, month }));
  };

  // Days array memo
  const days = useMemo(() => {
    const { count, firstDay } = getDaysInMonth(cursor.year, cursor.month);
    const startWeekday = (firstDay.getDay() + 6) % 7; // Monday=0
    const list: Array<{ date: Date; ymd: string } | null> = [];
    for (let i = 0; i < startWeekday; i++) list.push(null);
    for (let d = 1; d <= count; d++) {
      const date = new Date(cursor.year, cursor.month - 1, d);
      const ymd = formatYmdLocal(date);
      list.push({ date, ymd });
    }
    return list;
  }, [cursor.year, cursor.month]);

  // Monthly summary
  const summary = useMemo(() => {
    let breakfast = 0,
      lunch = 0,
      dinner = 0,
      snack = 0,
      daysWithAny = 0;
    data?.dailyEntries.forEach((e) => {
      const any = e.hasBreakfast || e.hasLunch || e.hasDinner || e.hasSnack;
      if (any) daysWithAny++;
      if (e.hasBreakfast) breakfast++;
      if (e.hasLunch) lunch++;
      if (e.hasDinner) dinner++;
      if (e.hasSnack) snack++;
    });
    return { breakfast, lunch, dinner, snack, daysWithAny };
  }, [data]);

  // Keyboard shortcuts for month navigation
  const onKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === 'ArrowLeft' && (e.metaKey || e.altKey)) handlePrev();
      else if (e.key === 'ArrowRight' && (e.metaKey || e.altKey)) handleNext();
      else if (e.key.toLowerCase() === 't') handleToday();
    },
    [], // eslint-disable-line react-hooks/exhaustive-deps
  );
  useEffect(() => {
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onKeyDown]);

  // Keyboard navigation inside grid (arrow moves focus)
  const gridRef = useRef<HTMLDivElement | null>(null);
  const handleGridKey = (e: React.KeyboardEvent<HTMLDivElement>) => {
    if (focusedIndex === -1) return;
    const cols = 7;
    let next = focusedIndex;
    if (e.key === 'ArrowRight') next = focusedIndex + 1;
    else if (e.key === 'ArrowLeft') next = focusedIndex - 1;
    else if (e.key === 'ArrowDown') next = focusedIndex + cols;
    else if (e.key === 'ArrowUp') next = focusedIndex - cols;
    if (next !== focusedIndex && next >= 0 && next < days.length) {
      e.preventDefault();
      setFocusedIndex(next);
      const cell = gridRef.current?.querySelectorAll('[role="gridcell"]')[next] as HTMLElement | undefined;
      cell?.focus();
    }
  };

  const openDay = (ymd: string) => navigate(`/diary/${ymd}`);

  // Focus first real day when days change
  useEffect(() => {
    const firstReal = days.findIndex((d) => d !== null);
    setFocusedIndex(firstReal);
  }, [days]);

  // Jump to today's cell if needed
  useEffect(() => {
    if (jumpToToday && cursor.year === today.getFullYear() && cursor.month === today.getMonth() + 1) {
      // find today's index in days
      const todayYmd = formatYmdLocal(today);
      const idx = days.findIndex((d) => d && d.ymd === todayYmd);
      if (idx >= 0) {
        setFocusedIndex(idx);
        // focus DOM element after paint
        requestAnimationFrame(() => {
          const cell = gridRef.current?.querySelectorAll('[role="gridcell"]')[idx] as HTMLElement | undefined;
          cell?.focus();
        });
      }
      setJumpToToday(false);
    }
  }, [jumpToToday, cursor.year, cursor.month, days, today]);

  return (
    <div className="max-w-7xl mx-auto px-8 py-16" aria-labelledby="diary-calendar-heading">
      <div className="mb-12 text-center">
        <h1 id="diary-calendar-heading" className="text-6xl font-bold gradient-text mb-4">
          📖 내 식단 다이어리
        </h1>
        <p className="text-2xl text-gray-600">매일의 식사를 기록하고 관리하세요</p>
      </div>

      {/* Navigation */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-10 p-8 bg-white rounded-xl shadow-md">
        <div className="flex items-center gap-3">
          <button 
            className="px-5 py-2.5 bg-white border-2 border-purple-200 text-purple-600 rounded-lg hover:bg-purple-50 transition-colors font-medium text-base" 
            onClick={handlePrev} 
            aria-label="이전 달"
          >
            ◀
          </button>
          <select
            aria-label="연도 선택"
            value={cursor.year}
            onChange={onChangeYear}
            className="px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent text-base"
          >
            {Array.from({ length: 5 }, (_, i) => today.getFullYear() - 2 + i).map((y) => (
              <option key={y} value={y}>
                {y}년
              </option>
            ))}
          </select>
          <select
            aria-label="월 선택"
            value={cursor.month}
            onChange={onChangeMonth}
            className="px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent text-base"
          >
            {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
              <option key={m} value={m}>
                {m}월
              </option>
            ))}
          </select>
          <button 
            className="px-5 py-2.5 bg-white border-2 border-purple-200 text-purple-600 rounded-lg hover:bg-purple-50 transition-colors font-medium text-base" 
            onClick={handleNext} 
            aria-label="다음 달"
          >
            ▶
          </button>
          <button 
            className="px-5 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-lg hover:shadow-lg hover:scale-105 transition-all font-medium text-base" 
            onClick={handleToday} 
            title="오늘로 이동 (T 키)"
          >
            오늘
          </button>
        </div>
        <div className="flex gap-3">
          <button
            className="px-5 py-2.5 border-2 border-gray-200 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 font-medium text-base"
            onClick={() => refetch()}
            disabled={isFetching}
            aria-label="새로고침"
          >
            🔄 새로고침
          </button>
        </div>
      </div>

      {/* Legend + Summary */}
      <div className="flex flex-wrap items-center justify-between mb-6 gap-4">
        <div className="flex items-center gap-3 text-sm">
          {MEALS.map((m) => (
            <div key={m.key} className="flex items-center gap-1.5">
              <span className={`w-6 h-6 flex items-center justify-center rounded ${m.color}`}>{m.code}</span>
              <span className="text-gray-600">{m.label}</span>
            </div>
          ))}
        </div>
        <div className="text-base text-gray-700 flex flex-wrap gap-4 font-medium">
          <span>기록 있는 날짜: {summary.daysWithAny}일</span>
          <span>아침: {summary.breakfast}</span>
          <span>점심: {summary.lunch}</span>
          <span>저녁: {summary.dinner}</span>
          <span>간식: {summary.snack}</span>
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
          <div
            ref={gridRef}
            role="grid"
            aria-label={`${cursor.year}년 ${cursor.month}월 식단 캘린더`}
            className="grid grid-cols-7 gap-2"
            onKeyDown={handleGridKey}
          >
            {['월', '화', '수', '목', '금', '토', '일'].map((w) => (
              <div key={w} role="columnheader" className="text-center text-sm font-medium text-gray-500">
                {w}
              </div>
            ))}
            {days.map((cell, idx) => {
              if (!cell) return <div key={idx} />;
              const entry = entriesByDate.get(cell.ymd);
              const isToday = cell.ymd === formatYmdLocal(today);
              return (
                <DayCell
                  key={cell.ymd}
                  date={cell.date}
                  ymd={cell.ymd}
                  entry={entry}
                  isToday={isToday}
                  pulseToday={jumpToToday && isToday}
                  onOpenDay={openDay}
                  index={idx}
                  focused={focusedIndex === idx}
                  setFocusedIndex={setFocusedIndex}
                />
              );
            })}
          </div>
          <p className="mt-3 text-xs text-gray-500">단축키: Alt/⌘ + ←/→ 이전/다음 달, T 오늘</p>
        </>
      )}
    </div>
  );
}
