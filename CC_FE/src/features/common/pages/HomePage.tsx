import Container from '@/components/ui/Container';
import Button from '@/components/ui/Button';
import { Link } from 'react-router-dom';

export default function HomePage() {
  return (
    <div>
      <section className="bg-white border-b">
        <Container className="py-12">
          <div className="grid md:grid-cols-2 gap-8 items-center">
            <div>
              <h1 className="text-3xl md:text-4xl font-extrabold text-gray-900">나만의 레시피와 식생활을 한 곳에서</h1>
              <p className="mt-3 text-gray-600">
                레시피 작성·공유, 내 냉장고 재료 비교, 식단 다이어리까지. 간편하게 시작하세요.
              </p>
              <div className="mt-6 flex gap-3">
                <Link to="/boards" className="inline-block">
                  <Button>레시피/게시글 둘러보기</Button>
                </Link>
                <Link to="/recipes/new" className="inline-block">
                  <Button variant="secondary">레시피 작성</Button>
                </Link>
              </div>
            </div>
            <div className="hidden md:block">
              <div className="aspect-video rounded-lg border bg-gray-100" />
            </div>
          </div>
        </Container>
      </section>

      <section>
        <Container className="py-10 grid md:grid-cols-3 gap-6">
          <Link to="/refrigerator" className="block rounded-lg border bg-white p-5 hover:shadow">
            <h3 className="font-semibold">내 냉장고</h3>
            <p className="text-sm text-gray-600 mt-1">보유 재료를 관리하고 레시피와 비교해 부족한 재료를 확인하세요.</p>
          </Link>
          <Link to="/diary" className="block rounded-lg border bg-white p-5 hover:shadow">
            <h3 className="font-semibold">식단 다이어리</h3>
            <p className="text-sm text-gray-600 mt-1">하루 식단을 기록하고 레시피를 연동해 더 편하게 관리하세요.</p>
          </Link>
          <Link to="/community" className="block rounded-lg border bg-white p-5 hover:shadow">
            <h3 className="font-semibold">커뮤니티</h3>
            <p className="text-sm text-gray-600 mt-1">자유롭게 소통하고 질문/팁을 나누세요.</p>
          </Link>
        </Container>
      </section>
    </div>
  );
}
