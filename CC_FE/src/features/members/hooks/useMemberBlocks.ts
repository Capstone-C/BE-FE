// src/features/members/hooks/useMemberBlocks.ts
import type { BlockedMember } from '@/apis/memberBlocks';
export type { BlockedMember };

import { listBlockedMembers, blockMember, unblockMember } from '@/apis/memberBlocks';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

export function useBlockedMembers(enabled: boolean = true) {
  return useQuery({ queryKey: ['blocked-members'], queryFn: listBlockedMembers, enabled });
}

export function useBlockMemberMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (blockedId: number) => blockMember(blockedId),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ['blocked-members'] });
    },
  });
}

export function useUnblockMemberMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (blockedId: number) => unblockMember(blockedId),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ['blocked-members'] });
    },
  });
}
