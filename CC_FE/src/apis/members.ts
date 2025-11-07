// memberid 조회용
// src/apis/members.api.ts
import { authClient } from '@/apis/client';
export type Member = { id: number; name: string; nickname?: string };
export async function getMember(id: number): Promise<Member> {
  const { data } = await authClient.get(`/api/v1/members/${id}`);
  return data;
}