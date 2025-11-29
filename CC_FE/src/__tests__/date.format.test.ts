import { describe, it, expect } from 'vitest';
import { formatKST } from '@/utils/date';

// Helper to extract hour/minute from formatted string "YYYY.MM.DD. HH:MM:SS"
const parseHM = (formatted: string) => {
  const m = formatted.match(/(\d{2}):(\d{2}):(\d{2})$/);
  if (!m) throw new Error('Unexpected format: ' + formatted);
  return { h: Number(m[1]), min: Number(m[2]), s: Number(m[3]) };
};

describe('formatKST', () => {
  it('treats naive LocalDateTime (no Z) as UTC and converts to KST (+9h)', () => {
    // 2025-11-29T04:42:00 naive should become 13:42 in KST
    const naive = '2025-11-29T04:42:00';
    const out = formatKST(naive);
    const { h, min } = parseHM(out);
    expect(h).toBe(13);
    expect(min).toBe(42);
  });

  it('keeps explicit Z as UTC then converts to KST correctly', () => {
    const isoZ = '2025-11-29T00:00:00Z';
    const out = formatKST(isoZ);
    const { h } = parseHM(out);
    expect(h).toBe(9); // midnight UTC -> 09 KST
  });
});
