import { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import DOMPurify from 'dompurify';

import { usePost } from '@/features/boards/hooks/usePosts';
import CommentList from '@/features/comments/components/CommentList';
import { formatYMDHMKorean } from '@/utils/date';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import DOMPurify from 'dompurify';
import { useState, useEffect } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/contexts/ToastContext';

import { formatYMDHMKorean } from '@/utils/date';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import {
  useBlockMemberMutation,
  useUnblockMemberMutation,
  useBlockedMembers,
} from '@/features/members/hooks/useMemberBlocks';
import { getDeductPreview, postDeduct, getRefrigeratorItems } from '@/apis/refrigerator.api';
import type { DeductPreviewResponse, DeductResponse } from '@/types/refrigerator';
import { toggleScrap as toggleScrapApi } from '@/apis/scraps.api';

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

  const [deductPreview, setDeductPreview] = useState<DeductPreviewResponse | null>(null);
  const [deductOpen, setDeductOpen] = useState(false);
  const [deductLoading, setDeductLoading] = useState(false);
  const [deductExecuting, setDeductExecuting] = useState(false);
  const [ignoreWarnings, setIgnoreWarnings] = useState(false);

  const [fridgeNames, setFridgeNames] = useState<Set<string> | null>(null);
  const [fridgeLoading, setFridgeLoading] = useState(false);
  const [fridgeError, setFridgeError] = useState<string | null>(null);

  useEffect(() => {
    // 로그인 사용자만 냉장고 재료를 가져와서 간단한 이름 매칭 셋 구성
    async function loadFridge() {
      if (!user) {
        setFridgeNames(null);
        return;
      }
      try {
        setFridgeLoading(true);
        setFridgeError(null);
        const res = await getRefrigeratorItems('name');
        const names = new Set<string>((res.items ?? []).map((i) => i.name.trim().toLowerCase()).filter(Boolean));
        setFridgeNames(names);
      } catch {
        setFridgeError('냉장고 정보를 불러오지 못했습니다.');
        setFridgeNames(null);
      } finally {
        setFridgeLoading(false);
      }
    }
    loadFridge();
  }, [user]);

  const isOwnedIngredient = (name?: string) => {
    if (!name) return false;
    if (!fridgeNames || fridgeNames.size === 0) return false;
    const key = name.trim().toLowerCase();
    return fridgeNames.has(key);
  };

  if (Number.isNaN(id)) return <div className="p-6">잘못된 글 ID입니다.</div>;
  if (isLoading) return <div className="p-6">불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">글을 찾을 수 없거나 오류가 발생했습니다.</div>;

  const isRecipePost = (data as any).isRecipe === true;

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

  const onToggleScrap = async () => {
    try {
      const res = await toggleScrapApi(id);
      show(res.scrapped ? '스크랩북에 추가했습니다.' : '스크랩북에서 삭제했습니다.', { type: 'success' });
    } catch {
      show('로그인이 필요한 기능입니다.', { type: 'error' });
      nav('/login');
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

      {/* 상호작용 버튼 영역 */}
      <div className="flex flex-wrap gap-3 items-center">
        <button
          onClick={onToggleLike}
          className={`border px-3 py-1 rounded ${currentLiked ? 'bg-blue-600 text-white' : ''}`}
        >
          {currentLiked ? '추천 취소' : '추천'} ({currentLikeCount})
        </button>
        {isRecipePost && (
          <>
            <button onClick={onToggleScrap} className="border px-3 py-1 rounded">
              스크랩
            </button>
            <button onClick={onShare} className="border px-3 py-1 rounded">
              공유하기
            </button>
            <button onClick={onAddToDiary} className="border px-3 py-1 rounded bg-green-600 text-white">
              내 식단 다이어리에 추가
            </button>
            <button
              onClick={onOpenDeductPreview}
              disabled={!user || deductLoading}
              className="border px-3 py-1 rounded bg-purple-600 text-white disabled:opacity-50"
            >
              {deductLoading ? '미리보기…' : '재료 차감'}
            </button>
          </>
        )}
        <Link to={editHref} state={editState} className="underline">
          수정
        </Link>
        <button onClick={onDelete}>삭제</button>
        {memberId && user && user.id !== memberId && (
          <button
            onClick={handleShare}
            className="flex items-center justify-center gap-2 px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 shadow-sm transition-colors"
          >
            <ShareIcon className="w-4 h-4 text-gray-500" />
            공유하기
          </button>
        )}
      </div>

      {/* 재료 섹션 */}
      {isRecipePost && data.ingredients && data.ingredients.length > 0 && (
        <section className="mt-6 space-y-2">
          <h2 className="text-xl font-semibold">재료 정보</h2>
          {/* fridgeError나 로딩 상태를 보조적으로 표시 */}
          {user && fridgeLoading && <p className="text-xs text-gray-500">냉장고 정보를 불러오는 중...</p>}
          {user && fridgeError && <p className="text-xs text-amber-700">{fridgeError}</p>}
          <ul className="list-disc list-inside space-y-1">
            {data.ingredients.map((ing) => {
              const owned = isOwnedIngredient(ing.name);
              return (
                <li key={ing.id ?? ing.name}>
                  <span className={owned ? 'text-green-700' : 'text-red-700'}>{ing.name}</span>
                  {ing.quantity != null && (
                    <span>
                      {' '}
                      - {ing.quantity}
                      {ing.unit ?? ''}
                    </span>
                  )}
                </li>
              );
            })}
          </ul>
          {user && fridgeNames && (
            <p className="text-xs text-gray-500">
              색상 안내: <span className="text-green-700 font-medium">초록색</span>은 냉장고에 있음,{' '}
              <span className="text-red-700 font-medium">붉은색</span>은 부족함을 의미합니다.
            </p>
          )}
        </section>
      )}

      {/* 조리 순서(본문) 섹션 */}
      {!authorBlockedAndHidden && (
        <section className="mt-6 space-y-2">
          {isRecipePost && <h2 className="text-xl font-semibold">조리 순서</h2>}
          <div className="prose max-w-none" dangerouslySetInnerHTML={{ __html: safeHtml }} />
        </section>
      )}
      {authorBlockedAndHidden && (
        <div className="mt-6 p-6 bg-gray-100 rounded text-sm text-gray-600 space-y-3">
          <p>이 작성자는 차단되어 본문이 숨겨졌습니다.</p>
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

      {/* 댓글 섹션 */}
      {!authorBlockedAndHidden && (
        <section className="mt-6">
          <CommentList postId={id} />
        </section>
      )}
      {authorBlockedAndHidden && (
        <div className="mt-6 p-4 bg-gray-50 border rounded text-xs text-gray-500">
          차단된 작성자의 글이므로 댓글도 숨겨졌습니다.
        </div>
      )}

      {/* 재료 차감 미리보기 모달 */}
      {deductOpen && deductPreview && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/40" onClick={() => setDeductOpen(false)} />
          <div className="relative bg-white rounded-lg shadow-xl w-full max-w-xl mx-4 max-h-[85vh] overflow-hidden flex flex-col">
            <div className="flex items-center justify-between border-b px-5 py-3">
              <h2 className="text-lg font-semibold">재료 차감 미리보기</h2>
              <button className="px-3 py-1 border rounded" onClick={() => setDeductOpen(false)}>
                닫기
              </button>
            </div>
            <div className="p-5 overflow-y-auto space-y-4 text-sm">
              <p>
                레시피: <span className="font-semibold">{deductPreview.recipeName}</span>
              </p>
              {!deductPreview.canProceed && (
                <div className="p-3 bg-amber-50 border border-amber-200 rounded">
                  <p className="text-amber-800">필수 재료가 부족하여 기본적으로 차감이 불가능합니다.</p>
                  <p className="text-amber-700 mt-1 text-xs">경고를 무시하고 진행하려면 아래 옵션을 선택하세요.</p>
                </div>
              )}
              <div className="space-y-2">
                <table className="w-full text-xs border">
                  <thead>
                    <tr className="bg-gray-50">
                      <th className="border px-2 py-1 text-left">재료</th>
                      <th className="border px-2 py-1 text-left">필요량</th>
                      <th className="border px-2 py-1 text-left">현재</th>
                      <th className="border px-2 py-1 text-left">상태</th>
                      <th className="border px-2 py-1 text-left">메시지</th>
                    </tr>
                  </thead>
                  <tbody>
                    {deductPreview.ingredients.map((ing) => (
                      <tr key={ing.name} className="hover:bg-gray-50">
                        <td className="border px-2 py-1">
                          {ing.name}
                          {ing.isRequired ? (
                            <span className="ml-1 text-red-600">*</span>
                          ) : (
                            <span className="ml-1 text-gray-400">(선택)</span>
                          )}
                        </td>
                        <td className="border px-2 py-1">{ing.requiredAmount ?? '—'}</td>
                        <td className="border px-2 py-1">{ing.currentAmount ?? '없음'}</td>
                        <td className="border px-2 py-1">
                          <span
                            className={
                              ing.status === 'OK'
                                ? 'text-green-600'
                                : ing.status === 'INSUFFICIENT'
                                  ? 'text-amber-600'
                                  : 'text-red-600'
                            }
                          >
                            {ing.status}
                          </span>
                        </td>
                        <td className="border px-2 py-1 text-gray-600">{ing.message ?? ''}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
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