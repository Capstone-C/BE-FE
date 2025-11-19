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

// Bulk create (REF-03/04)
export interface BulkCreateRequest {
  items: CreateRefrigeratorItemRequest[];
}

export interface BulkCreateResponse {
  successCount: number;
  failCount: number;
  failedItems: string[];
  addedItems: RefrigeratorItem[];
}

// Receipt OCR purchase-history scan (REF-04 backend endpoint)
export interface ScanPurchaseHistoryResponse {
  items: PurchasedItem[];
}

export interface PurchasedItem {
  name: string;
  quantity?: number; // default 1
  unit?: string | null; // e.g., 개, 팩, 봉, 병, g, ml, L
}

// 추천 레시피 (REF-07) 및 재료 차감 (REF-08) 기능 타입 추가
export interface RecommendationResponse {
  recommendations: RecommendedRecipe[];
  totalCount: number;
}

export interface RecommendedRecipe {
  recipeId: number;
  recipeName: string;
  description: string | null;
  cookTime: number | null; // 분
  servings: number | null; // 인분
  difficulty: string | null; // VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH 등
  imageUrl: string | null;
  matchRate: number; // 0~100 (소수 1자리)
  matchedIngredients: string[];
  missingIngredients: MissingIngredient[];
  totalIngredientsCount: number;
  matchedIngredientsCount: number;
}

export interface MissingIngredient {
  name: string;
  amount: string | null; // 예: '200g'
  isRequired: boolean | null; // 필수 여부 (Boolean 객체 가능성 대비 null 허용)
}

// 재료 차감 미리보기 (REF-08)
export type DeductionStatus = 'OK' | 'INSUFFICIENT' | 'NOT_FOUND';

export interface IngredientDeductionStatus {
  name: string;
  requiredAmount: string | null; // 레시피에 정의된 필요량(문자열)
  currentAmount: string | null; // 냉장고 현재량 (예: '2개', '500g')
  currentQuantity: number | null; // 정수 수량 (차감 단위)
  status: DeductionStatus;
  isRequired: boolean | null;
  message: string | null; // 경고/상태 메시지
}

export interface DeductPreviewResponse {
  recipeId: number;
  recipeName: string;
  ingredients: IngredientDeductionStatus[];
  canProceed: boolean;
  warnings: string[];
}

export interface DeductRequest {
  recipeId: number;
  ignoreWarnings?: boolean; // 기본 false
}

export interface DeductedIngredient {
  name: string;
  previousQuantity: number | null;
  newQuantity: number | null;
}

export interface DeductResponse {
  recipeId: number;
  recipeName: string;
  successCount: number;
  failedCount: number;
  deductedIngredients: DeductedIngredient[];
  failedIngredients: string[];
}
