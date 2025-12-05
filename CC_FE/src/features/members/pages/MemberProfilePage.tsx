import { useParams, Link, useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getMember, type Member } from '@/apis/members';
import { getMe } from '@/apis/auth';
import { formatDateYMDKorean } from '@/utils/date';
import {
  useBlockMemberMutation,
  useUnblockMemberMutation,
  useBlockedMembers,
} from '../hooks/useMemberBlocks';
import { useToast } from '@/contexts/ToastContext';
import type { BlockedMember } from '@/apis/memberBlocks';
import { BoardIcon, ChatIcon } from '@/components/ui/Icons';

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

  if (isLoading && hasParam) return <div className="p-12 text-center text-gray-500">프로필 불러오는 중...</div>;

  const viewingSelf = isMyPageRoute || (!!me && !!member && me.id === member?.id);
  const effectiveMember: any = viewingSelf ? me : member;

  if (!effectiveMember) return <div className="p-12 text-center text-red-500">회원 정보를 찾을 수 없습니다.</div>;

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
    <div className="max-w-5xl mx-auto py-12 px-4 sm:px-6 lg:px-8">

      {/* 헤더 섹션: 프로필 정보 */}
      <div className="bg-white rounded-2xl shadow-md overflow-hidden mb-8 border border-gray-100">
        <div className="h-32 bg-gradient-to-r from-[#4E652F] to-[#71853A] relative">
          {/* 배경 장식 (옵션) */}
          <div className="absolute inset-0 opacity-10 bg-[url('/images/pattern.svg')]"></div>
        </div>
        <div className="px-8 pb-8">
          <div className="relative flex flex-col sm:flex-row items-center sm:items-end -mt-12 sm:-mt-16 mb-6 text-center sm:text-left">
            <div className="w-32 h-32 rounded-full border-4 border-white bg-gray-200 shadow-md overflow-hidden flex-shrink-0 z-10">
              {effectiveMember.profile ? (
                <img src={effectiveMember.profile} alt="Profile" className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center bg-gray-100 text-4xl text-gray-400 font-bold">
                  {displayName.charAt(0)}
                </div>
              )}
            </div>
            <div className="mt-4 sm:mt-0 sm:ml-6 flex-1">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h1 className="text-3xl font-bold text-gray-900">{displayName}</h1>
                  <p className="text-gray-500 font-medium mt-1">{effectiveMember.email}</p>
                </div>
                {/* 타인 프로필일 때 차단 버튼 */}
                {!viewingSelf && (
                  <div className="mt-4 sm:mt-0">
                    <button
                      onClick={onBlockToggle}
                      disabled={blockMutation.isPending || unblockMutation.isPending}
                      className={`px-5 py-2 rounded-full font-medium text-sm transition-colors shadow-sm ${
                        isBlocked
                          ? 'bg-gray-100 text-gray-700 border border-gray-300 hover:bg-gray-200'
                          : 'bg-red-50 text-red-600 border border-red-200 hover:bg-red-100'
                      } disabled:opacity-50`}
                    >
                      {isBlocked ? '차단 해제' : '차단하기'}
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="flex flex-wrap gap-4 justify-center sm:justify-start text-sm text-gray-500 border-t border-gray-100 pt-4">
            {effectiveMember.joinedAt && (
              <div className="flex items-center gap-1">
                <span>📅 가입일:</span>
                <span className="font-medium text-gray-700">{formatDateYMDKorean(effectiveMember.joinedAt)}</span>
              </div>
            )}
            {/* [삭제됨] 활동 점수 표시 제거 */}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

        {/* 왼쪽 컬럼: 활동 요약 (내비게이션) */}
        <div className="lg:col-span-2 space-y-8">
          {/* 활동 바로가기 */}
          <section>
            <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
              <span className="w-1.5 h-6 bg-[#4E652F] rounded-full"></span>
              활동 내역
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Link
                to={viewingSelf ? '/mypage/posts' : `/boards?authorId=${encodeURIComponent(String(effectiveMember.id))}`}
                className="group bg-white p-6 rounded-xl shadow-sm border border-gray-100 hover:border-[#4E652F] hover:shadow-md transition-all flex items-center gap-4"
              >
                <div className="w-12 h-12 rounded-full bg-[#F0F5E5] flex items-center justify-center text-[#4E652F] group-hover:bg-[#4E652F] group-hover:text-white transition-colors">
                  <BoardIcon className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="font-bold text-gray-900 group-hover:text-[#4E652F] transition-colors">작성한 게시글</h3>
                  <p className="text-sm text-gray-500 mt-1">레시피 및 커뮤니티 글 모아보기</p>
                </div>
              </Link>

              <Link
                to={
                  viewingSelf
                    ? '/mypage/comments'
                    : `/mypage/comments?authorId=${encodeURIComponent(String(effectiveMember.id))}`
                }
                className="group bg-white p-6 rounded-xl shadow-sm border border-gray-100 hover:border-[#4E652F] hover:shadow-md transition-all flex items-center gap-4"
              >
                <div className="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
                  <ChatIcon className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="font-bold text-gray-900 group-hover:text-blue-600 transition-colors">작성한 댓글</h3>
                  <p className="text-sm text-gray-500 mt-1">참여한 대화 목록</p>
                </div>
              </Link>

              {viewingSelf && (
                <Link to="/mypage/scraps" className="group bg-white p-6 rounded-xl shadow-sm border border-gray-100 hover:border-amber-500 hover:shadow-md transition-all flex items-center gap-4 sm:col-span-2">
                  <div className="w-12 h-12 rounded-full bg-amber-50 flex items-center justify-center text-amber-600 group-hover:bg-amber-500 group-hover:text-white transition-colors">
                    <span className="text-xl">⭐</span>
                  </div>
                  <div>
                    <h3 className="font-bold text-gray-900 group-hover:text-amber-600 transition-colors">내 스크랩북</h3>
                    <p className="text-sm text-gray-500 mt-1">저장한 레시피와 유용한 정보</p>
                  </div>
                </Link>
              )}
            </div>
          </section>
        </div>

        {/* 오른쪽 컬럼: 계정 관리 (본인일 때만 표시) */}
        {viewingSelf && (
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 sticky top-24">
              <h3 className="text-lg font-bold text-gray-800 mb-4 pb-2 border-b border-gray-100">계정 관리</h3>
              <nav className="space-y-2">
                <Link to="/mypage/edit" className="flex items-center justify-between px-4 py-3 rounded-lg text-gray-700 hover:bg-gray-50 hover:text-[#4E652F] transition-colors group">
                  <span className="font-medium">회원정보 수정</span>
                  <span className="text-gray-400 group-hover:translate-x-1 transition-transform">→</span>
                </Link>
                <Link to="/mypage/password" className="flex items-center justify-between px-4 py-3 rounded-lg text-gray-700 hover:bg-gray-50 hover:text-[#4E652F] transition-colors group">
                  <span className="font-medium">비밀번호 변경</span>
                  <span className="text-gray-400 group-hover:translate-x-1 transition-transform">→</span>
                </Link>
                <Link to="/mypage/blocked" className="flex items-center justify-between px-4 py-3 rounded-lg text-gray-700 hover:bg-gray-50 hover:text-[#4E652F] transition-colors group">
                  <span className="font-medium">차단된 사용자 관리</span>
                  <span className="text-gray-400 group-hover:translate-x-1 transition-transform">→</span>
                </Link>
                <div className="pt-4 mt-2 border-t border-gray-100">
                  <Link to="/mypage/withdraw" className="flex items-center justify-between px-4 py-2 rounded-lg text-sm text-gray-500 hover:text-red-600 hover:bg-red-50 transition-colors">
                    <span>회원 탈퇴</span>
                  </Link>
                </div>
              </nav>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}