import { authClient } from '@/apis/client';

export interface ImageUploadResponse {
  url: string;
}

export async function uploadImage(file: File): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);

  const { data } = await authClient.post<ImageUploadResponse>('/api/v1/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return data.url;
}