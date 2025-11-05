// src/utils/author.ts
export function extractAuthorRef(obj: any) {
  const inlineName =
    obj?.authorNickname ??          // 댓글
    obj?.authorName ??
    obj?.writerName ??
    obj?.memberName ??
    obj?.nickname ??
    obj?.username ??
    obj?.author?.nickname ??        // 중첩: author { nickname, name }
    obj?.author?.name ??
    obj?.member?.nickname ??
    obj?.member?.name ??
    null;

  const rawId =
    obj?.authorId ??                // 게시글/댓글에서 자주 보임
    obj?.memberId ??
    obj?.userId ??
    obj?.author?.id ??              // 중첩 케이스
    obj?.member?.id ??
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
