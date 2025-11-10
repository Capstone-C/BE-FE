// src/apis/refrigerator.api.ts
import { authClient } from '@/apis/client';
import type {
  RefrigeratorItemListResponse,
  CreateRefrigeratorItemRequest,
  UpdateRefrigeratorItemRequest,
  RefrigeratorItem,
  BulkCreateRequest,
  BulkCreateResponse,
  ScanPurchaseHistoryResponse,
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

// 일괄 추가 (영수증 결과 등) REF-03/04
export async function bulkCreateRefrigeratorItems(payload: BulkCreateRequest) {
  const { data } = await authClient.post<BulkCreateResponse>('/api/v1/refrigerator/items/bulk', payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  return data;
}

// 영수증 구매 이력 OCR 스캔 (REF-04 백엔드 구현됨)
export async function scanPurchaseHistory(imageFile: File) {
  const formData = new FormData();
  formData.append('image', imageFile);
  // Gemini 전용으로 provider 파라미터 제거
  const { data } = await authClient.post<ScanPurchaseHistoryResponse>(
    '/api/v1/refrigerator/scan/purchase-history',
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
    },
  );
  return data;
}
