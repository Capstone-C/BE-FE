import { publicClient } from '@/apis/client';

export interface ProductSearchRequest {
    keyword?: string;
    mallType?: string;
    category?: string;
    minPrice?: number;
    maxPrice?: number;
    sortBy?: string;
    page?: number;
    size?: number;
}

export interface ProductDto {
    id: string;
    name: string;
    price: number;
    originalPrice?: number;
    discountRate?: number;
    imageUrl: string;
    productUrl: string;
    mallType: string;
    category: string;
    rating?: number;
    reviewCount?: number;
    deliveryInfo?: string;
}

export interface ProductSearchResponse {
    products: ProductDto[];
    totalCount: number;
    currentPage: number;
    totalPages: number;
    pageSize: number;
    aggregations?: Record<string, unknown>;
}

export const searchProducts = async (params: ProductSearchRequest): Promise<ProductSearchResponse> => {
    const { data } = await publicClient.get<ProductSearchResponse>('/api/v1/shopping/search', {
        params,
    });
    return data;
};
