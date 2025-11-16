import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { usePost } from '@/features/boards/hooks/usePosts';
import { updatePost, type UpsertPostDto } from '@/apis/boards.api';
import { useToast } from '@/contexts/ToastContext';
import type { Post } from '@/types/post';
import { IngredientsEditor } from '@/features/recipes/components/IngredientsEditor';
import StepsEditor from '@/features/recipes/components/StepsEditor';

const allowedDiet = [
  'VEGAN',
  'VEGETARIAN',
  'KETO',
  'PALEO',
  'MEDITERRANEAN',
  'LOW_CARB',
  'HIGH_PROTEIN',
  'GENERAL',
] as const;
const allowedDiff = ['VERY_LOW', 'LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'] as const;

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
  const [ingredients, setIngredients] = useState<
    { name: string; amount?: string; quantity?: number | null; unit?: string | null }[]
  >([{ name: '', amount: '', quantity: null, unit: null }]);
  const [steps, setSteps] = useState<{ description: string; imageUrl?: string }[]>([{ description: '', imageUrl: '' }]);
  const [thumbnail, setThumbnail] = useState('');
  const [status, setStatus] = useState<'DRAFT' | 'PUBLISHED'>('PUBLISHED');
  const [categoryName, setCategoryName] = useState('');

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!data) return;
    const post = data as Post;
    // 레시피 여부는 라우트로 분리되었으므로 별도 isRecipe 플래그 검사는 수행하지 않음
    setTitle(post.title ?? '');
    // 요약/단계 추출
    const extracted = extractFromContentHtml(post.content ?? '');
    setSummary(extracted.summary ?? '');
    setSteps(extracted.steps.length ? extracted.steps : [{ description: '', imageUrl: '' }]);
    setDietType((post.dietType as string) ?? '');
    setCookTime(post.cookTimeInMinutes ?? '');
    setServings(post.servings ?? '');
    setDifficulty((post.difficulty as string) ?? '');
    setStatus((post.status as 'DRAFT' | 'PUBLISHED' | 'ARCHIVED') === 'DRAFT' ? 'DRAFT' : 'PUBLISHED');
    setCategoryName(post.categoryName ?? `카테고리 #${post.categoryId}`);
    // thumbnail은 현재 content 내 img 첫 번째를 추출(임시)
    const firstImg = /<img[^>]*src=["']([^"']+)["'][^>]*>/i.exec(post.content ?? '')?.[1];
    if (firstImg) setThumbnail(firstImg);
    // ingredients는 별도 필드로 분리되어 있으므로, 기존 포스트 데이터에서 직접 매핑
    if (post.ingredients && post.ingredients.length) {
      setIngredients(
        post.ingredients.map((ing) => ({
          name: ing.name,
          quantity: ing.quantity ?? null,
          unit: ing.unit ?? null,
          amount: ing.memo ?? '',
        })),
      );
    } else {
      setIngredients([{ name: '', amount: '', quantity: null, unit: null }]);
    }
  }, [data, id, nav]);

  const validate = useMemo(() => {
    return () => {
      const e: Record<string, string> = {};
      const t = title.trim();
      if (!t) e.title = '레시피 제목을 입력해주세요.';
      else if (t.length < 5) e.title = '제목은 5자 이상 입력해주세요.';
      else if (t.length > 100) e.title = '제목은 100자 이하로 입력해주세요.';

      if (!ingredients.length || !ingredients.some((i) => i.name.trim()))
        e.ingredients = '재료를 최소 1개 이상 입력해주세요.';
      else if (ingredients.some((i) => i.quantity != null && i.quantity! < 0))
        e.ingredients = '재료 수량은 음수가 될 수 없습니다.';
      if (!steps.length || !steps.some((s) => s.description.trim()))
        e.steps = '조리 단계를 최소 1개 이상 입력해주세요.';
      if (summary.length > 100) e.summary = '요약은 100자 이하로 입력해주세요.';

      const plainTextLen = (summary + ' ' + steps.map((s) => s.description).join(' '))
        .replace(/<[^>]+>/g, '')
        .replace(/\s+/g, ' ')
        .trim().length;
      if (plainTextLen < 10)
        e.steps = e.steps ? e.steps + ' 본문은 10자 이상 입력해주세요.' : '본문은 10자 이상 입력해주세요.';

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
        status,
        dietType: isDietType(dietType) ? dietType : undefined,
        cookTimeInMinutes: typeof cookTimeInMinutes === 'number' ? cookTimeInMinutes : undefined,
        servings: typeof servings === 'number' ? servings : undefined,
        difficulty: isDifficulty(difficulty) ? difficulty : undefined,
        ingredients: ingredients
          .filter((i) => i.name.trim())
          .map((i) => ({
            name: i.name.trim(),
            quantity: i.quantity != null ? i.quantity : undefined,
            unit: i.unit ?? undefined,
            memo: i.amount?.trim() || undefined,
          })),
        isRecipe: true,
        thumbnailUrl: thumbnail || undefined,
      };

      await updatePost(id, dto);
      show('레시피가 성공적으로 수정되었습니다.', { type: 'success' });
      nav(`/boards/${id}`);
    } catch (err: any) {
      const status = err?.response?.status as number | undefined;
      const message = err?.response?.data?.message as string | undefined;
      if (status === 401) {
        show('로그인이 필요한 서비스입니다.', { type: 'error' });
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
            maxLength={100}
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
          <div className="text-sm font-medium">대표 이미지 URL (선택)</div>
          <input
            value={thumbnail}
            onChange={(e) => setThumbnail(e.target.value)}
            placeholder="https://..."
            className="border p-2 w-full"
          />
        </label>
        <div className="grid grid-cols-3 gap-2">
          <label className="block">
            <div className="text-sm font-medium">상태</div>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value as 'DRAFT' | 'PUBLISHED')}
              className="border p-2 w-full"
            >
              <option value="DRAFT">임시 저장</option>
              <option value="PUBLISHED">발행</option>
            </select>
          </label>
          <label className="block col-span-2">
            <div className="text-sm font-medium">카테고리</div>
            <input value={categoryName} disabled className="border p-2 w-full bg-gray-100" />
          </label>
        </div>
      </section>

      <section className="space-y-2">
        <IngredientsEditor items={ingredients as any} onChange={setIngredients as any} />
        {errors.ingredients && <p className="text-sm text-red-600 mt-1">{errors.ingredients}</p>}
      </section>

      <section className="space-y-2">
        <StepsEditor steps={steps} onChange={setSteps} />
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

function isDietType(v: string): v is (typeof allowedDiet)[number] {
  return (allowedDiet as readonly string[]).includes(v);
}
function isDifficulty(v: string): v is (typeof allowedDiff)[number] {
  return (allowedDiff as readonly string[]).includes(v);
}
