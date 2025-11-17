import { useQuery } from '@tanstack/react-query';
import { getCommentsByAuthor } from '@/apis/comments.api';

export function useAuthorComments(authorId: number | undefined) {
  return useQuery({
    queryKey: ['comments', 'author', authorId],
    queryFn: () => getCommentsByAuthor(authorId!),
    enabled: typeof authorId === 'number' && Number.isFinite(authorId),
  });
}
