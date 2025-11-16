// src/features/boards/pages/BoardNewPage.tsx
import axios from 'axios';
import { useState, type ChangeEvent, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams, useLocation } from 'react-router-dom';
import { listCategories, type Category } from '@/apis/categories.api';
import { useToast } from '@/contexts/ToastContext';
import { useCreatePostMutation } from '@/features/boards/hooks/usePostMutations';

export default function BoardNewPage() {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [categoryId, setCategoryId] = useState<number>(0);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);

  const nav = useNavigate();
  const [sp] = useSearchParams();
  const location = useLocation() as { state?: { fromCategoryId?: number } };
  const { show } = useToast();
  const createMutation = useCreatePostMutation();

  const fixedCategoryId = useMemo(() => {
    const fromState = location.state?.fromCategoryId;
    const fromQuery = sp.get('categoryId');
    if (Number.isFinite(fromState)) return fromState as number;
    if (fromQuery) {
      const n = Number(fromQuery);
      if (Number.isFinite(n)) return n;
    }
    return undefined;
  }, [location.state, sp]);

  useEffect(() => {
    (async () => {
      try {
        const data = await listCategories();
        const topLevel = data.filter((c) => c.parentId == null && c.type !== 'RECIPE');
        setCategories(topLevel);

        if (fixedCategoryId && topLevel.some((c) => c.id === fixedCategoryId)) {
          setCategoryId(fixedCategoryId);
        } else {
          const first = topLevel[0];
          if (first && typeof first.id === 'number') setCategoryId(first.id);
        }
      } catch (e) {
        console.error('카테고리를 불러오지 못했습니다.', e);
      }
    })();
  }, [fixedCategoryId]);

  const onTitle = (e: ChangeEvent<HTMLInputElement>) => setTitle(e.target.value);
  const onContent = (e: ChangeEvent<HTMLTextAreaElement>) => setContent(e.target.value);

  const onSubmit = async () => {
    const t = title.trim();
    const c = content.trim();
    if (!t || !c) return show('제목과 본문을 입력해주세요.', { type: 'error' });
    if (t.length < 5) return show('제목은 5자 이상 입력해주세요.', { type: 'error' });
    if (t.length > 100) return show('제목은 100자 이하로 입력해주세요.', { type: 'error' });
    if (c.length < 10) return show('본문은 10자 이상 입력해주세요.', { type: 'error' });
    if (c.length > 10000) return show('본문은 10000자 이하로 입력해주세요.', { type: 'error' });
    if (!categoryId) return show('카테고리를 선택해주세요.', { type: 'error' });
    setLoading(true);
    try {
      await createMutation.mutateAsync({ title: t, content: c, categoryId });
      const board = fixedCategoryId ?? undefined;
      const to = board ? `/boards?categoryId=${board}` : '/boards';
      show('글이 등록되었습니다.', { type: 'success' });
      nav(to);
    } catch (err: unknown) {
      let msg: string = '작성에 실패했습니다.';
      if (axios.isAxiosError(err)) {
        const status = err.response?.status;
        const data = err.response?.data as { message?: string; error?: string } | undefined;
        if (status === 401) msg = '로그인이 필요합니다.';
        else if (status === 403) msg = '글을 작성할 권한이 없습니다.';
        else if (status === 404) msg = '선택한 카테고리를 찾을 수 없습니다.';
        else if (status === 400 || status === 422) msg = data?.message ?? '입력값을 확인해주세요.';
        else msg = data?.message ?? data?.error ?? err.message ?? msg;
      } else if (err instanceof Error) {
        msg = err.message;
      }
      show(msg, { type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const isCategoryLocked = Number.isFinite(fixedCategoryId);

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">새 글 작성</h1>

      <input className="border p-2 w-full" placeholder="제목" value={title} onChange={onTitle} />

      <textarea className="border p-2 w-full h-40" placeholder="본문" value={content} onChange={onContent} />

      {/* 카테고리 선택 */}
      <label className="block">
        <span className="mr-2">카테고리</span>
        <select
          className="border p-2"
          value={categoryId}
          onChange={(e) => setCategoryId(Number(e.target.value))}
          disabled={isCategoryLocked}
          title={isCategoryLocked ? '게시판에서 진입하여 카테고리가 고정되었습니다' : undefined}
        >
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name ?? `카테고리 ${c.id}`}
            </option>
          ))}
        </select>
      </label>

      <div className="flex gap-2">
        <button
          disabled={!title || !content || loading}
          onClick={onSubmit}
          className="border px-3 py-2 rounded-md disabled:opacity-50"
        >
          {loading ? '등록 중…' : '등록'}
        </button>
        <button onClick={() => nav(-1)} className="border px-3 py-2 rounded-md">
          취소
        </button>
      </div>
    </div>
  );
}
