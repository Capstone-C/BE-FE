import React from 'react';
import { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getRefrigeratorItems,
  createRefrigeratorItem,
  updateRefrigeratorItem,
  deleteRefrigeratorItem,
  getRefrigeratorItem,
} from '@/apis/refrigerator.api';
import type {
  RefrigeratorItem,
  CreateRefrigeratorItemRequest,
  UpdateRefrigeratorItemRequest,
} from '@/types/refrigerator';
import { useToast } from '@/contexts/ToastContext';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { ko } from 'date-fns/locale';
import { formatDateYMDKorean, toYmd } from '@/utils/date';
import { Link } from 'react-router-dom';
import { COMMON_UNITS } from '@/constants/units';
import { PlusIcon, ReceiptIcon, TrashIcon } from '@/components/ui/Icons';

function formatDDay(days: number | null): string {
  if (days === null || days === undefined) return '';
  if (days === 0) return '(D-Day)';
  if (days > 0) return `(D-${days})`;
  return `(D+${Math.abs(days)})`;
}

export default function RefrigeratorPage() {
  const [sortBy, setSortBy] = useState<'expirationDate' | 'name' | 'createdAt'>('expirationDate');
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingItem, setEditingItem] = useState<RefrigeratorItem | null>(null);
  const qc = useQueryClient();
  const { show: showToast } = useToast();

  // Add form state
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const nameInputRef = useRef<HTMLInputElement | null>(null);
  const [nameValue, setNameValue] = useState('');
  const [quantityValue, setQuantityValue] = useState('');
  const [unitValue, setUnitValue] = useState('');
  const [memoValue, setMemoValue] = useState('');
  const [addExpirationDate, setAddExpirationDate] = useState<Date | null>(null);
  const [showUnitSuggestions, setShowUnitSuggestions] = useState(false);

  // Edit form state
  const [editErrors, setEditErrors] = useState<Record<string, string>>({});
  const [editingPrefill, setEditingPrefill] = useState<RefrigeratorItem | null>(null);
  const [editExpirationDate, setEditExpirationDate] = useState<Date | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<RefrigeratorItem | null>(null);
  const [removingIds, setRemovingIds] = useState<Set<number>>(new Set());

  // [수정] isPending, isError 변수 사용하도록 수정
  const { data, isPending, isError } = useQuery({
    queryKey: ['refrigeratorItems', sortBy],
    queryFn: () => getRefrigeratorItems(sortBy),
  });

  useEffect(() => {
    if (showAddForm) {
      setFormErrors({});
      setNameValue('');
      setQuantityValue('');
      setUnitValue('');
      setMemoValue('');
      setAddExpirationDate(null);
      window.requestAnimationFrame(() => nameInputRef.current?.focus());
    }
  }, [showAddForm]);

  useEffect(() => {
    if (editingItem) {
      setEditErrors({});
      setEditingPrefill(editingItem);
      setEditExpirationDate(editingItem.expirationDate ? new Date(editingItem.expirationDate) : null);
      getRefrigeratorItem(editingItem.id)
        .then((fresh) => {
          setEditingPrefill(fresh);
          setEditExpirationDate(fresh.expirationDate ? new Date(fresh.expirationDate) : null);
        })
        .catch(() => {
          showToast('식재료 정보를 불러오지 못했습니다.', { type: 'error' });
          setEditingItem(null);
        });
    } else {
      setEditingPrefill(null);
      setEditExpirationDate(null);
    }
  }, [editingItem, showToast]);

  const createMutation = useMutation({
    mutationFn: (payload: CreateRefrigeratorItemRequest) => createRefrigeratorItem(payload),
    onSuccess: () => {
      showToast('식재료가 추가되었습니다.', { type: 'success' });
      void qc.invalidateQueries({ queryKey: ['refrigeratorItems', sortBy] });
      setShowAddForm(false);
    },
    onError: () => {
      showToast('등록 중 오류가 발생했습니다.', { type: 'error' });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: UpdateRefrigeratorItemRequest }) =>
      updateRefrigeratorItem(id, payload),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['refrigeratorItems', sortBy] });
      setEditingItem(null);
      showToast('정보가 수정되었습니다.', { type: 'success' });
    },
    onError: () => {
      setEditErrors({ global: '수정 중 오류가 발생했습니다.' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteRefrigeratorItem(id),
    onSuccess: (_data, id) => {
      showToast('식재료가 삭제되었습니다.', { type: 'success' });
      setDeleteTarget(null);
      setRemovingIds((prev) => new Set([...prev, id]));
      setTimeout(() => {
        void qc.invalidateQueries({ queryKey: ['refrigeratorItems', sortBy] });
        setRemovingIds((prev) => {
          const next = new Set(prev);
          next.delete(id);
          return next;
        });
      }, 320);
    },
    onError: () => {
      showToast('삭제 중 오류가 발생했습니다.', { type: 'error' });
      setDeleteTarget(null);
    },
  });

  const handleAddSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if(!nameValue.trim()) {
      setFormErrors({name: '식재료명을 입력해주세요.'});
      return;
    }

    const payload: CreateRefrigeratorItemRequest = {
      name: nameValue.trim(),
      quantity: quantityValue ? Number(quantityValue) : undefined,
      unit: unitValue || undefined,
      expirationDate: toYmd(addExpirationDate),
      memo: memoValue || undefined,
    };
    createMutation.mutate(payload);
  };

  const handleEditSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!editingItem) return;
    const form = e.currentTarget;
    const fd = new FormData(form);
    const quantityStr = String(fd.get('quantity')).trim();
    const unit = String(fd.get('unit')).trim();
    const memo = String(fd.get('memo')).trim();

    const payload: UpdateRefrigeratorItemRequest = {
      quantity: quantityStr ? Number(quantityStr) : undefined,
      unit: unit || undefined,
      expirationDate: toYmd(editExpirationDate),
      memo: memo || undefined,
    };
    updateMutation.mutate({ id: editingItem.id, payload });
  };

  const items: RefrigeratorItem[] = data?.items ?? [];
  const imminentCount = data?.expiringCount ?? 0;
  const expiredCount = data?.expiredCount ?? 0;

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold leading-tight text-gray-900">내 냉장고</h1>
      </div>

      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
        <div className="flex items-center">
          <span className="text-gray-700 font-medium mr-2 whitespace-nowrap">정렬:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="block w-auto pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm rounded-md"
          >
            <option value="expirationDate">소비기한 임박순</option>
            <option value="name">이름순</option>
            <option value="createdAt">등록일순</option>
          </select>
        </div>

        <div className="flex space-x-2">
          <Link to="/refrigerator/recommendations" className="px-4 py-2 bg-white border border-[#4E652F] text-[#4E652F] text-sm font-medium rounded-md hover:bg-[#F0F5E5] transition-colors">
            레시피 추천 보기
          </Link>
          <button
            onClick={() => setShowAddForm(true)}
            className="px-4 py-2 bg-[#4E652F] text-white text-sm font-medium rounded-md hover:bg-[#425528] transition-colors flex items-center"
          >
            <PlusIcon className="w-4 h-4 mr-1"/>
            식재료 추가
          </button>
          <Link to="/refrigerator/receipt-scan" className="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-md hover:bg-gray-200 transition-colors flex items-center">
            <ReceiptIcon className="w-4 h-4 mr-1" />
            영수증으로 추가
          </Link>
        </div>
      </div>

      {showAddForm && (
        <div className="fixed inset-0 bg-black/20 z-50 flex items-center justify-center" onClick={() => setShowAddForm(false)}>
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md max-h-[90vh] flex flex-col m-4" onClick={e => e.stopPropagation()}>
            <header className="flex items-center justify-between p-5 border-b">
              <h2 className="text-xl font-bold text-gray-800">식재료 추가</h2>
              <button onClick={() => setShowAddForm(false)} className="p-2 text-gray-500 hover:bg-gray-100 rounded-full">
                ✕
              </button>
            </header>

            <form onSubmit={handleAddSubmit} className="p-6 flex-grow overflow-y-auto space-y-6">
              <div>
                <label className="block text-sm font-bold text-gray-800 mb-1">식재료명 <span className="text-red-500">*</span></label>
                <input
                  ref={nameInputRef}
                  value={nameValue}
                  onChange={(e) => setNameValue(e.target.value)}
                  className={`block w-full border border-gray-300 rounded-md shadow-sm focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm py-2 px-3 ${formErrors.name ? 'border-red-500' : ''}`}
                  placeholder="예: 계란"
                />
                {formErrors.name && <p className="mt-1 text-xs text-red-600">{formErrors.name}</p>}
                <p className="mt-1 text-xs text-gray-500">같은 이름+같은 소비기한은 수량이 합산됩니다.</p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-bold text-gray-800 mb-1">수량</label>
                  <input
                    type="number"
                    min={0}
                    value={quantityValue}
                    onChange={(e) => setQuantityValue(e.target.value)}
                    className="block w-full border border-gray-300 rounded-md shadow-sm focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm py-2 px-3"
                    placeholder="0"
                  />
                </div>
                <div className="relative">
                  <label className="block text-sm font-bold text-gray-800 mb-1">단위</label>
                  <input
                    value={unitValue}
                    onChange={(e) => setUnitValue(e.target.value)}
                    onFocus={() => setShowUnitSuggestions(true)}
                    onBlur={() => setTimeout(() => setShowUnitSuggestions(false), 150)}
                    className="block w-full border border-gray-300 rounded-md shadow-sm focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm py-2 px-3"
                    placeholder="예: 개, g"
                  />
                  {showUnitSuggestions && (
                    <ul className="absolute top-full left-0 right-0 bg-white border rounded shadow mt-1 max-h-40 overflow-auto text-xs z-10">
                      {COMMON_UNITS.filter((u) => !unitValue || u.includes(unitValue)).map((u) => (
                        <li key={u} className="px-2 py-1 hover:bg-blue-50 cursor-pointer" onMouseDown={(e) => { e.preventDefault(); setUnitValue(u); }}>
                          {u}
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-sm font-bold text-gray-800 mb-1">소비기한</label>
                <DatePicker
                  selected={addExpirationDate}
                  onChange={(d) => setAddExpirationDate(d)}
                  locale={ko}
                  dateFormat="yyyy년 MM월 dd일"
                  placeholderText="선택"
                  className="block w-full border border-gray-300 rounded-md shadow-sm focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm py-2 px-3"
                  isClearable
                />
              </div>

              <div>
                <label className="block text-sm font-bold text-gray-800 mb-1">메모</label>
                <textarea
                  value={memoValue}
                  onChange={(e) => setMemoValue(e.target.value)}
                  className="block w-full border border-gray-300 rounded-md shadow-sm focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm py-2 px-3 h-24 resize-none"
                  placeholder="메모를 입력하세요"
                />
              </div>

              <div className="pt-4 flex justify-end space-x-3 border-t border-gray-100">
                <button type="button" onClick={() => setShowAddForm(false)} className="px-6 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50">
                  취소
                </button>
                <button type="submit" className="px-6 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-[#4E652F] hover:bg-[#425528]">
                  저장
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="bg-white rounded-lg shadow overflow-hidden min-h-[400px]">
        {/* [수정] 로딩 및 에러 상태 UI 추가 */}
        {isPending ? (
          <div className="h-96 flex flex-col items-center justify-center text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[#4E652F] mb-4"></div>
            <p className="text-xl text-gray-500 font-medium">불러오는 중...</p>
          </div>
        ) : isError ? (
          <div className="h-96 flex flex-col items-center justify-center text-center">
            <div className="text-red-500 text-4xl mb-4">⚠️</div>
            <p className="text-xl text-gray-500 font-medium">식재료를 불러오지 못했습니다.</p>
            <button onClick={() => window.location.reload()} className="mt-4 px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 text-sm">
              다시 시도
            </button>
          </div>
        ) : items.length === 0 ? (
          <div className="h-96 flex flex-col items-center justify-center text-center">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <span className="text-4xl">🧺</span>
            </div>
            <p className="text-xl text-gray-500 font-medium">냉장고가 비어있습니다.</p>
            <p className="text-gray-400 mt-1">첫 식재료를 추가해보세요!</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
              <tr>
                <th scope="col" className="px-6 py-3 text-left text-xs font-bold text-gray-800 uppercase tracking-wider w-1/5">식재료명</th>
                <th scope="col" className="px-6 py-3 text-center text-xs font-bold text-gray-800 uppercase tracking-wider w-1/12">수량</th>
                <th scope="col" className="px-6 py-3 text-center text-xs font-bold text-gray-800 uppercase tracking-wider w-1/12">단위</th>
                <th scope="col" className="px-6 py-3 text-center text-xs font-bold text-gray-800 uppercase tracking-wider w-1/4">소비기한 (D-day)</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-bold text-gray-800 uppercase tracking-wider w-1/4">메모</th>
                <th scope="col" className="px-6 py-3 text-center text-xs font-bold text-gray-800 uppercase tracking-wider w-1/6">관리</th>
              </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
              {items.map((item) => (
                <tr key={item.id} className={`hover:bg-gray-50 ${removingIds.has(item.id) ? 'opacity-0 transition-opacity duration-300' : ''}`}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{item.name}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-center text-gray-700">{item.quantity}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-center text-gray-500">{item.unit || '—'}</td>
                  <td className={`px-6 py-4 whitespace-nowrap text-sm text-center ${item.expired ? 'text-red-600 font-bold' : item.expirationSoon ? 'text-orange-600 font-bold' : 'text-gray-700'}`}>
                    {item.expirationDate ? `${formatDateYMDKorean(item.expirationDate)} ${formatDDay(item.daysUntilExpiration)}` : '—'}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500 truncate max-w-xs">{item.memo || ''}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-center text-sm font-medium">
                    <button onClick={() => setEditingItem(item)} className="text-gray-600 hover:text-gray-900 bg-gray-200 hover:bg-gray-300 px-3 py-1 rounded-md mr-2 text-xs transition-colors">수정</button>
                    <button onClick={() => setDeleteTarget(item)} className="text-white bg-red-600 hover:bg-red-700 px-3 py-1 rounded-md text-xs transition-colors inline-flex items-center">
                      삭제 <TrashIcon className="w-3 h-3 ml-1" />
                    </button>
                  </td>
                </tr>
              ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="mt-4 flex space-x-6 text-sm text-gray-600">
        <p>총 <span className="font-bold text-gray-900">{data?.totalCount ?? 0}</span>개</p>
        <p>임박 <span className="font-bold text-red-600">{imminentCount}</span>개</p>
        <p>지남 <span className="font-bold text-gray-500">{expiredCount}</span>개</p>
      </div>

      {/* Edit Modal */}
      {editingItem && editingPrefill && (
        <div className="fixed inset-0 bg-black/20 z-50 flex items-center justify-center p-4" onClick={() => setEditingItem(null)}>
          <div className="bg-white w-full max-w-md rounded shadow p-6 space-y-4" onClick={e => e.stopPropagation()}>
            <h2 className="text-lg font-semibold">식재료 수정</h2>
            <form onSubmit={handleEditSubmit} className="space-y-3">
              {/* [수정] 에러 메시지 표시 */}
              {editErrors.global && (
                <div className="p-2 bg-red-50 border border-red-200 text-red-600 text-sm rounded">
                  {editErrors.global}
                </div>
              )}
              <div>
                <label className="block text-xs text-gray-600">식재료명</label>
                <input name="name" disabled defaultValue={editingPrefill.name} className="mt-1 w-full border rounded px-2 py-1 text-sm bg-gray-100 cursor-not-allowed" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs text-gray-600">수량</label>
                  <input name="quantity" type="number" min={0} defaultValue={editingPrefill.quantity} className="mt-1 w-full border rounded px-2 py-1 text-sm" />
                </div>
                <div>
                  <label className="block text-xs text-gray-600">단위</label>
                  <input name="unit" defaultValue={editingPrefill.unit ?? ''} className="mt-1 w-full border rounded px-2 py-1 text-sm" />
                </div>
              </div>
              <div>
                <label className="block text-xs text-gray-600 mb-1">소비기한</label>
                <DatePicker
                  selected={editExpirationDate}
                  onChange={(d) => setEditExpirationDate(d)}
                  locale={ko}
                  dateFormat="yyyy년 MM월 dd일"
                  placeholderText="선택"
                  className="w-full border rounded px-2 py-1 text-sm"
                />
              </div>
              <div>
                <label className="block text-xs text-gray-600">메모</label>
                <textarea name="memo" rows={2} defaultValue={editingPrefill.memo ?? ''} className="mt-1 w-full border rounded px-2 py-1 text-sm" />
              </div>
              <div className="flex gap-2 pt-2 justify-end">
                <button type="button" onClick={() => setEditingItem(null)} className="px-4 py-2 bg-gray-200 rounded text-sm hover:bg-gray-300">닫기</button>
                <button type="submit" className="px-4 py-2 bg-[#4E652F] text-white rounded text-sm hover:bg-[#425528]">수정 완료</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {deleteTarget && (
        <div className="fixed inset-0 bg-black/20 flex items-center justify-center p-4 z-50">
          <div className="bg-white w-full max-w-md rounded shadow-lg p-6 space-y-4">
            <h2 className="text-lg font-semibold">식재료 삭제</h2>
            <p className="text-sm text-gray-700 leading-relaxed">
              정말로 '<span className="font-semibold">{deleteTarget.name}</span>' 항목을 삭제하시겠습니까?
            </p>
            <div className="flex justify-end gap-2 pt-2">
              <button type="button" onClick={() => setDeleteTarget(null)} className="px-4 py-2 bg-gray-200 rounded text-sm hover:bg-gray-300">취소</button>
              <button type="button" onClick={() => deleteMutation.mutate(deleteTarget.id)} className="px-4 py-2 bg-red-600 text-white rounded text-sm hover:bg-red-700">삭제</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}