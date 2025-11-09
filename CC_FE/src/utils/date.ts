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
