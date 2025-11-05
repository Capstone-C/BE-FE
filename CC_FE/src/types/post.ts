// src/types/post.ts
export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type Post = {
  id: number;
  title: string;
  content: string;

  categoryId: number;
  isRecipe: boolean;

  /** BE에 맞춰 작성자 식별자는 memberId 사용 */
  memberId: number;

  status: 'ARCHIVED' | 'DRAFT' | 'PUBLISHED';
  likeCount: number;
  commentCount: number;
  viewCount: number;

  /** 서버가 TRUE/FALSE 문자열을 주는 경우가 있어 임시로 union 유지 */
  file: 'FALSE' | 'TRUE';
  selected: 'FALSE' | 'TRUE';

  createdAt: string;
  updatedAt?: string | null;

  /** 선택적 파생 필드 */
  authorName?: string | null;
  categoryName?: string | null;
};

export type UpsertPostDto = {
  title: string;
  content: string;
  categoryId: number;            // 필수
  isRecipe: boolean;             // 필수
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
};
