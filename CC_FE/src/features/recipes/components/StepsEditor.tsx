// src/features/recipes/components/StepsEditor.tsx

export type StepItem = { description: string; imageUrl?: string };

export function StepsEditor({
  steps,
  onChange,
}: {
  steps: StepItem[];
  onChange: (/* eslint-disable-line no-unused-vars */ next: StepItem[]) => void;
}) {
  const move = (idx: number, dir: -1 | 1) => {
    const j = idx + dir;
    if (j < 0 || j >= steps.length) return;
    const next = steps.slice();
    const a = next[idx];
    const b = next[j];
    if (!a || !b) return;
    next[idx] = b;
    next[j] = a;
    onChange(next);
  };
  const remove = (idx: number) => onChange(steps.filter((_, i) => i !== idx));
  const add = () => onChange([...steps, { description: '', imageUrl: '' }]);
  const edit = (idx: number, patch: Partial<StepItem>) => {
    const next = steps.slice();
    const prev = next[idx];
    if (!prev) return;
    next[idx] = { ...prev, ...patch };
    onChange(next);
  };

  return (
    <div className="space-y-2">
      <div className="flex justify-between items-center">
        <h3 className="font-semibold">조리 순서</h3>
        <button type="button" onClick={add} className="border px-2 py-1 rounded">
          + 단계 추가
        </button>
      </div>
      {steps.map((s, i) => (
        <div key={i} className="border rounded p-2 space-y-2">
          <div className="text-sm text-gray-600">STEP {i + 1}</div>
          <textarea
            value={s.description}
            onChange={(e) => edit(i, { description: e.target.value })}
            placeholder="단계 설명"
            className="w-full border rounded p-2 h-24"
          />
          <input
            value={s.imageUrl ?? ''}
            onChange={(e) => edit(i, { imageUrl: e.target.value })}
            placeholder="과정 이미지 URL (선택)"
            className="w-full border rounded p-2"
          />
          <div className="flex gap-2">
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
              aria-label="단계 삭제"
            >
              - 삭제
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}

export default StepsEditor;
