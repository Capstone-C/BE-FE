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
  RecommendationResponse,
  DeductPreviewResponse,
  DeductRequest,
  DeductResponse,
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

// 추천 레시피 목록 (REF-07)
export async function getRecommendations(limit: number = 10) {
  const safeLimit = limit <= 0 || limit > 50 ? 10 : limit;
  const { data } = await authClient.get<RecommendationResponse>('/api/v1/refrigerator/recommendations', {
    params: { limit: safeLimit },
  });
  return data;
}

// 재료 차감 미리보기 (REF-08)
export async function getDeductPreview(recipeId: number) {
  const { data } = await authClient.get<DeductPreviewResponse>('/api/v1/refrigerator/deduct-preview', {
    params: { recipeId },
  });
  return data;
}

// 재료 차감 실행 (REF-08)
export async function postDeduct(payload: DeductRequest) {
  const body: DeductRequest = {
    recipeId: payload.recipeId,
    ignoreWarnings: payload.ignoreWarnings ?? false,
  };
  const { data } = await authClient.post<DeductResponse>('/api/v1/refrigerator/deduct', body, {
    headers: { 'Content-Type': 'application/json' },
  });
  return data;
}
