// src/apis/refrigerator.api.ts
import { authClient } from '@/apis/client';
import type {
  RefrigeratorItemListResponse,
  CreateRefrigeratorItemRequest,
  UpdateRefrigeratorItemRequest,
  RefrigeratorItem,
} from '@/types/refrigerator';

// 목록 조회 (현재 백엔드: /api/v1/refrigerator/items) - sortBy: expirationDate | name | createdAt
export async function getRefrigeratorItems(sortBy: string = 'expirationDate') {
  const { data } = await authClient.get<RefrigeratorItemListResponse>('/api/v1/refrigerator/items', {
    params: { sortBy },
  });
  return data;
}

// 단건 조회 (수정용 프리필)
export async function getRefrigeratorItem(id: number) {
  const { data } = await authClient.get<RefrigeratorItem>(`/api/v1/refrigerator/items/${id}`);
  return data;
}

export async function createRefrigeratorItem(payload: CreateRefrigeratorItemRequest) {
  const { data } = await authClient.post<RefrigeratorItem>('/api/v1/refrigerator/items', payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  return data;
}

export async function updateRefrigeratorItem(id: number, payload: UpdateRefrigeratorItemRequest) {
  const { data } = await authClient.put<RefrigeratorItem>(`/api/v1/refrigerator/items/${id}`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  return data;
}

export async function deleteRefrigeratorItem(id: number) {
  await authClient.delete(`/api/v1/refrigerator/items/${id}`);
}
