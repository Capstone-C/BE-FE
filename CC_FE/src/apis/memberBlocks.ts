// src/apis/memberBlocks.ts
import { authClient } from '@/apis/client';

export type BlockedMember = {
  id: number;
  blockedId: number;
  blockedEmail: string;
  createdAt: string; // ISO
};

export async function listBlockedMembers(): Promise<BlockedMember[]> {
  const { data } = await authClient.get('/api/v1/members/blocks');
  return data;
}

export async function blockMember(blockedId: number): Promise<void> {
  await authClient.post('/api/v1/members/blocks', { blockedId });
}

export async function unblockMember(blockedId: number): Promise<void> {
  await authClient.delete(`/api/v1/members/blocks/${blockedId}`);
}

