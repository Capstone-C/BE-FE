// src/apis/categories.api.ts
import { publicClient } from '@/apis/client';

export type CategoryType = 'VEGAN' | 'CARNIVORE' | 'RECIPE' | 'FREE' | 'QA';

export type Category = {
  id: number;
  name: string;
  type: CategoryType;
  parentId: number | null;
};

export async function listCategories() {
  const { data } = await publicClient.get<Category[]>('/api/v1/categories');
  return data;
}
