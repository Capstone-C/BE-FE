// src/features/members/pages/BlockedMembersPage.tsx
import { useBlockedMembers, useUnblockMemberMutation } from '@/features/members/hooks/useMemberBlocks';
import { useToast } from '@/contexts/ToastContext';

export default function BlockedMembersPage() {
  const { data, isLoading, isError, refetch } = useBlockedMembers();
  const unblock = useUnblockMemberMutation();
  const { show } = useToast();

  const onUnblock = async (blockedId: number) => {
    if (!confirm('이 회원의 차단을 해제하시겠습니까?')) return;
    try {
      await unblock.mutateAsync(blockedId);
      show('차단이 해제되었습니다.', { type: 'success' });
      await refetch();
    } catch (e: any) {
      const message = e?.response?.data?.message as string | undefined;
      show(message ?? '차단 해제 중 오류가 발생했습니다.', { type: 'error' });
    }
  };

  if (isLoading) return <div className="p-6">목록을 불러오는 중...</div>;
  if (isError) return <div className="p-6 text-red-600">차단 목록을 불러오지 못했습니다.</div>;

  return (
    <div className="p-6 max-w-3xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">차단된 사용자 관리</h1>
      <p className="text-sm text-gray-600 mb-6">불쾌하거나 원치 않는 상호작용을 한 사용자를 차단 해제할 수 있습니다.</p>
      {!data?.length ? (
        <div className="text-gray-600">차단한 사용자가 없습니다.</div>
      ) : (
        <ul className="space-y-3">
          {data.map((b: any) => (
            <li key={b.id} className="flex items-center justify-between border rounded p-3 bg-white">
              <div className="space-y-1 text-sm">
                <p className="font-medium">{b.blockedEmail ?? `회원 #${b.blockedId}`}</p>
                <p className="text-gray-500">차단일: {new Date(b.createdAt).toLocaleString()}</p>
              </div>
              <button
                onClick={() => onUnblock(b.blockedId)}
                className="px-3 py-1 rounded bg-red-600 text-white text-sm hover:bg-red-700"
              >
                차단 해제
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
