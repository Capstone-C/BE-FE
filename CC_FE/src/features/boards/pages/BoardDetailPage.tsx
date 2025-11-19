// src/features/boards/pages/BoardDetailPage.tsx
import { useParams, Link, useNavigate, useSearchParams } from 'react-router-dom';
import { usePost } from '@/features/boards/hooks/usePosts';
import { compareRecipeWithRefrigerator, type RecipeRefrigeratorComparison } from '@/apis/boards.api';
import CommentList from '@/features/comments/components/CommentList';
import { formatDateYMDKorean } from '@/utils/date';
import { extractAuthorRef, getDisplayName } from '@/utils/author';
import DOMPurify from 'dompurify';
import { useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/contexts/ToastContext';
import { useToggleLikeMutation, useDeletePostMutation } from '@/features/boards/hooks/usePostMutations';
import {
  useBlockMemberMutation,
  useUnblockMemberMutation,
  useBlockedMembers,
} from '@/features/members/hooks/useMemberBlocks';
import { getDeductPreview, postDeduct } from '@/apis/refrigerator.api';
import type { DeductPreviewResponse, DeductResponse } from '@/types/refrigerator';

export default function BoardDetailPage() {
  const { postId } = useParams();
  const id = Number(postId);
  const nav = useNavigate();
  const [sp] = useSearchParams();

  const { data, isLoading, isError } = usePost(id);
  const { user } = useAuth();
  const { show } = useToast();
  const likeMutation = useToggleLikeMutation();
  const deleteMutation = useDeletePostMutation();
  const blockMutation = useBlockMemberMutation();
  const unblockMutation = useUnblockMemberMutation();
  const { data: blockedMembers } = useBlockedMembers();

  const { inlineName, memberId } = extractAuthorRef(data);
  const authorName = getDisplayName(memberId, inlineName);
  const authorNode = memberId ? (
    <Link to={`/members/${memberId}`} className="hover:underline">
      {authorName}
    </Link>
  ) : (
    <span>{authorName}</span>
  );

  const [liked, setLiked] = useState<boolean | null>(null);
  const [likeCount, setLikeCount] = useState<number | null>(null);

  const [compareOpen, setCompareOpen] = useState(false);
  const [compareResult, setCompareResult] = useState<RecipeRefrigeratorComparison | null>(null);
  const [compareLoading, setCompareLoading] = useState(false);

  const [deductPreview, setDeductPreview] = useState<DeductPreviewResponse | null>(null);
  const [deductOpen, setDeductOpen] = useState(false);
  const [deductLoading, setDeductLoading] = useState(false);
  const [deductExecuting, setDeductExecuting] = useState(false);
  const [ignoreWarnings, setIgnoreWarnings] = useState(false);

  if (Number.isNaN(id)) return <div className="p-6">잘못된 글 ID입니다.</div>;
  if (isLoading) return <div className="p-6">불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">글을 찾을 수 없거나 오류가 발생했습니다.</div>;

  const isRecipePost = (data as any).isRecipe === true;

  const safeHtml = DOMPurify.sanitize(data.content ?? '');

  const onDelete = async () => {
    const confirmMsg = isRecipePost
      ? '정말로 이 레시피를 삭제하시겠습니까?\n삭제된 레시피와 관련된 모든 댓글, 추천 정보가 함께 사라지며, 이 작업은 복구할 수 없습니다.'
      : '정말로 이 글을 삭제하시겠습니까? 삭제된 글과 관련된 댓글/추천 정보도 함께 사라지며 복구할 수 없습니다.';
    if (!confirm(confirmMsg)) return;
    try {
      await deleteMutation.mutateAsync(id);
      show(isRecipePost ? '레시피가 성공적으로 삭제되었습니다.' : '글이 성공적으로 삭제되었습니다.', {
        type: 'success',
      });
      const to = isRecipePost
        ? '/boards'
        : sp.get('categoryId')
          ? `/boards?categoryId=${sp.get('categoryId')}`
          : '/boards';
      nav(to);
    } catch (e: any) {
      const status = e?.response?.status as number | undefined;
      const message = e?.response?.data?.message as string | undefined;
      if (status === 401) {
        show('로그인이 필요한 서비스입니다.', { type: 'error' });
        nav('/login');
        return;
      }
      if (status === 403) {
        show(message ?? '삭제 권한이 없습니다.', { type: 'error' });
        return;
      }
      if (status === 404) {
        show(message ?? (isRecipePost ? '이미 삭제된 레시피입니다.' : '이미 삭제된 글입니다.'), { type: 'error' });
        nav('/boards');
        return;
      }
      show(message ?? '삭제 중 오류가 발생했습니다.', { type: 'error' });
    }
  };

  const onToggleLike = async () => {
    try {
      const res = await likeMutation.mutateAsync(id);
      setLiked(res.liked);
      setLikeCount(res.likeCount);
    } catch {
      show('로그인이 필요한 기능입니다.', { type: 'error' });
      nav('/login');
    }
  };

  const onCompareClick = async () => {
    try {
      setCompareLoading(true);
      const res = await compareRecipeWithRefrigerator(id);
      setCompareResult(res);
      setCompareOpen(true);
    } catch {
      show('냉장고 재료 비교를 위해서는 로그인이 필요합니다.', { type: 'error' });
      nav('/login');
    } finally {
      setCompareLoading(false);
    }
  };

  const onShare = async () => {
    try {
      await navigator.clipboard.writeText(window.location.href);
      show('현재 레시피 링크가 클립보드에 복사되었습니다.', { type: 'success' });
    } catch {
      show('클립보드 복사에 실패했습니다. 주소창의 URL을 직접 복사해주세요.', { type: 'error' });
    }
  };

  const onAddToDiary = () => {
    const today = new Date();
    const y = today.getFullYear();
    const m = String(today.getMonth() + 1).padStart(2, '0');
    const d = String(today.getDate()).padStart(2, '0');
    const dateStr = `${y}-${m}-${d}`;
    nav(`/diary/${dateStr}/new`, { state: { recipeId: id } });
  };

  const currentLiked = liked ?? (data as unknown as { likedByMe?: boolean }).likedByMe ?? false;
  const currentLikeCount = likeCount ?? data.likeCount ?? 0;

  const editHref = isRecipePost
    ? `/recipes/${id}/edit`
    : sp.get('categoryId')
      ? `/boards/${id}/edit?categoryId=${sp.get('categoryId')}`
      : `/boards/${id}/edit`;
  const editState = isRecipePost
    ? undefined
    : sp.get('categoryId')
      ? { fromCategoryId: Number(sp.get('categoryId')) }
      : undefined;

  const isAuthorBlocked = memberId != null && blockedMembers?.some((b) => b.blockedId === memberId);
  const authorBlockedAndHidden = isAuthorBlocked && user && user.id !== memberId;

  const onToggleBlockAuthor = async () => {
    if (!memberId) return;
    try {
      if (isAuthorBlocked) {
        if (!confirm('작성자 차단을 해제하시겠습니까?')) return;
        await unblockMutation.mutateAsync(memberId);
        show('작성자 차단이 해제되었습니다.', { type: 'success' });
      } else {
        if (!confirm('이 작성자를 차단하시겠습니까?')) return;
        await blockMutation.mutateAsync(memberId);
        show('작성자가 차단되었습니다.', { type: 'success' });
      }
    } catch (e: any) {
      const message = e?.response?.data?.message as string | undefined;
      show(message ?? '차단 처리 중 오류가 발생했습니다.', { type: 'error' });
    }
  };

  const onOpenDeductPreview = async () => {
    if (!isRecipePost) return;
    try {
      setDeductLoading(true);
      const preview = await getDeductPreview(id);
      setDeductPreview(preview);
      setDeductOpen(true);
    } catch (e: any) {
      const status = e?.response?.status as number | undefined;
      if (status === 401) {
        show('로그인이 필요한 기능입니다.', { type: 'error' });
        nav('/login');
        return;
      }
      show(e?.response?.data?.message ?? '재료 차감 미리보기 로딩 중 오류가 발생했습니다.', { type: 'error' });
    } finally {
      setDeductLoading(false);
    }
  };

  const executeDeduction = async () => {
    if (!deductPreview) return;
    try {
      setDeductExecuting(true);
      const res: DeductResponse = await postDeduct({ recipeId: deductPreview.recipeId, ignoreWarnings });
      show(`재료 차감 완료: 성공 ${res.successCount}개 / 실패 ${res.failedCount}개`, { type: 'success' });
      // 낙관적 업데이트: ingredients 섹션의 수량 감소 (실제 실패 제외 단순화)
      // 구현 단순화를 위해 전체 새로고침 권장 가능성: 현재는 별도 refetch API 없음
      setDeductOpen(false);
    } catch (e: any) {
      const status = e?.response?.status as number | undefined;
      if (status === 401) {
        show('로그인이 필요한 기능입니다.', { type: 'error' });
        nav('/login');
        return;
      }
      show(e?.response?.data?.message ?? '재료 차감 중 오류가 발생했습니다.', { type: 'error' });
    } finally {
      setDeductExecuting(false);
    }
  };

  return (
    <div className="p-6 space-y-4">
      {/* 상단 카테고리/뒤로가기 */}
      <div className="text-sm text-blue-600">
        <Link
          to={sp.get('categoryId') ? `/boards?categoryId=${sp.get('categoryId')}` : '/boards'}
          className="hover:underline"
        >
          {data.categoryName ?? '게시판'} 목록
        </Link>
      </div>

      {/* 레시피 개요 섹션 */}
      {isRecipePost && (
        <section className="space-y-3">
          <h1 className="text-2xl font-bold">{data.title}</h1>
          <div className="flex flex-wrap items-center gap-3 text-sm text-gray-600">
            <span>작성자: {authorNode}</span>
            <span>· {formatDateYMDKorean(data.createdAt)}</span>
            {data.updatedAt ? <span>· 수정 {formatDateYMDKorean(data.updatedAt)}</span> : null}
            <span>· 조회 {data.viewCount}</span>
            <span>· 추천 {currentLikeCount}</span>
            {data.dietType && <span>· 식단 타입: {data.dietType}</span>}
            {data.cookTimeInMinutes != null && <span>· 조리 시간: {data.cookTimeInMinutes}분</span>}
            {data.servings != null && <span>· 분량: {data.servings}인분</span>}
            {data.difficulty && <span>· 난이도: {data.difficulty}</span>}
          </div>
          {/* 대표 이미지는 현재 content 내 이미지 또는 추후 확장 영역 */}
        </section>
      )}

      {/* 기존 제목/메타 정보 (레시피가 아닌 일반 글일 때 주로 사용) */}
      {!isRecipePost && (
        <>
          <h1 className="text-2xl font-bold">{data.title}</h1>
          <div className="text-sm text-gray-600 flex flex-wrap gap-2">
            {authorNode}
            <span>· {formatDateYMDKorean(data.createdAt)}</span>
            {data.updatedAt ? <span>· 수정 {formatDateYMDKorean(data.updatedAt)}</span> : null}
            <span>· 조회 {data.viewCount}</span>
            <span>· 추천 {currentLikeCount}</span>
          </div>
        </>
      )}

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
            <button onClick={onShare} className="border px-3 py-1 rounded">
              공유하기
            </button>
            <button onClick={onAddToDiary} className="border px-3 py-1 rounded bg-green-600 text-white">
              내 식단 다이어리에 추가
            </button>
            <button
              onClick={onCompareClick}
              disabled={!user || compareLoading}
              className="border px-3 py-1 rounded bg-amber-600 text-white disabled:opacity-50"
            >
              {compareLoading ? '비교 중…' : '내 냉장고와 재료 비교'}
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
            onClick={onToggleBlockAuthor}
            disabled={blockMutation.isPending || unblockMutation.isPending}
            className={`border px-3 py-1 rounded text-sm ${isAuthorBlocked ? 'bg-red-600 text-white' : ''} disabled:opacity-50`}
          >
            {isAuthorBlocked ? '작성자 차단 해제' : '작성자 차단'}
          </button>
        )}
      </div>

      {/* 재료 섹션 */}
      {isRecipePost && data.ingredients && data.ingredients.length > 0 && (
        <section className="mt-6 space-y-2">
          <h2 className="text-xl font-semibold">재료 정보</h2>
          <ul className="list-disc list-inside space-y-1">
            {data.ingredients.map((ing) => (
              <li key={ing.id ?? ing.name}>
                {ing.name}
                {ing.quantity != null && (
                  <span>
                    {' '}
                    - {ing.quantity}
                    {ing.unit ?? ''}
                  </span>
                )}
              </li>
            ))}
          </ul>
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
            onClick={onToggleBlockAuthor}
            disabled={blockMutation.isPending || unblockMutation.isPending}
            className="px-3 py-1 rounded bg-red-600 text-white text-xs disabled:opacity-50"
          >
            차단 해제하고 보기
          </button>
        </div>
      )}

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

      {/* 냉장고 비교 모달 */}
      {compareOpen && compareResult && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black/40" onClick={() => setCompareOpen(false)} />
          <div className="relative bg-white rounded-lg shadow-xl w-full max-w-lg mx-4 max-h-[80vh] overflow-hidden">
            <div className="flex items-center justify-between border-b px-5 py-3">
              <h2 className="text-lg font-semibold">냉장고 재료 비교</h2>
              <button className="px-3 py-1 border rounded" onClick={() => setCompareOpen(false)}>
                닫기
              </button>
            </div>
            <div className="p-5 overflow-y-auto space-y-3">
              <p className="text-sm text-gray-700">
                총 {compareResult.totalNeeded}개 재료 중{' '}
                <span className="text-green-600 font-semibold">{compareResult.ownedCount}개</span>는 냉장고에 있고,{' '}
                <span className="text-red-600 font-semibold">{compareResult.missingCount}개</span>는 부족합니다.
              </p>
              <ul className="space-y-1 text-sm">
                {compareResult.ingredients.map((ing) => (
                  <li key={ing.name} className="flex justify-between">
                    <span>
                      {ing.name} - {ing.amount}
                    </span>
                    <span className={ing.status === 'OWNED' ? 'text-green-600' : 'text-red-600'}>
                      {ing.status === 'OWNED' ? '보유' : '부족'}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
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
              {deductPreview.warnings.length > 0 && (
                <div className="space-y-1">
                  <h3 className="font-semibold text-sm">경고</h3>
                  <ul className="list-disc list-inside text-xs text-amber-700">
                    {deductPreview.warnings.map((w, i) => (
                      <li key={i}>{w}</li>
                    ))}
                  </ul>
                </div>
              )}
              <div className="flex items-center gap-2 text-xs">
                <input
                  id="ignoreWarnings"
                  type="checkbox"
                  checked={ignoreWarnings}
                  onChange={(e) => setIgnoreWarnings(e.target.checked)}
                  className="cursor-pointer"
                />
                <label htmlFor="ignoreWarnings" className="cursor-pointer">
                  경고를 무시하고 강제 실행
                </label>
              </div>
            </div>
            <div className="border-t px-5 py-3 flex justify-end gap-3">
              <button onClick={() => setDeductOpen(false)} className="px-3 py-1 border rounded text-sm">
                취소
              </button>
              <button
                onClick={executeDeduction}
                disabled={deductExecuting || (!deductPreview.canProceed && !ignoreWarnings)}
                className="px-4 py-1 rounded text-sm bg-purple-600 text-white disabled:opacity-50"
              >
                {deductExecuting ? '차감 중…' : '재료 차감 실행'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
