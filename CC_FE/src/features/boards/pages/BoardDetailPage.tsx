import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import DOMPurify from 'dompurify'; // HTML 정화용

import { usePost } from '../hooks/usePosts';
import { useToggleLikeMutation, useDeletePostMutation } from '../hooks/usePostMutations';
import { compareRecipeWithRefrigerator } from '../../../apis/boards.api';
import { getDeductPreview, postDeduct, getRefrigeratorItems } from '../../../apis/refrigerator.api';
import { toggleScrap as toggleScrapApi } from '../../../apis/scraps.api';
import { useBlockedMembers } from '../../members/hooks/useMemberBlocks';
import BoardSidebar from '../components/BoardSidebar';
import CommentList from '../../comments/components/CommentList';

import { useAuth } from '../../../hooks/useAuth';
import { useToast } from '../../../contexts/ToastContext';

import { formatYMDHMKorean } from '../../../utils/date';
import { extractAuthorRef, getDisplayName } from '../../../utils/author';
import { ThumbsUpIcon, EyeIcon, DiaryIcon, FridgeIcon, ReceiptIcon } from '../../../components/ui/Icons';
import type { DeductPreviewResponse } from '../../../types/refrigerator';

// 공유하기 아이콘 (인라인 SVG)
const ShareIcon = ({ className = 'w-5 h-5' }) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    className={className}
    fill="none"
    viewBox="0 0 24 24"
    stroke="currentColor"
    strokeWidth={2}
  >
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"
    />
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

  const { data: blockedList } = useBlockedMembers(!!user);
  const blockedIds = blockedList?.map((b) => b.blockedId) ?? [];

  const [liked, setLiked] = useState<boolean | null>(null);
  const [likeCount, setLikeCount] = useState<number | null>(null);

  // 재료 차감 미리보기 상태
  const [deductPreview, setDeductPreview] = useState<DeductPreviewResponse | null>(null);
  const [deductOpen, setDeductOpen] = useState(false);
  const [deductLoading, setDeductLoading] = useState(false);
  const [deductExecuting, setDeductExecuting] = useState(false);
  const [ignoreWarnings, setIgnoreWarnings] = useState(false);

  // 냉장고 재료 이름 셋 (매칭 확인용)
  const [fridgeNames, setFridgeNames] = useState<Set<string> | null>(null);
  const [fridgeLoading, setFridgeLoading] = useState(false);
  const [fridgeError, setFridgeError] = useState<string | null>(null);

  const isRecipePost = (data as any)?.isRecipe === true;

  // 냉장고 재료 로딩 (로그인 사용자에게만 필요)
  useEffect(() => {
    async function loadFridge() {
      if (!user || !isRecipePost) {
        setFridgeNames(null);
        return;
      }
      try {
        setFridgeLoading(true);
        setFridgeError(null);
        // NOTE: 'name'으로 정렬하여 DB 부하를 줄임
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
  }, [user, isRecipePost]);

  const isOwnedIngredient = (name?: string) => {
    if (!name) return false;
    if (!fridgeNames || fridgeNames.size === 0) return false;
    const key = name.trim().toLowerCase();
    // 간단한 퍼지 매칭 또는 정확한 매칭 (여기서는 정확한 매칭 기반으로 구현)
    return fridgeNames.has(key);
  };

  if (Number.isNaN(id)) return <div className="p-6">잘못된 글 ID입니다.</div>;
  if (isLoading) return <div className="p-6 text-center">불러오는 중…</div>;
  if (isError || !data)
    return <div className="p-8 text-center text-red-600">글을 찾을 수 없거나 오류가 발생했습니다.</div>;

  const { inlineName, memberId } = extractAuthorRef(data);
  const authorName = getDisplayName(memberId, inlineName);
  const isAuthor = user && user.id === memberId;
  const isAuthorBlocked = memberId != null && blockedIds.includes(memberId);
  const authorBlockedAndHidden = isAuthorBlocked && !isAuthor; // 본인은 볼 수 있음

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

  const onToggleScrap = async () => {
    if (!user) {
      show('로그인이 필요한 기능입니다.', { type: 'error' });
      nav('/login');
      return;
    }
    try {
      const res = await toggleScrapApi(id);
      show(res.scrapped ? '스크랩북에 추가했습니다.' : '스크랩북에서 삭제했습니다.', { type: 'success' });
    } catch {
      show('스크랩 처리 중 오류가 발생했습니다.', { type: 'error' });
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
      const missingNames = res.ingredients
        .filter((i) => i.status === 'MISSING')
        .map((i) => i.name)
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

  // 재료 차감 미리보기 모달 열기
  const onOpenDeductPreview = async () => {
    if (!user) {
      show('로그인이 필요한 기능입니다.', { type: 'error' });
      return nav('/login');
    }
    try {
      setDeductLoading(true);
      const res = await getDeductPreview(id);
      setDeductPreview(res);
      setDeductOpen(true);
      // 미리보기 결과에 따라 경고 무시 옵션 초기화
      setIgnoreWarnings(!res.canProceed);
    } catch (e) {
      show('차감 미리보기를 불러오지 못했습니다.', { type: 'error' });
    } finally {
      setDeductLoading(false);
    }
  };

  // 재료 차감 실행
  const handleExecuteDeduct = async () => {
    if (!user || !deductPreview) return;

    if (!ignoreWarnings && !deductPreview.canProceed) {
      alert('필수 재료가 부족합니다. 경고를 무시하고 진행하거나 취소해주세요.');
      return;
    }
    // NOTE: alert() 대신 confirm()을 사용하여 사용자에게 재확인
    if (!confirm('재료를 차감하시겠습니까? (냉장고 수량 -1, 되돌릴 수 없음)')) return;

    try {
      setDeductExecuting(true);
      const res = await postDeduct({ recipeId: id, ignoreWarnings });
      show(`${res.successCount}개의 재료가 냉장고에서 차감되었습니다.`, { type: 'success' });
      setDeductOpen(false);
    } catch (e: any) {
      const msg = e?.response?.data?.message ?? '재료 차감 중 오류가 발생했습니다.';
      show(msg, { type: 'error' });
    } finally {
      setDeductExecuting(false);
    }
  };

  const editLink = isRecipePost ? `/recipes/${id}/edit` : `/boards/${id}/edit`;

  const renderRecipeInfo = () => {
    if (!isRecipePost) return null;

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
            <span className="font-semibold text-gray-800">
              {data.cookTimeInMinutes ? `${data.cookTimeInMinutes}분` : '-'}
            </span>
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
            onClick={onOpenDeductPreview} // 미리보기 모달 열기
            disabled={deductLoading}
            className="flex items-center justify-center gap-2 px-3 py-2 bg-[#4E652F] border border-[#4E652F] rounded-lg text-sm font-medium text-white hover:bg-[#425528] shadow-sm transition-colors disabled:opacity-50"
          >
            <ReceiptIcon className="w-4 h-4 text-white" />
            {deductLoading ? '미리보기...' : '재료 차감'}
          </button>
        </div>

        {/* 스크랩 버튼 */}
        <div className="text-center pt-2">
          <button
            onClick={onToggleScrap}
            className="px-4 py-2 border rounded-full text-sm font-medium text-gray-700 hover:bg-gray-100 transition-colors"
          >
            스크랩
          </button>
        </div>

        {/* 재료 목록 */}
        {data.ingredients && data.ingredients.length > 0 && (
          <div className="mt-8">
            <h3 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
              <span className="w-1.5 h-6 bg-[#4E652F] mr-2 rounded-full"></span>
              재료 준비
              {fridgeLoading && <span className="ml-3 text-sm text-gray-500"> (냉장고 정보 로딩 중...)</span>}
              {fridgeError && <span className="ml-3 text-sm text-red-500"> (냉장고 정보 오류)</span>}
            </h3>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {data.ingredients.map((ing: any, idx: number) => {
                const isOwned = isOwnedIngredient(ing.name);
                return (
                  <div
                    key={ing.id ?? idx}
                    className={`flex items-center justify-between p-3 bg-white border rounded-lg transition-colors ${
                      isOwned ? 'border-green-400 shadow-sm' : 'border-gray-200'
                    }`}
                  >
                    <div className="flex items-center space-x-3">
                      <span
                        className={`w-3 h-3 rounded-full ${isOwned ? 'bg-green-600' : 'bg-red-500'}`}
                        title={isOwned ? '냉장고에 있음' : '부족함'}
                      ></span>
                      <span className={`font-medium ${isOwned ? 'text-green-700' : 'text-gray-800'}`}>{ing.name}</span>
                    </div>
                    <div className="text-sm">
                      <span className={`font-bold ${isOwned ? 'text-green-700' : 'text-[#4E652F]'}`}>
                        {ing.quantity ? `${ing.quantity}${ing.unit || ''}` : ''}
                      </span>
                      {ing.memo && <span className="text-gray-400 text-xs ml-1">({ing.memo})</span>}
                    </div>
                  </div>
                );
              })}
            </div>
            {user && (
              <p className="text-xs text-gray-500 mt-3">
                초록색 아이콘: 냉장고에 보유 중, 붉은색 아이콘: 부족 (이름 기반 매칭)
              </p>
            )}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col md:flex-row gap-8">
        {/* 사이드바 */}
        <BoardSidebar />

        {/* 메인 내용 */}
        <div className="flex-1 min-w-0">
          <div className="bg-white p-6 sm:p-8 rounded-lg shadow-lg mb-6">
            {/* 헤더 및 메타 정보 */}
            <div className="border-b border-gray-200 pb-4 mb-6">
              <div className="flex justify-between items-start gap-4">
                <h1 className="text-2xl lg:text-3xl font-bold text-gray-900 leading-tight">{data.title}</h1>
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
                {memberId ? (
                  <Link
                    to={`/members/${memberId}`}
                    className="font-medium text-gray-700 hover:text-[#4E652F] hover:underline transition-colors"
                  >
                    {authorName}
                  </Link>
                ) : (
                  <span className="font-medium text-gray-700">{authorName}</span>
                )}
                <span className="hidden sm:inline text-gray-300">|</span>
                <span>{formatYMDHMKorean(data.createdAt)}</span>
                <span className="hidden sm:inline text-gray-300">|</span>
                <span className="flex items-center gap-1">
                  <EyeIcon className="w-4 h-4" /> {data.viewCount}
                </span>
              </div>
            </div>

            {/* 레시피 정보 및 액션 버튼 */}
            {!authorBlockedAndHidden && renderRecipeInfo()}

            {/* 본문 */}
            {!authorBlockedAndHidden && (
              <div className="prose max-w-none text-gray-800 min-h-[200px]">
                <div dangerouslySetInnerHTML={{ __html: safeHtml }} />
              </div>
            )}

            {/* 작성자 차단 시 본문 숨김 */}
            {authorBlockedAndHidden && (
              <div className="prose max-w-none text-gray-800 min-h-[200px] p-6 bg-gray-100 rounded text-center text-gray-600 border border-gray-300">
                이 작성자는 차단되어 글 내용이 표시되지 않습니다.
              </div>
            )}

            {/* 좋아요 버튼 */}
            {!authorBlockedAndHidden && (
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
                  <ThumbsUpIcon className="w-8 h-8 mb-1" />
                  <span className="text-sm font-bold">{currentLikeCount}</span>
                </button>
              </div>
            )}
          </div>

          {/* 댓글 섹션 */}
          {!authorBlockedAndHidden && (
            <div className="bg-white p-6 sm:p-8 rounded-lg shadow-lg">
              <CommentList postId={id} />
            </div>
          )}
          {authorBlockedAndHidden && (
            <div className="mt-6 p-4 bg-gray-50 border rounded text-xs text-gray-500">
              차단된 작성자의 글이므로 댓글 섹션이 표시되지 않습니다.
            </div>
          )}
        </div>
      </div>

      {/* 재료 차감 미리보기 모달 (Deduct Preview Modal) */}
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
                  <div className="flex items-center gap-2 mt-2">
                    <input
                      type="checkbox"
                      id="ignore-warnings"
                      checked={ignoreWarnings}
                      onChange={(e) => setIgnoreWarnings(e.target.checked)}
                      className="w-4 h-4 text-amber-600 rounded focus:ring-amber-500 border-amber-300"
                    />
                    <label htmlFor="ignore-warnings" className="text-xs text-amber-700">
                      경고를 무시하고 강제 차감 실행
                    </label>
                  </div>
                </div>
              )}
              <div className="space-y-2">
                <table className="w-full text-xs border">
                  <thead>
                    <tr className="bg-gray-50">
                      <th className="border px-2 py-1 text-left">재료</th>
                      <th className="border px-2 py-1 text-left">필요량</th>
                      <th className="border px-2 py-1 text-left">현재 (수량)</th>
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

              <div className="flex justify-end gap-3 pt-4 border-t">
                <button
                  onClick={() => setDeductOpen(false)}
                  className="px-4 py-2 border rounded text-sm hover:bg-gray-100"
                >
                  취소
                </button>
                <button
                  onClick={handleExecuteDeduct}
                  disabled={deductExecuting || (!ignoreWarnings && !deductPreview.canProceed)}
                  className="px-4 py-2 bg-purple-600 text-white rounded text-sm hover:bg-purple-700 disabled:opacity-50"
                >
                  {deductExecuting ? '차감 중...' : '차감 실행'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
