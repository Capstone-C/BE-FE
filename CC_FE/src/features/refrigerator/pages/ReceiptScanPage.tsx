import { useEffect, useMemo, useRef, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { bulkCreateRefrigeratorItems, scanPurchaseHistory } from '@/apis/refrigerator.api';
import type {
  BulkCreateRequest,
  CreateRefrigeratorItemRequest,
  PurchasedItem,
  ScanPurchaseHistoryResponse,
} from '@/types/refrigerator';
import { useToast } from '@/contexts/ToastContext';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { ko } from 'date-fns/locale';
import { useNavigate } from 'react-router-dom';

// Client-side image compression helper (canvas-based)
async function compressImage(file: File, maxBytes = 5 * 1024 * 1024): Promise<File> {
  if (file.size <= maxBytes) return file;

  const img = document.createElement('img');
  const reader = new FileReader();
  const dataUrl: string = await new Promise((resolve, reject) => {
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
  img.src = dataUrl;
  await new Promise((res) => (img.onload = () => res(null)));

  // Downscale keeping aspect ratio
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  if (!ctx) return file;

  const MAX_WIDTH = 1600; // reasonable size for OCR
  const scale = Math.min(1, MAX_WIDTH / img.naturalWidth);
  canvas.width = Math.floor(img.naturalWidth * scale);
  canvas.height = Math.floor(img.naturalHeight * scale);
  ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

  let quality = 0.85;
  let blob: Blob | null = await new Promise((resolve) => canvas.toBlob(resolve, 'image/jpeg', quality));

  // Try multiple qualities to fit under limit
  while (blob && blob.size > maxBytes && quality > 0.4) {
    quality -= 0.1;
    blob = await new Promise((resolve) => canvas.toBlob(resolve, 'image/jpeg', quality));
  }

  if (!blob) return file;
  const compressed = new File([blob], file.name.replace(/\.[^.]+$/, '.jpg'), { type: 'image/jpeg' });
  return compressed.size < file.size ? compressed : file;
}

const toYmd = (d: Date | null): string | undefined => {
  if (!d) return undefined;
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

interface EditableItem {
  id: string; // local id
  checked: boolean;
  name: string;
  quantity?: number;
  unit?: string;
  expirationDate?: Date | null;
  memo?: string;
}

// Utility to create a blank editable item row
function newBlankItem(idx: number): EditableItem {
  return {
    id: `manual-${idx}-${Date.now()}`,
    checked: true,
    name: '',
    quantity: 1,
    unit: undefined,
    expirationDate: null,
    memo: undefined,
  };
}

const UNIT_OPTIONS = ['', '개', '팩', '봉', '병', '캔', '컵', '박스', 'g', 'kg', 'ml', 'L', '포', '묶음'];

export default function ReceiptScanPage() {
  const { show: showToast } = useToast();
  const navigate = useNavigate();

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [loadingText, setLoadingText] = useState<string | null>(null);
  const [editableItems, setEditableItems] = useState<EditableItem[]>([]);
  const [failedItemsFeedback, setFailedItemsFeedback] = useState<string[]>([]);

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  const scanMutation = useMutation({
    mutationFn: async (file: File) => {
      setLoadingText('영수증을 분석하고 있습니다. 잠시만 기다려주세요...');
      const compressed = await compressImage(file);
      return scanPurchaseHistory(compressed);
    },
    onSuccess: (data: ScanPurchaseHistoryResponse) => {
      setLoadingText(null);
      // Map to editable items - only food guessed by backend, but we let user curate
      const mapped: EditableItem[] = (data.items || []).map((it: PurchasedItem, idx) => ({
        id: `${idx}`,
        checked: true,
        name: (it.name || '').replace(/\s+\d+(ml|l|g|kg|개|팩|봉|캔)?$/i, '').trim(),
        quantity: it.quantity && it.quantity > 0 ? it.quantity : 1,
        unit: it.unit || '', // preserve inferred unit
        expirationDate: null,
        memo: undefined,
      }));
      setEditableItems(mapped);

      if (!mapped.length) {
        // Case 2: insufficient results
        showToast('영수증 인식이 원활하지 않습니다. 더 밝고 선명한 사진으로 다시 시도해주세요.', { type: 'warning' });
      }

      if (mapped.length < 5) {
        showToast('영수증 인식 결과가 적습니다. 더 밝고 선명한 사진으로 다시 시도하거나 수동으로 항목을 추가하세요.', {
          type: 'warning',
        });
      }
    },
    onError: (err: any) => {
      setLoadingText(null);
      const status = err?.response?.status;
      if (status === 429 || status === 502 || status === 503) {
        showToast('영수증 인식 서비스가 원활하지 않습니다. 잠시 후 다시 시도해주세요.', { type: 'error' });
      } else {
        showToast('영수증 인식 서비스에 오류가 발생했습니다. 잠시 후 다시 시도해주세요.', { type: 'error' });
      }
    },
  });

  const bulkMutation = useMutation({
    mutationFn: async (payload: BulkCreateRequest) => bulkCreateRefrigeratorItems(payload),
    onSuccess: (res) => {
      const successNames = res.addedItems
        .map((i) => `'${i.name}'`)
        .slice(0, 3)
        .join(', ');
      const successPrefix = successNames ? `${successNames} 등 ` : '';
      const msg = `${successPrefix}${res.successCount}개의 식재료가 냉장고에 추가되었습니다.`;
      const failMsg = res.failCount > 0 ? ` (${res.failCount}개 항목은 추가되지 않음)` : '';
      showToast(msg + failMsg, { type: 'success' });
      navigate('/refrigerator');

      if (res.failCount > 0) {
        setFailedItemsFeedback(res.failedItems);
      }
    },
    onError: () => {
      showToast('등록 중 오류가 발생했습니다.', { type: 'error' });
    },
  });

  const handlePickFile = () => fileInputRef.current?.click();

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setSelectedFile(file);
    const url = URL.createObjectURL(file);
    setPreviewUrl((prev) => {
      if (prev) URL.revokeObjectURL(prev);
      return url;
    });
  };

  const handleAnalyze = () => {
    if (!selectedFile) {
      showToast('먼저 영수증 이미지를 선택해주세요.', { type: 'info' });
      return;
    }
    scanMutation.mutate(selectedFile);
  };

  const handleToggleItem = (id: string) => {
    setEditableItems((prev) => prev.map((it) => (it.id === id ? { ...it, checked: !it.checked } : it)));
  };

  const handleChangeItem = (
    id: string,
    patch: Partial<Pick<EditableItem, 'name' | 'quantity' | 'unit' | 'memo' | 'expirationDate'>>,
  ) => {
    setEditableItems((prev) => prev.map((it) => (it.id === id ? { ...it, ...patch } : it)));
  };

  const selectedCount = useMemo(() => editableItems.filter((i) => i.checked).length, [editableItems]);

  const handleBulkAdd = () => {
    const items: CreateRefrigeratorItemRequest[] = editableItems
      .filter((i) => i.checked && i.name.trim())
      .map((i) => ({
        name: i.name.trim(),
        quantity: i.quantity && i.quantity >= 0 ? i.quantity : undefined,
        unit: i.unit?.trim() || undefined,
        expirationDate: toYmd(i.expirationDate ?? null),
        memo: i.memo?.trim() || undefined,
      }));

    if (!items.length) {
      showToast('추가할 항목을 선택해주세요.', { type: 'info' });
      return;
    }

    const payload: BulkCreateRequest = { items };
    bulkMutation.mutate(payload);
  };

  // Add manual blank item row
  const handleAddManualItem = () => {
    setEditableItems((prev) => [...prev, newBlankItem(prev.length)]);
  };

  const handleSelectAllToggle = () => {
    const allSelected = editableItems.every((i) => i.checked);
    setEditableItems((prev) => prev.map((it) => ({ ...it, checked: !allSelected })));
  };

  return (
    <div className="max-w-3xl mx-auto p-6">
      {/* Header */}
      <h1 className="text-2xl font-bold mb-4">영수증으로 추가</h1>
      <p className="text-sm text-gray-600 mb-6 leading-relaxed">
        종이 영수증을 촬영하거나 선택하여 자동으로 식재료명을 추출한 뒤, 필요한 최소 수정으로 한 번에 등록할 수
        있습니다.
        <br /> 불필요한 항목(봉투, 할인 등)은 체크 해제하거나 삭제하세요. 인식이 부정확하면 더 선명한 사진으로 다시
        분석하거나 수동으로 행을 추가할 수 있습니다.
      </p>
      {/* Main card */}
      <div className="bg-white rounded shadow p-4 space-y-4">
        {/* File controls */}
        <div className="flex flex-wrap items-center gap-2">
          {/* file input */}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            capture="environment"
            className="hidden"
            onChange={handleFileChange}
          />
          <button onClick={handlePickFile} className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 text-sm">
            사진 촬영/선택
          </button>
          {selectedFile && <span className="text-sm text-gray-700">{selectedFile.name}</span>}
          <button
            onClick={handleAnalyze}
            disabled={!selectedFile || scanMutation.isPending}
            className="px-4 py-2 bg-blue-600 text-white rounded text-sm disabled:opacity-50 hover:bg-blue-700"
          >
            {scanMutation.isPending ? '분석 중…' : '영수증 분석'}
          </button>
          <button
            onClick={handleAddManualItem}
            className="px-4 py-2 bg-green-100 text-green-700 rounded text-sm hover:bg-green-200"
            type="button"
          >
            행 추가
          </button>
          {editableItems.length > 0 && (
            <button
              onClick={handleSelectAllToggle}
              type="button"
              className="px-4 py-2 bg-gray-100 text-gray-700 rounded text-sm hover:bg-gray-200"
            >
              {editableItems.every((i) => i.checked) ? '전체 선택 해제' : '전체 선택'}
            </button>
          )}
        </div>

        {/* Failed items feedback (after bulk) */}
        {failedItemsFeedback.length > 0 && (
          <div className="border border-yellow-300 bg-yellow-50 rounded p-3 text-xs text-yellow-800">
            등록되지 않은 항목: {failedItemsFeedback.slice(0, 5).join(', ')}
            {failedItemsFeedback.length > 5 && ` 외 ${failedItemsFeedback.length - 5}개`}
          </div>
        )}

        {previewUrl && (
          <div className="mt-2">
            <img src={previewUrl} alt="미리보기" className="max-h-72 object-contain mx-auto border rounded" />
          </div>
        )}

        {loadingText && <div className="py-8 text-center text-gray-700 animate-pulse">{loadingText}</div>}

        {editableItems.length > 0 && (
          <div className="mt-4">
            <h2 className="font-semibold mb-2">검토 및 수정</h2>
            <div className="space-y-3">
              {editableItems.map((it) => (
                <div key={it.id} className="border rounded p-3 flex flex-col gap-2 bg-gray-50">
                  <div className="flex items-center gap-2">
                    <input type="checkbox" checked={it.checked} onChange={() => handleToggleItem(it.id)} />
                    <input
                      value={it.name}
                      onChange={(e) => handleChangeItem(it.id, { name: e.target.value })}
                      className="flex-1 border rounded px-2 py-1 text-sm focus:ring focus:ring-blue-100"
                      placeholder="식재료명 (예: 양파)"
                    />
                    <button
                      onClick={() => setEditableItems((prev) => prev.filter((x) => x.id !== it.id))}
                      className="px-2 py-1 text-xs bg-red-50 text-red-600 rounded hover:bg-red-100"
                    >
                      삭제
                    </button>
                  </div>
                  <div className="grid grid-cols-4 gap-2">
                    <div>
                      <label className="block text-[10px] text-gray-500">수량</label>
                      <input
                        type="number"
                        min={0}
                        value={it.quantity ?? ''}
                        onChange={(e) =>
                          handleChangeItem(it.id, { quantity: e.target.value ? Number(e.target.value) : undefined })
                        }
                        className="w-full border rounded px-2 py-1 text-sm"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] text-gray-500">단위</label>
                      <select
                        value={it.unit ?? ''}
                        onChange={(e) => handleChangeItem(it.id, { unit: e.target.value })}
                        className="w-full border rounded px-2 py-1 text-sm bg-white"
                      >
                        {UNIT_OPTIONS.map((u) => (
                          <option key={u} value={u}>
                            {u === '' ? '(없음)' : u}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div className="col-span-2">
                      <label className="block text-[10px] text-gray-500">소비기한</label>
                      <DatePicker
                        selected={it.expirationDate ?? null}
                        onChange={(date) => handleChangeItem(it.id, { expirationDate: date })}
                        dateFormat="yyyy-MM-dd"
                        locale={ko}
                        placeholderText="선택"
                        className="w-full border rounded px-2 py-1 text-sm"
                        isClearable
                      />
                    </div>
                    <div className="col-span-4">
                      <label className="block text-[10px] text-gray-500">메모</label>
                      <input
                        value={it.memo ?? ''}
                        onChange={(e) => handleChangeItem(it.id, { memo: e.target.value })}
                        className="w-full border rounded px-2 py-1 text-sm"
                        placeholder="메모 (선택)"
                      />
                    </div>
                  </div>
                  {/* end grid */}
                </div>
              ))}
            </div>
            {/* end space-y-3 */}
            <div className="mt-6 flex flex-wrap items-center gap-3 border-t pt-4">
              <div className="text-sm text-gray-600">
                선택된 항목: <span className="font-semibold">{selectedCount}</span> / {editableItems.length}
              </div>
              <button
                type="button"
                onClick={handleBulkAdd}
                disabled={bulkMutation.isPending}
                className="px-5 py-2 bg-emerald-600 text-white rounded text-sm disabled:opacity-50 hover:bg-emerald-700"
              >
                {bulkMutation.isPending ? '등록 중…' : '선택 항목 일괄 추가'}
              </button>
            </div>
          </div>
        )}
      </div>
      {/* end main card */}
    </div>
  );
}
