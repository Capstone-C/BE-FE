import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as api from '@/apis/refrigerator.api';
import { authClient } from '@/apis/client';

// Mock axios post/get
vi.mock('@/apis/client', () => {
  const get = vi.fn();
  const post = vi.fn();
  return {
    authClient: { get, post },
    publicClient: { get },
  };
});

describe('refrigerator.api recommendation & deduction helpers', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('getRecommendations clamps invalid limit', async () => {
    (authClient.get as any).mockResolvedValue({ data: { recommendations: [], totalCount: 0 } });
    await api.getRecommendations(-5);
    expect(authClient.get).toHaveBeenCalledWith('/api/v1/refrigerator/recommendations', { params: { limit: 10 } });
    await api.getRecommendations(999);
    expect(authClient.get).toHaveBeenCalledWith('/api/v1/refrigerator/recommendations', { params: { limit: 10 } });
    await api.getRecommendations(15);
    expect(authClient.get).toHaveBeenCalledWith('/api/v1/refrigerator/recommendations', { params: { limit: 15 } });
  });

  it('getDeductPreview passes recipeId as query param', async () => {
    (authClient.get as any).mockResolvedValue({
      data: { recipeId: 1, recipeName: 'R', ingredients: [], canProceed: true, warnings: [] },
    });
    await api.getDeductPreview(1);
    expect(authClient.get).toHaveBeenCalledWith('/api/v1/refrigerator/deduct-preview', { params: { recipeId: 1 } });
  });

  it('postDeduct sends ignoreWarnings default false', async () => {
    (authClient.post as any).mockResolvedValue({
      data: {
        recipeId: 1,
        recipeName: 'R',
        successCount: 0,
        failedCount: 0,
        deductedIngredients: [],
        failedIngredients: [],
      },
    });
    await api.postDeduct({ recipeId: 1 });
    expect(authClient.post).toHaveBeenCalledWith(
      '/api/v1/refrigerator/deduct',
      { recipeId: 1, ignoreWarnings: false },
      { headers: { 'Content-Type': 'application/json' } },
    );
  });

  it('postDeduct forwards provided ignoreWarnings true', async () => {
    (authClient.post as any).mockResolvedValue({
      data: {
        recipeId: 1,
        recipeName: 'R',
        successCount: 0,
        failedCount: 0,
        deductedIngredients: [],
        failedIngredients: [],
      },
    });
    await api.postDeduct({ recipeId: 1, ignoreWarnings: true });
    expect(authClient.post).toHaveBeenCalledWith(
      '/api/v1/refrigerator/deduct',
      { recipeId: 1, ignoreWarnings: true },
      { headers: { 'Content-Type': 'application/json' } },
    );
  });
});
