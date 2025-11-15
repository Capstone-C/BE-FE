// src/apis/boards.api.ts
import { authClient, publicClient } from '@/apis/client';
import type { Page } from '@/types/pagination';
import type { Post } from '@/types/post';

// 서버 요구 스키마에 맞춰 정규화할 DTO
export type UpsertPostDto = {
  title: string;
  content: string;
  categoryId: number | string; // select 값이 string일 수 있음
  isRecipe: boolean | number | 'Y' | 'N';
  status?: 'DRAFT' | 'PUBLISHED' | string; // 스웨거 예시: 'DRAFT'
  // --- 레시피 확장 필드 (선택) ---
  dietType?: 'VEGAN' | 'VEGETARIAN' | 'KETO' | 'PALEO' | 'MEDITERRANEAN' | 'LOW_CARB' | 'HIGH_PROTEIN' | 'GENERAL';
  cookTimeInMinutes?: number | null;
  servings?: number | null;
  difficulty?: 'VERY_HIGH' | 'HIGH' | 'MEDIUM' | 'LOW' | 'VERY_LOW';
  ingredients?: Array<{
    name: string;
    quantity?: number | null;
    unit?: string | null;
    memo?: string | null;
    expirationDate?: string | null; // ISO 문자열
  }>;
};

// 서버에 보낼 실제 payload를 안전하게 변환
function toPayload(dto: UpsertPostDto) {
  // categoryId 숫자 강제
  const categoryId = typeof dto.categoryId === 'string' ? Number(dto.categoryId) : dto.categoryId;

  // isRecipe → boolean 강제(서버 스웨거가 boolean이므로)
  const isRecipeValue = dto.isRecipe;
  const isRecipe =
    typeof isRecipeValue === 'boolean'
      ? isRecipeValue
      : typeof isRecipeValue === 'number'
        ? isRecipeValue !== 0
        : String(isRecipeValue).toUpperCase() === 'Y';

  // status 기본값 보강
  const status = (dto.status ?? 'DRAFT') as string;

  const payload: Record<string, unknown> = {
    title: (dto.title ?? '').trim(),
    content: dto.content ?? '',
    categoryId: Number.isFinite(categoryId as number) ? Number(categoryId) : 0,
    isRecipe,
    status,
  };

  // 레시피 확장 필드 조건부 포함
  if (dto.dietType) payload.dietType = dto.dietType;
  if (dto.cookTimeInMinutes != null) payload.cookTimeInMinutes = Number(dto.cookTimeInMinutes);
  if (dto.servings != null) payload.servings = Number(dto.servings);
  if (dto.difficulty) payload.difficulty = dto.difficulty;
  if (dto.ingredients && dto.ingredients.length > 0) {
    payload.ingredients = dto.ingredients.map((ing) => ({
      name: ing.name,
      quantity: ing.quantity != null ? Number(ing.quantity) : undefined,
      unit: ing.unit ?? undefined,
      memo: ing.memo ?? undefined,
      expirationDate: ing.expirationDate ?? undefined,
    }));
  }

  return payload;
}

export async function listPosts(params: {
  page?: number;
  size?: number;
  keyword?: string;
  sort?: string;
  searchType?: 'TITLE' | 'CONTENT' | 'AUTHOR';
  categoryId?: number;
}) {
  // 백엔드 파라미터 명세: boardId => categoryId 매핑 필요 시 변환
  const { page, size, keyword, sort, searchType, categoryId } = params;
  const query: Record<string, unknown> = {};
  if (page) query.page = page;
  if (size) query.size = size;
  if (keyword) {
    query.keyword = keyword;
    if (searchType) query.searchType = searchType;
  }
  if (sort) query.sortBy = sort; // 서버는 sortBy 사용
  if (categoryId) query.boardId = categoryId; // 서버 파라미터명 boardId
  const { data } = await publicClient.get<Page<Post>>('/api/v1/posts', { params: query });
  return data;
}

// 레시피 전용 목록: 레시피 카테고리 id를 인자로 받아 호출 (isRecipe 필터 미구현이므로 category 기반)
export async function listRecipePosts(params: {
  page?: number;
  size?: number;
  keyword?: string;
  sort?: string;
  searchType?: 'TITLE' | 'CONTENT' | 'AUTHOR';
  recipeCategoryId: number;
}) {
  return listPosts({ ...params, categoryId: params.recipeCategoryId });
}

export async function getPost(id: number) {
  const { data } = await publicClient.get<Post>(`/api/v1/posts/${id}`);
  return data;
}

export async function createPost(dto: UpsertPostDto) {
  const payload = toPayload(dto);
  const res = await authClient.post('/api/v1/posts', payload, {
    headers: { 'Content-Type': 'application/json' }, // 415 방지
    validateStatus: (s) => s >= 200 && s < 400, // 201 Created 포함
  });
  // Location 헤더에서 ID 추출 시도: /api/posts/{id} 혹은 /api/v1/posts/{id}
  const loc = res.headers?.location as string | undefined;
  if (loc) {
    const m = loc.match(/\/(?:api|api\/v1)\/posts\/(\d+)/);
    if (m && m[1]) return Number(m[1]);
    const tail = loc.split('/').pop();
    if (tail && !Number.isNaN(Number(tail))) return Number(tail);
  }
  return undefined; // 기존 사용처 호환
}

export async function updatePost(id: number, dto: UpsertPostDto) {
  const payload = toPayload(dto);
  const { data } = await authClient.put<Post>(`/api/v1/posts/${id}`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  return data;
}

export async function deletePost(id: number) {
  await authClient.delete(`/api/v1/posts/${id}`);
}

export type BoardSummary = {
  id: number;
  name: string;
  description?: string | null;
  imageUrl?: string | null;
  totalPosts: number;
  todayPosts: number;
  latestTitle?: string | null;
};

export async function listBoards() {
  const { data } = await publicClient.get<BoardSummary[]>('/api/boards');
  return data;
}

export type ToggleLikeResult = { liked: boolean; likeCount: number };

export async function toggleLike(postId: number) {
  const { data } = await authClient.post<ToggleLikeResult>(`/api/v1/posts/${postId}/like`, {});
  return data;
}

export type ComparedIngredient = {
  name: string;
  amount: string;
  status: 'OWNED' | 'MISSING';
};

export type RecipeRefrigeratorComparison = {
  postId: number;
  postTitle: string;
  ingredients: ComparedIngredient[];
  totalNeeded: number;
  ownedCount: number;
  missingCount: number;
};

export async function compareRecipeWithRefrigerator(postId: number) {
  const { data } = await authClient.get<RecipeRefrigeratorComparison>(`/api/v1/posts/${postId}/compare-refrigerator`);
  return data;
}
