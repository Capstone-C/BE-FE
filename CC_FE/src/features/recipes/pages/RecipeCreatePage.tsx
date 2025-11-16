import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listCategories } from '@/apis/categories.api';
import { createPost, type UpsertPostDto } from '@/apis/boards.api';
import { useToast } from '@/contexts/ToastContext';
import { IngredientsEditor } from '@/features/recipes/components/IngredientsEditor';
import StepsEditor from '@/features/recipes/components/StepsEditor';

// 간단 텍스트 에디터(설명 + 이미지 URL 삽입)
// function StepsEditor({
//   steps,
//   setSteps,
// }: {
//   steps: { description: string; imageUrl?: string }[];
//   // eslint-disable-next-line no-unused-vars
//   setSteps: (steps: { description: string; imageUrl?: string }[]) => void;
// }) {
//   const move = (idx: number, dir: -1 | 1) => {
//     const j = idx + dir;
//     if (j < 0 || j >= steps.length) return;
//     const next = steps.slice();
//     const current = next[idx];
//     const target = next[j];
//     if (!current || !target) return;
//     next[idx] = target;
//     next[j] = current;
//     setSteps(next);
//   };
//   const remove = (idx: number) => setSteps(steps.filter((_, i) => i !== idx));
//   const add = () => setSteps([...steps, { description: '', imageUrl: '' }]);
//   const edit = (idx: number, field: 'description' | 'imageUrl', val: string) => {
//     const next = steps.slice();
//     const prev = next[idx];
//     if (!prev) return;
//     next[idx] = {
//       description: field === 'description' ? val : prev.description,
//       imageUrl: field === 'imageUrl' ? val : prev.imageUrl,
//     };
//     setSteps(next);
//   };
//   return (
//     <div className="space-y-2">
//       <div className="flex justify-between items-center">
//         <h3 className="font-semibold">조리 순서</h3>
//         <button type="button" onClick={add} className="border px-2 py-1 rounded">
//           + 단계 추가
//         </button>
//       </div>
//       {steps.map((s, i) => (
//         <div key={i} className="border rounded p-2 space-y-2">
//           <div className="text-sm text-gray-600">STEP {i + 1}</div>
//           <textarea
//             value={s.description}
//             onChange={(e) => edit(i, 'description', e.target.value)}
//             placeholder="단계 설명"
//             className="w-full border rounded p-2 h-24"
//           />
//           <input
//             value={s.imageUrl ?? ''}
//             onChange={(e) => edit(i, 'imageUrl', e.target.value)}
//             placeholder="과정 이미지 URL (선택)"
//             className="w-full border rounded p-2"
//           />
//           <div className="flex gap-2">
//             <button type="button" onClick={() => move(i, -1)} className="border px-2 py-1 rounded">
//               ↑
//             </button>
//             <button type="button" onClick={() => move(i, 1)} className="border px-2 py-1 rounded">
//               ↓
//             </button>
//             <button type="button" onClick={() => remove(i)} className="border px-2 py-1 rounded text-red-600">
//               - 삭제
//             </button>
//           </div>
//         </div>
//       ))}
//     </div>
//   );
// }

