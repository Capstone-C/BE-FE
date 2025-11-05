// src/features/boards/pages/BoardNewPage.tsx
import axios from 'axios';
import { useState, type ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { createPost } from '@/apis/boards.api';

export default function BoardNewPage() {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [categoryId, setCategoryId] = useState<number>(1); // ✅ 실제 존재하는 카테고리 ID로 기본값
  const [isRecipe, setIsRecipe] = useState<boolean>(false); // ✅ bit(1) ↔ boolean
  const [loading, setLoading] = useState(false);

  const nav = useNavigate();

  const onTitle = (e: ChangeEvent<HTMLInputElement>) => setTitle(e.target.value);
  const onContent = (e: ChangeEvent<HTMLTextAreaElement>) => setContent(e.target.value);

  const onSubmit = async () => {
    if (!title.trim() || !content.trim()) return alert('제목과 본문을 입력하세요.');
    setLoading(true);
    try {
      await createPost({ title, content, categoryId, isRecipe });
      nav('/boards');
    } catch (err: unknown) {
      // ✅ any 금지: unknown으로 받고 안전하게 분기
      let msg: string = '작성에 실패했습니다.';
      if (axios.isAxiosError(err)) {
        const data = err.response?.data as { message?: string; error?: string } | undefined;
        msg = data?.message ?? data?.error ?? err.message ?? msg;
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

      <input
        className="border p-2 w-full"
        placeholder="제목"
        value={title}
        onChange={onTitle}
      />

      <textarea
        className="border p-2 w-full h-40"
        placeholder="본문"
        value={content}
        onChange={onContent}
      />

      {/* ✅ 카테고리 선택 (임시 옵션: 실제로는 카테고리 목록 API로 교체) */}
      <label className="block">
        <span className="mr-2">카테고리</span>
        <select
          className="border p-2"
          value={categoryId}
          onChange={(e) => setCategoryId(Number(e.target.value))}
        >
          <option value={1}>카테고리 1</option>
          <option value={2}>카테고리 2</option>
          {/* TODO: 백엔드 category 테이블 값으로 옵션 채우기 */}
        </select>
      </label>

      {/* ✅ 레시피 여부 */}
      <label className="inline-flex items-center gap-2">
        <input
          type="checkbox"
          checked={isRecipe}
          onChange={(e) => setIsRecipe(e.target.checked)}
        />
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
