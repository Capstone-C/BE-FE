import { useState, useRef, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { usePost } from '@/features/boards/hooks/usePosts';
import { useUpdatePostMutation } from '@/features/boards/hooks/usePostMutations';
import { listCategories, type Category } from '@/apis/categories.api';
import { useToast } from '@/contexts/ToastContext';
import BoardSidebar from '@/features/boards/components/BoardSidebar';

export default function BoardEditPage() {
  const { postId } = useParams();
  const id = Number(postId);
  const nav = useNavigate();
  const { show } = useToast();

  const { data: post, isLoading } = usePost(id);
  const updateMutation = useUpdatePostMutation();
  const editorRef = useRef<HTMLDivElement>(null);

  const [title, setTitle] = useState('');
  const [categoryId, setCategoryId] = useState<number>(0);
  const [categories, setCategories] = useState<Category[]>([]);
  const [isDragging, setIsDragging] = useState(false);

  // 초기 데이터 로드
  useEffect(() => {
    listCategories().then((data) => {
      const filtered = data.filter((c) => c.parentId === null && c.type !== 'RECIPE');
      setCategories(filtered);
    });
  }, []);

  useEffect(() => {
    if (post) {
      setTitle(post.title);
      setCategoryId(post.categoryId);
      if (editorRef.current) {
        editorRef.current.innerHTML = post.content;
      }
    }
  }, [post]);

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
          editorRef.current?.appendChild(img);
        };
        reader.readAsDataURL(file);
      } else {
        show('이미지 파일만 첨부할 수 있습니다.', { type: 'error' });
      }
    }
  };

  const handleSubmit = async () => {
    if (!title.trim()) return show('제목을 입력해주세요.', { type: 'error' });

    if (!editorRef.current) return; // Null check
    if (!editorRef.current.innerHTML.trim()) return show('내용을 입력해주세요.', { type: 'error' });

    try {
      const contentHtml = editorRef.current.innerHTML;
      await updateMutation.mutateAsync({
        id,
        dto: {
          title,
          content: contentHtml,
          categoryId,
          isRecipe: false,
          status: 'PUBLISHED',
        },
      });
      show('게시글이 수정되었습니다.', { type: 'success' });
      nav(`/boards/${id}`);
    } catch (e) {
      show('게시글 수정에 실패했습니다.', { type: 'error' });
    }
  };

  if (isLoading) return <div className="p-8 text-center">불러오는 중...</div>;

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col md:flex-row gap-8">
        <BoardSidebar />
        <div className="w-full md:w-3/4">
          <div className="mb-6 pb-4 border-b border-gray-200">
            <h1 className="text-3xl font-bold leading-tight text-gray-900">글 수정하기</h1>
          </div>

          <div className="bg-white p-8 rounded-lg shadow-lg">
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

            <div className="mb-6">
              <input
                type="text"
                className="block w-full text-2xl px-0 border-0 border-b-2 border-gray-200 focus:ring-0 focus:border-[#4E652F] placeholder-gray-400 py-2"
                placeholder="제목을 입력하세요"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </div>

            <div>
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
                수정
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}