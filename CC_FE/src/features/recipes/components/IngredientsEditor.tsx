// src/features/recipes/components/IngredientsEditor.tsx

export type IngredientItem = {
  name: string;
  quantity?: number | null;
  unit?: string | null;
  memo?: string | null;
};

export function IngredientsEditor({
  items,
  onChange,
}: {
  items: IngredientItem[];
  onChange: (/* eslint-disable-line no-unused-vars */ next: IngredientItem[]) => void;
}) {
  const move = (idx: number, dir: -1 | 1) => {
    const j = idx + dir;
    if (j < 0 || j >= items.length) return;
    const next = items.slice();
    const a = next[idx];
    const b = next[j];
    if (!a || !b) return;
    next[idx] = b;
    next[j] = a;
    onChange(next);
  };
  const remove = (idx: number) => onChange(items.filter((_, i) => i !== idx));
  const add = () => onChange([...items, { name: '', quantity: null, unit: null, memo: '' }]);
  const edit = (idx: number, patch: Partial<IngredientItem>) => {
    const next = items.slice();
    const prev = next[idx];
    if (!prev) return;
    next[idx] = { ...prev, ...patch };
    onChange(next);
  };

  return (
    <div className="space-y-3">
      <div className="flex justify-between items-center">
        <h3 className="font-semibold text-lg">재료</h3>
        <button
          type="button"
          onClick={add}
          className="px-3 py-1.5 text-sm bg-blue-50 text-blue-600 rounded hover:bg-blue-100 transition-colors"
        >
          + 재료 추가
        </button>
      </div>

      {items.length === 0 && (
        <div className="text-center py-4 text-gray-500 bg-gray-50 rounded border border-dashed">
          등록된 재료가 없습니다. 재료를 추가해주세요.
        </div>
      )}

      <div className="space-y-2">
        {items.map((ing, i) => (
          <div key={i} className="flex gap-2 items-start group">
            <div className="grid grid-cols-12 gap-2 flex-1">
              <div className="col-span-4">
                <input
                  value={ing.name}
                  onChange={(e) => edit(i, { name: e.target.value })}
                  placeholder="재료명 (예: 돼지고기)"
                  className="w-full border rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                />
              </div>
              <div className="col-span-2">
                <input
                  value={ing.quantity == null ? '' : String(ing.quantity)}
                  onChange={(e) => edit(i, { quantity: e.target.value === '' ? null : Number(e.target.value) })}
                  placeholder="수량"
                  type="number"
                  min={0}
                  step="0.1"
                  className="w-full border rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                />
              </div>
              <div className="col-span-2">
                <input
                  value={ing.unit ?? ''}
                  onChange={(e) => edit(i, { unit: e.target.value })}
                  placeholder="단위 (g, 근)"
                  className="w-full border rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                />
              </div>
              <div className="col-span-4">
                <input
                  value={ing.memo ?? ''}
                  onChange={(e) => edit(i, { memo: e.target.value })}
                  placeholder="메모 (예: 다진 것)"
                  className="w-full border rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                />
              </div>
            </div>

            <div className="flex flex-col gap-1 pt-1 opacity-50 group-hover:opacity-100 transition-opacity">
              <div className="flex gap-1">
                <button
                  type="button"
                  onClick={() => move(i, -1)}
                  disabled={i === 0}
                  className="p-1 text-gray-500 hover:text-blue-600 disabled:opacity-30"
                  title="위로 이동"
                >
                  ↑
                </button>
                <button
                  type="button"
                  onClick={() => move(i, 1)}
                  disabled={i === items.length - 1}
                  className="p-1 text-gray-500 hover:text-blue-600 disabled:opacity-30"
                  title="아래로 이동"
                >
                  ↓
                </button>
              </div>
              <button
                type="button"
                onClick={() => remove(i)}
                className="p-1 text-red-400 hover:text-red-600"
                title="삭제"
              >
                ✕
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
