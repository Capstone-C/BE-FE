// src/types/post.ts
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

  /** 레시피 관련 확장 필드 */
  dietType?:
    | 'VEGAN'
    | 'VEGETARIAN'
    | 'KETO'
    | 'PALEO'
    | 'MEDITERRANEAN'
    | 'LOW_CARB'
    | 'HIGH_PROTEIN'
    | 'GENERAL';
  cookTimeInMinutes?: number | null;
  servings?: number | null;
  difficulty?: 'VERY_HIGH' | 'HIGH' | 'MEDIUM' | 'LOW' | 'VERY_LOW';
  /** 현재 로그인 사용자가 좋아요 했는지 (단건 조회 시 제공) */
  likedByMe?: boolean | null;
  /** 재료 목록 (레시피일 때만) */
  ingredients?: PostIngredient[];
};

export type PostIngredient = {
  id?: number; // 백엔드 Response에 포함될 수 있는 식별자
  name: string;
  quantity?: number | null;
  unit?: string | null;
  memo?: string | null;
  expirationDate?: string | null;
};
