import { useState, useRef, ChangeEvent } from 'react';
import { uploadImage } from '@/apis/image.api';
import Spinner from '@/components/ui/Spinner';

interface ImageUploaderProps {
  value?: string | null;
  onChange: (url: string) => void;
  placeholder?: string;
  className?: string;
}

export default function ImageUploader({ value, onChange, placeholder = "이미지 업로드", className = "" }: ImageUploaderProps) {
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 간단한 유효성 검사
    if (!file.type.startsWith('image/')) {
      alert('이미지 파일만 업로드 가능합니다.');
      return;
    }

    try {
      setUploading(true);
      const url = await uploadImage(file);
      onChange(url);
    } catch (error) {
      console.error('Image upload failed', error);
      alert('이미지 업로드에 실패했습니다.');
    } finally {
      setUploading(false);
      // 같은 파일 재선택 가능하도록 초기화
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleRemove = () => {
    onChange('');
  };

  return (
    <div className={`flex flex-col gap-2 ${className}`}>
      {value ? (
        <div className="relative w-full h-48 bg-gray-100 rounded-lg overflow-hidden border group">
          <img src={value} alt="Uploaded" className="w-full h-full object-cover" />
          <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="px-3 py-1 bg-white rounded text-sm font-medium hover:bg-gray-100"
            >
              변경
            </button>
            <button
              type="button"
              onClick={handleRemove}
              className="px-3 py-1 bg-red-600 text-white rounded text-sm font-medium hover:bg-red-700"
            >
              삭제
            </button>
          </div>
        </div>
      ) : (
        <div
          onClick={() => !uploading && fileInputRef.current?.click()}
          className={`
            w-full h-48 border-2 border-dashed rounded-lg flex flex-col items-center justify-center cursor-pointer transition-colors
            ${uploading ? 'bg-gray-50 border-gray-300 cursor-wait' : 'border-gray-300 hover:border-blue-500 hover:bg-blue-50'}
          `}
        >
          {uploading ? (
            <div className="flex flex-col items-center gap-2">
              <Spinner size={24} />
              <span className="text-sm text-gray-500">업로드 중...</span>
            </div>
          ) : (
            <>
              <div className="text-4xl text-gray-300 mb-2">+</div>
              <span className="text-sm text-gray-500">{placeholder}</span>
              <span className="text-xs text-gray-400 mt-1">(클릭하여 선택)</span>
            </>
          )}
        </div>
      )}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={handleFileChange}
      />
    </div>
  );
}