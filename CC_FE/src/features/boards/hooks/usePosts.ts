// src/features/boards/hooks/usePosts.ts
import { useQuery } from '@tanstack/react-query';
import { publicClient } from '@/apis/client';
import type { Page } from '@/types/pagination';
import type { Post } from '@/types/post';

type ListQuery = {
  page?: number;
  size?: number;
  keyword?: string;
  sort?: string;
  boardId?: number;
  authorId?: number;
  searchType?: string;
};

type ListParams = {
  page: number;
  size: number;
  sortBy: string;
  boardId?: number;
  authorId?: number;
  searchType?: string;
  keyword?: string;
};

export function usePosts(q: ListQuery) {
  return useQuery<Page<Post>>({
    queryKey: ['posts', q],
    queryFn: async () => {
      const params: ListParams = {
        page: q.page ?? 1,
        size: q.size ?? 20,
        sortBy: q.sort ?? 'createdAt',
      };
      if (q.boardId) params.boardId = q.boardId;
      if (q.authorId) params.authorId = q.authorId;
      if (q.searchType && q.keyword) {
        params.searchType = q.searchType;
        params.keyword = q.keyword;
      }
      const { data } = await publicClient.get<Page<Post>>('/api/v1/posts', { params });
      return data;
    },
  });
}

export function usePost(id: number) {
  return useQuery<Post>({
    queryKey: ['post', id],
    queryFn: async () => {
      const { data } = await publicClient.get<Post>(`/api/v1/posts/${id}`);
      return data;
    },
    enabled: Number.isFinite(id),
  });
}
