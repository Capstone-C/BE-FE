// src/apis/diary.api.ts
import { authClient } from '@/apis/client';

export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK';

export interface MonthlyDiaryEntrySummary {
  date: string; // 'YYYY-MM-DD'
  hasBreakfast: boolean;
  hasLunch: boolean;
  hasDinner: boolean;
  hasSnack?: boolean;
  thumbnailUrl?: string | null;
}

export interface MonthlyDiaryResponse {
  year: number;
  month: number; // 1-12
  dailyEntries: MonthlyDiaryEntrySummary[]; // 백엔드 테스트 명세와 일치
}

export interface DiaryEntryResponse {
  id: number;
  date: string; // 'YYYY-MM-DD'
  mealType: MealType;
  content: string;
  imageUrl?: string | null;
  recipeId?: number | null;
  // Added: optional recipe title if backend enriches response
  recipeTitle?: string | null;
}

export interface CreateDiaryRequest {
  date: string; // 'YYYY-MM-DD'
  mealType: MealType;
  content: string;
  imageUrl?: string | null;
  recipeId?: number | null;
}

export interface UpdateDiaryRequest {
  mealType?: MealType;
  content?: string;
  imageUrl?: string | null;
  recipeId?: number | null;
}

export const getMonthlyDiary = async (year: number, month: number): Promise<MonthlyDiaryResponse> => {
  const { data } = await authClient.get<MonthlyDiaryResponse>('/api/v1/diary', {
    params: { year, month },
  });
  return data;
};

export const getDiaryByDate = async (date: string): Promise<DiaryEntryResponse[]> => {
  const { data } = await authClient.get<DiaryEntryResponse[]>('/api/v1/diary/daily', {
    params: { date },
  });
  return data;
};

export const createDiary = async (payload: CreateDiaryRequest): Promise<DiaryEntryResponse> => {
  const { data } = await authClient.post<DiaryEntryResponse>('/api/v1/diary', payload);
  return data;
};

export const updateDiary = async (id: number, payload: UpdateDiaryRequest): Promise<DiaryEntryResponse> => {
  const { data } = await authClient.put<DiaryEntryResponse>(`/api/v1/diary/${id}`, payload);
  return data;
};

export const deleteDiary = async (id: number): Promise<void> => {
  await authClient.delete(`/api/v1/diary/${id}`);
};
