// src/utils/date.ts
// ISO(UTC) 시간대를 KST(한국 시간)로 포맷

const toDate = (v: string | number | Date) => {
  if (typeof v === 'string') {
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?$/.test(v)) {
      return new Date(v + 'Z'); // ← UTC로 보정
    }
  }
  return new Date(v);
};

export const formatKST = (value: string | number | Date) =>
  new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
    timeZone: 'Asia/Seoul',
  }).format(toDate(value));

export const formatDateYMD = (value: string | number | Date) => {
  const d = toDate(value);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`; // ISO-like 유지
};

export const formatDateYMDKorean = (value: string | number | Date) => {
  const d = toDate(value);
  return `${d.getFullYear()}년 ${String(d.getMonth() + 1).padStart(2, '0')}월 ${String(d.getDate()).padStart(2, '0')}일`;
};

export const toYmd = (d: Date | null): string | undefined => {
  if (!d) return undefined;
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

export const formatDateYMDKoreanWithTime = (value: string | number | Date) => {
  // YYYY년 MM월 DD일 HH:MM 형식 (초까지 필요하면 formatKST 사용)
  const d = toDate(value);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  return `${y}년 ${m}월 ${day}일 ${hh}:${mm}`;
};

// 별칭: 화면에서 분 단위까지 표기할 때 권장
export const formatYMDHMKorean = formatDateYMDKoreanWithTime;
