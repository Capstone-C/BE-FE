import { Link } from 'react-router-dom';
import type { Post } from '@/types/post';
import { ThumbsUpIcon, EyeIcon, ChatIcon, CalendarIcon } from '@/components/ui/Icons';
import { formatYMDHMKorean } from '@/utils/date';
import { extractAuthorRef, getDisplayName } from '@/utils/author';

interface RecipeCardProps {
  recipe: Post;
}

export default function RecipeCard({ recipe }: RecipeCardProps) {
  const { inlineName, memberId } = extractAuthorRef(recipe);
  const authorName = getDisplayName(memberId, inlineName);
  const dateStr = formatYMDHMKorean(recipe.createdAt).split(' ')[0]; // 날짜만 표시

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl transition-shadow duration-300 cursor-pointer group border border-gray-100">
      <Link to={`/boards/${recipe.id}`}>
        {/* Thumbnail Image */}
        <div className="aspect-video bg-gray-200 overflow-hidden relative">
          {recipe.thumbnailUrl ? (
            <img
              src={recipe.thumbnailUrl}
              alt={recipe.title}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              loading="lazy"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-400 bg-gray-100">이미지 없음</div>
          )}
          {/* Overlay for stats on hover (Optional style choice from combined code, or keep static) */}
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

          {/* Stats on Image (Hover 시 표시되는 스타일로 변경 - combined_project_code 스타일 적용) */}
          <div className="absolute bottom-3 left-3 right-3 flex justify-between items-center text-white text-xs font-medium opacity-0 group-hover:opacity-100 transition-opacity duration-300">
            <div className="flex items-center space-x-1 bg-black/30 px-2 py-1 rounded-full backdrop-blur-sm">
              <ThumbsUpIcon className="w-3 h-3 text-red-400" />
              <span>{recipe.likeCount}</span>
            </div>
            <div className="flex items-center space-x-1 bg-black/30 px-2 py-1 rounded-full backdrop-blur-sm">
              <EyeIcon className="w-3 h-3" />
              <span>{recipe.viewCount > 999 ? (recipe.viewCount / 1000).toFixed(1) + 'k' : recipe.viewCount}</span>
            </div>
          </div>
        </div>

        <div className="p-4">
          {/* Recipe Name */}
          <h3 className="text-lg font-bold text-gray-800 truncate mb-3 group-hover:text-[#4E652F] transition-colors">
            {recipe.title}
          </h3>

          {/* Author Info */}
          <div className="flex items-center mb-4">
            <div className="w-8 h-8 rounded-full bg-gray-300 flex-shrink-0 overflow-hidden mr-2 flex items-center justify-center text-white text-xs font-bold bg-gradient-to-br from-[#4E652F] to-[#71853A]">
              {/* Simple avatar placeholder with gradient background */}
              {authorName.charAt(0)}
            </div>
            {memberId ? (
              <Link
                to={`/members/${memberId}`}
                onClick={(e) => e.stopPropagation()}
                className="text-sm font-medium text-gray-700 truncate hover:text-[#4E652F] hover:underline transition-colors"
              >
                {authorName}
              </Link>
            ) : (
              <span className="text-sm font-medium text-gray-700 truncate">{authorName}</span>
            )}
          </div>

          {/* Meta Stats (Always visible) */}
          <div className="flex justify-between items-center text-xs text-gray-500 border-t pt-3">
            <div className="flex items-center space-x-3">
              <div className="flex items-center space-x-1" title="작성일">
                <CalendarIcon className="w-3 h-3" />
                <span>{dateStr}</span>
              </div>
            </div>

            <div className="flex items-center space-x-3">
              {/* 기본 상태에서도 아이콘 색상 포인트 적용 */}
              <div className="flex items-center space-x-1" title="조회수">
                <EyeIcon className="w-3 h-3" />
                <span>{recipe.viewCount > 999 ? (recipe.viewCount / 1000).toFixed(1) + 'k' : recipe.viewCount}</span>
              </div>
              <div className="flex items-center space-x-1" title="추천수">
                <ThumbsUpIcon className="w-3 h-3 text-red-400" />
                <span>{recipe.likeCount}</span>
              </div>
              <div className="flex items-center space-x-1" title="댓글수">
                <ChatIcon className="w-3 h-3 text-blue-400" />
                <span>{recipe.commentCount}</span>
              </div>
            </div>
          </div>
        </div>
      </Link>
    </div>
  );
}
