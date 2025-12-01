import Container from '@/components/ui/Container';

export function Footer() {
  return (
    <footer className="border-t border-gray-200 bg-gradient-to-br from-gray-50 to-white mt-20">
      <Container>
        <div className="py-8">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <div className="text-center md:text-left">
              <div className="text-lg font-bold gradient-text mb-2">🍽️ Capstone</div>
              <p className="text-sm text-gray-600">나만의 레시피와 식생활을 한 곳에서</p>
            </div>
            <div className="flex gap-6 text-sm text-gray-600">
              <a href="#" className="hover:text-purple-600 transition-colors">서비스 소개</a>
              <a href="#" className="hover:text-purple-600 transition-colors">이용약관</a>
              <a href="#" className="hover:text-purple-600 transition-colors">개인정보처리방침</a>
            </div>
          </div>
          <div className="mt-6 pt-6 border-t border-gray-200 text-center text-xs text-gray-500">
            © {new Date().getFullYear()} Capstone. All rights reserved.
          </div>
        </div>
      </Container>
    </footer>
  );
}
