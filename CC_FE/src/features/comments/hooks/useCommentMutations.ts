import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addComment, updateComment, deleteComment } from '@/apis/comments.api';
import type { CreateCommentDto, UpdateCommentDto } from '@/types/comment';

export function useAddComment(postId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (dto: CreateCommentDto) => addComment(postId, dto),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['comments', postId] }),
  });
}

export function useUpdateComment(postId: number, commentId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (dto: UpdateCommentDto) => updateComment(postId, commentId, dto),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['comments', postId] }),
  });
}

export function useDeleteComment(postId: number, commentId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => deleteComment(postId, commentId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['comments', postId] }),
  });
}
