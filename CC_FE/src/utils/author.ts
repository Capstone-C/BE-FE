// src/utils/author.ts
function get(obj: unknown, key: string): unknown {
  if (!obj || typeof obj !== 'object') return undefined;
  return (obj as Record<string, unknown>)[key];
}

export function extractAuthorRef(obj: unknown) {
  const inlineName =
    (get(obj, 'authorNickname') as string | undefined) ??
    (get(obj, 'authorName') as string | undefined) ??
    (get(obj, 'writerName') as string | undefined) ??
    (get(obj, 'memberName') as string | undefined) ??
    (get(obj, 'nickname') as string | undefined) ??
    (get(obj, 'username') as string | undefined) ??
    (get(get(obj, 'author'), 'nickname') as string | undefined) ??
    (get(get(obj, 'author'), 'name') as string | undefined) ??
    (get(get(obj, 'member'), 'nickname') as string | undefined) ??
    (get(get(obj, 'member'), 'name') as string | undefined) ??
    null;

  const rawId =
    (get(obj, 'authorId') as unknown) ??
    (get(obj, 'memberId') as unknown) ??
    (get(obj, 'userId') as unknown) ??
    (get(get(obj, 'author'), 'id') as unknown) ??
    (get(get(obj, 'member'), 'id') as unknown) ??
    null;

  let memberId: number | undefined;
  if (typeof rawId === 'string') {
    const n = Number(rawId);
    memberId = Number.isFinite(n) && n > 0 ? n : undefined;
  } else if (typeof rawId === 'number') {
    memberId = rawId > 0 ? rawId : undefined;
  }

  return { inlineName, memberId };
}

export function getDisplayName(memberId?: number, inlineName?: string | null) {
  const inlineOK = typeof inlineName === 'string' && inlineName.trim().length > 0;
  if (inlineOK) return inlineName!.trim();
  if (Number.isFinite(memberId) && (memberId as number) > 0) return `작성자 #${memberId}`;
  return '익명';
}
