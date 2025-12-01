import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { searchProducts, ProductSearchRequest } from '@/apis/shopping.api';
import { useDebounce } from '@/hooks/useDebounce';

export default function ShoppingPage() {
  const [keyword, setKeyword] = useState(''); // input field value
  const [submittedKeyword, setSubmittedKeyword] = useState(''); // keyword actually used for search
  const [mallType, setMallType] = useState<string>('');
  const [minPrice, setMinPrice] = useState<string>('');
  const [maxPrice, setMaxPrice] = useState<string>('');
  const [page, setPage] = useState(0);

  // Only debounce price filters to avoid rapid requests while typing numbers
  const debouncedMinPrice = useDebounce(minPrice, 500);
  const debouncedMaxPrice = useDebounce(maxPrice, 500);

  const searchParams: ProductSearchRequest = {
    keyword: submittedKeyword || undefined,
    mallType: mallType || undefined,
    minPrice: debouncedMinPrice ? Number(debouncedMinPrice) : undefined,
    maxPrice: debouncedMaxPrice ? Number(debouncedMaxPrice) : undefined,
    page,
    size: 20,
    sortBy: 'relevance',
  };

  const { data, isLoading, error, isFetching } = useQuery({
    queryKey: [
      'products',
      submittedKeyword || null,
      mallType || null,
      debouncedMinPrice ? Number(debouncedMinPrice) : null,
      debouncedMaxPrice ? Number(debouncedMaxPrice) : null,
      page,
      20,
      'relevance',
    ],
    queryFn: () => searchProducts(searchParams),
    enabled: (submittedKeyword?.length ?? 0) > 0,
    staleTime: 0,
    refetchOnMount: 'always',
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    setSubmittedKeyword(keyword.trim());
  };

  return (
    <div className="container mx-auto p-4 space-y-6">
      <h1 className="text-2xl font-bold">쇼핑 / 상품 검색</h1>

      {/* Search & Filters */}
      <div className="bg-white p-4 rounded-lg shadow space-y-4">
        <form onSubmit={handleSearch} className="flex gap-2">
          <input
            type="text"
            placeholder="상품명 검색..."
            className="flex-1 border p-2 rounded focus:outline-none focus:ring-2 focus:ring-[#71853A]"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <button
            type="submit"
            // [수정됨] 버튼 색상 변경: bg-blue-600 -> bg-[#4E652F]
            className="px-4 py-2 bg-[#4E652F] text-white rounded hover:bg-[#425528] transition-colors"
            aria-label="검색"
          >
            검색
          </button>
        </form>

        <div className="flex flex-wrap gap-4">
          <select
            className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-[#71853A]"
            value={mallType}
            onChange={(e) => setMallType(e.target.value)}
          >
            <option value="">모든 쇼핑몰</option>
            <option value="NAVER">네이버</option>
            <option value="COUPANG">쿠팡</option>
            <option value="11ST">11번가</option>
          </select>

          <div className="flex items-center gap-2">
            <input
              type="number"
              placeholder="최소 가격"
              className="border p-2 rounded w-32 focus:outline-none focus:ring-2 focus:ring-[#71853A]"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
            />
            <span>~</span>
            <input
              type="number"
              placeholder="최대 가격"
              className="border p-2 rounded w-32 focus:outline-none focus:ring-2 focus:ring-[#71853A]"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
            />
          </div>
        </div>
      </div>

      {/* Results */}
      {isLoading || isFetching ? (
        <div className="text-center py-10">로딩 중...</div>
      ) : error ? (
        <div className="text-center py-10 text-red-500">에러가 발생했습니다.</div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {data?.products.map((product) => (
              <a
                key={product.id}
                href={product.productUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="block bg-white rounded-lg shadow hover:shadow-md transition overflow-hidden group"
              >
                <div className="aspect-square bg-gray-100 relative">
                  {product.imageUrl ? (
                    <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
                  ) : (
                    <div className="flex items-center justify-center h-full text-gray-400">No Image</div>
                  )}
                  {product.mallType && (
                    <span className="absolute top-2 left-2 bg-black/50 text-white text-xs px-2 py-1 rounded">
                      {product.mallType}
                    </span>
                  )}
                </div>
                <div className="p-4 space-y-2">
                  <h3 className="font-medium line-clamp-2 h-12 group-hover:text-[#4E652F] transition-colors">{product.name}</h3>
                  <div className="flex justify-between items-end">
                    <div className="font-bold text-lg">{product.price.toLocaleString()}원</div>
                    {product.discountRate && product.discountRate > 0 && (
                      <div className="text-red-500 font-bold">{product.discountRate}%</div>
                    )}
                  </div>
                  {product.deliveryInfo && <div className="text-sm text-gray-500">{product.deliveryInfo}</div>}
                </div>
              </a>
            ))}
          </div>

          {/* Pagination */}
          {data && data.totalPages > 0 && (
            <div className="flex justify-center gap-2 mt-8">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="px-4 py-2 border rounded disabled:opacity-50 hover:bg-gray-50"
              >
                이전
              </button>
              <span className="px-4 py-2">
                {page + 1} / {data.totalPages}
              </span>
              <button
                disabled={page >= data.totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="px-4 py-2 border rounded disabled:opacity-50 hover:bg-gray-50"
              >
                다음
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}