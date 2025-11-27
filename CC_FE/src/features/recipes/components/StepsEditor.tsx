// src/features/recipes/components/StepsEditor.tsx
import type { RecipeStep } from '@/features/recipes/utils/recipeContent';
import ImageUploader from '@/components/ui/ImageUploader';

export function StepsEditor({
  steps,
  onChange,
}: {
  steps: RecipeStep[];
  onChange: (/* eslint-disable-line no-unused-vars */ next: RecipeStep[]) => void;
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
  const edit = (idx: number, patch: Partial<RecipeStep>) => {
    const next = steps.slice();
    const prev = next[idx];
    if (!prev) return;
    next[idx] = { ...prev, ...patch };
    onChange(next);
  };

  return (
    <div className="space-y-3">
      <div className="flex justify-between items-center">
        <h3 className="font-semibold text-lg">조리 순서</h3>
        <button
          type="button"
          onClick={add}
          className="px-3 py-1.5 text-sm bg-blue-50 text-blue-600 rounded hover:bg-blue-100 transition-colors"
        >
          + 단계 추가
        </button>
      </div>

      {steps.length === 0 && (
        <div className="text-center py-4 text-gray-500 bg-gray-50 rounded border border-dashed">
          등록된 조리 순서가 없습니다. 단계를 추가해주세요.
        </div>
      )}

      <div className="space-y-4">
        {steps.map((s, i) => (
          <div key={i} className="border rounded-lg p-4 space-y-3 bg-white shadow-sm relative group">
            <div className="flex justify-between items-center">
              <div className="font-medium text-blue-600">STEP {i + 1}</div>
              <div className="flex gap-1">
                <button
                  type="button"
                  onClick={() => move(i, -1)}
                  disabled={i === 0}
                  className="p-1 text-gray-400 hover:text-blue-600 disabled:opacity-30"
                  title="위로 이동"
                >
                  ↑
                </button>
                <button
                  type="button"
                  onClick={() => move(i, 1)}
                  disabled={i === steps.length - 1}
                  className="p-1 text-gray-400 hover:text-blue-600 disabled:opacity-30"
                  title="아래로 이동"
                >
                  ↓
                </button>
                <button
                  type="button"
                  onClick={() => remove(i)}
                  className="p-1 text-red-400 hover:text-red-600 ml-2"
                  title="삭제"
                >
                  ✕
                </button>
              </div>
            </div>

            <div className="flex gap-4 items-start">
              <div className="flex-1 space-y-2">
                <textarea
                  value={s.description}
                  onChange={(e) => edit(i, { description: e.target.value })}
                  placeholder={`STEP ${i + 1} 설명을 입력해주세요.`}
                  className="w-full border rounded px-3 py-2 h-24 resize-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                />
                <div className="w-40 flex-shrink-0">
                  <ImageUploader
                    value={s.imageUrl}
                    onChange={(url) => edit(i, { imageUrl: url })}
                    placeholder="단계 사진"
                    className="h-32" // ImageUploader 내부 높이 조절 필요 시 className으로 전달하거나 스타일 조정
                  />
                </div>
              </div>

              {/* Image Preview */}
              <div className="w-32 h-32 flex-shrink-0 bg-gray-100 rounded border flex items-center justify-center overflow-hidden">
                {s.imageUrl ? (
                  <img
                    src={s.imageUrl}
                    alt={`Step ${i + 1}`}
                    className="w-full h-full object-cover"
                    onError={(e) => {
                      (e.target as HTMLImageElement).src = ''; // Clear broken link
                      (e.target as HTMLImageElement).style.display = 'none';
                      // Show placeholder instead?
                    }}
                  />
                ) : (
                  <span className="text-xs text-gray-400 text-center px-2">이미지 미리보기</span>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default StepsEditor;
