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
    <div className="max-w-7xl mx-auto px-8 py-16">
      <h1 className="text-5xl font-bold mb-10 gradient-text">🛒 쇼핑 / 상품 검색</h1>

      {/* Search & Filters */}
      <div className="bg-white p-8 rounded-lg shadow-md space-y-6">
        <form onSubmit={handleSearch} className="flex gap-3">
          <input
            type="text"
            placeholder="상품명 검색..."
            className="flex-1 border-2 border-gray-200 px-4 py-3 rounded-lg text-base focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <button
            type="submit"
            className="px-6 py-3 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-lg hover:shadow-lg hover:scale-105 transition-all font-semibold text-base"
            aria-label="검색"
          >
            검색
          </button>
        </form>

        <div className="flex flex-wrap gap-4">
          <select className="border-2 border-gray-200 px-4 py-3 rounded-lg text-base focus:ring-2 focus:ring-purple-500 focus:border-transparent" value={mallType} onChange={(e) => setMallType(e.target.value)}>
            <option value="">모든 쇼핑몰</option>
            <option value="NAVER">네이버</option>
            <option value="COUPANG">쿠팡</option>
            <option value="11ST">11번가</option>
          </select>

          <div className="flex items-center gap-3">
            <input
              type="number"
              placeholder="최소 가격"
              className="border-2 border-gray-200 px-4 py-3 rounded-lg w-40 text-base focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
            />
            <span className="text-gray-500">~</span>
            <input
              type="number"
              placeholder="최대 가격"
              className="border-2 border-gray-200 px-4 py-3 rounded-lg w-40 text-base focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
            />
          </div>
        </div>
      </div>

      {/* Results */}
      {isLoading || isFetching ? (
        <div className="text-center py-16 text-lg text-gray-600">로딩 중...</div>
      ) : error ? (
        <div className="text-center py-16 text-lg text-red-500">에러가 발생했습니다.</div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mt-8">
            {data?.products.map((product) => (
              <a
                key={product.id}
                href={product.productUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="block bg-white rounded-lg shadow hover:shadow-md transition overflow-hidden"
              >
                <div className="aspect-square bg-gray-100 relative">
                  {product.imageUrl ? (
                    <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
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
                  <h3 className="font-medium line-clamp-2 h-12">{product.name}</h3>
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
                className="px-4 py-2 border rounded disabled:opacity-50"
              >
                이전
              </button>
              <span className="px-4 py-2">
                {page + 1} / {data.totalPages}
              </span>
              <button
                disabled={page >= data.totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="px-4 py-2 border rounded disabled:opacity-50"
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
