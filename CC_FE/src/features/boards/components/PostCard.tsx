import { Link } from 'react-router-dom';
import type { Post } from '@/types/post';
import { Card } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import { formatYMDHMKorean } from '@/utils/date';
import { toggleScrap as toggleScrapApi } from '@/apis/scraps.api';

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

  const onToggleScrap = async (e: React.MouseEvent) => {
    e.preventDefault();
    try {
      const res = await toggleScrapApi(post.id);
      // No count in card; just notify via alert for now or integrate ToastContext if available
      alert(res.scrapped ? '스크랩북에 추가했습니다.' : '스크랩북에서 삭제했습니다.');
    } catch {
      alert('로그인이 필요한 기능입니다.');
    }
  };

  return (
    <Card className="overflow-hidden h-full flex flex-col hover:shadow-2xl hover:-translate-y-1 transition-all duration-300 group">
      {/* 썸네일 이미지 */}
      {post.thumbnailUrl && (
        <Link to={link} className="block w-full h-48 overflow-hidden bg-gradient-to-br from-gray-100 to-gray-50 relative">
          <img
            src={post.thumbnailUrl}
            alt={post.title}
            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
            loading="lazy"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
        </Link>
      )}

      <div className="flex-1 flex flex-col p-6">
        <div className="flex items-center gap-2 mb-4">
          {!boardId && post.categoryName && (
            <Badge variant="purple">{post.categoryName}</Badge>
          )}
        </div>

        <Link to={link} className="font-bold hover:text-purple-600 transition-colors block mb-4 text-xl leading-snug line-clamp-2 group-hover:text-purple-600">
          {post.title}
        </Link>

        <div className="mt-auto space-y-4">
          <div className="flex flex-wrap items-center gap-x-2 gap-y-1 text-base">
            <span className="font-medium text-gray-900">{authorNode}</span>
            <span className="text-gray-300">·</span>
            <span className="text-gray-500 text-sm">{formatYMDHMKorean(post.createdAt)}</span>
          </div>

          <div className="flex items-center justify-between pt-4 border-t border-gray-100">
            <div className="flex items-center gap-5 text-sm text-gray-500">
              <span className="flex items-center gap-1.5">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                {post.viewCount}
              </span>
              <span className="flex items-center gap-1.5">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
                {post.likeCount}
              </span>
              <span className="flex items-center gap-1.5">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
                {post.commentCount}
              </span>
            </div>
            <button 
              onClick={onToggleScrap} 
              className="text-sm px-4 py-2 border border-purple-200 text-purple-600 hover:bg-purple-50 rounded-lg transition-colors font-medium"
            >
              북마크
            </button>
          </div>
        </div>
      </div>
    </Card>
  );
}

export default PostCard;
