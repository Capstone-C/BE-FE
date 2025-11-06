// src/features/boards/pages/BoardNewPage.tsx
import axios from 'axios';
import { useState, type ChangeEvent, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createPost } from '@/apis/boards.api';
import { listCategories, type Category, type CategoryType } from '@/apis/categories.api';

export default function BoardNewPage() {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [categoryId, setCategoryId] = useState<number>(0);
  const [categories, setCategories] = useState<Category[]>([]);
  const [isRecipe, setIsRecipe] = useState<boolean>(false);
  const [loading, setLoading] = useState(false);

  const nav = useNavigate();

  useEffect(() => {
    // 초기 카테고리 불러오기
    (async () => {
      try {
        const data = await listCategories();
        // 최상위 카테고리만 노출
        const topLevel = data.filter((c) => c.parentId == null);
        setCategories(topLevel);
        const first = topLevel[0];
        if (first && typeof first.id === 'number') setCategoryId(first.id);
      } catch (e) {
        console.error('카테고리를 불러오지 못했습니다.', e);
      }
    })();
  }, []);

  const typeLabelMap: Record<CategoryType, string> = {
    VEGAN: '비건',
    CARNIVORE: '잡식',
    RECIPE: '레시피',
    FREE: '자유',
    QA: 'Q&A',
  };

  const renderCategoryLabel = (c: Category) => {
    return typeLabelMap[c.type] ?? c.name ?? `카테고리 ${c.id}`;
  };

  const onTitle = (e: ChangeEvent<HTMLInputElement>) => setTitle(e.target.value);
  const onContent = (e: ChangeEvent<HTMLTextAreaElement>) => setContent(e.target.value);

  const onSubmit = async () => {
    if (!title.trim() || !content.trim()) return alert('제목과 본문을 입력하세요.');
    if (title.trim().length < 5) return alert('제목은 5자 이상 입력해주세요.');
    if (content.trim().length < 10) return alert('본문은 10자 이상 입력해주세요.');
    if (!categoryId) return alert('카테고리를 선택하세요.');
    setLoading(true);
    try {
      await createPost({ title, content, categoryId, isRecipe });
      nav('/boards');
    } catch (err: unknown) {
      // any 금지: unknown으로 받고 안전하게 분기
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
      alert(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold">새 글 작성</h1>

      <input className="border p-2 w-full" placeholder="제목" value={title} onChange={onTitle} />

      <textarea className="border p-2 w-full h-40" placeholder="본문" value={content} onChange={onContent} />

      {/* 카테고리 선택 */}
      <label className="block">
        <span className="mr-2">카테고리</span>
        <select className="border p-2" value={categoryId} onChange={(e) => setCategoryId(Number(e.target.value))}>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {renderCategoryLabel(c)}
            </option>
          ))}
        </select>
      </label>

      {/* 레시피 여부 */}
      <label className="inline-flex items-center gap-2">
        <input type="checkbox" checked={isRecipe} onChange={(e) => setIsRecipe(e.target.checked)} />
        레시피 글
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
