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

  return {
    title: (dto.title ?? '').trim(),
    content: dto.content ?? '',
    categoryId: Number.isFinite(categoryId as number) ? Number(categoryId) : 0,
    isRecipe,
    status,
  };
}

export async function listPosts(params: { page?: number; size?: number; keyword?: string; sort?: string }) {
  const { data } = await publicClient.get<Page<Post>>('/api/v1/posts', { params });
  return data;
}

export async function getPost(id: number) {
  const { data } = await publicClient.get<Post>(`/api/v1/posts/${id}`);
  return data;
}

export async function createPost(dto: UpsertPostDto) {
  const payload = toPayload(dto);
  const { data } = await authClient.post<Post>('/api/v1/posts', payload, {
    headers: { 'Content-Type': 'application/json' }, // 415 방지
  });
  return data;
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
