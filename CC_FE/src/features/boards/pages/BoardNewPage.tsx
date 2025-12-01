import { useState, useRef, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useCreatePostMutation } from '@/features/boards/hooks/usePostMutations';
import { listCategories, type Category } from '@/apis/categories.api';
import { useToast } from '@/contexts/ToastContext';
import BoardSidebar from '@/features/boards/components/BoardSidebar';

export default function BoardNewPage() {
  const nav = useNavigate();
  const [sp] = useSearchParams();
  const { show } = useToast();

  const createMutation = useCreatePostMutation();
  const editorRef = useRef<HTMLDivElement>(null);

  const [title, setTitle] = useState('');
  const [categoryId, setCategoryId] = useState<number>(0);
  const [categories, setCategories] = useState<Category[]>([]);
  const [isDragging, setIsDragging] = useState(false);

  const initialCategoryId = Number(sp.get('categoryId') || 0);

  useEffect(() => {
    listCategories().then((data) => {
      const filtered = data.filter((c) => c.parentId === null && c.type !== 'RECIPE');
      setCategories(filtered);
      if (initialCategoryId && filtered.some((c) => c.id === initialCategoryId)) {
        setCategoryId(initialCategoryId);
      } else if (filtered.length > 0) {
        // [수정] 배열 인덱스 접근 안전성 확보 (noUncheckedIndexedAccess 대응)
        const firstCategory = filtered[0];
        if (firstCategory) {
          setCategoryId(firstCategory.id);
        }
      }
    });
  }, [initialCategoryId]);

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      const file = files[0];
      if (!file) return; // Null check

      if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = (loadEvent) => {
          const img = document.createElement('img');
          img.src = loadEvent.target?.result as string;
          img.style.maxWidth = '100%';
          img.style.borderRadius = '8px';
          // [수정] editorRef.current 안전하게 접근
          if (editorRef.current) {
            editorRef.current.appendChild(img);
          }
        };
        reader.readAsDataURL(file);
      } else {
        show('이미지 파일만 첨부할 수 있습니다.', { type: 'error' });
      }
    }
  };

  const handleSubmit = async () => {
    if (!title.trim()) return show('제목을 입력해주세요.', { type: 'error' });

    // [수정] editorRef.current를 로컬 변수에 할당하여 타입 가드(Type Guard)가 유효하도록 수정
    // 이렇게 하면 이후 코드에서 editor 변수는 null이 아님이 보장됩니다.
    const editor = editorRef.current;
    if (!editor) return;

    if (!editor.innerHTML.trim()) {
      return show('내용을 입력해주세요.', { type: 'error' });
    }

    try {
      // 로컬 변수 editor 사용
      const contentHtml = editor.innerHTML;

      await createMutation.mutateAsync({
        title,
        content: contentHtml,
        categoryId,
        isRecipe: false,
        status: 'PUBLISHED',
      });

      show('게시글이 등록되었습니다.', { type: 'success' });
      nav(categoryId ? `/boards?categoryId=${categoryId}` : '/boards');
    } catch (e) {
      show('게시글 등록에 실패했습니다.', { type: 'error' });
    }
  };

  const currentCategoryName = categories.find((c) => c.id === categoryId)?.name || '게시판';

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col md:flex-row gap-8">
        <BoardSidebar />
        <div className="w-full md:w-3/4">
          <div className="mb-6 pb-4 border-b border-gray-200">
            <h1 className="text-3xl font-bold leading-tight text-gray-900">{currentCategoryName} 글쓰기</h1>
          </div>

          <div className="bg-white p-8 rounded-lg shadow-lg">
            {/* Category Select */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">카테고리</label>
              <select
                className="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm"
                value={categoryId}
                onChange={(e) => setCategoryId(Number(e.target.value))}
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Title Input */}
            <div className="mb-6">
              <input
                type="text"
                className="block w-full text-2xl px-0 border-0 border-b-2 border-gray-200 focus:ring-0 focus:border-[#4E652F] placeholder-gray-400 py-2"
                placeholder="제목을 입력하세요"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </div>

            {/* Editor */}
            <div>
              {/* Content Area */}
              <div
                ref={editorRef}
                contentEditable={true}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                className={`w-full h-96 p-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#71853A] overflow-y-auto ${isDragging ? 'ring-2 ring-[#4E652F] bg-gray-100' : ''}`}
                suppressContentEditableWarning={true}
              />
              <p className="text-xs text-gray-500 mt-2 text-right">이미지를 드래그 앤 드롭하여 첨부할 수 있습니다.</p>
            </div>

            {/* Action Buttons */}
            <div className="mt-8 flex justify-end space-x-4">
              <button
                onClick={() => nav(-1)}
                className="px-6 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50"
              >
                취소
              </button>
              <button
                onClick={handleSubmit}
                className="px-6 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-[#4E652F] hover:bg-[#425528]"
              >
                등록
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}