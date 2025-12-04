import { useEffect, useMemo, useState, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getMonthlyDiary, type MonthlyDiaryResponse } from '@/apis/diary.api';
import { CalendarIcon, PlusIcon } from '@/components/ui/Icons';

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

// [수정] 식사 키 타입을 명시적으로 정의하여 타입 에러 해결
type MealKey = 'hasBreakfast' | 'hasLunch' | 'hasDinner' | 'hasSnack';

const MEALS: Array<{
  key: MealKey; // keyof MonthlyDiaryResponse... 대신 MealKey 사용
  label: string;
  code: string;
  className: string;
  mealType: string;
}> = [
  { key: 'hasBreakfast', label: '아침', code: '아침', className: 'bg-orange-100 text-orange-700 border-orange-200', mealType: 'BREAKFAST' },
  { key: 'hasLunch', label: '점심', code: '점심', className: 'bg-green-100 text-green-700 border-green-200', mealType: 'LUNCH' },
  { key: 'hasDinner', label: '저녁', code: '저녁', className: 'bg-indigo-100 text-indigo-700 border-indigo-200', mealType: 'DINNER' },
  { key: 'hasSnack', label: '간식', code: '간식', className: 'bg-pink-100 text-pink-700 border-pink-200', mealType: 'SNACK' },
];

interface DayCellProps {
  date: Date;
  ymd: string;
  entry?: MonthlyDiaryResponse['dailyEntries'][number];
  isToday: boolean;
  pulseToday: boolean;
  onOpenDay: (_ymd: string) => void;
  index: number;
  focused: boolean;
  setFocusedIndex: (_i: number) => void;
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
  const hasEntry = !!entry && (entry.hasBreakfast || entry.hasLunch || entry.hasDinner || entry.hasSnack);

  const todayStyle = isToday
    ? `bg-[#F7F9F2] border-[#4E652F] shadow-sm ${pulseToday ? 'ring-2 ring-[#71853A] animate-pulse' : ''}`
    : 'bg-white hover:bg-gray-50 border-gray-100';

  const focusStyle = focused ? 'ring-2 ring-[#71853A] z-10' : '';

