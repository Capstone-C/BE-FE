import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createPost, updatePost, deletePost } from '@/apis/boards.api';
import type { UpsertPostDto } from '@/apis/boards.api';

export function useCreatePost() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (dto: UpsertPostDto) => createPost(dto),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['posts'] }),
  });
}

export function useUpdatePost(id: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (dto: UpsertPostDto) => updatePost(id, dto),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['posts'] });
      qc.invalidateQueries({ queryKey: ['post', id] });
    },
  });
}

export function useDeletePost(id: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => deletePost(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['posts'] }),
  });
}
