import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listCategories, type Category } from '@/apis/categories.api';
import { createPost, type UpsertPostDto } from '@/apis/boards.api';
import { useToast } from '@/contexts/ToastContext';

// 간단 텍스트 에디터(설명 + 이미지 URL 삽입)
function StepsEditor({
  steps,
  setSteps,
}: {
  steps: { description: string; imageUrl?: string }[];
  setSteps: (s: { description: string; imageUrl?: string }[]) => void;
}) {
  const move = (idx: number, dir: -1 | 1) => {
    const j = idx + dir;
    if (j < 0 || j >= steps.length) return;
    const next = steps.slice();
    const current = next[idx];
    const target = next[j];
    if (!current || !target) return;
    next[idx] = target;
    next[j] = current;
    setSteps(next);
  };
  const remove = (idx: number) => setSteps(steps.filter((_, i) => i !== idx));
  const add = () => setSteps([...steps, { description: '', imageUrl: '' }]);
  const edit = (idx: number, field: 'description' | 'imageUrl', val: string) => {
    const next = steps.slice();
    const prev = next[idx];
    if (!prev) return;
    next[idx] = {
      description: field === 'description' ? val : prev.description,
      imageUrl: field === 'imageUrl' ? val : prev.imageUrl,
    };
    setSteps(next);
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
            onChange={(e) => edit(i, 'description', e.target.value)}
            placeholder="단계 설명"
            className="w-full border rounded p-2 h-24"
          />
          <input
            value={s.imageUrl ?? ''}
            onChange={(e) => edit(i, 'imageUrl', e.target.value)}
            placeholder="과정 이미지 URL (선택)"
            className="w-full border rounded p-2"
          />
          <div className="flex gap-2">
            <button type="button" onClick={() => move(i, -1)} className="border px-2 py-1 rounded">
              ↑
            </button>
            <button type="button" onClick={() => move(i, 1)} className="border px-2 py-1 rounded">
              ↓
            </button>
            <button type="button" onClick={() => remove(i)} className="border px-2 py-1 rounded text-red-600">
              - 삭제
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}

function IngredientsEditor({
  ingredients,
  setIngredients,
}: {
  ingredients: { name: string; amount?: string }[];
  setIngredients: (v: { name: string; amount?: string }[]) => void;
}) {
  const move = (idx: number, dir: -1 | 1) => {
    const j = idx + dir;
    if (j < 0 || j >= ingredients.length) return;
    const next = ingredients.slice();
    const current = next[idx];
    const target = next[j];
    if (!current || !target) return;
    next[idx] = target;
    next[j] = current;
    setIngredients(next);
  };
  const remove = (idx: number) => setIngredients(ingredients.filter((_, i) => i !== idx));
  const add = () => setIngredients([...ingredients, { name: '', amount: '' }]);
  const edit = (idx: number, field: 'name' | 'amount', val: string) => {
    const next = ingredients.slice();
    const prev = next[idx];
    if (!prev) return;
    next[idx] = {
      name: field === 'name' ? val : prev.name,
      amount: field === 'amount' ? val : prev.amount,
    };
    setIngredients(next);
  };
  return (
    <div className="space-y-2">
      <div className="flex justify-between items-center">
        <h3 className="font-semibold">재료</h3>
        <button type="button" onClick={add} className="border px-2 py-1 rounded">
          + 재료 추가
        </button>
      </div>
      {ingredients.map((ing, i) => (
        <div key={i} className="grid grid-cols-12 gap-2 items-center">
          <input
            value={ing.name}
            onChange={(e) => edit(i, 'name', e.target.value)}
            placeholder="재료명"
            className="border rounded p-2 col-span-5"
          />
          <input
            value={ing.amount ?? ''}
            onChange={(e) => edit(i, 'amount', e.target.value)}
            placeholder="양 (예: 300g, 1개)"
            className="border rounded p-2 col-span-5"
          />
          <div className="col-span-2 flex gap-1">
            <button type="button" onClick={() => move(i, -1)} className="border px-2 py-1 rounded">
              ↑
            </button>
            <button type="button" onClick={() => move(i, 1)} className="border px-2 py-1 rounded">
              ↓
            </button>
            <button type="button" onClick={() => remove(i)} className="border px-2 py-1 rounded text-red-600">
              - 삭제
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}

export default function RecipeCreatePage() {
  const nav = useNavigate();
  const { show } = useToast();

  const [categories, setCategories] = useState<Category[]>([]);
  const [categoryId, setCategoryId] = useState<number>(0);

  // 개요
  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [thumbnail, setThumbnail] = useState(''); // 이미지 업로드 모듈 없음 → URL 입력으로 대체
  const [dietType, setDietType] = useState<string>('');

  // 기본 정보
  const [cookTimeInMinutes, setCookTime] = useState<number | ''>('');
  const [servings, setServings] = useState<number | ''>('');
  const [difficulty, setDifficulty] = useState<string>('');

  // 동적 리스트
  const [ingredients, setIngredients] = useState<{ name: string; amount?: string }[]>([{ name: '', amount: '' }]);
  const [steps, setSteps] = useState<{ description: string; imageUrl?: string }[]>([{ description: '', imageUrl: '' }]);

  // 검증 에러 상태
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    (async () => {
      const list = await listCategories();
      // 레시피용 카테고리는 서버 정책에 따라 RECIPE 타입 또는 특정 ID
      const recipeCats = list.filter((c) => c.type === 'RECIPE');
      setCategories(recipeCats.length ? recipeCats : list);
      const first = (recipeCats.length ? recipeCats : list)[0];
      if (first) setCategoryId(first.id);
    })();
  }, []);

  const validate = useMemo(() => {
    return () => {
      const e: Record<string, string> = {};
      const t = title.trim();
      if (!t) e.title = '레시피 제목을 입력해주세요.';
      else if (t.length < 5) e.title = '제목은 5자 이상 입력해주세요.';
      else if (t.length > 50) e.title = '제목은 50자 이하로 입력해주세요.';

      if (!thumbnail.trim()) e.thumbnail = '대표 이미지를 입력(업로드)해주세요.';
      if (!ingredients.length || !ingredients.some((i) => i.name.trim()))
        e.ingredients = '재료를 최소 1개 이상 입력해주세요.';
      if (!steps.length || !steps.some((s) => s.description.trim()))
        e.steps = '조리 단계를 최소 1개 이상 입력해주세요.';
      if (summary.length > 100) e.summary = '요약은 100자 이하로 입력해주세요.';

      setErrors(e);
      return Object.keys(e).length === 0;
    };
  }, [title, thumbnail, ingredients, steps, summary]);

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
    if (!validate()) return;
    try {
      const content = buildContentHtml();
      const dto: UpsertPostDto = {
        title: title.trim(),
        content,
        categoryId,
        isRecipe: true,
        status: 'PUBLISHED',
        dietType: (dietType || undefined) as any,
        cookTimeInMinutes: typeof cookTimeInMinutes === 'number' ? cookTimeInMinutes : undefined,
        servings: typeof servings === 'number' ? servings : undefined,
        difficulty: (difficulty || undefined) as any,
        ingredients: ingredients
          .filter((i) => i.name.trim())
          .map((i) => ({
            name: i.name.trim(),
            // amount를 quantity+unit으로 쪼갤 수 없으므로 단일 필드 → memo에 저장
            memo: i.amount?.trim() ?? undefined,
          })),
      };

      const newId = await createPost(dto);
      show('레시피가 성공적으로 등록되었습니다.', { type: 'success' });
      // 상세로 이동: /boards/{id} (레시피는 게시글 상세 재사용)
      if (typeof newId === 'number') nav(`/boards/${newId}`);
      else nav('/boards');
    } catch (err: any) {
      if (err?.response?.status === 401) {
        alert('로그인이 필요한 서비스입니다.');
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
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="border p-2 w-full"
            maxLength={50}
          />
          {errors.title && <p className="text-sm text-red-600 mt-1">{errors.title}</p>}
        </label>
        <label className="block">
          <div className="text-sm font-medium">레시피 요약 (선택, 100자 이하)</div>
          <textarea
            value={summary}
            onChange={(e) => setSummary(e.target.value)}
            className="border p-2 w-full h-20"
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
      <section>
        <IngredientsEditor ingredients={ingredients} setIngredients={setIngredients} />
        {errors.ingredients && <p className="text-sm text-red-600 mt-1">{errors.ingredients}</p>}
      </section>

      {/* 조리 순서 */}
      <section>
        <StepsEditor steps={steps} setSteps={setSteps} />
        {errors.steps && <p className="text-sm text-red-600 mt-1">{errors.steps}</p>}
      </section>

      {/* 카테고리 선택 */}
      <section className="space-y-2">
        <label className="block">
          <div className="text-sm font-medium">레시피 카테고리</div>
          <select
            value={categoryId}
            onChange={(e) => setCategoryId(Number(e.target.value))}
            className="border p-2 w-full"
          >
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
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
