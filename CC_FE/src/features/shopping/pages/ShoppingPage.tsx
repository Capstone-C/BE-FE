import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { searchProducts, ProductSearchRequest } from '@/apis/shopping.api';
import { useDebounce } from '@/hooks/useDebounce';

// --- [Helper Components] ---

// 1. 로딩 중일 때 보여줄 스켈레톤 카드
function ProductSkeleton() {
  return (
    <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden animate-pulse">
      <div className="aspect-square bg-gray-200" />
      <div className="p-4 space-y-3">
        <div className="flex justify-between">
          <div className="h-4 bg-gray-200 rounded w-1/3" />
          <div className="h-4 bg-gray-200 rounded w-10" />
        </div>
        <div className="h-4 bg-gray-200 rounded w-full" />
        <div className="h-4 bg-gray-200 rounded w-2/3" />
        <div className="h-8 bg-gray-200 rounded w-full mt-2" />
      </div>
    </div>
  );
}

// 2. 쇼핑몰 타입에 따른 배지 스타일
function MallBadge({ type }: { type: string }) {
  const getStyle = (t: string) => {
    const lower = t.toLowerCase();
    // 브랜드 고유 색상은 유지하되, 테두리를 부드럽게 처리
    if (lower.includes('naver')) return 'bg-[#03C75A] text-white';
    if (lower.includes('coupang')) return 'bg-[#E4312D] text-white';
    if (lower.includes('11st')) return 'bg-[#F4364C] text-white';
    if (lower.includes('gmarket')) return 'bg-[#02C95E] text-white';
    return 'bg-gray-600 text-white';
  };

  return (
    <span className={`absolute top-3 left-3 px-2 py-1 rounded-md text-[10px] font-bold shadow-sm ${getStyle(type)}`}>
      {type}
    </span>
  );
}

// --- [Main Page] ---

