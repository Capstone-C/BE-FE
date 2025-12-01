// src/features/refrigerator/pages/RecommendationsPage.tsx
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getRecommendations } from '@/apis/refrigerator.api';
import type { RecommendedRecipe } from '@/types/refrigerator';
import { Link } from 'react-router-dom';

export default function RecommendationsPage() {
  const [limit, setLimit] = useState(10);
  const { data, isPending, isError, refetch, isFetching } = useQuery({
    queryKey: ['recommendations', limit],
    queryFn: () => getRecommendations(limit),
  });

  const onRetry = () => void refetch();

  const recommendations: RecommendedRecipe[] = data?.recommendations ?? [];

  return (
    <div className="max-w-6xl mx-auto p-8 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">냉장고 기반 레시피 추천</h1>
        <div className="flex items-center gap-2">
          <label className="text-sm text-gray-600" htmlFor="limit">
            표시 개수:
          </label>
          <select
            id="limit"
            value={limit}
            onChange={(e) => setLimit(Number(e.target.value))}
            className="border rounded px-2 py-1 text-sm"
          >
            {[5, 10, 15, 20].map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>
          <button
            onClick={() => refetch()}
            disabled={isFetching}
            className="px-3 py-1 rounded border text-sm disabled:opacity-50"
          >
            {isFetching ? '새로고침…' : '새로고침'}
          </button>
        </div>
      </div>

      {isPending && <div className="p-4">불러오는 중…</div>}
      {isError && (
        <div className="p-4 bg-red-50 border border-red-200 rounded space-y-2">
          <p className="text-sm text-red-700">추천 정보를 불러오는 중 오류가 발생했습니다.</p>
          <button onClick={onRetry} className="px-3 py-1 border rounded text-sm">
            다시 시도
          </button>
        </div>
      )}

      {!isPending && !isError && recommendations.length === 0 && (
        <div className="p-6 bg-gray-50 border rounded text-gray-600 text-sm">
          현재 냉장고 재료와 매칭되는 레시피가 없습니다. 재료를 더 추가하거나 다른 레시피를 등록해보세요.
        </div>
      )}

      <ul className="grid md:grid-cols-2 lg:grid-cols-3 gap-5">
        {recommendations.map((r) => {
          return (
            <li key={r.recipeId} className="border rounded shadow-sm bg-white flex flex-col overflow-hidden">
              {r.imageUrl && (
                <div className="h-40 bg-gray-100 overflow-hidden">
                  <img src={r.imageUrl} alt={r.recipeName} className="w-full h-full object-cover" />
                </div>
              )}
              <div className="p-4 space-y-2 flex-1 flex flex-col">
                <h2 className="text-lg font-semibold line-clamp-2" title={r.recipeName}>
                  {r.recipeName}
                </h2>
                <p className="text-xs text-gray-600">
                  매칭률: <span className="font-semibold text-blue-600">{r.matchRate}%</span>
                </p>
                <p className="text-xs text-gray-600">
                  보유: {r.matchedIngredientsCount}/{r.totalIngredientsCount}
                </p>
                <div className="text-xs flex flex-wrap gap-2">
                  {r.difficulty && <span className="px-2 py-1 bg-gray-100 rounded">난이도 {r.difficulty}</span>}
                  {r.cookTime != null && <span className="px-2 py-1 bg-gray-100 rounded">{r.cookTime}분</span>}
                  {r.servings != null && <span className="px-2 py-1 bg-gray-100 rounded">{r.servings}인분</span>}
                </div>
                {r.description && (
                  <p className="text-sm text-gray-700 line-clamp-3" title={r.description}>
                    {r.description}
                  </p>
                )}
                <div className="mt-auto flex flex-col gap-2">
                  <Link
                    to={`/boards/${r.recipeId}`}
                    className="text-sm px-3 py-2 rounded bg-blue-600 text-white text-center hover:bg-blue-700"
                  >
                    상세 보기
                  </Link>
                </div>
              </div>
              {r.missingIngredients.length > 0 && (
                <details className="border-t text-xs">
                  <summary className="cursor-pointer px-4 py-2 bg-gray-50">부족 재료 목록</summary>
                  <ul className="p-4 space-y-1">
                    {r.missingIngredients.map((m, i) => (
                      <li key={i} className={m.isRequired ? 'text-red-600' : 'text-gray-600'}>
                        {m.name}
                        {m.amount ? ` (${m.amount})` : ''} {m.isRequired ? '(필수)' : ''}
                      </li>
                    ))}
                  </ul>
                </details>
              )}
            </li>
          );
        })}
      </ul>
    </div>
  );
}
