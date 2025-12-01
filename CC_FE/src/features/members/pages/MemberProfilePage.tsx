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

export default function MemberProfilePage() {
  const { memberId } = useParams();
  const location = useLocation();
  const hasParam = typeof memberId === 'string' && memberId.length > 0;

  const { data: me } = useQuery({ queryKey: ['me'], queryFn: getMe, retry: 1 });
  const targetId = hasParam ? Number(memberId) : undefined;
  const { data: member, isLoading } = useQuery<Member>({
    queryKey: ['member', targetId],
    queryFn: () => getMember(targetId as number),
    enabled: !!targetId && targetId > 0,
  });

  const blockMutation = useBlockMemberMutation();
  const unblockMutation = useUnblockMemberMutation();
  const { data: blockedList } = useBlockedMembers(!!me);
  const { show } = useToast();
  const isMyPageRoute = location.pathname.startsWith('/mypage');

  if (isLoading && hasParam) return <div className="p-8 text-center">프로필 불러오는 중...</div>;

  const viewingSelf = isMyPageRoute || (!!me && !!member && me.id === member?.id);
  const effectiveMember: any = viewingSelf ? me : member;

  if (!effectiveMember) return <div className="p-8 text-center">회원 정보를 찾을 수 없습니다.</div>;

  const displayName = effectiveMember.nickname ?? effectiveMember.name ?? `회원 #${effectiveMember.id}`;
  const isBlocked = !viewingSelf && blockedList?.some((b: BlockedMember) => b.blockedId === effectiveMember.id);

  const onBlockToggle = async () => {
    if (!effectiveMember.id) return;
    try {
      if (isBlocked) {
        await unblockMutation.mutateAsync(effectiveMember.id);
        show('차단이 해제되었습니다.', { type: 'success' });
      } else {
        await blockMutation.mutateAsync(effectiveMember.id);
        show('회원이 차단되었습니다.', { type: 'success' });
      }
    } catch {
      show('처리 중 오류가 발생했습니다.', { type: 'error' });
    }
  };

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold leading-tight text-gray-900">
          {viewingSelf ? '마이페이지' : '회원 프로필'}
        </h1>
      </div>

      <div className="bg-white rounded-lg shadow-lg overflow-hidden">
        <div className="p-8">
          <div className="flex items-center space-x-6">
            <div className="w-24 h-24 rounded-full bg-gray-300 flex-shrink-0 flex items-center justify-center overflow-hidden">
              {effectiveMember.profile ? (
                <img src={effectiveMember.profile} alt="Profile" className="w-full h-full object-cover" />
              ) : (
                <span className="text-3xl text-gray-500">{displayName.charAt(0)}</span>
              )}
            </div>
            <div>
              <h2 className="text-2xl font-bold text-gray-800">{displayName}</h2>
              <p className="text-md text-gray-500 mt-1">{effectiveMember.email}</p>
              {effectiveMember.joinedAt && (
                <p className="text-sm text-gray-400 mt-2">가입일: {formatDateYMDKorean(effectiveMember.joinedAt)}</p>
              )}
            </div>
          </div>
        </div>

        <div className="border-t border-gray-200 p-8">
          <h3 className="text-xl font-semibold text-gray-700 mb-4">활동 및 관리</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* 공통 메뉴 */}
            <Link
              to={viewingSelf ? '/mypage/posts' : `/boards?authorId=${effectiveMember.id}`}
              className="p-4 border rounded-lg hover:bg-gray-50 transition-colors"
            >
              <p className="font-medium text-gray-800">작성한 게시글</p>
              <p className="text-sm text-gray-500">작성한 게시글 목록을 확인합니다.</p>
            </Link>
            <Link
              to={viewingSelf ? '/mypage/comments' : `/mypage/comments?authorId=${effectiveMember.id}`}
              className="p-4 border rounded-lg hover:bg-gray-50 transition-colors"
            >
              <p className="font-medium text-gray-800">작성한 댓글</p>
              <p className="text-sm text-gray-500">작성한 댓글 목록을 확인합니다.</p>
            </Link>

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
      </div>
    </div>
  );
}