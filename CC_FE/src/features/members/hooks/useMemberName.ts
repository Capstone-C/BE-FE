// src/features/members/hooks/useMemberName.ts
import { useQuery } from '@tanstack/react-query';
import { authClient } from '@/apis/client';

type AnyMember = any;

function pickDisplayName(m?: AnyMember) {
  if (!m) return undefined;
  const c =
    m.displayName ?? m.memberName ?? m.nickname ?? m.name ?? m.username ??
    m.profile?.displayName ?? m.profile?.name ?? m.profile?.nickname ??
    m.user?.name ?? m.user?.nickname ??
    (m.email ? String(m.email).split('@')[0] : undefined);
  return (typeof c === 'string' && c.trim()) ? c.trim() : undefined;
}

async function fetchMemberFromCandidates(id: number): Promise<AnyMember | undefined> {
  const candidates = [
    `/api/v1/members/${id}`,
    `/api/v1/member/${id}`,
    `/api/v1/users/${id}`,
    `/api/v1/members/info/${id}`,
  ];
  for (const url of candidates) {
    try {
      const res = await authClient.get(url);
      const data = res.data?.data ?? res.data?.result ?? res.data;
      if (data) return data;
    } catch (_e) {
      // 다음 후보 시도
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
  const name =
    (inlineOK ? inlineName!.trim() : fetchedName) ??
    (hasId ? `작성자 #${memberId}` : undefined); // ✅ 보기 좋은 마지막 폴백

  return { name };
}