export default function ShoppingPage() {
  const [keyword, setKeyword] = useState('');
  const [mallType, setMallType] = useState<string>('');
  const [minPrice, setMinPrice] = useState<string>('');
  const [maxPrice, setMaxPrice] = useState<string>('');
  const [page, setPage] = useState(0);

  const debouncedKeyword = useDebounce(keyword, 500);
  const debouncedMinPrice = useDebounce(minPrice, 500);
  const debouncedMaxPrice = useDebounce(maxPrice, 500);

  const searchParams: ProductSearchRequest = {
    keyword: debouncedKeyword || undefined,
    mallType: mallType || undefined,
    minPrice: debouncedMinPrice ? Number(debouncedMinPrice) : undefined,
    maxPrice: debouncedMaxPrice ? Number(debouncedMaxPrice) : undefined,
    page,
    size: 20,
    sortBy: 'relevance',
  };

  const { data, isLoading, error, isFetching } = useQuery({
    queryKey: ['products', searchParams],
    queryFn: () => searchProducts(searchParams),
    placeholderData: (prev) => prev,
  });

  useEffect(() => {
    setPage(0);
  }, [debouncedKeyword, mallType, debouncedMinPrice, debouncedMaxPrice]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
  };

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [page]);

  // 메인 테마 색상 (Diary와 통일)
  const themeColor = "text-[#4E652F]";
  const borderColor = "border-[#4E652F]";
  const bgColor = "bg-[#4E652F]";
  const hoverBgColor = "hover:bg-[#3d5024]";
  const ringColor = "focus:ring-[#4E652F]";

  return (
    <div className="min-h-screen bg-[#F9FAFB] pb-20">
      {/* 1. 헤더 & 필터 영역 (Sticky) */}
      <div className="sticky top-16 z-30 bg-white/95 backdrop-blur-md border-b border-gray-200 shadow-sm transition-all">
        <div className="max-w-7xl mx-auto px-4 py-5 space-y-5">

          <div className="flex flex-col md:flex-row gap-6 items-center">
            <h1 className={`text-2xl font-extrabold ${themeColor} shrink-0 hidden md:block`}>
              상품 검색
            </h1>

            {/* 검색창 */}
            <form onSubmit={handleSearch} className="relative w-full md:max-w-3xl">
              <div className="relative flex items-center group">
                {/* 입력 필드: 올리브 그린 테두리 적용 */}
                <input
                  type="text"
                  placeholder="필요한 식재료를 검색해보세요 (예: 유기농 계란)"
                  className={`block w-full pl-5 pr-14 py-3.5 border-2 rounded-full leading-5 bg-white placeholder-gray-400 text-gray-900 focus:outline-none transition-all shadow-sm ${borderColor}/30 focus:border-[#4E652F] focus:ring-4 focus:ring-[#4E652F]/10`}
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                />

                {/* 검색 버튼 */}
                <button
                  type="submit"
                  className={`absolute right-2 top-1.5 bottom-1.5 px-5 rounded-full text-white flex items-center justify-center shadow-sm transition-colors ${bgColor} ${hoverBgColor}`}
                >
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                </button>
              </div>
            </form>
          </div>

          {/* 필터 옵션 */}
          <div className="flex flex-wrap items-center gap-3 text-sm pl-1">
            <select
              className={`border border-gray-300 rounded-lg px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:border-[#4E652F] ${ringColor} cursor-pointer hover:border-gray-400 transition-colors`}
              value={mallType}
              onChange={(e) => setMallType(e.target.value)}
            >
              <option value="">모든 쇼핑몰</option>
              <option value="NAVER">네이버 쇼핑</option>
              <option value="COUPANG">쿠팡</option>
              <option value="11ST">11번가</option>
            </select>

            <div className={`flex items-center gap-2 bg-white border border-gray-300 rounded-lg px-3 py-2 focus-within:ring-2 focus-within:border-[#4E652F] ${ringColor}`}>
              <span className="text-gray-500 text-xs">가격</span>
              <input
                type="number"
                placeholder="최소"
                className="w-20 outline-none text-right bg-transparent placeholder:text-gray-300"
                value={minPrice}
                onChange={(e) => setMinPrice(e.target.value)}
              />
              <span className="text-gray-400">~</span>
              <input
                type="number"
                placeholder="최대"
                className="w-20 outline-none text-right bg-transparent placeholder:text-gray-300"
                value={maxPrice}
                onChange={(e) => setMaxPrice(e.target.value)}
              />
              <span className="text-gray-500 text-xs">원</span>
            </div>

            {isFetching && !isLoading && (
              <span className="text-xs text-[#4E652F] animate-pulse font-medium ml-auto bg-[#4E652F]/10 px-2 py-1 rounded-full">
                결과 갱신 중...
              </span>
            )}
          </div>
        </div>
      </div>

      {/* 2. 콘텐츠 영역 */}
      <div className="max-w-7xl mx-auto px-4 py-8">
        {isLoading ? (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
            {Array.from({ length: 10 }).map((_, i) => <ProductSkeleton key={i} />)}
          </div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <div className="bg-red-50 p-4 rounded-full mb-4">
              <svg className="w-8 h-8 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold text-gray-900">상품을 불러오지 못했습니다</h3>
            <p className="text-gray-500 mt-1">잠시 후 다시 시도해주세요.</p>
          </div>
        ) : data?.products.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-24 text-center bg-white rounded-xl border border-gray-100 shadow-sm">
            <div className="bg-gray-50 p-4 rounded-full mb-4">
              <svg className="w-10 h-10 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold text-gray-900">검색 결과가 없습니다</h3>
            <p className="text-gray-500 mt-2">
              다른 키워드로 검색하거나<br />
              필터 조건을 변경해보세요.
            </p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
              {data?.products.map((product) => (
                <a
                  key={product.id}
                  href={product.productUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="group block bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 overflow-hidden relative"
                >
                  {/* 이미지 영역 */}
                  <div className="aspect-square bg-gray-50 relative overflow-hidden">
                    {product.imageUrl ? (
                      <img
                        src={product.imageUrl}
                        alt={product.name}
                        className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                        loading="lazy"
                      />
                    ) : (
                      <div className="flex items-center justify-center h-full text-gray-300 bg-gray-100">
                        <svg className="w-12 h-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                    )}
                    {product.mallType && <MallBadge type={product.mallType} />}

                    {/* 호버 오버레이 */}
                    <div className="absolute inset-0 bg-black/10 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                      <div className="bg-white/90 p-2 rounded-full shadow-sm transform scale-90 group-hover:scale-100 transition-transform">
                        <svg className={`w-6 h-6 ${themeColor}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                        </svg>
                      </div>
                    </div>
                  </div>

                  {/* 정보 영역 */}
                  <div className="p-4">
                    <div className="h-11 mb-2 overflow-hidden">
                      <h3 className={`text-sm font-medium text-gray-900 line-clamp-2 leading-snug group-hover:text-[#4E652F] transition-colors`}>
                        {product.name}
                      </h3>
                    </div>

                    <div className="flex items-baseline gap-2 mt-1">
                      <span className="text-lg font-bold text-gray-900">
                        {product.price.toLocaleString()}
                        <span className="text-xs font-normal text-gray-500 ml-0.5">원</span>
                      </span>
                      {product.discountRate && product.discountRate > 0 && (
                        <span className="text-xs font-bold text-[#D32F2F] bg-red-50 px-1.5 py-0.5 rounded">
                          {product.discountRate}%
                        </span>
                      )}
                    </div>

                    {product.originalPrice && product.originalPrice > product.price && (
                      <div className="text-xs text-gray-400 line-through mt-0.5">
                        {product.originalPrice.toLocaleString()}원
                      </div>
                    )}

                    <div className="mt-3 pt-3 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500">
                      <span>{product.deliveryInfo || '배송정보 없음'}</span>
                      {product.reviewCount != null && product.reviewCount > 0 && (
                        <span className="flex items-center gap-1">
                            <svg className="w-3 h-3 text-yellow-400 fill-yellow-400" viewBox="0 0 20 20">
                              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                            </svg>
                          {product.reviewCount > 999 ? '999+' : product.reviewCount}
                          </span>
                      )}
                    </div>
                  </div>
                </a>
              ))}
            </div>

            {/* Pagination */}
            {data && data.totalPages > 0 && (
              <div className="mt-12 flex justify-center items-center gap-4">
                <button
                  disabled={page === 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  className="flex items-center gap-1 px-4 py-2 rounded-lg border border-gray-300 bg-white text-gray-700 text-sm font-medium hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                  </svg>
                  이전
                </button>
                <span className="text-sm font-medium text-gray-600">
                  <span className="text-gray-900 font-bold">{page + 1}</span> / {data.totalPages}
                </span>
                <button
                  disabled={page >= data.totalPages - 1}
                  onClick={() => setPage((p) => p + 1)}
                  className="flex items-center gap-1 px-4 py-2 rounded-lg border border-gray-300 bg-white text-gray-700 text-sm font-medium hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  다음
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}