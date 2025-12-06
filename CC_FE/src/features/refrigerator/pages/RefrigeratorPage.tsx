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
  if (days === 0) return 'D-Day';
  if (days > 0) return `D-${days}`;
  return `D+${Math.abs(days)}`;
}

// 상태에 따른 뱃지 컴포넌트
function DDayBadge({ days, expired }: { days: number | null; expired: boolean }) {
  if (days === null) return <span className="text-gray-400 text-xs">-</span>;

  let colorClass = 'bg-gray-100 text-gray-600';
  if (expired) colorClass = 'bg-red-100 text-red-600';
  else if (days <= 3) colorClass = 'bg-amber-100 text-amber-700';
  else if (days <= 7) colorClass = 'bg-green-100 text-green-700';

  return (
    <span className={`px-2 py-1 rounded-full text-xs font-bold ${colorClass}`}>
      {formatDDay(days)}
    </span>
  );
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
  const totalCount = data?.totalCount ?? 0;

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="mb-10 flex flex-col md:flex-row md:items-end justify-between gap-4 border-b border-gray-200 pb-6">
        <div>
          <h1 className="text-3xl font-bold leading-tight text-gray-900">내 냉장고</h1>
          <p className="text-gray-500 mt-2">유통기한을 관리하고 신선한 식재료로 요리하세요.</p>
        </div>

        <div className="flex gap-3">
          <Link to="/refrigerator/recommendations" className="px-4 py-2.5 bg-white border border-[#4E652F] text-[#4E652F] text-sm font-medium rounded-lg hover:bg-[#F0F5E5] transition-colors shadow-sm">
            🍽️ 레시피 추천
          </Link>
          <Link to="/refrigerator/receipt-scan" className="px-4 py-2.5 bg-white border border-gray-300 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-50 transition-colors shadow-sm flex items-center gap-1.5">
            <ReceiptIcon className="w-4 h-4 text-gray-500" />
            영수증 등록
          </Link>
          <button
            onClick={() => setShowAddForm(true)}
            className="px-5 py-2.5 bg-[#4E652F] text-white text-sm font-bold rounded-lg hover:bg-[#425528] transition-all shadow-md hover:shadow-lg flex items-center gap-1.5"
          >
            <PlusIcon className="w-4 h-4 text-white" />
            재료 추가
          </button>
        </div>
      </div>

      {/* 요약 통계 카드 */}
      <div className="grid grid-cols-3 gap-4 mb-8">
        <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-100 flex flex-col items-center justify-center">
          <span className="text-gray-500 text-sm font-medium mb-1">전체 식재료</span>
          <span className="text-3xl font-bold text-gray-800">{totalCount}</span>
        </div>
        <div className="bg-amber-50 p-5 rounded-xl border border-amber-100 flex flex-col items-center justify-center">
          <span className="text-amber-600 text-sm font-bold mb-1">소비기한 임박</span>
          <span className="text-3xl font-bold text-amber-700">{imminentCount}</span>
        </div>
        <div className="bg-red-50 p-5 rounded-xl border border-red-100 flex flex-col items-center justify-center">
          <span className="text-red-600 text-sm font-bold mb-1">소비기한 경과</span>
          <span className="text-3xl font-bold text-red-700">{expiredCount}</span>
        </div>
      </div>

      <div className="flex justify-end mb-4">
        <div className="flex items-center bg-white rounded-lg border border-gray-200 p-1">
          <span className="text-xs font-medium text-gray-500 px-2">정렬:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="text-sm border-none focus:ring-0 text-gray-700 bg-transparent py-1 pl-0 pr-8 cursor-pointer font-medium"
          >
            <option value="expirationDate">소비기한 임박순</option>
            <option value="name">이름순</option>
            <option value="createdAt">등록일순</option>
          </select>
        </div>
      </div>

      {showAddForm && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center backdrop-blur-sm" onClick={() => setShowAddForm(false)}>
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md max-h-[90vh] flex flex-col m-4 overflow-hidden animate-in fade-in zoom-in duration-200" onClick={e => e.stopPropagation()}>
            <header className="flex items-center justify-between p-6 border-b border-gray-100 bg-gray-50">
              <h2 className="text-xl font-bold text-gray-800">식재료 추가</h2>
              <button onClick={() => setShowAddForm(false)} className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200 rounded-full transition-colors">
                ✕
              </button>
            </header>

            <form onSubmit={handleAddSubmit} className="p-6 flex-grow overflow-y-auto space-y-5">
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">식재료명 <span className="text-red-500">*</span></label>
                <input
                  ref={nameInputRef}
                  value={nameValue}
                  onChange={(e) => setNameValue(e.target.value)}
                  className={`block w-full border rounded-lg shadow-sm focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm py-2.5 px-4 transition-all ${formErrors.name ? 'border-red-500 ring-1 ring-red-500' : 'border-gray-300'}`}
                  placeholder="예: 계란"
                />
                {formErrors.name && <p className="mt-1 text-xs text-red-600 font-medium">{formErrors.name}</p>}
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1.5">수량</label>
                  <input
                    type="number"
                    min={0}
                    value={quantityValue}
                    onChange={(e) => setQuantityValue(e.target.value)}
                    className="block w-full border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm py-2.5 px-4 transition-all"
                    placeholder="0"
                  />
                  {formErrors.quantity && <p className="mt-2 text-sm text-red-600">⚠️ {formErrors.quantity}</p>}
                </div>
                <div className="relative">
                  <label className="block text-sm font-bold text-gray-700 mb-1.5">단위</label>
                  <input
                    value={unitValue}
                    onChange={(e) => setUnitValue(e.target.value)}
                    onFocus={() => setShowUnitSuggestions(true)}
                    onBlur={() => setTimeout(() => setShowUnitSuggestions(false), 150)}
                    className="block w-full border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm py-2.5 px-4 transition-all"
                    placeholder="예: 개, g"
                  />
                  {showUnitSuggestions && (
                    <ul className="absolute top-full left-0 right-0 bg-white border border-gray-200 rounded-lg shadow-lg mt-1 max-h-40 overflow-auto text-sm z-10 py-1">
                      {COMMON_UNITS.filter((u) => !unitValue || u.includes(unitValue)).map((u) => (
                        <li key={u} className="px-4 py-2 hover:bg-gray-50 cursor-pointer text-gray-700" onMouseDown={(e) => { e.preventDefault(); setUnitValue(u); }}>
                          {u}
                        </li>
                      ))}
                      {COMMON_UNITS.filter((u) => !unitValue || u.includes(unitValue)).length === 0 && (
                        <li className="px-4 py-2 text-gray-400">일치하는 제안 없음</li>
                      )}
                    </ul>
                  )}
                  {formErrors.unit && <p className="mt-2 text-sm text-red-600">⚠️ {formErrors.unit}</p>}
                </div>
              </div>

              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">소비기한</label>
                <DatePicker
                  selected={addExpirationDate}
                  onChange={(d) => setAddExpirationDate(d)}
                  locale={ko}
                  dateFormat="yyyy년 MM월 dd일"
                  placeholderText="날짜 선택"
                  className="block w-full border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm py-2.5 px-4 transition-all"
                  isClearable
                />
                <p className="mt-1 text-sm text-gray-600">
                  {addExpirationDate ? `선택: ${formatDateYMDKorean(addExpirationDate)}` : '선택된 날짜 없음'}
                </p>
              </div>

              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">메모</label>
                <textarea
                  value={memoValue}
                  onChange={(e) => setMemoValue(e.target.value)}
                  className="block w-full border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm py-2.5 px-4 h-24 resize-none transition-all"
                  placeholder="메모를 입력하세요 (선택)"
                />
                {formErrors.memo && <p className="mt-2 text-sm text-red-600">⚠️ {formErrors.memo}</p>}
              </div>

              <div className="pt-2 flex justify-end space-x-3">
                <button type="button" onClick={() => setShowAddForm(false)} className="px-5 py-2.5 border border-gray-300 text-sm font-bold rounded-lg text-gray-600 bg-white hover:bg-gray-50 transition-colors">
                  취소
                </button>
                <button type="submit" className="px-5 py-2.5 border border-transparent text-sm font-bold rounded-lg text-white bg-[#4E652F] hover:bg-[#425528] shadow-sm hover:shadow transition-all">
                  저장
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="bg-white rounded-xl shadow-md overflow-hidden border border-gray-100 min-h-[400px]">
        {isPending ? (
          <div className="h-96 flex flex-col items-center justify-center text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-b-4 border-[#4E652F] mb-4"></div>
            <p className="text-lg text-gray-500 font-medium">냉장고를 열고 있습니다...</p>
          </div>
        ) : isError ? (
          <div className="h-96 flex flex-col items-center justify-center text-center bg-red-50">
            <div className="text-red-400 text-5xl mb-4">⚠️</div>
            <p className="text-lg text-gray-700 font-bold">문제가 발생했습니다.</p>
            <p className="text-gray-500 mb-6">식재료 목록을 불러오지 못했습니다.</p>
            <button onClick={() => window.location.reload()} className="px-5 py-2.5 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 text-sm font-medium shadow-sm">
              다시 시도
            </button>
          </div>
        ) : items.length === 0 ? (
          <div className="h-96 flex flex-col items-center justify-center text-center bg-gray-50">
            <div className="w-24 h-24 bg-white rounded-full flex items-center justify-center mb-6 shadow-sm border border-gray-100">
              <span className="text-5xl">🧺</span>
            </div>
            <h3 className="text-xl text-gray-800 font-bold mb-2">냉장고가 비어있습니다</h3>
            <p className="text-gray-500 mb-6">첫 식재료를 추가하고 관리를 시작해보세요!</p>
            <button
              onClick={() => setShowAddForm(true)}
              className="px-6 py-3 bg-[#4E652F] text-white font-bold rounded-lg hover:bg-[#425528] transition-all shadow-md hover:shadow-lg"
            >
              + 식재료 추가하기
            </button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-100">
              <thead>
              <tr className="bg-gray-50">
                <th scope="col" className="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider w-1/5">식재료명</th>
                <th scope="col" className="px-6 py-4 text-center text-xs font-bold text-gray-500 uppercase tracking-wider w-1/12">수량</th>
                <th scope="col" className="px-6 py-4 text-center text-xs font-bold text-gray-500 uppercase tracking-wider w-1/12">단위</th>
                <th scope="col" className="px-6 py-4 text-center text-xs font-bold text-gray-500 uppercase tracking-wider w-1/4">소비기한</th>
                <th scope="col" className="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider w-1/4">메모</th>
                <th scope="col" className="px-6 py-4 text-center text-xs font-bold text-gray-500 uppercase tracking-wider w-1/6">관리</th>
              </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-100">
              {items.map((item) => (
                <tr key={item.id} className={`group hover:bg-[#F7F9F2] transition-colors ${removingIds.has(item.id) ? 'opacity-0 transition-opacity duration-300' : ''}`}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <span className="font-bold text-gray-800 text-base">{item.name}</span>
                      {item.expired && <span className="ml-2 px-1.5 py-0.5 bg-red-100 text-red-600 text-[10px] font-bold rounded">만료</span>}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-center">
                    <span className="bg-gray-100 text-gray-700 px-2.5 py-1 rounded-md font-bold text-sm">{item.quantity}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-center text-sm text-gray-500 font-medium">{item.unit || '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-center">
                    <div className="flex flex-col items-center">
                       <span className={`text-sm font-medium ${item.expired ? 'text-red-600' : item.expirationSoon ? 'text-amber-600' : 'text-gray-700'}`}>
                        {item.expirationDate ? formatDateYMDKorean(item.expirationDate) : '-'}
                      </span>
                      {item.expirationDate && (
                        <div className="mt-1">
                          <DDayBadge days={item.daysUntilExpiration} expired={item.expired} />
                        </div>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500 truncate max-w-xs">
                    {item.memo ? (
                      <span className="inline-block bg-yellow-50 text-yellow-800 px-2 py-0.5 rounded border border-yellow-100 text-xs">
                        {item.memo}
                      </span>
                    ) : (
                      <span className="text-gray-300 text-xs">-</span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-center text-sm font-medium">
                    <div className="flex justify-center gap-2 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity">
                      <button
                        onClick={() => setEditingItem(item)}
                        className="text-gray-600 hover:text-[#4E652F] bg-white border border-gray-200 hover:border-[#4E652F] px-3 py-1.5 rounded-lg text-xs font-bold transition-all shadow-sm"
                      >
                        수정
                      </button>
                      <button
                        onClick={() => setDeleteTarget(item)}
                        className="text-red-500 hover:text-red-700 bg-white border border-gray-200 hover:border-red-200 px-3 py-1.5 rounded-lg text-xs font-bold transition-all shadow-sm flex items-center gap-1"
                      >
                        <TrashIcon className="w-3 h-3" /> 삭제
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="p-5 text-base text-gray-600 flex gap-6 font-medium">
            <span>총 {data?.totalCount}개</span>
            <span>임박 {data?.expiringCount}개</span>
            <span>지남 {data?.expiredCount}개</span>
          </div>
        )}
      </div>

      {/* Edit Modal */}
      {editingItem && editingPrefill && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 backdrop-blur-sm" onClick={() => setEditingItem(null)}>
          <div className="bg-white w-full max-w-md rounded-2xl shadow-2xl p-6 space-y-5 animate-in fade-in zoom-in duration-200" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center border-b border-gray-100 pb-4">
              <h2 className="text-xl font-bold text-gray-800">식재료 수정</h2>
              <button onClick={() => setEditingItem(null)} className="text-gray-400 hover:text-gray-600 p-1">✕</button>
            </div>

            <form onSubmit={handleEditSubmit} className="space-y-5">
              {editErrors.global && (
                <div className="p-3 bg-red-50 border border-red-200 text-red-600 text-sm rounded-lg font-medium">
                  {editErrors.global}
                </div>
              )}
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">식재료명</label>
                <input name="name" disabled defaultValue={editingPrefill.name} className="w-full border border-gray-200 rounded-lg px-4 py-2.5 text-sm bg-gray-50 text-gray-500 cursor-not-allowed font-medium" />
                <p className="text-xs text-gray-400 mt-1">식재료명은 수정할 수 없습니다.</p>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1.5">수량</label>
                  <input name="quantity" type="number" min={0} defaultValue={editingPrefill.quantity} className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A]" />
                </div>
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1.5">단위</label>
                  <input name="unit" defaultValue={editingPrefill.unit ?? ''} className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A]" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">소비기한</label>
                <DatePicker
                  selected={editExpirationDate}
                  onChange={(d) => setEditExpirationDate(d)}
                  locale={ko}
                  dateFormat="yyyy년 MM월 dd일"
                  placeholderText="선택"
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A]"
                />
              </div>
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">메모</label>
                <textarea name="memo" rows={3} defaultValue={editingPrefill.memo ?? ''} className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A] resize-none" />
              </div>
              <div className="flex gap-3 pt-4 border-t border-gray-100">
                <button type="button" onClick={() => setEditingItem(null)} className="flex-1 px-4 py-2.5 bg-white border border-gray-300 rounded-lg text-sm font-bold text-gray-600 hover:bg-gray-50 transition-colors">취소</button>
                <button type="submit" className="flex-1 px-4 py-2.5 bg-[#4E652F] text-white rounded-lg text-sm font-bold hover:bg-[#425528] transition-colors shadow-sm">수정 완료</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {deleteTarget && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50 backdrop-blur-sm">
          <div className="bg-white w-full max-w-sm rounded-2xl shadow-2xl p-6 space-y-6 animate-in fade-in zoom-in duration-200 text-center">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto">
              <TrashIcon className="w-8 h-8 text-red-600" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-900">식재료 삭제</h2>
              <p className="text-sm text-gray-600 mt-2">
                정말로 '<span className="font-bold text-gray-900">{deleteTarget.name}</span>'을(를) 삭제하시겠습니까?
                <br/>삭제된 데이터는 복구할 수 없습니다.
              </p>
            </div>
            <div className="flex justify-center gap-3 pt-2">
              <button type="button" onClick={() => setDeleteTarget(null)} className="flex-1 px-4 py-2.5 bg-white border border-gray-300 rounded-lg text-sm font-bold text-gray-600 hover:bg-gray-50 transition-colors">취소</button>
              <button type="button" onClick={() => deleteMutation.mutate(deleteTarget.id)} className="flex-1 px-4 py-2.5 bg-red-600 text-white rounded-lg text-sm font-bold hover:bg-red-700 transition-colors shadow-md">삭제하기</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}