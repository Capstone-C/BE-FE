// src/features/refrigerator/pages/RefrigeratorPage.tsx
import { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getRefrigeratorItems,
  createRefrigeratorItem,
  updateRefrigeratorItem,
  deleteRefrigeratorItem,
} from '@/apis/refrigerator.api';
import type {
  RefrigeratorItem,
  CreateRefrigeratorItemRequest,
  UpdateRefrigeratorItemRequest,
} from '@/types/refrigerator';
import { useToast } from '@/contexts/ToastContext';

function formatDDay(days: number | null): string {
  if (days === null || days === undefined) return '—';
  if (days === 0) return 'D-Day';
  if (days > 0) return `D-${days}`;
  return `D+${Math.abs(days)}`; // 지난 항목
}

function classForItem(item: RefrigeratorItem): string {
  if (item.expired) return 'bg-red-50';
  if (item.expirationSoon) return 'bg-orange-50';
  return 'bg-white';
}

function dDayTextColor(item: RefrigeratorItem): string {
  if (item.expired) return 'text-red-600 font-semibold';
  if (item.expirationSoon) return 'text-orange-600 font-semibold';
  return 'text-gray-700';
}

export default function RefrigeratorPage() {
  const [sortBy, setSortBy] = useState<'expirationDate' | 'name' | 'createdAt'>('expirationDate');
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingItem, setEditingItem] = useState<RefrigeratorItem | null>(null);
  const qc = useQueryClient();
  const { show: showToast } = useToast();
  const [formErrors, setFormErrors] = useState<{ [k: string]: string }>({});
  const [nameValue, setNameValue] = useState('');
  const nameInputRef = useRef<HTMLInputElement | null>(null);
  const [unitValue, setUnitValue] = useState('');
  const [showUnitSuggestions, setShowUnitSuggestions] = useState(false);
  const COMMON_UNITS = ['개', 'g', 'kg', 'ml', 'L', '포기', '팩', '봉', '캔'];

  useEffect(() => {
    if (showAddForm) {
      setFormErrors({});
      // 브라우저 페인트 이후 안전하게 포커스 보장
      requestAnimationFrame(() => nameInputRef.current?.focus());
      setTimeout(() => nameInputRef.current?.focus(), 0);
    }
  }, [showAddForm]);

  const { data, isPending, isError } = useQuery({
    queryKey: ['refrigeratorItems', sortBy],
    queryFn: () => getRefrigeratorItems(sortBy),
  });

  const createMutation = useMutation({
    mutationFn: (payload: CreateRefrigeratorItemRequest) => createRefrigeratorItem(payload),
    onSuccess: () => {
      showToast('식재료가 추가되었습니다.', { type: 'success' });
      qc.invalidateQueries({ queryKey: ['refrigeratorItems', sortBy] });
      setShowAddForm(false);
      setFormErrors({});
      setNameValue('');
      setUnitValue('');
    },
    onError: (error: any) => {
      const resp = error?.response;
      // 중복 식재료: 상태 409 또는 코드 DUPLICATE_ITEM 둘 다 지원
      if (resp?.status === 409 || resp?.data?.code === 'DUPLICATE_ITEM') {
        setFormErrors((prev) => ({ ...prev, name: '이미 등록된 식재료입니다. 기존 항목을 수정해주세요.' }));
        nameInputRef.current?.focus();
      } else if (resp?.data?.code === 'VALIDATION_ERROR') {
        const fieldErrors: Record<string, string> = {};
        resp.data.errors?.forEach((fe: any) => {
          fieldErrors[fe.field] = fe.message;
        });
        setFormErrors(fieldErrors);
        // 첫번째 에러 필드 포커스
        const firstField = Object.keys(fieldErrors)[0];
        if (firstField) {
          const formEl = document.querySelector('[data-refrigerator-add-form="true"]') as HTMLElement | null;
          const target = formEl?.querySelector(`[name="${firstField}"]`) as HTMLElement | null;
          (target || nameInputRef.current)?.focus();
        }
      } else {
        showToast('등록 중 오류가 발생했습니다.', { type: 'error' });
      }
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: UpdateRefrigeratorItemRequest }) =>
      updateRefrigeratorItem(id, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['refrigeratorItems', sortBy] });
      setEditingItem(null);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteRefrigeratorItem(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['refrigeratorItems', sortBy] }),
  });

  const handleAddSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setFormErrors({});
    const form = e.currentTarget as HTMLFormElement;
    const formData = new FormData(form);
    const name = String(formData.get('name')).trim();
    const quantityStr = String(formData.get('quantity')).trim();
    const unit = String(formData.get('unit')).trim();
    const expirationDate = String(formData.get('expirationDate')).trim();
    const memo = String(formData.get('memo')).trim();

    const errors: Record<string, string> = {};
    if (!name) {
      errors.name = '식재료명을 입력해주세요.';
    } else if (name.length > 50) {
      errors.name = '식재료명은 50자 이하이어야 합니다.';
    }

    if (quantityStr) {
      if (!/^\d+$/.test(quantityStr)) {
        errors.quantity = '수량은 숫자로만 입력할 수 있습니다.';
      } else if (Number(quantityStr) < 0) {
        errors.quantity = '수량은 0 이상이어야 합니다.';
      }
    }

    if (unit) {
      if (unit.length > 10) {
        errors.unit = '단위는 10자 이하이어야 합니다.';
      }
    }

    if (memo) {
      if (memo.length > 200) {
        errors.memo = '메모는 200자 이하이어야 합니다.';
      }
    }

    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      // 포커스 처리
      const firstField = Object.keys(errors)[0];
      const el = form.querySelector(`[name="${firstField}"]`) as HTMLElement | null;
      el?.focus();
      return;
    }

    const payload: CreateRefrigeratorItemRequest = {
      name,
      quantity: quantityStr ? Number(quantityStr) : undefined,
      unit: unit || undefined,
      expirationDate: expirationDate || undefined,
      memo: memo || undefined,
    };
    createMutation.mutate(payload);
  };

  const handleEditSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!editingItem) return;
    const form = e.currentTarget as HTMLFormElement;
    const formData = new FormData(form);
    const payload: UpdateRefrigeratorItemRequest = {
      quantity: formData.get('quantity') ? Number(formData.get('quantity')) : undefined,
      unit: formData.get('unit') ? String(formData.get('unit')) : undefined,
      expirationDate: formData.get('expirationDate') ? String(formData.get('expirationDate')) : undefined,
      memo: formData.get('memo') ? String(formData.get('memo')) : undefined,
    };
    updateMutation.mutate({ id: editingItem.id, payload });
  };

  const items: RefrigeratorItem[] = data?.items ?? [];

  return (
    <div className="max-w-6xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-6">내 냉장고</h1>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <label htmlFor="sort" className="text-sm text-gray-600">
            정렬:
          </label>
          <select
            id="sort"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="border rounded px-2 py-1 text-sm"
          >
            <option value="expirationDate">소비기한 임박순</option>
            <option value="name">이름순</option>
            <option value="createdAt">등록일순</option>
          </select>
        </div>
        <button
          onClick={() => setShowAddForm((v) => !v)}
          className="px-4 py-2 rounded bg-blue-600 text-white text-sm hover:bg-blue-700"
        >
          식재료 추가
        </button>
      </div>

      {showAddForm && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center p-4">
          <div className="bg-white w-full max-w-lg rounded shadow p-6">
            <h2 className="text-lg font-semibold mb-4">식재료 추가</h2>
            <form onSubmit={handleAddSubmit} className="space-y-4" data-refrigerator-add-form="true">
              <div>
                <label className="block text-xs text-gray-600">식재료명 *</label>
                <input
                  name="name"
                  ref={nameInputRef}
                  value={nameValue}
                  onChange={(e) => setNameValue(e.target.value)}
                  autoFocus
                  className={`mt-1 w-full border rounded px-2 py-2 text-sm ${formErrors.name ? 'border-red-500' : ''}`}
                />
                {formErrors.name && <p className="mt-1 text-xs text-red-600">{formErrors.name}</p>}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs text-gray-600">수량</label>
                  <input
                    name="quantity"
                    type="number"
                    min={0}
                    className={`mt-1 w-full border rounded px-2 py-2 text-sm ${formErrors.quantity ? 'border-red-500' : ''}`}
                  />
                  {formErrors.quantity && <p className="mt-1 text-xs text-red-600">{formErrors.quantity}</p>}
                </div>
                <div className="relative">
                  <label className="block text-xs text-gray-600">단위</label>
                  <input
                    name="unit"
                    value={unitValue}
                    onChange={(e) => setUnitValue(e.target.value)}
                    onFocus={() => setShowUnitSuggestions(true)}
                    onBlur={() => setTimeout(() => setShowUnitSuggestions(false), 150)}
                    className={`mt-1 w-full border rounded px-2 py-2 text-sm ${formErrors.unit ? 'border-red-500' : ''}`}
                    placeholder="예: 개, g, ml"
                  />
                  {showUnitSuggestions && (
                    <ul className="absolute top-full left-0 right-0 bg-white border rounded shadow mt-1 max-h-40 overflow-auto text-xs z-10">
                      {COMMON_UNITS.filter((u) => !unitValue || u.includes(unitValue)).map((u) => (
                        <li
                          key={u}
                          className="px-2 py-1 hover:bg-blue-50 cursor-pointer"
                          onMouseDown={(e) => {
                            e.preventDefault();
                            setUnitValue(u);
                          }}
                        >
                          {u}
                        </li>
                      ))}
                      {COMMON_UNITS.filter((u) => !unitValue || u.includes(unitValue)).length === 0 && (
                        <li className="px-2 py-1 text-gray-400">일치하는 제안 없음</li>
                      )}
                    </ul>
                  )}
                  {formErrors.unit && <p className="mt-1 text-xs text-red-600">{formErrors.unit}</p>}
                </div>
              </div>
              <div>
                <label className="block text-xs text-gray-600">소비기한</label>
                <input name="expirationDate" type="date" className="mt-1 w-full border rounded px-2 py-2 text-sm" />
              </div>
              <div>
                <label className="block text-xs text-gray-600">메모</label>
                <textarea
                  name="memo"
                  rows={3}
                  className={`mt-1 w-full border rounded px-2 py-2 text-sm resize-none ${formErrors.memo ? 'border-red-500' : ''}`}
                />
                {formErrors.memo && <p className="mt-1 text-xs text-red-600">{formErrors.memo}</p>}
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAddForm(false)}
                  className="px-4 py-2 bg-gray-200 rounded text-sm hover:bg-gray-300"
                >
                  취소
                </button>
                <button
                  type="submit"
                  disabled={createMutation.isPending}
                  className="px-4 py-2 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 disabled:opacity-50"
                >
                  저장
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {isPending && <div className="p-4 text-center text-gray-600">불러오는 중...</div>}
      {isError && <div className="p-4 text-center text-red-600">목록을 불러오는 중 오류가 발생했습니다.</div>}

      {!isPending && !isError && items.length === 0 && (
        <div className="bg-white p-8 rounded shadow text-center">
          <p className="text-lg font-medium mb-2">냉장고가 비어있습니다. 첫 식재료를 추가해보세요!</p>
        </div>
      )}

      {items.length > 0 && (
        <div className="bg-white rounded shadow overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead>
              <tr className="bg-gray-100 text-left">
                <th className="px-3 py-2">식재료명</th>
                <th className="px-3 py-2">수량</th>
                <th className="px-3 py-2">단위</th>
                <th className="px-3 py-2">소비기한 (D-day)</th>
                <th className="px-3 py-2">메모</th>
                <th className="px-3 py-2">관리</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item: RefrigeratorItem) => (
                <tr key={item.id} className={`${classForItem(item)} border-t`}>
                  <td className="px-3 py-2 font-medium">{item.name}</td>
                  <td className="px-3 py-2">{item.quantity}</td>
                  <td className="px-3 py-2">{item.unit ?? '—'}</td>
                  <td className={`px-3 py-2 ${dDayTextColor(item)}`}>
                    {item.expirationDate ? `${item.expirationDate} (${formatDDay(item.daysUntilExpiration)})` : '—'}
                  </td>
                  <td className="px-3 py-2 max-w-xs truncate" title={item.memo ?? ''}>
                    {item.memo ?? '—'}
                  </td>
                  <td className="px-3 py-2 space-x-2">
                    <button
                      onClick={() => setEditingItem(item)}
                      className="px-2 py-1 bg-gray-200 rounded hover:bg-gray-300"
                    >
                      수정
                    </button>
                    <button
                      onClick={() => {
                        if (confirm('삭제하시겠습니까?')) deleteMutation.mutate(item.id);
                      }}
                      className="px-2 py-1 bg-red-600 text-white rounded hover:bg-red-700"
                    >
                      삭제
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="p-4 text-xs text-gray-500 flex gap-4">
            <span>총 {data?.totalCount}개</span>
            <span>임박 {data?.expiringCount}개</span>
            <span>지남 {data?.expiredCount}개</span>
          </div>
        </div>
      )}

      {editingItem && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center p-4">
          <div className="bg-white w-full max-w-md rounded shadow p-6 space-y-4">
            <h2 className="text-lg font-semibold">식재료 수정 - {editingItem.name}</h2>
            <form onSubmit={handleEditSubmit} className="space-y-3">
              <div>
                <label className="block text-xs text-gray-600">수량</label>
                <input
                  name="quantity"
                  type="number"
                  min={0}
                  defaultValue={editingItem.quantity}
                  className="mt-1 w-full border rounded px-2 py-1 text-sm"
                />
              </div>
              <div>
                <label className="block text-xs text-gray-600">단위</label>
                <input
                  name="unit"
                  defaultValue={editingItem.unit ?? ''}
                  className="mt-1 w-full border rounded px-2 py-1 text-sm"
                />
              </div>
              <div>
                <label className="block text-xs text-gray-600">소비기한</label>
                <input
                  name="expirationDate"
                  type="date"
                  defaultValue={editingItem.expirationDate ?? ''}
                  className="mt-1 w-full border rounded px-2 py-1 text-sm"
                />
              </div>
              <div>
                <label className="block text-xs text-gray-600">메모</label>
                <textarea
                  name="memo"
                  rows={2}
                  defaultValue={editingItem.memo ?? ''}
                  className="mt-1 w-full border rounded px-2 py-1 text-sm"
                />
              </div>
              <div className="flex gap-2 pt-2">
                <button
                  type="submit"
                  disabled={updateMutation.isPending}
                  className="px-4 py-2 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 disabled:opacity-50"
                >
                  저장
                </button>
                <button
                  type="button"
                  onClick={() => setEditingItem(null)}
                  className="px-4 py-2 bg-gray-200 rounded text-sm hover:bg-gray-300"
                >
                  닫기
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
