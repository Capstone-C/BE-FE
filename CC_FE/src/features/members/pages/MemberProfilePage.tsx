import { useParams, Link, useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getMember, type Member } from '@/apis/members';
import { getMe } from '@/apis/auth';
import { formatDateYMDKorean } from '@/utils/date';
import {
  useBlockMemberMutation,
  useUnblockMemberMutation,
  useBlockedMembers,
} from '@/features/members/hooks/useMemberBlocks';
import { useToast } from '@/contexts/ToastContext';
import type { BlockedMember } from '@/apis/memberBlocks';

/**
 * 통합 프로필 페이지
 * - /members/:memberId : 공개 프로필 (비로그인 가능, 닉네임/활동 버튼만)
 * - /mypage : 내 프로필 (로그인 필요, 개인정보 + 관리 버튼 + 활동 버튼)
 */
export default function MemberProfilePage() {
  const { memberId } = useParams();
  const location = useLocation();
  const hasParam = typeof memberId === 'string' && memberId.length > 0;

  // Hooks (must be at top, no conditional returns before these)
  const blockMutation = useBlockMemberMutation();
  const unblockMutation = useUnblockMemberMutation();
  // blocked list는 로그인 상태(me 존재)일 때만 요청
  const { data: me, isLoading: meLoading, isError: meError } = useQuery({ queryKey: ['me'], queryFn: getMe, retry: 1 });
  const targetId = hasParam ? Number(memberId) : undefined;
  const {
    data: member,
    isLoading: memberLoading,
    isError: memberError,
  } = useQuery<Member>({
    queryKey: ['member', targetId],
    queryFn: () => getMember(targetId as number),
    enabled: typeof targetId === 'number' && Number.isFinite(targetId) && targetId > 0,
    retry: 1,
  });
  const { data: blockedList } = useBlockedMembers(!!me);
  const { show } = useToast();
  const isMyPageRoute = location.pathname.startsWith('/mypage');

  const invalidId = hasParam && (!targetId || Number.isNaN(targetId) || targetId <= 0);

  const viewingSelf = isMyPageRoute || (!!me && !!member && me.id === member?.id);
  const effectiveMember: Partial<Member> & { id?: number; nickname?: string; name?: string } = viewingSelf
    ? (me as any)
    : (member as any);

  const isBlocked =
    !viewingSelf &&
    !!effectiveMember?.id &&
    blockedList?.some((b: BlockedMember) => b.blockedId === effectiveMember.id);

  const onBlockToggle = async () => {
    if (!effectiveMember?.id) return;
    try {
      if (isBlocked) {
        if (!confirm('차단을 해제하시겠습니까?')) return;
        await unblockMutation.mutateAsync(effectiveMember.id);
        show('차단이 해제되었습니다.', { type: 'success' });
      } else {
        if (!confirm('이 회원을 차단하시겠습니까?')) return;
        await blockMutation.mutateAsync(effectiveMember.id);
        show('회원이 차단되었습니다.', { type: 'success' });
      }
    } catch (e: any) {
      const status = e?.response?.status as number | undefined;
      const message = e?.response?.data?.message as string | undefined;
      show(message ?? (status === 400 ? '요청을 처리할 수 없습니다.' : '차단 처리 중 오류가 발생했습니다.'), {
        type: 'error',
      });
    }
  };

  // 최종 렌더링 분기 (hook 뒤에서 처리)
  if (invalidId) return <div className="p-8 text-center">잘못된 회원 ID 입니다.</div>;

  if (isMyPageRoute && meLoading) return <div className="p-8 text-center">프로필 정보를 불러오는 중입니다...</div>;
  if (isMyPageRoute && (meError || !me))
    return <div className="p-8 text-center text-red-600">로그인이 필요합니다.</div>;

  if (hasParam && memberLoading) return <div className="p-8 text-center">프로필 정보를 불러오는 중입니다...</div>;
  if (hasParam && (memberError || !member))
    return <div className="p-8 text-center text-red-600">회원 정보를 찾을 수 없거나 오류가 발생했습니다.</div>;

  if (!effectiveMember?.id) return <div className="p-8 text-center">프로필을 불러올 수 없습니다.</div>;

  const displayName = effectiveMember.nickname ?? effectiveMember.name ?? `작성자 #${effectiveMember.id}`;
  const profileImageUrl = viewingSelf && (effectiveMember as any).profile ? (effectiveMember as any).profile : null;

  return (
    <div className="max-w-4xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-6">{viewingSelf ? '마이페이지' : '회원 프로필'}</h1>

      <div className="bg-white shadow-md rounded-lg p-6 flex items-center space-x-6">
        {profileImageUrl ? (
          <img
            src={profileImageUrl}
            alt={`${displayName}의 프로필 이미지`}
            className="w-32 h-32 rounded-full object-cover border-4 border-gray-200"
          />
        ) : (
          <div className="w-32 h-32 rounded-full bg-gray-200 flex items-center justify-center text-3xl text-gray-500">
            {displayName.charAt(0)}
          </div>
        )}

        <div className="flex-grow">
          <div className="mb-4">
            <p className="text-sm text-gray-500">닉네임</p>
            <p className="text-2xl font-semibold">{displayName}</p>
          </div>

          {viewingSelf ? (
            <>
              {(effectiveMember as any).email && (
                <div className="mb-4">
                  <p className="text-sm text-gray-500">이메일</p>
                  <p className="text-lg text-gray-800">{(effectiveMember as any).email}</p>
                </div>
              )}
              {(effectiveMember as any).joinedAt && (
                <div className="text-sm">
                  <p className="text-gray-500">가입일: {formatDateYMDKorean((effectiveMember as any).joinedAt)}</p>
                </div>
              )}
            </>
          ) : null}
        </div>
      </div>

      {/* 활동 보기 버튼 (공개/본인 공통) */}
      <div className="mt-6 flex flex-wrap gap-4">
        <Link
          to={viewingSelf ? '/mypage/posts' : `/boards?authorId=${encodeURIComponent(String(effectiveMember.id))}`}
          className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm"
        >
          {viewingSelf ? '내가 작성한 글 보기' : '이 회원이 작성한 글 보기'}
        </Link>
        <Link
          to={
            viewingSelf
              ? '/mypage/comments'
              : `/mypage/comments?authorId=${encodeURIComponent(String(effectiveMember.id))}`
          }
          className="px-6 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 text-sm"
        >
          {viewingSelf ? '내가 작성한 댓글 보기' : '이 회원이 작성한 댓글 보기'}
        </Link>
      </div>

      {/* 본인 전용 관리 버튼 */}
      {viewingSelf && (
        <div className="mt-6 flex flex-wrap gap-4">
          <Link to="/mypage/edit" className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm">
            회원정보 수정
          </Link>
          <Link to="/mypage/password" className="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 text-sm">
            비밀번호 변경
          </Link>
          <Link to="/mypage/withdraw" className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm">
            회원탈퇴
          </Link>
          <Link
            to="/mypage/blocked"
            className="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 text-sm"
          >
            차단된 사용자 관리
          </Link>
          <Link to="/mypage/scraps" className="px-6 py-2 bg-amber-600 text-white rounded-lg hover:bg-amber-700 text-sm">
            내 스크랩북
          </Link>
        </div>
      )}
      {!viewingSelf && (
        <div className="mt-6 flex flex-wrap gap-4">
          <button
            onClick={onBlockToggle}
            disabled={blockMutation.isPending || unblockMutation.isPending}
            className={`px-6 py-2 rounded-lg text-white text-sm ${isBlocked ? 'bg-red-600 hover:bg-red-700' : 'bg-gray-800 hover:bg-gray-900'} disabled:opacity-50`}
          >
            {isBlocked ? '차단 해제' : '차단하기'}
          </button>
        </div>
      )}
    </div>
  );
}
