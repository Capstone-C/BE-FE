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
    <div className="space-y-2">
      <div className="flex justify-between items-center">
        <h3 className="font-semibold">재료</h3>
        <button type="button" onClick={add} className="border px-2 py-1 rounded">
          + 재료 추가
        </button>
      </div>
      {items.map((ing, i) => (
        <div key={i} className="grid grid-cols-12 gap-2 items-center">
          <input
            value={ing.name}
            onChange={(e) => edit(i, { name: e.target.value })}
            placeholder="재료명"
            className="border rounded p-2 col-span-3"
          />
          <input
            value={ing.quantity == null ? '' : String(ing.quantity)}
            onChange={(e) => edit(i, { quantity: e.target.value === '' ? null : Number(e.target.value) })}
            placeholder="수량"
            type="number"
            min={0}
            className="border rounded p-2 col-span-2"
          />
          <input
            value={ing.unit ?? ''}
            onChange={(e) => edit(i, { unit: e.target.value })}
            placeholder="단위(g,개 등)"
            className="border rounded p-2 col-span-2"
          />
          <input
            value={ing.memo ?? ''}
            onChange={(e) => edit(i, { memo: e.target.value })}
            placeholder="메모(예: 다진)"
            className="border rounded p-2 col-span-3"
          />
          <div className="col-span-2 flex gap-1">
            <button
              type="button"
              onClick={() => move(i, -1)}
              className="border px-2 py-1 rounded"
              aria-label="위로 이동"
            >
              ↑
            </button>
            <button
              type="button"
              onClick={() => move(i, 1)}
              className="border px-2 py-1 rounded"
              aria-label="아래로 이동"
            >
              ↓
            </button>
            <button
              type="button"
              onClick={() => remove(i)}
              className="border px-2 py-1 rounded text-red-600"
              aria-label="재료 삭제"
            >
              - 삭제
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}
