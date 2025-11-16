// src/features/boards/hooks/usePostMutations.ts
import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  createPost as apiCreatePost,
  updatePost as apiUpdatePost,
  deletePost as apiDeletePost,
  toggleLike as apiToggleLike,
  type UpsertPostDto,
} from '@/apis/boards.api';

export function useCreatePostMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (dto: UpsertPostDto) => apiCreatePost(dto),
    onSuccess: async () => {
      await Promise.all([qc.invalidateQueries({ queryKey: ['posts'] })]);
    },
  });
}

export function useUpdatePostMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, dto }: { id: number; dto: UpsertPostDto }) => apiUpdatePost(id, dto),
    onSuccess: async (_data, variables) => {
      await Promise.all([
        qc.invalidateQueries({ queryKey: ['posts'] }),
        qc.invalidateQueries({ queryKey: ['post', variables.id] }),
      ]);
    },
  });
}

export function useDeletePostMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDeletePost(id),
    onSuccess: async (_data, id) => {
      await Promise.all([
        qc.invalidateQueries({ queryKey: ['posts'] }),
        qc.invalidateQueries({ queryKey: ['post', id] }),
      ]);
    },
  });
}

export function useToggleLikeMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiToggleLike(id),
    onSuccess: async (_data, id) => {
      await Promise.all([
        qc.invalidateQueries({ queryKey: ['post', id] }),
        qc.invalidateQueries({ queryKey: ['posts'] }),
      ]);
    },
  });
}
