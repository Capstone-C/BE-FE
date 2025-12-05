import { useParams, Link, useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getMember, type Member } from '../../../apis/members';
import { getMe } from '../../../apis/auth';
import { formatDateYMDKorean } from '../../../utils/date';
import {
  useBlockMemberMutation,
  useUnblockMemberMutation,
  useBlockedMembers,
} from '../hooks/useMemberBlocks';
import { useToast } from '../../../contexts/ToastContext';
import type { BlockedMember } from '../../../apis/memberBlocks';

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
    <div className="max-w-4xl mx-auto p-8">
      <div className="text-center mb-8">
        <h1 className="text-4xl font-bold gradient-text mb-2">{viewingSelf ? '👤 마이페이지' : '👥 회원 프로필'}</h1>
        <p className="text-gray-600">{viewingSelf ? '내 정보와 활동을 관리하세요' : '회원 프로필 정보'}</p>
      </div>

      <div className="bg-white shadow-xl rounded-2xl p-8 flex items-center space-x-8 border-2 border-gray-100">
        {profileImageUrl ? (
          <div className="relative">
            <img
              src={profileImageUrl}
              alt={`${displayName}의 프로필 이미지`}
              className="w-36 h-36 rounded-full object-cover border-4 border-purple-100 shadow-lg"
            />
            <div className="absolute -bottom-2 -right-2 w-10 h-10 bg-gradient-to-r from-purple-600 to-indigo-600 rounded-full flex items-center justify-center">
              <span className="text-white text-xl">✨</span>
            </div>
          </div>
        ) : (
          <div className="w-36 h-36 rounded-full bg-gradient-to-br from-purple-100 to-indigo-100 flex items-center justify-center text-4xl text-purple-600 font-bold shadow-lg border-4 border-purple-200">
            {displayName.charAt(0)}
          </div>
        )}

        <div className="flex-grow">
          <div className="mb-4">
            <p className="text-sm text-gray-500 font-medium">닉네임</p>
            <p className="text-3xl font-bold text-gray-800">{displayName}</p>
          </div>

          {viewingSelf ? (
            <>
              {(effectiveMember as any).email && (
                <div className="mb-4 bg-gradient-to-r from-purple-50 to-indigo-50 p-3 rounded-xl border-2 border-purple-100">
                  <p className="text-xs text-purple-600 font-semibold mb-1">📧 이메일</p>
                  <p className="text-base text-gray-800 font-medium">{(effectiveMember as any).email}</p>
                </div>
              )}
              {(effectiveMember as any).joinedAt && (
                <div className="text-sm">
                  <p className="text-gray-500">📅 가입일: <span className="font-medium">{formatDateYMDKorean((effectiveMember as any).joinedAt)}</span></p>
                </div>
              )}
            </div>
          </div>
        </div>

      {/* 활동 보기 버튼 (공개/본인 공통) */}
      <div className="mt-8 flex flex-wrap gap-4">
        <Link
          to={viewingSelf ? '/mypage/posts' : `/boards?authorId=${encodeURIComponent(String(effectiveMember.id))}`}
          className="px-6 py-3 bg-gradient-to-r from-green-600 to-emerald-600 text-white rounded-xl hover:shadow-lg hover:scale-105 transition-all font-semibold flex items-center gap-2"
        >
          📝 {viewingSelf ? '내가 작성한 글 보기' : '이 회원이 작성한 글 보기'}
        </Link>
        <Link
          to={
            viewingSelf
              ? '/mypage/comments'
              : `/mypage/comments?authorId=${encodeURIComponent(String(effectiveMember.id))}`
          }
          className="px-6 py-3 bg-gradient-to-r from-emerald-600 to-teal-600 text-white rounded-xl hover:shadow-lg hover:scale-105 transition-all font-semibold flex items-center gap-2"
        >
          💬 {viewingSelf ? '내가 작성한 댓글 보기' : '이 회원이 작성한 댓글 보기'}
        </Link>
      </div>

      {/* 본인 전용 관리 버튼 */}
      {viewingSelf && (
        <div className="mt-6 flex flex-wrap gap-4">
          <Link to="/mypage/edit" className="px-6 py-3 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-xl hover:shadow-lg hover:scale-105 transition-all font-semibold flex items-center gap-2">
            ✏️ 회원정보 수정
          </Link>
          <Link to="/mypage/password" className="px-6 py-3 bg-gradient-to-r from-gray-600 to-slate-600 text-white rounded-xl hover:shadow-lg hover:scale-105 transition-all font-semibold flex items-center gap-2">
            🔐 비밀번호 변경
          </Link>
          <Link to="/mypage/withdraw" className="px-6 py-3 bg-gradient-to-r from-red-600 to-rose-600 text-white rounded-xl hover:shadow-lg hover:scale-105 transition-all font-semibold flex items-center gap-2">
            🚪 회원탈퇴
          </Link>
          <Link
            to="/mypage/blocked"
            className="px-6 py-3 bg-gradient-to-r from-orange-600 to-amber-600 text-white rounded-xl hover:shadow-lg hover:scale-105 transition-all font-semibold flex items-center gap-2"
          >
            🚫 차단된 사용자 관리
          </Link>
          <Link to="/mypage/scraps" className="px-6 py-3 bg-gradient-to-r from-yellow-600 to-amber-600 text-white rounded-xl hover:shadow-lg hover:scale-105 transition-all font-semibold flex items-center gap-2">
            ⭐ 내 스크랩북
          </Link>
        </div>
      )}
      {!viewingSelf && (
        <div className="mt-6 flex flex-wrap gap-4">
          <button
            onClick={onBlockToggle}
            disabled={blockMutation.isPending || unblockMutation.isPending}
            className={`px-6 py-3 rounded-xl text-white font-semibold flex items-center gap-2 hover:shadow-lg hover:scale-105 transition-all disabled:opacity-50 disabled:hover:scale-100 ${
              isBlocked 
                ? 'bg-gradient-to-r from-red-600 to-rose-600' 
                : 'bg-gradient-to-r from-gray-800 to-slate-900'
            }`}
          >
            {isBlocked ? '🚫 차단 해제' : '🚫 차단하기'}
          </button>
        </div>
      )}
    </div>
  );
}