import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { usePost } from '@/features/boards/hooks/usePosts';
import { updatePost, type UpsertPostDto } from '@/apis/boards.api';
import { useToast } from '@/contexts/ToastContext';
import type { Post } from '@/types/post';

// 재사용: IngredientsEditor와 StepsEditor는 Create 페이지와 유사하지만 여기서는 내부에서 정의
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

// 아주 단순한 HTML -> 요약/단계 추출 (정확한 round-trip은 어려우므로 보조용)
function extractFromContentHtml(content: string) {
  const div = document.createElement('div');
  div.innerHTML = content ?? '';
  let summary = '';
  const steps: { description: string; imageUrl?: string }[] = [];

  const ps = Array.from(div.querySelectorAll('p')) as HTMLParagraphElement[];
  if (ps.length > 0 && ps[0]) {
    summary = ps[0].innerText ?? '';
  }

  // h4 + p 구조를 찾아 단계로 사용
  const h4s = Array.from(div.querySelectorAll('h4')) as HTMLHeadingElement[];
  h4s.forEach((h4) => {
    let desc = '';
    let img: string | undefined;
    let next = h4.nextElementSibling;
    while (next && next.tagName !== 'H4') {
      if (next.tagName === 'P') {
        const imgEl = next.querySelector('img') as HTMLImageElement | null;
        if (imgEl && imgEl.src) {
          img = imgEl.src;
        } else {
          desc += (next as HTMLParagraphElement).innerText + '\n';
        }
      }
      next = next.nextElementSibling;
    }
    if (desc.trim() || img) {
      steps.push({ description: desc.trim(), imageUrl: img });
    }
  });

  if (!steps.length && ps.length > 1) {
    // fallback: 첫 p는 요약, 나머지는 하나의 STEP으로
    const rest = ps
      .slice(1)
      .map((p) => p.innerText)
      .join('\n');
    if (rest.trim()) steps.push({ description: rest.trim() });
  }

  return { summary, steps };
}

export default function RecipeEditPage() {
  const { postId } = useParams();
  const id = Number(postId);
  const nav = useNavigate();
  const { show } = useToast();

  const { data, isLoading, isError } = usePost(id);

  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [dietType, setDietType] = useState<string>('');
  const [cookTimeInMinutes, setCookTime] = useState<number | ''>('');
  const [servings, setServings] = useState<number | ''>('');
  const [difficulty, setDifficulty] = useState<string>('');
  const [ingredients, setIngredients] = useState<{ name: string; amount?: string }[]>([{ name: '', amount: '' }]);
  const [steps, setSteps] = useState<{ description: string; imageUrl?: string }[]>([{ description: '', imageUrl: '' }]);

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!data) return;
    const post = data as Post;
    if (!post.isRecipe) {
      // 레시피가 아닌 경우 게시글 상세로 돌려보냄
      nav(`/boards/${id}`);
      return;
    }
    setTitle(post.title ?? '');
    // 요약/단계 추출
    const extracted = extractFromContentHtml(post.content ?? '');
    setSummary(extracted.summary ?? '');
    setSteps(extracted.steps.length ? extracted.steps : [{ description: '', imageUrl: '' }]);
    setDietType((post.dietType as string) ?? '');
    setCookTime(post.cookTimeInMinutes ?? '');
    setServings(post.servings ?? '');
    setDifficulty((post.difficulty as string) ?? '');
    // thumbnail은 현재 content 내 img로만 알 수 있으므로, 별도 필드가 생기면 여기서 매핑
    if (post.ingredients && post.ingredients.length) {
      setIngredients(
        post.ingredients.map((ing) => ({
          name: ing.name,
          amount: ing.unit ? `${ing.quantity ?? ''}${ing.unit}` : ing.quantity != null ? String(ing.quantity) : '',
        })),
      );
    } else {
      setIngredients([{ name: '', amount: '' }]);
    }
  }, [data, id, nav]);

  const validate = useMemo(() => {
    return () => {
      const e: Record<string, string> = {};
      const t = title.trim();
      if (!t) e.title = '레시피 제목을 입력해주세요.';
      else if (t.length < 5) e.title = '제목은 5자 이상 입력해주세요.';
      else if (t.length > 50) e.title = '제목은 50자 이하로 입력해주세요.';

      if (!ingredients.length || !ingredients.some((i) => i.name.trim()))
        e.ingredients = '재료를 최소 1개 이상 입력해주세요.';
      if (!steps.length || !steps.some((s) => s.description.trim()))
        e.steps = '조리 단계를 최소 1개 이상 입력해주세요.';
      if (summary.length > 100) e.summary = '요약은 100자 이하로 입력해주세요.';

      setErrors(e);
      return Object.keys(e).length === 0;
    };
  }, [title, ingredients, steps, summary]);

  const buildContentHtml = () => {
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
      setSaving(true);
      const content = buildContentHtml();
      const dto: UpsertPostDto = {
        title: title.trim(),
        content,
        categoryId: data?.categoryId ?? 0,
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
            memo: i.amount?.trim() ?? undefined,
          })),
      };

      await updatePost(id, dto);
      show('레시피가 성공적으로 수정되었습니다.', { type: 'success' });
      nav(`/boards/${id}`);
    } catch (err: any) {
      const status = err?.response?.status as number | undefined;
      const message = err?.response?.data?.message as string | undefined;
      if (status === 401) {
        alert('로그인이 필요한 서비스입니다.');
        nav('/login');
        return;
      }
      if (status === 403) {
        show(message ?? '수정 권한이 없습니다.', { type: 'error' });
        nav('/');
        return;
      }
      if (status === 404) {
        show(message ?? '원본 레시피를 찾을 수 없습니다.', { type: 'error' });
        nav('/boards');
        return;
      }
      show(message ?? '수정 중 오류가 발생했습니다.', { type: 'error' });
    } finally {
      setSaving(false);
    }
  };

  if (Number.isNaN(id)) return <div className="p-6">잘못된 레시피 ID입니다.</div>;
  if (isLoading) return <div className="p-6">불러오는 중…</div>;
  if (isError || !data) return <div className="p-6">레시피를 찾을 수 없거나 오류가 발생했습니다.</div>;

  return (
    <div className="p-6 space-y-5">
      <h1 className="text-2xl font-bold">레시피 수정</h1>

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
      </section>

      <section>
        <IngredientsEditor ingredients={ingredients} setIngredients={setIngredients} />
        {errors.ingredients && <p className="text-sm text-red-600 mt-1">{errors.ingredients}</p>}
      </section>

      <section>
        <StepsEditor steps={steps} setSteps={setSteps} />
        {errors.steps && <p className="text-sm text-red-600 mt-1">{errors.steps}</p>}
      </section>

      <section className="grid grid-cols-3 gap-2">
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
      </section>

      <div className="flex gap-2">
        <button
          onClick={onSubmit}
          disabled={saving}
          className="border px-3 py-2 rounded bg-blue-600 text-white disabled:opacity-50"
        >
          {saving ? '수정 중…' : '수정 완료'}
        </button>
        <button onClick={() => nav(-1)} className="border px-3 py-2 rounded">
          취소
        </button>
      </div>
    </div>
  );
}
