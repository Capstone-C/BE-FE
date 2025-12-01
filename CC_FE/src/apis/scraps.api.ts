import { authClient } from '@/apis/client';
import type { Page } from '@/types/pagination';

export type ScrapItem = {
  scrapId: number;
  postId: number;
  title: string;
  authorName: string;
  thumbnailUrl: string | null;
  likeCount: number;
  viewCount: number;
  scrappedAt: string;
  postCreatedAt: string;
  isRecipe: boolean;
  dietType?: string | null;
  difficulty?: string | null;
  cookTimeInMinutes?: number | null;
};

export type MyScrapsQuery = {
  page?: number; // default 1
  size?: number; // default 12
  sortBy?: 'scrappedAt_desc' | 'scrappedAt_asc' | 'likes_desc';
  keyword?: string;
};

export async function getMyScraps(params: MyScrapsQuery = {}): Promise<Page<ScrapItem>> {
  const query = {
    page: params.page ?? 1,
    size: params.size ?? 12,
    sortBy: params.sortBy ?? 'scrappedAt_desc',
    keyword: params.keyword ?? undefined,
  };
  const { data } = await authClient.get<Page<ScrapItem>>('/api/v1/users/me/scraps', { params: query });
  return data;
}

export type ToggleScrapResult = { scrapped: boolean };

export async function toggleScrap(postId: number): Promise<ToggleScrapResult> {
  const { data } = await authClient.post<ToggleScrapResult>(`/api/v1/posts/${postId}/scrap`, {});
  return data;
}