// function IngredientsEditor({
//   ingredients,
//   setIngredients,
// }: {
//   ingredients: { name: string; quantity?: number | null; unit?: string | null; memo?: string }[];
//   // eslint-disable-next-line no-unused-vars
//   setIngredients: (
//     ingredients: { name: string; quantity?: number | null; unit?: string | null; memo?: string }[],
//   ) => void;
// }) {
//   const move = (idx: number, dir: -1 | 1) => {
//     const j = idx + dir;
//     if (j < 0 || j >= ingredients.length) return;
//     const next = ingredients.slice();
//     const current = next[idx];
//     const target = next[j];
//     if (!current || !target) return;
//     next[idx] = target;
//     next[j] = current;
//     setIngredients(next);
//   };
//   const remove = (idx: number) => setIngredients(ingredients.filter((_, i) => i !== idx));
//   const add = () => setIngredients([...ingredients, { name: '', quantity: null, unit: null, memo: '' }]);
//   const edit = (idx: number, field: 'name' | 'memo', val: string) => {
//     const next = ingredients.slice();
//     const prev = next[idx];
//     if (!prev) return;
//     next[idx] = {
//       name: field === 'name' ? val : prev.name,
//       memo: field === 'memo' ? val : prev.memo,
//     };
//     setIngredients(next);
//   };
//   const editQuantity = (idx: number, val: string) => {
//     const quantity = val === '' ? null : Number(val);
//     const next = ingredients.slice();
//     const prev = next[idx];
//     if (!prev) return;
//     next[idx] = {
//       ...prev,
//       quantity,
//     };
//     setIngredients(next);
//   };
//   const editUnit = (idx: number, val: string) => {
//     const next = ingredients.slice();
//     const prev = next[idx];
//     if (!prev) return;
//     next[idx] = {
//       ...prev,
//       unit: val,
//     };
//     setIngredients(next);
//   };
//   return (
//     <div className="space-y-2">
//       <div className="flex justify-between items-center">
//         <h3 className="font-semibold">재료</h3>
//         <button type="button" onClick={add} className="border px-2 py-1 rounded">
//           + 재료 추가
//         </button>
//       </div>
//       {ingredients.map((ing, i) => (
//         <div key={i} className="grid grid-cols-12 gap-2 items-center">
//           <input
//             value={ing.name}
//             onChange={(e) => edit(i, 'name', e.target.value)}
//             placeholder="재료명"
//             className="border rounded p-2 col-span-3"
//           />
//           <input
//             value={ing.quantity == null ? '' : String(ing.quantity)}
//             onChange={(e) => editQuantity(i, e.target.value)}
//             placeholder="수량"
//             type="number"
//             min={0}
//             className="border rounded p-2 col-span-2"
//           />
//           <input
//             value={ing.unit ?? ''}
//             onChange={(e) => editUnit(i, e.target.value)}
//             placeholder="단위(g,개 등)"
//             className="border rounded p-2 col-span-2"
//           />
//           <input
//             value={ing.memo ?? ''}
//             onChange={(e) => edit(i, 'memo', e.target.value)}
//             placeholder="메모(예: 다진)"
//             className="border rounded p-2 col-span-3"
//           />
//           <div className="col-span-2 flex gap-1">
//             <button type="button" onClick={() => move(i, -1)} className="border px-2 py-1 rounded">
//               ↑
//             </button>
//             <button type="button" onClick={() => move(i, 1)} className="border px-2 py-1 rounded">
//               ↓
//             </button>
//             <button type="button" onClick={() => remove(i)} className="border px-2 py-1 rounded text-red-600">
//               - 삭제
//             </button>
//           </div>
//         </div>
//       ))}
//     </div>
//   );
// }

