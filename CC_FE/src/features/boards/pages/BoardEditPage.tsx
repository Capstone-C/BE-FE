// src/features/boards/pages/BoardEditPage.tsx
import axios from 'axios';
import { useEffect, useState, type ChangeEvent } from 'react';
import { useParams, useNavigate, useSearchParams, useLocation } from 'react-router-dom';
import { usePost } from '@/features/boards/hooks/usePosts';
import { listCategories, type Category } from '@/apis/categories.api';
import { useToast } from '@/contexts/ToastContext';
import { useUpdatePostMutation } from '@/features/boards/hooks/usePostMutations';

export default function BoardEditPage() {
  const { postId } = useParams();
  const id = Number(postId);
  const nav = useNavigate();
  const [sp] = useSearchParams();
  const location = useLocation() as { state?: { fromCategoryId?: number } };
  const { show } = useToast();
  const updateMutation = useUpdatePostMutation();

  const { data, isLoading } = usePost(id);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [categoryId, setCategoryId] = useState<number>(1);
  const [saving, setSaving] = useState(false);
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    (async () => {
      try {
        const datas = await listCategories();
        const topLevel = datas.filter((c) => c.parentId == null && c.type !== 'RECIPE');
        setCategories(topLevel);
      } catch (e) {
        console.error('카테고리를 불러오지 못했습니다.', e);
      }
    })();
  }, []);

  useEffect(() => {
    if (!data) return;
    setTitle(data.title ?? '');
    setContent(data.content ?? '');
    setCategoryId(data.categoryId ?? 1);
  }, [data]);

  const onTitle = (e: ChangeEvent<HTMLInputElement>) => setTitle(e.target.value);
  const onContent = (e: ChangeEvent<HTMLTextAreaElement>) => setContent(e.target.value);

  const onSave = async () => {
    const t = title.trim();
    const c = content.trim();
    if (!t || !c) return show('제목과 본문을 입력해주세요.', { type: 'error' });
    if (t.length < 5) return show('제목은 5자 이상 입력해주세요.', { type: 'error' });
    if (t.length > 100) return show('제목은 100자 이하로 입력해주세요.', { type: 'error' });
    if (c.length < 10) return show('본문은 10자 이상 입력해주세요.', { type: 'error' });
    if (c.length > 10000) return show('본문은 10000자 이하로 입력해주세요.', { type: 'error' });
    setSaving(true);
    try {
      await updateMutation.mutateAsync({ id, dto: { title: t, content: c, categoryId } });
      const cat =
        sp.get('categoryId') ??
        (Number.isFinite(location.state?.fromCategoryId) ? String(location.state?.fromCategoryId) : undefined);
      show('글이 수정되었습니다.', { type: 'success' });
      nav(cat ? `/boards/${id}?categoryId=${cat}` : `/boards/${id}`);
    } catch (err: unknown) {
      let msg = '수정에 실패했습니다.';
      if (axios.isAxiosError(err)) {
        const d = err.response?.data as { message?: string; error?: string } | undefined;
        msg = d?.message ?? d?.error ?? err.message ?? msg;
      } else if (err instanceof Error) msg = err.message;
      show(msg, { type: 'error' });
    } finally {
      setSaving(false);
    }
  };

  if (Number.isNaN(id)) return <div className="p-6">잘못된 글 ID입니다.</div>;
  if (isLoading || !data) return <div className="p-6">불러오는 중…</div>;

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">글 수정</h1>

      <input className="border p-2 w-full" value={title} onChange={onTitle} placeholder="제목" />
      <textarea className="border p-2 w-full h-40" value={content} onChange={onContent} placeholder="본문" />

      <label className="block">
        <span className="mr-2">카테고리</span>
        <select className="border p-2" value={categoryId} onChange={(e) => setCategoryId(Number(e.target.value))}>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name ?? `카테고리 ${c.id}`}
            </option>
          ))}
        </select>
      </label>

      <div className="flex gap-2">
        <button
          className="border px-3 py-2 rounded-md disabled:opacity-50"
          disabled={!title || !content || saving}
          onClick={onSave}
        >
          {saving ? '저장 중…' : '수정'}
        </button>
        <button className="border px-3 py-2 rounded-md" onClick={() => nav(-1)}>
          취소
        </button>
      </div>
    </div>
  );
}
