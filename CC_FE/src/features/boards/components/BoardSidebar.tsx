import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { listCategories, type Category } from '@/apis/categories.api.ts';

const BoardSidebar: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [searchParams] = useSearchParams();
  const currentCategoryId = searchParams.get('categoryId');

  useEffect(() => {
    listCategories().then((data) => {
      // 상위 카테고리만 필터링 (필요에 따라 로직 조정)
      const filtered = data.filter(c => c.parentId === null && c.type !== 'RECIPE');
      setCategories(filtered);
    }).catch(console.error);
  }, []);

  return (
    <aside className="bg-white p-4 rounded-lg shadow-md w-full md:w-64 flex-shrink-0 h-fit">
      <h3 className="text-lg font-semibold text-gray-800 pb-3 mb-3 border-b border-gray-200">
        게시판
      </h3>
      <nav>
        <ul className="space-y-1">
          <li>
            <Link
              to="/boards"
              className={`block py-2 px-3 rounded-md text-sm font-medium transition-colors ${
                !currentCategoryId
                  ? 'bg-[#E4E9D9] text-[#4E652F] font-bold'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
              }`}
            >
              전체 글 보기
            </Link>
          </li>
          {categories.map((board) => (
            <li key={board.id}>
              <Link
                to={`/boards?categoryId=${board.id}`}
                className={`block py-2 px-3 rounded-md text-sm font-medium transition-colors ${
                  Number(currentCategoryId) === board.id
                    ? 'bg-[#E4E9D9] text-[#4E652F] font-bold'
                    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                }`}
              >
                {board.name}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
};

export default BoardSidebar;