// src/types/refrigerator.ts
export interface RefrigeratorItem {
  id: number;
  memberId: number;
  name: string;
  quantity: number;
  unit: string | null;
  expirationDate: string | null; // yyyy-MM-dd
  memo: string | null;
  daysUntilExpiration: number | null; // D-day (null if no expiration)
  expirationSoon: boolean; // 3일 이내
  expired: boolean; // 지남
  createdAt: string; // ISO
  updatedAt: string; // ISO
}

export interface RefrigeratorItemListResponse {
  items: RefrigeratorItem[];
  totalCount: number;
  expiringCount: number;
  expiredCount: number;
}

export interface CreateRefrigeratorItemRequest {
  name: string;
  quantity?: number;
  unit?: string;
  expirationDate?: string; // yyyy-MM-dd
  memo?: string;
}

export interface UpdateRefrigeratorItemRequest {
  quantity?: number;
  unit?: string;
  expirationDate?: string; // yyyy-MM-dd
  memo?: string;
}
