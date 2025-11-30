import { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import DOMPurify from 'dompurify';

import { usePost } from '@/features/boards/hooks/usePosts';
import { useToggleLikeMutation, useDeletePostMutation } from '@/features/boards/hooks/usePostMutations';
import { compareRecipeWithRefrigerator } from '@/apis/boards.api';
import { postDeduct } from '@/apis/refrigerator.api';
import BoardSidebar from '@/features/boards/components/BoardSidebar';
import CommentList from '@/features/comments/components/CommentList';

import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/contexts/ToastContext';

import { formatYMDHMKorean } from '@/utils/date';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import {
  ThumbsUpIcon,
  EyeIcon,
  DiaryIcon,
  FridgeIcon,
  ReceiptIcon
} from '@/components/ui/Icons';

// 공유하기 아이콘 (인라인 SVG)
const ShareIcon = ({ className = "w-5 h-5" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
  </svg>
);

export default function BoardDetailPage() {
  const { postId } = useParams();
  const id = Number(postId);
  const nav = useNavigate();
  const { user } = useAuth();
  const { show } = useToast();

  const { data, isLoading, isError } = usePost(id);
  const likeMutation = useToggleLikeMutation();
  const deleteMutation = useDeletePostMutation();

  const [liked, setLiked] = useState<boolean | null>(null);
  const [likeCount, setLikeCount] = useState<number | null>(null);

  if (isLoading) return <div className="p-8 text-center">불러오는 중…</div>;
  if (isError || !data) return <div className="p-8 text-center text-red-600">글을 찾을 수 없습니다.</div>;

  const { inlineName, memberId } = extractAuthorRef(data);
  const authorName = getDisplayName(memberId, inlineName);
  const isAuthor = user && user.id === memberId;

  const currentLiked = liked ?? (data as any).likedByMe ?? false;
  const currentLikeCount = likeCount ?? data.likeCount ?? 0;
  const safeHtml = DOMPurify.sanitize(data.content ?? '');

  const onToggleLike = async () => {
    if (!user) {
      show('로그인이 필요한 기능입니다.', { type: 'error' });
      nav('/login');
      return;
    }
    try {
      const res = await likeMutation.mutateAsync(id);
      setLiked(res.liked);
      setLikeCount(res.likeCount);
    } catch {
      show('오류가 발생했습니다.', { type: 'error' });
    }
  };

  const onDelete = async () => {
    if (!confirm('정말로 이 글을 삭제하시겠습니까?')) return;
    try {
      await deleteMutation.mutateAsync(id);
      show('삭제되었습니다.', { type: 'success' });
      nav('/boards');
    } catch {
      show('삭제 권한이 없거나 오류가 발생했습니다.', { type: 'error' });
    }
  };

  // --- 레시피 액션 핸들러 ---

  const handleShare = async () => {
    try {
      await navigator.clipboard.writeText(window.location.href);
      show('링크가 클립보드에 복사되었습니다.', { type: 'success' });
    } catch (err) {
      show('링크 복사에 실패했습니다.', { type: 'error' });
    }
  };

  const handleAddToDiary = () => {
    if (!user) {
      show('로그인이 필요한 기능입니다.', { type: 'error' });
      return nav('/login');
    }
    // 오늘 날짜로 다이어리 생성 페이지 이동 (레시피 ID 전달)
    const today = new Date().toISOString().split('T')[0];
    nav(`/diary/${today}/new`, { state: { recipeId: id } });
  };

  const handleCompare = async () => {
    if (!user) {
      show('로그인이 필요한 기능입니다.', { type: 'error' });
      return nav('/login');
    }
    try {
      const res = await compareRecipeWithRefrigerator(id);
      // 간단한 알림으로 결과 표시 (추후 모달 등으로 고도화 가능)
      const missingNames = res.ingredients
        .filter(i => i.status === 'MISSING')
        .map(i => i.name)
        .join(', ');

      if (missingNames) {
        alert(`[재료 비교]\n부족한 재료: ${missingNames}\n\n(총 ${res.totalNeeded}개 중 ${res.ownedCount}개 보유)`);
      } else {
        alert(`[재료 비교]\n모든 재료를 보유하고 있습니다! 요리를 시작해보세요.`);
      }
    } catch {
      show('재료 비교 중 오류가 발생했습니다.', { type: 'error' });
    }
  };

  const handleDeduct = async () => {
    if (!user) {
      show('로그인이 필요한 기능입니다.', { type: 'error' });
      return nav('/login');
    }
    if (!confirm('이 레시피에 사용된 재료를 냉장고에서 차감하시겠습니까?')) return;

    try {
      // ignoreWarnings: true로 설정하여 강제 차감 시도
      await postDeduct({ recipeId: id, ignoreWarnings: true });
      show('냉장고에서 재료가 차감되었습니다.', { type: 'success' });
    } catch (e: any) {
      const msg = e?.response?.data?.message ?? '재료 차감 중 오류가 발생했습니다.';
      show(msg, { type: 'error' });
    }
  };

  const editLink = data.isRecipe ? `/recipes/${id}/edit` : `/boards/${id}/edit`;

  const renderRecipeInfo = () => {
    if (!data.isRecipe) return null;

    return (
      <div className="mb-8 bg-[#F7F9F2] rounded-xl p-6 border border-[#E4E9D9]">
        {/* 메타 정보 */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center mb-6">
          <div className="bg-white p-3 rounded-lg shadow-sm">
            <span className="block text-xs text-gray-500 mb-1">식단 타입</span>
            <span className="font-semibold text-[#4E652F]">{data.dietType || '-'}</span>
          </div>
          <div className="bg-white p-3 rounded-lg shadow-sm">
            <span className="block text-xs text-gray-500 mb-1">난이도</span>
            <span className="font-semibold text-gray-800">{data.difficulty || '-'}</span>
          </div>
          <div className="bg-white p-3 rounded-lg shadow-sm">
            <span className="block text-xs text-gray-500 mb-1">조리 시간</span>
            <span className="font-semibold text-gray-800">{data.cookTimeInMinutes ? `${data.cookTimeInMinutes}분` : '-'}</span>
          </div>
          <div className="bg-white p-3 rounded-lg shadow-sm">
            <span className="block text-xs text-gray-500 mb-1">분량</span>
            <span className="font-semibold text-gray-800">{data.servings ? `${data.servings}인분` : '-'}</span>
          </div>
        </div>

        {/* 액션 버튼 그룹 */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-8">
          <button
            onClick={handleShare}
            className="flex items-center justify-center gap-2 px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 shadow-sm transition-colors"
          >
            <ShareIcon className="w-4 h-4 text-gray-500" />
            공유하기
          </button>
          <button
            onClick={handleAddToDiary}
            className="flex items-center justify-center gap-2 px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 shadow-sm transition-colors"
          >
            <DiaryIcon className="w-4 h-4 text-gray-500" />
            식단 추가
          </button>
          <button
            onClick={handleCompare}
            className="flex items-center justify-center gap-2 px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 shadow-sm transition-colors"
          >
            <FridgeIcon className="w-4 h-4 text-gray-500" />
            재료 비교
          </button>
          <button
            onClick={handleDeduct}
            className="flex items-center justify-center gap-2 px-3 py-2 bg-[#4E652F] border border-[#4E652F] rounded-lg text-sm font-medium text-white hover:bg-[#425528] shadow-sm transition-colors"
          >
            <ReceiptIcon className="w-4 h-4 text-white" />
            재료 차감
          </button>
        </div>

        {/* 재료 목록 */}
        {data.ingredients && data.ingredients.length > 0 && (
          <div>
            <h3 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
              <span className="w-1.5 h-6 bg-[#4E652F] mr-2 rounded-full"></span>
              재료 준비
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {data.ingredients.map((ing: any, idx: number) => (
                <div key={idx} className="flex items-center justify-between p-3 bg-white border border-gray-200 rounded-lg hover:border-[#4E652F] transition-colors">
                  <div className="flex items-center space-x-3">
                    <input type="checkbox" className="w-4 h-4 text-[#4E652F] rounded focus:ring-[#4E652F] border-gray-300" />
                    <span className="font-medium text-gray-800">{ing.name}</span>
                  </div>
                  <div className="text-sm">
                    <span className="font-bold text-[#4E652F]">
                      {ing.quantity ? `${ing.quantity}${ing.unit || ''}` : ''}
                    </span>
                    {ing.memo && <span className="text-gray-400 text-xs ml-1">({ing.memo})</span>}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col md:flex-row gap-8">

        <BoardSidebar />

        <div className="flex-1 min-w-0">
          <div className="bg-white p-6 sm:p-8 rounded-lg shadow-lg mb-6">

            <div className="border-b border-gray-200 pb-4 mb-6">
              <div className="flex justify-between items-start gap-4">
                <h1 className="text-2xl lg:text-3xl font-bold text-gray-900 leading-tight">
                  {data.title}
                </h1>
                {isAuthor && (
                  <div className="flex items-center space-x-2 flex-shrink-0 text-sm">
                    <Link to={editLink} className="text-gray-500 hover:text-[#4E652F] transition-colors">
                      수정
                    </Link>
                    <span className="text-gray-300">|</span>
                    <button onClick={onDelete} className="text-gray-500 hover:text-red-600 transition-colors">
                      삭제
                    </button>
                  </div>
                )}
              </div>

              <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-gray-500 mt-3">
                <span className="font-medium text-gray-700">{authorName}</span>
                <span className="hidden sm:inline text-gray-300">|</span>
                <span>{formatYMDHMKorean(data.createdAt)}</span>
                <span className="hidden sm:inline text-gray-300">|</span>
                <span className="flex items-center gap-1">
                  <EyeIcon className="w-4 h-4"/> {data.viewCount}
                </span>
              </div>
            </div>

            {renderRecipeInfo()}

            <div className="prose max-w-none text-gray-800 min-h-[200px]">
              <div dangerouslySetInnerHTML={{ __html: safeHtml }} />
            </div>

            <div className="mt-12 pt-8 border-t border-gray-100 flex flex-col items-center">
              <button
                onClick={onToggleLike}
                className={`flex flex-col items-center justify-center w-20 h-20 rounded-full border-2 transition-all shadow-sm ${
                  currentLiked
                    ? 'border-[#4E652F] text-[#4E652F] bg-[#F0F5E5]'
                    : 'border-gray-200 text-gray-400 hover:border-[#4E652F] hover:text-[#4E652F]'
                }`}
                title="추천하기"
              >
                <ThumbsUpIcon className="w-8 h-8 mb-1"/>
                <span className="text-sm font-bold">{currentLikeCount}</span>
              </button>
            </div>
          </div>

          <div className="bg-white p-6 sm:p-8 rounded-lg shadow-lg">
            <CommentList postId={id} />
          </div>
        </div>
      </div>
    </div>
  );
}