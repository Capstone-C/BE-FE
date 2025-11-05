// src/features/boards/hooks/usePosts.ts
import { useQuery } from '@tanstack/react-query';
import { listPosts, getPost } from '@/apis/boards.api';
import type { Page, Post } from '@/types/post';

type ListQuery = { page?: number; size?: number; keyword?: string; sort?: string };

export function usePosts(q: ListQuery) {
  return useQuery<Page<Post>>({
    queryKey: ['posts', q],
    queryFn: () => listPosts(q),
  });
}

export function usePost(id: number) {
  return useQuery<Post>({
    queryKey: ['post', id],
    queryFn: () => getPost(id),
    enabled: Number.isFinite(id),
  });
}