  return (
    <div
      role="gridcell"
      aria-selected={focused}
      tabIndex={focused ? 0 : -1}
      onFocus={() => setFocusedIndex(index)}
      onClick={navigateToDay}
      className={`
        relative border rounded-xl p-2 min-h-[140px] cursor-pointer flex flex-col gap-2 transition-all duration-200 outline-none
        ${todayStyle} ${focusStyle}
      `}
    >
      <div className="flex items-center justify-between">
        <span
          className={`
            text-sm font-bold w-7 h-7 flex items-center justify-center rounded-full
            ${isToday ? 'bg-[#4E652F] text-white' : 'text-gray-700'}
          `}
        >
          {date.getDate()}
        </span>
        {hasEntry && <div className="md:hidden w-1.5 h-1.5 rounded-full bg-[#4E652F]"></div>}
      </div>

      <div className="flex-1 flex flex-col gap-1.5">
        {entry?.thumbnailUrl ? (
          <div className="w-full h-20 rounded-lg overflow-hidden bg-gray-100 mb-1 border border-gray-100">
            <img
              src={entry.thumbnailUrl}
              alt="대표 식단"
              className="w-full h-full object-cover transform hover:scale-105 transition-transform duration-300"
              loading="lazy"
            />
          </div>
        ) : (
          <div className="flex-1"></div>
        )}

        <div className="hidden md:flex flex-wrap gap-1 content-end">
          {entry && MEALS.filter((m) => entry[m.key]).map((m) => (
            <span
              key={m.key}
              className={`text-[10px] px-1.5 py-0.5 rounded border ${m.className} font-medium`}
            >
              {m.code}
            </span>
          ))}
        </div>
      </div>

      <div className="absolute top-2 right-2 opacity-0 hover:opacity-100 transition-opacity">
        <PlusIcon className="w-4 h-4 text-gray-400" />
      </div>
    </div>
  );
};

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

  const onChangeYear = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setCursor((c) => ({ ...c, year: Number(e.target.value) }));
  };
  const onChangeMonth = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setCursor((c) => ({ ...c, month: Number(e.target.value) }));
  };

  const days = useMemo(() => {
    const { count, firstDay } = getDaysInMonth(cursor.year, cursor.month);
    const startWeekday = (firstDay.getDay() + 6) % 7;
    const list: Array<{ date: Date; ymd: string } | null> = [];
    for (let i = 0; i < startWeekday; i++) list.push(null);
    for (let d = 1; d <= count; d++) {
      const date = new Date(cursor.year, cursor.month - 1, d);
      const ymd = formatYmdLocal(date);
      list.push({ date, ymd });
    }
    return list;
  }, [cursor.year, cursor.month]);

  const summary = useMemo(() => {
    const stats: Record<MealKey | 'daysWithAny', number> = {
      hasBreakfast: 0,
      hasLunch: 0,
      hasDinner: 0,
      hasSnack: 0,
      daysWithAny: 0,
    };

    data?.dailyEntries.forEach((e) => {
      const any = e.hasBreakfast || e.hasLunch || e.hasDinner || e.hasSnack;
      if (any) stats.daysWithAny++;
      if (e.hasBreakfast) stats.hasBreakfast++;
      if (e.hasLunch) stats.hasLunch++;
      if (e.hasDinner) stats.hasDinner++;
      if (e.hasSnack) stats.hasSnack++;
    });
    return stats;
  }, [data]);

  const onKeyDown = useCallback((e: KeyboardEvent) => {
    if (e.key === 'ArrowLeft' && (e.metaKey || e.altKey)) handlePrev();
    else if (e.key === 'ArrowRight' && (e.metaKey || e.altKey)) handleNext();
    else if (e.key.toLowerCase() === 't') handleToday();
  }, []);

  useEffect(() => {
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onKeyDown]);

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

  useEffect(() => {
    const firstReal = days.findIndex((d) => d !== null);
    setFocusedIndex(firstReal);
  }, [days]);

  useEffect(() => {
    if (jumpToToday && cursor.year === today.getFullYear() && cursor.month === today.getMonth() + 1) {
      const todayYmd = formatYmdLocal(today);
      const idx = days.findIndex((d) => d && d.ymd === todayYmd);
      if (idx >= 0) {
        setFocusedIndex(idx);
        requestAnimationFrame(() => {
          const cell = gridRef.current?.querySelectorAll('[role="gridcell"]')[idx] as HTMLElement | undefined;
          cell?.focus();
        });
      }
      setJumpToToday(false);
    }
  }, [jumpToToday, cursor.year, cursor.month, days, today]);

  return (
    <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8" aria-labelledby="diary-calendar-heading">

      {/* 상단 컨트롤러 및 타이틀 */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div className="flex items-center gap-3">
          <div className="bg-[#F0F5E5] p-2 rounded-full text-[#4E652F]">
            <CalendarIcon className="w-6 h-6" />
          </div>
          <h1 id="diary-calendar-heading" className="text-2xl font-bold text-gray-900">
            식단 캘린더
          </h1>
        </div>

        <div className="flex flex-wrap items-center gap-2 bg-white p-1.5 rounded-xl shadow-sm border border-gray-200">
          <button
            className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-600 transition-colors"
            onClick={handlePrev}
            aria-label="이전 달"
          >
            &lt;
          </button>

          <div className="flex items-center gap-1 px-2">
            <select
              aria-label="연도 선택"
              value={cursor.year}
              onChange={onChangeYear}
              className="appearance-none bg-transparent font-bold text-gray-800 text-center cursor-pointer focus:outline-none hover:text-[#4E652F]"
            >
              {Array.from({ length: 5 }, (_, i) => today.getFullYear() - 2 + i).map((y) => (
                <option key={y} value={y}>{y}년</option>
              ))}
            </select>
            <span className="text-gray-300">|</span>
            <select
              aria-label="월 선택"
              value={cursor.month}
              onChange={onChangeMonth}
              className="appearance-none bg-transparent font-bold text-gray-800 text-center cursor-pointer focus:outline-none hover:text-[#4E652F]"
            >
              {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                <option key={m} value={m}>{m}월</option>
              ))}
            </select>
          </div>

          <button
            className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-600 transition-colors"
            onClick={handleNext}
            aria-label="다음 달"
          >
            &gt;
          </button>

          <div className="w-px h-6 bg-gray-200 mx-1"></div>

          <button
            className="px-3 py-1.5 text-sm font-medium text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
            onClick={handleToday}
          >
            오늘
          </button>
          <button
            className="px-3 py-1.5 text-sm font-medium text-[#4E652F] hover:bg-[#F0F5E5] rounded-lg transition-colors"
            onClick={() => refetch()}
            disabled={isFetching}
          >
            새로고침
          </button>
        </div>
      </div>

      {/* 요약 통계 배너 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 mb-6 flex flex-wrap justify-between items-center gap-4">
        <div className="flex items-center gap-4 text-sm text-gray-600">
          <span className="font-semibold text-gray-900">이번 달 기록</span>
          <span className="bg-gray-100 px-2 py-1 rounded text-xs">총 {summary.daysWithAny}일</span>
        </div>
        <div className="flex flex-wrap gap-2 text-xs">
          {MEALS.map((m) => (
            <div key={m.key} className={`px-2 py-1 rounded border ${m.className} flex items-center gap-1.5`}>
              <span>{m.code}:</span>
              {/* key가 MealKey로 타입 지정되어 안전하게 접근 가능 */}
              <span className="font-bold">{summary[m.key]}</span>
            </div>
          ))}
        </div>
      </div>

      {isLoading || isFetching ? (
        <div className="py-20 text-center text-gray-500">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-[#4E652F] mx-auto mb-2"></div>
          캘린더를 불러오는 중입니다...
        </div>
      ) : isError ? (
        <div className="py-20 text-center text-red-600 bg-red-50 rounded-xl">
          데이터를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
        </div>
      ) : (
        <>
          <div
            ref={gridRef}
            role="grid"
            aria-label={`${cursor.year}년 ${cursor.month}월 캘린더`}
            className="grid grid-cols-7 gap-3 sm:gap-4"
            onKeyDown={handleGridKey}
          >
            {['월', '화', '수', '목', '금', '토', '일'].map((w, i) => (
              <div key={w} role="columnheader" className={`text-center text-sm font-bold pb-2 ${i >= 5 ? 'text-red-400' : 'text-gray-500'}`}>
                {w}
              </div>
            ))}

            {days.map((cell, idx) => {
              if (!cell) return <div key={`empty-${idx}`} />;

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

          <div className="mt-6 flex justify-end text-xs text-gray-400">
            <span>Tip: 날짜를 클릭하여 식단을 기록하거나 수정할 수 있습니다.</span>
          </div>
        </>
      )}
    </div>
  );
}