export default function RecipeCreatePage() {
  const nav = useNavigate();
  const { show } = useToast();

  const [categoryId, setCategoryId] = useState<number>(0);
  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [thumbnail, setThumbnail] = useState('');
  const [dietType, setDietType] = useState<string>('');
  const [cookTimeInMinutes, setCookTime] = useState<number | ''>('');
  const [servings, setServings] = useState<number | ''>('');
  const [difficulty, setDifficulty] = useState<string>('');
  const [ingredients, setIngredients] = useState<
    { name: string; quantity?: number | null; unit?: string | null; memo?: string | null }[]
  >([{ name: '', quantity: null, unit: null, memo: '' }]);
  const [steps, setSteps] = useState<{ description: string; imageUrl?: string }[]>([{ description: '', imageUrl: '' }]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    (async () => {
      const list = await listCategories();
      // 레시피용 카테고리는 서버 정책에 따라 RECIPE 타입 또는 특정 ID
      const recipeCats = list.filter((c) => c.type === 'RECIPE');
      const categories = recipeCats.length ? recipeCats : list;
      const first = categories[0];
      if (first) setCategoryId(first.id);
    })();
  }, []);

  const validate = useMemo(() => {
    return () => {
      const e: Record<string, string> = {};
      const t = title.trim();
      if (!t) e.title = '레시피 제목을 입력해주세요.';
      else if (t.length < 5) e.title = '제목은 5자 이상 입력해주세요.';
      else if (t.length > 100) e.title = '제목은 100자 이하로 입력해주세요.';

      // 대표 이미지 URL은 선택값으로 변경 (서버에 별도 전송되지 않음)
      // if (!thumbnail.trim()) e.thumbnail = '대표 이미지를 입력(업로드)해주세요.';
      if (!ingredients.length || !ingredients.some((i) => i.name.trim()))
        e.ingredients = '재료를 최소 1개 이상 입력해주세요.';
      else if (ingredients.some((i) => i.quantity != null && i.quantity! < 0))
        e.ingredients = '재료 수량은 음수가 될 수 없습니다.';
      if (!steps.length || !steps.some((s) => s.description.trim()))
        e.steps = '조리 단계를 최소 1개 이상 입력해주세요.';
      if (summary.length > 100) e.summary = '요약은 100자 이하로 입력해주세요.';

      // 본문(요약+단계)의 순수 텍스트 길이 검증: 10자 이상
      const plainTextLen = (summary + ' ' + steps.map((s) => s.description).join(' '))
        .replace(/<[^>]+>/g, '')
        .replace(/\s+/g, ' ')
        .trim().length;
      if (plainTextLen < 10)
        e.steps = e.steps ? e.steps + ' 본문은 10자 이상 입력해주세요.' : '본문은 10자 이상 입력해주세요.';

      setErrors(e);
      return { isValid: Object.keys(e).length === 0, errors: e };
    };
  }, [title, ingredients, steps, summary]);

  const buildContentHtml = () => {
    // 간단한 HTML 템플릿: 요약 + 단계별 설명/이미지
    const esc = (s: string) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    const parts: string[] = [];
    if (summary.trim()) parts.push(`<p>${esc(summary.trim())}</p>`);
    parts.push('<h3>조리 순서</h3>');
    steps
      .filter((s) => s.description.trim())
      .forEach((s, i) => {
        parts.push(`<h4>STEP ${i + 1}</h4>`);
        if (s.imageUrl) parts.push(`<p><img src="${esc(s.imageUrl)}" alt="step-${i + 1}" /></p>`);
        parts.push(`<p>${esc(s.description.trim())}</p>`);
      });
    return parts.join('\n');
  };

  const onSubmit = async () => {
    const { isValid, errors: nextErrors } = validate();
    if (!isValid) {
      // 검증 실패 시 사용자에게 어떤 필드가 잘못되었는지 알림
      const errorMessages = Object.values(nextErrors).filter(Boolean);
      if (errorMessages.length > 0) {
        show(`입력값을 확인해주세요: ${errorMessages.join(', ')}`, { type: 'error' });
        // 첫 번째 에러 필드로 스크롤
        const firstErrorField = Object.keys(nextErrors)[0];
        if (firstErrorField) {
          const element = document.querySelector(
            `[name="${firstErrorField}"], [data-field="${firstErrorField}"]`,
          ) as HTMLElement | null;
          if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'center' });
            if ('focus' in element) (element as HTMLElement).focus();
          }
        }
      }
      return;
    }
    if (!categoryId || categoryId <= 0) {
      show('카테고리를 불러오는 중입니다. 잠시 후 다시 시도해주세요.', { type: 'error' });
      return;
    }
    try {
      const content = buildContentHtml();
      const dto: UpsertPostDto = {
        title: title.trim(),
        content,
        categoryId,
        status: 'PUBLISHED',
        dietType: (dietType || undefined) as any,
        cookTimeInMinutes: typeof cookTimeInMinutes === 'number' ? cookTimeInMinutes : undefined,
        servings: typeof servings === 'number' ? servings : undefined,
        difficulty: (difficulty || undefined) as any,
        ingredients: ingredients
          .filter((i) => i.name.trim())
          .map((i) => ({
            name: i.name.trim(),
            quantity: i.quantity != null ? i.quantity : undefined,
            unit: i.unit ?? undefined,
            memo: i.memo?.trim() || undefined,
          })),
        isRecipe: true,
      };

      const newId = await createPost(dto);
      show('레시피가 성공적으로 등록되었습니다.', { type: 'success' });
      // 상세로 이동: /boards/{id} (레시피는 게시글 상세 재사용)
      if (typeof newId === 'number') nav(`/boards/${newId}`);
      else nav('/boards');
    } catch (err: any) {
      if (err?.response?.status === 401) {
        show('로그인이 필요한 서비스입니다.', { type: 'error' });
        nav('/login');
        return;
      }
      show(err?.response?.data?.message ?? '등록 중 오류가 발생했습니다.', { type: 'error' });
    }
  };

  return (
    <div className="p-6 space-y-5">
      <h1 className="text-2xl font-bold">레시피 작성</h1>

      {/* 개요 */}
      <section className="space-y-2">
        <label className="block">
          <div className="text-sm font-medium">레시피 제목</div>
          <input
            name="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className={`border p-2 w-full ${errors.title ? 'border-red-500' : ''}`}
            maxLength={100}
          />
          {errors.title && <p className="text-sm text-red-600 mt-1">{errors.title}</p>}
        </label>
        <label className="block">
          <div className="text-sm font-medium">레시피 요약 (선택, 100자 이하)</div>
          <textarea
            name="summary"
            value={summary}
            onChange={(e) => setSummary(e.target.value)}
            className={`border p-2 w-full h-20 ${errors.summary ? 'border-red-500' : ''}`}
            maxLength={100}
          />
          {errors.summary && <p className="text-sm text-red-600 mt-1">{errors.summary}</p>}
        </label>
        <label className="block">
          <div className="text-sm font-medium">대표 이미지 URL (5MB 이하 jpg/png 업로드 모듈은 추후 연동)</div>
          <input
            value={thumbnail}
            onChange={(e) => setThumbnail(e.target.value)}
            className="border p-2 w-full"
            placeholder="https://..."
          />
          {errors.thumbnail && <p className="text-sm text-red-600 mt-1">{errors.thumbnail}</p>}
        </label>
        <div className="grid grid-cols-3 gap-2">
          <label className="block">
            <div className="text-sm font-medium">식단 타입 (선택)</div>
            <select value={dietType} onChange={(e) => setDietType(e.target.value)} className="border p-2 w-full">
              <option value="">선택 안함</option>
              <option value="VEGAN">VEGAN</option>
              <option value="VEGETARIAN">VEGETARIAN</option>
              <option value="KETO">KETO</option>
              <option value="PALEO">PALEO</option>
              <option value="MEDITERRANEAN">MEDITERRANEAN</option>
              <option value="LOW_CARB">LOW_CARB</option>
              <option value="HIGH_PROTEIN">HIGH_PROTEIN</option>
              <option value="GENERAL">GENERAL</option>
            </select>
          </label>
          <label className="block">
            <div className="text-sm font-medium">조리 시간(분)</div>
            <input
              type="number"
              value={cookTimeInMinutes}
              onChange={(e) => setCookTime(e.target.value ? Number(e.target.value) : '')}
              className="border p-2 w-full"
              min={0}
            />
          </label>
          <label className="block">
            <div className="text-sm font-medium">분량(인분)</div>
            <input
              type="number"
              value={servings}
              onChange={(e) => setServings(e.target.value ? Number(e.target.value) : '')}
              className="border p-2 w-full"
              min={0}
            />
          </label>
          <label className="block col-span-3">
            <div className="text-sm font-medium">난이도</div>
            <select value={difficulty} onChange={(e) => setDifficulty(e.target.value)} className="border p-2 w-full">
              <option value="">선택 안함</option>
              <option value="VERY_LOW">매우 쉬움</option>
              <option value="LOW">쉬움</option>
              <option value="MEDIUM">중간</option>
              <option value="HIGH">어려움</option>
              <option value="VERY_HIGH">매우 어려움</option>
            </select>
          </label>
        </div>
      </section>

      {/* 재료 */}
      <section data-field="ingredients">
        <IngredientsEditor items={ingredients} onChange={setIngredients} />
        {errors.ingredients && <p className="text-sm text-red-600 mt-1 font-semibold">{errors.ingredients}</p>}
      </section>

      {/* 조리 순서 */}
      <section data-field="steps">
        <StepsEditor steps={steps} onChange={setSteps} />
        {errors.steps && <p className="text-sm text-red-600 mt-1 font-semibold">{errors.steps}</p>}
      </section>

      <div className="flex gap-2">
        <button onClick={onSubmit} className="border px-3 py-2 rounded bg-blue-600 text-white">
          레시피 등록
        </button>
        <button onClick={() => nav(-1)} className="border px-3 py-2 rounded">
          취소
        </button>
      </div>
    </div>
  );
}
