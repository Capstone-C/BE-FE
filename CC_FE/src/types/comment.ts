export interface Comment {
  id: number;
  postId: number;
  parentId?: number | null;  // null이면 루트 댓글
  content: string;
  memberId: number;

  authorName?: string | null;

  createdAt: string;
  updatedAt?: string | null;

  // 중첩 트리를 원하면 children 사용(서버가 납품하면 그대로, 아니면 FE에서 구성)
  children?: Comment[];
}

export interface CreateCommentDto {
  content: string;
  parentId?: number | null;
}

export interface UpdateCommentDto {
  content: string;
}
