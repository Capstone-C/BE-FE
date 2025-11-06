import { authClient, publicClient } from '@/apis/client';
import type { Comment, CreateCommentDto, UpdateCommentDto } from '@/types/comment';

export async function getComments(postId: number) {
  const { data } = await publicClient.get<Comment[]>(`/api/v1/posts/${postId}/comments`);
  return data;
}

export async function addComment(postId: number, dto: CreateCommentDto) {
  const { data } = await authClient.post<Comment>(`/api/v1/posts/${postId}/comments`, dto);
  return data;
}

export async function updateComment(postId: number, commentId: number, dto: UpdateCommentDto) {
  const { data } = await authClient.put<Comment>(`/api/v1/posts/${postId}/comments/${commentId}`, dto);
  return data;
}

export async function deleteComment(postId: number, commentId: number) {
  await authClient.delete(`/api/v1/posts/${postId}/comments/${commentId}`);
}
