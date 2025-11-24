import { Link } from 'react-router-dom';
import type { Post } from '@/types/post';
import { Card, CardContent, CardHeader } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import { formatDateYMDKorean } from '@/utils/date';

export function PostCard({ post, boardId }: { post: Post; boardId?: string | null }) {
  const { inlineName, memberId } = extractAuthorRef(post);
  const name = getDisplayName(memberId, inlineName);
  const link = boardId ? `/boards/${post.id}?categoryId=${boardId}` : `/boards/${post.id}`;

  const authorNode = memberId ? (
    <Link to={`/members/${memberId}`} className="hover:underline">
      {name}
    </Link>
  ) : (
    <span>{name}</span>
  );

  return (
    <Card className="overflow-hidden h-full flex flex-col hover:shadow-md transition-shadow">
      {/* [수정됨] 썸네일 이미지가 있으면 렌더링 */}
      {post.thumbnailUrl && (
        <Link to={link} className="block w-full h-48 overflow-hidden bg-gray-100 relative group">
          <img
            src={post.thumbnailUrl}
            alt={post.title}
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
            loading="lazy"
          />
        </Link>
      )}

      <div className="flex-1 flex flex-col">
        <CardHeader className="flex items-center gap-2 pt-4">
          {!boardId && post.categoryName && <Badge>{post.categoryName}</Badge>}
          <Link to={link} className="font-semibold hover:underline truncate block flex-1 text-lg">
            {post.title}
          </Link>
        </CardHeader>
        <CardContent className="text-sm text-gray-600 pb-4">
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
            <span className="font-medium text-gray-900">{authorNode}</span>
            <span className="text-gray-300">|</span>
            <span>{formatDateYMDKorean(post.createdAt)}</span>
          </div>

          <div className="mt-3 flex items-center gap-3 text-xs text-gray-500">
            <span className="flex items-center gap-1">
              👁️ {post.viewCount}
            </span>
            <span className="flex items-center gap-1">
              👍 {post.likeCount}
            </span>
            <span className="flex items-center gap-1">
              💬 {post.commentCount}
            </span>
          </div>
        </CardContent>
      </div>
    </Card>
  );
}

export default PostCard;