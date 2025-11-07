// src/features/members/hooks/useMemberName.ts
import { useQuery } from '@tanstack/react-query';
import { publicClient } from '@/apis/client';

type MemberLike = Record<string, unknown> & {
  profile?: Record<string, unknown>;
  user?: Record<string, unknown>;
  email?: unknown;
};

function pickDisplayName(m?: MemberLike) {
  if (!m) return undefined;
  const get = (o: unknown, k: string) => (o && typeof o === 'object' ? (o as Record<string, unknown>)[k] : undefined);
  const raw =
    (get(m, 'displayName') as unknown) ??
    (get(m, 'memberName') as unknown) ??
    (get(m, 'nickname') as unknown) ??
    (get(m, 'name') as unknown) ??
    (get(m, 'username') as unknown) ??
    (m.profile
      ? ((get(m.profile, 'displayName') as unknown) ??
        (get(m.profile, 'name') as unknown) ??
        (get(m.profile, 'nickname') as unknown))
      : undefined) ??
    (m.user ? ((get(m.user, 'name') as unknown) ?? (get(m.user, 'nickname') as unknown)) : undefined) ??
    (typeof m.email === 'string' ? m.email.split('@')[0] : undefined);
  return typeof raw === 'string' && raw.trim() ? raw.trim() : undefined;
}

async function fetchMemberFromCandidates(id: number): Promise<MemberLike | undefined> {
  const candidates = [
    `/api/v1/members/${id}`,
    `/api/v1/member/${id}`,
    `/api/v1/users/${id}`,
    `/api/v1/members/info/${id}`,
  ];
  for (const url of candidates) {
    try {
      const res = await publicClient.get(url);
      const data = (res.data?.data ?? res.data?.result ?? res.data) as MemberLike | undefined;
      if (data) return data;
    } catch {
      // ignore and try next
    }
  }
  return undefined;
}

/**
 * inlineName이 유효하면 그대로 사용.
 * 아니면 여러 엔드포인트를 순차 시도해 이름을 얻는다.
 * 전부 실패하면 '작성자 #<id>'로 폴백.
 */
export function useMemberName(memberId?: number, inlineName?: string | null) {
  const inlineOK = typeof inlineName === 'string' && inlineName.trim().length > 0;
  const hasId = Number.isFinite(memberId) && (memberId as number) > 0;

  const { data } = useQuery({
    queryKey: ['member', memberId],
    enabled: hasId && !inlineOK,
    queryFn: () => fetchMemberFromCandidates(memberId as number),
    staleTime: 5 * 60 * 1000,
    retry: 0,
  });

  const fetchedName = pickDisplayName(data);
  const name = (inlineOK ? inlineName!.trim() : fetchedName) ?? (hasId ? `작성자 #${memberId}` : undefined); // ✅ 보기 좋은 마지막 폴백

  return { name };
}
