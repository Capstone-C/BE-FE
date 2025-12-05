import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { BoardIcon, QnaIcon, RecipeIcon, DiaryIcon, ShoppingIcon, FridgeIcon } from '@/components/ui/Icons';
import { listCategories, type Category } from '@/apis/categories.api';
// 알림 컴포넌트 import 제거
// import NotificationDropdown from '@/features/common/components/NotificationDropdown';

const NavigationBar: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [isBoardOpen, setIsBoardOpen] = useState(false);

  // 알림 관련 state 및 ref 제거
  // const [isNotiOpen, setIsNotiOpen] = useState(false);
  // const notiRef = useRef<HTMLLIElement>(null);

  useEffect(() => {
    listCategories().then((list) => {
      const topLevel = list.filter(c => c.parentId === null && c.type !== 'RECIPE');
      setCategories(topLevel);
    }).catch(() => {});
  }, []);

  // 알림창 외부 클릭 시 닫기 useEffect 제거
  /*
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (notiRef.current && !notiRef.current.contains(event.target as Node)) {
        setIsNotiOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [notiRef]);
  */

  const navItems = [
    { name: '게시판', icon: BoardIcon, href: '/boards', hasDropdown: true },
    { name: 'QnA', icon: QnaIcon, href: '/boards?categoryId=4' },
    { name: '레시피', icon: RecipeIcon, href: '/recipes' },
    { name: '다이어리', icon: DiaryIcon, href: '/diary' },
    { name: '쇼핑몰', icon: ShoppingIcon, href: '/shopping' },
    { name: '냉장고', icon: FridgeIcon, href: '/refrigerator' },
    // 알림 항목은 여기서 제거됨
  ];

  return (
    <nav className="bg-white shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* 알림 아이콘 삭제로 인한 공간 재배치를 위해 justify-around 유지 혹은 필요시 gap 조정 */}
        <ul className="flex justify-around items-center h-20">
          {navItems.map((item) => (
            <li key={item.name} className="relative h-full flex items-center">
              {item.hasDropdown ? (
                <div
                  className="relative h-full flex items-center"
                  onMouseEnter={() => setIsBoardOpen(true)}
                  onMouseLeave={() => setIsBoardOpen(false)}
                >
                  <Link to={item.href} className="flex flex-col items-center text-gray-600 hover:text-[#4E652F] transition-colors duration-200 group px-2">
                    <item.icon className="w-8 h-8 mb-1 text-gray-400 group-hover:text-[#71853A] transition-colors duration-200" />
                    <span className="text-sm font-medium">{item.name}</span>
                  </Link>

                  {isBoardOpen && categories.length > 0 && (
                    <div className="absolute top-full left-1/2 -translate-x-1/2 w-48 z-50 pt-2">
                      <div className="absolute top-0.5 left-1/2 -translate-x-1/2 w-3 h-3 bg-white rotate-45 border-t border-l border-gray-200 z-10" />
                      <div className="bg-white rounded-lg shadow-2xl overflow-hidden border border-gray-200 relative z-0">
                        <ul className="divide-y divide-gray-100">
                          {categories.map((cat) => (
                            <li key={cat.id}>
                              <Link to={`/boards?categoryId=${cat.id}`} className="block px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 transition-colors duration-150">
                                {cat.name}
                              </Link>
                            </li>
                          ))}
                        </ul>
                        <div className="bg-gray-50 p-3 text-center border-t">
                          <Link to="/boards" className="text-sm font-medium text-[#4E652F] hover:text-[#425528]">
                            전체보기
                          </Link>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              ) : (
                <Link to={item.href} className="flex flex-col items-center text-gray-600 hover:text-[#4E652F] transition-colors duration-200 group px-2">
                  <item.icon className="w-8 h-8 mb-1 text-gray-400 group-hover:text-[#71853A] transition-colors duration-200" />
                  <span className="text-sm font-medium">{item.name}</span>
                </Link>
              )}
            </li>
          ))}

          {/* 알림 아이콘 삭제됨 */}
        </ul>
      </div>
    </nav>
  );
};

export default NavigationBar;