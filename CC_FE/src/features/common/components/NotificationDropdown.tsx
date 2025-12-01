import React from 'react';
import { Link } from 'react-router-dom';

const NotificationDropdown: React.FC = () => {
  // 실제로는 API에서 가져와야 할 데이터 (Mock)
  const notifications = [
    { id: 1, text: '새로운 댓글이 달렸습니다: 오늘의 첫 끼', link: '#' },
    { id: 2, text: '회원님이 좋아할 만한 레시피가 있어요: 두부면 파스타', link: '#' },
    { id: 3, text: 'Q&A에 답변이 등록되었습니다.', link: '#' },
  ];

  return (
    <div
      className="absolute top-full right-0 mt-2 w-80 z-50"
      aria-label="알림 목록"
      role="region"
    >
      {/* Arrow pointing up */}
      <div className="absolute -top-1.5 right-4 w-3 h-3 bg-white rotate-45 border-t border-l border-gray-200 z-10" />

      {/* Content box */}
      <div className="bg-white rounded-lg shadow-2xl overflow-hidden border border-gray-200 relative z-0">
        <div className="p-4 border-b">
          <h4 className="text-lg font-semibold text-gray-800">알림</h4>
        </div>
        <ul className="divide-y divide-gray-100 max-h-64 overflow-y-auto">
          {notifications.length > 0 ? (
            notifications.map((notification) => (
              <li key={notification.id}>
                <Link to={notification.link} className="block p-4 text-sm text-gray-600 hover:bg-gray-50 transition-colors duration-150">
                  {notification.text}
                </Link>
              </li>
            ))
          ) : (
            <li className="p-4 text-sm text-center text-gray-500">
              새로운 알림이 없습니다.
            </li>
          )}
        </ul>
        <div className="bg-gray-50 p-3 text-center border-t">
          <button className="text-sm font-medium text-[#4E652F] hover:text-[#425528]">
            모든 알림 보기
          </button>
        </div>
      </div>
    </div>
  );
};

export default NotificationDropdown;