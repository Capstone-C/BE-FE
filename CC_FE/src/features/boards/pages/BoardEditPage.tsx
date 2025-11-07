// src/features/boards/pages/BoardEditPage.tsx
import axios from 'axios';
import { useEffect, useState, type ChangeEvent } from 'react';
import { useParams, useNavigate, useSearchParams, useLocation } from 'react-router-dom';
import { usePost } from '@/features/boards/hooks/usePosts';
import { updatePost } from '@/apis/boards.api';

function toBool(v: unknown): boolean {
  // 서버가 0/1, "1"/"0", true/false 등 어떤 형태로 와도 안전하게 변환
  return v === true || v === 1 || v === '1';
}

export default function BoardEditPage() {
  const { postId } = useParams();
  const id = Number(postId);
  const nav = useNavigate();
  const [sp] = useSearchParams();
  const location = useLocation() as { state?: { fromCategoryId?: number } };

  const { data, isLoading } = usePost(id);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [categoryId, setCategoryId] = useState<number>(1);
  const [isRecipe, setIsRecipe] = useState<boolean>(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!data) return;
    setTitle(data.title ?? '');
    setContent(data.content ?? '');
    setCategoryId(data.categoryId ?? 1);
    setIsRecipe(toBool(data.isRecipe));
  }, [data]);

  const onTitle = (e: ChangeEvent<HTMLInputElement>) => setTitle(e.target.value);
  const onContent = (e: ChangeEvent<HTMLTextAreaElement>) => setContent(e.target.value);

  const onSave = async () => {
    if (!title.trim() || !content.trim()) return alert('제목과 본문을 입력하세요.');
    setSaving(true);
    try {
      await updatePost(id, { title, content, categoryId, isRecipe });
      // 쿼리 우선, 없으면 state에서 카테고리 복구
      const cat =
        sp.get('categoryId') ??
        (Number.isFinite(location.state?.fromCategoryId) ? String(location.state?.fromCategoryId) : undefined);
      nav(cat ? `/boards/${id}?categoryId=${cat}` : `/boards/${id}`);
    } catch (err: unknown) {
      let msg = '수정에 실패했습니다.';
      if (axios.isAxiosError(err)) {
        const d = err.response?.data as { message?: string; error?: string } | undefined;
        msg = d?.message ?? d?.error ?? err.message ?? msg;
      } else if (err instanceof Error) msg = err.message;
      alert(msg);
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
          <option value={1}>카테고리 1</option>
          <option value={2}>카테고리 2</option>
        </select>
      </label>

      <label className="inline-flex items-center gap-2">
        <input type="checkbox" checked={isRecipe} onChange={(e) => setIsRecipe(e.target.checked)} />
        레시피 글
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
