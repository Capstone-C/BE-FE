import { useParams, Link, useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getMember, type Member } from '@/apis/members';
import { getMe } from '@/apis/auth';
import { formatDateYMDKorean } from '@/utils/date';

/**
 * 통합 프로필 페이지
 * - /members/:memberId : 공개 프로필 (비로그인 가능, 닉네임/활동 버튼만)
 * - /mypage : 내 프로필 (로그인 필요, 개인정보 + 관리 버튼 + 활동 버튼)
 */
export default function MemberProfilePage() {
  const { memberId } = useParams();
  const location = useLocation();
  const hasParam = typeof memberId === 'string' && memberId.length > 0;

  // 내 정보(로그인 여부 판단). 실패해도 공개 프로필 열람은 계속 진행
  const {
    data: me,
    isLoading: meLoading,
    isError: meError,
  } = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
    retry: 1,
  });

  // 타겟 회원 정보: 파라미터가 있을 때만 조회 (/members/:memberId)
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

  // 경로 판별: /mypage 인지 여부
  const isMyPageRoute = location.pathname.startsWith('/mypage');

  // /mypage 인데 로그인 실패하거나 meError인 경우: 보호된 라우트에서 이미 막히겠지만 방어적으로 처리
  if (isMyPageRoute) {
    if (meLoading) return <div className="p-8 text-center">프로필 정보를 불러오는 중입니다...</div>;
    if (meError || !me) return <div className="p-8 text-center text-red-600">로그인이 필요합니다.</div>;
  }

  // 파라미터가 있는 경우 잘못된 id 검사
  if (hasParam) {
    if (!targetId || Number.isNaN(targetId) || targetId <= 0) {
      return <div className="p-8 text-center">잘못된 회원 ID 입니다.</div>;
    }
  }

  // 공개 프로필 로딩/에러 처리 (/members/:id 전용)
  if (hasParam) {
    if (memberLoading) return <div className="p-8 text-center">프로필 정보를 불러오는 중입니다...</div>;
    if (memberError || !member)
      return <div className="p-8 text-center text-red-600">회원 정보를 찾을 수 없거나 오류가 발생했습니다.</div>;
  }

  // 표시할 대상 결정
  const viewingSelf = isMyPageRoute || (!!me && !!member && me.id === member.id);
  const effectiveMember: Partial<Member> & { id?: number; nickname?: string; name?: string } = viewingSelf
    ? (me as any) // me는 MemberProfileResponse(넓은 타입)일 가능성 있음
    : (member as any);

  if (!effectiveMember || !effectiveMember.id) {
    // 이 경우는 이론상 발생하지 않지만 안전장치
    return <div className="p-8 text-center">프로필을 불러올 수 없습니다.</div>;
  }

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
          to={`/boards?searchType=AUTHOR&keyword=${encodeURIComponent(displayName)}`}
          className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm"
        >
          {viewingSelf ? '내가 작성한 글 보기' : '이 회원이 작성한 글 보기'}
        </Link>
        <Link
          to={`/boards?searchType=AUTHOR&keyword=${encodeURIComponent(displayName)}&view=comments`}
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
        </div>
      )}
    </div>
  );
}
