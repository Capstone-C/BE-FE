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

function formatDDay(days: number | null): string {
  if (days === null || days === undefined) return '—';
  if (days === 0) return 'D-Day';
  if (days > 0) return `D-${days}`;
  return `D+${Math.abs(days)}`;
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

  // Add form state
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const nameInputRef = useRef<HTMLInputElement | null>(null);
  const [nameValue, setNameValue] = useState('');
  const [unitValue, setUnitValue] = useState('');
  const [addExpirationDate, setAddExpirationDate] = useState<Date | null>(null);
  const [showUnitSuggestions, setShowUnitSuggestions] = useState(false);

  // Edit form state
  const [editErrors, setEditErrors] = useState<Record<string, string>>({});
  const [editingPrefill, setEditingPrefill] = useState<RefrigeratorItem | null>(null);
  const [editExpirationDate, setEditExpirationDate] = useState<Date | null>(null);
  // [REF-06] 삭제 확인 모달 / 애니메이션 관련 상태 추가
  const [deleteTarget, setDeleteTarget] = useState<RefrigeratorItem | null>(null);
  const [removingIds, setRemovingIds] = useState<Set<number>>(new Set());

  // Fetch list
  const { data, isPending, isError } = useQuery({
    queryKey: ['refrigeratorItems', sortBy],
    queryFn: () => getRefrigeratorItems(sortBy),
  });

  // Focus when add form opens
  useEffect(() => {
    if (showAddForm) {
      setFormErrors({});
      setNameValue('');
      setUnitValue('');
      setAddExpirationDate(null);
      window.requestAnimationFrame(() => nameInputRef.current?.focus());
    }
  }, [showAddForm]);

  // Prefetch single item when editing opens
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
        .catch((error) => {
          const status = error?.response?.status;
          if (status === 403) {
            showToast('권한이 없습니다.', { type: 'error' });
          } else if (status === 404) {
            showToast('식재료를 찾을 수 없습니다.', { type: 'error' });
          } else {
            showToast('식재료 정보를 불러오지 못했습니다.', { type: 'error' });
          }
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
      showToast('식재료가 추가되었습니다. (동일 이름+소비기한은 수량 합산)', { type: 'success' });
      void qc.invalidateQueries({ queryKey: ['refrigeratorItems', sortBy] });
      setShowAddForm(false);
    },
    onError: (error: any) => {
      const resp = error?.response;
      // 중복 409 정책 제거: 서버는 병합 처리하므로 여기서는 검증 오류만 처리
      if (resp?.data?.code === 'VALIDATION_ERROR') {
        const fieldErrors: Record<string, string> = {};
        resp.data.errors?.forEach((fe: any) => {
          fieldErrors[fe.field] = fe.message;
        });
        setFormErrors(fieldErrors);
        const firstField = Object.keys(fieldErrors)[0];
        if (firstField) {
          const el = document.querySelector(`[name="${firstField}"]`) as HTMLElement | null;
          el?.focus();
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
      void qc.invalidateQueries({ queryKey: ['refrigeratorItems', sortBy] });
      setEditingItem(null);
      showToast('정보가 수정되었습니다.', { type: 'success' });
    },
    onError: (error: any) => {
      const resp = error?.response;
      if (resp?.status === 403) {
        setEditErrors({ global: '권한이 없습니다.' });
      } else if (resp?.status === 404) {
        setEditErrors({ global: '식재료를 찾을 수 없습니다.' });
      } else if (resp?.data?.code === 'VALIDATION_ERROR') {
        const fieldErrors: Record<string, string> = {};
        resp.data.errors?.forEach((fe: any) => {
          if (fe.field === 'quantity') fieldErrors.quantity = fe.message;
          if (fe.field === 'unit') fieldErrors.unit = fe.message;
          if (fe.field === 'expirationDate') fieldErrors.expirationDate = fe.message;
          if (fe.field === 'memo') fieldErrors.memo = fe.message;
        });
        setEditErrors(fieldErrors);
      } else {
        setEditErrors({ global: '수정 중 오류가 발생했습니다.' });
      }
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteRefrigeratorItem(id),
    onSuccess: (_data, id) => {
      // 성공 Toast (문구 명확하게)
      showToast('식재료가 삭제되었습니다.', { type: 'success' });
      // 모달 닫기
      setDeleteTarget(null);
      // 애니메이션 시작: 해당 행 투명도 0
      setRemovingIds((prev) => new Set([...prev, id]));
      // 애니메이션 후 목록 새로고침
      setTimeout(() => {
        void qc.invalidateQueries({ queryKey: ['refrigeratorItems', sortBy] });
        // 제거된 ID 정리 (재사용성 위해)
        setRemovingIds((prev) => {
          const next = new Set(prev);
          next.delete(id);
          return next;
        });
      }, 320); // 약간 여유를 둔 300ms + α
    },
    onError: (error: any) => {
      const status = error?.response?.status;
      if (status === 403) {
        showToast('삭제 권한이 없습니다.', { type: 'error' });
      } else if (status === 404) {
        showToast('이미 삭제된 항목입니다.', { type: 'info' });
        // 최신 상태 반영
        void qc.invalidateQueries({ queryKey: ['refrigeratorItems', sortBy] });
      } else {
        showToast('삭제 중 오류가 발생했습니다.', { type: 'error' });
      }
      // 모달은 닫음 (사용자 실수 방지 목적 달성)
      setDeleteTarget(null);
    },
  });

  const handleAddSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setFormErrors({});
    const form = e.currentTarget;
    const fd = new FormData(form);
    const name = String(fd.get('name')).trim();
    const quantityStr = String(fd.get('quantity')).trim();
    const unit = String(fd.get('unit')).trim();
    const memo = String(fd.get('memo')).trim();

    const errors: Record<string, string> = {};
    if (!name) errors.name = '식재료명을 입력해주세요.';
    else if (name.length > 50) errors.name = '식재료명은 50자 이하이어야 합니다.';
    if (quantityStr) {
      if (!/^\d+$/.test(quantityStr)) errors.quantity = '수량은 숫자로만 입력할 수 있습니다.';
      else if (Number(quantityStr) < 0) errors.quantity = '수량은 0 이상이어야 합니다.';
    }
    if (unit && unit.length > 10) errors.unit = '단위는 10자 이하이어야 합니다.';
    if (memo && memo.length > 200) errors.memo = '메모는 200자 이하이어야 합니다.';

    if (Object.keys(errors).length) {
      setFormErrors(errors);
      const firstField = Object.keys(errors)[0];
      const el = form.querySelector(`[name="${firstField}"]`) as HTMLElement | null;
      el?.focus();
      return;
    }

    const payload: CreateRefrigeratorItemRequest = {
      name,
      quantity: quantityStr ? Number(quantityStr) : undefined,
      unit: unit || undefined,
      expirationDate: toYmd(addExpirationDate),
      memo: memo || undefined,
    };
    createMutation.mutate(payload);
  };

  const handleEditSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!editingItem) return;
    setEditErrors({});
    const form = e.currentTarget;
    const fd = new FormData(form);
    const quantityStr = String(fd.get('quantity')).trim();
    const unit = String(fd.get('unit')).trim();
    const memo = String(fd.get('memo')).trim();

    const fieldErrors: Record<string, string> = {};
    if (quantityStr) {
      if (!/^\d+$/.test(quantityStr)) fieldErrors.quantity = '수량은 숫자로만 입력할 수 있습니다.';
      else if (Number(quantityStr) < 0) fieldErrors.quantity = '수량은 0 이상이어야 합니다.';
    }
    if (unit && unit.length > 10) fieldErrors.unit = '단위는 10자 이하이어야 합니다.';
    if (memo && memo.length > 200) fieldErrors.memo = '메모는 200자 이하이어야 합니다.';

    if (Object.keys(fieldErrors).length) {
      setEditErrors(fieldErrors);
      const firstField = Object.keys(fieldErrors)[0];
      const el = form.querySelector(`[name="${firstField}"]`) as HTMLElement | null;
      el?.focus();
      return;
    }

    const payload: UpdateRefrigeratorItemRequest = {
      quantity: quantityStr ? Number(quantityStr) : undefined,
      unit: unit || undefined,
      expirationDate: toYmd(editExpirationDate),
      memo: memo || undefined,
    };
    updateMutation.mutate({ id: editingItem.id, payload });
  };

  const items: RefrigeratorItem[] = data?.items ?? [];

  return (
    <div className="max-w-7xl mx-auto px-8 py-16">
      <div className="text-center mb-16">
        <h1 className="text-6xl font-bold gradient-text mb-4">🧊 내 냉장고</h1>
        <p className="text-2xl text-gray-600">식재료를 관리하고 신선하게 보관하세요</p>
      </div>
      
      <div className="flex items-center justify-between mb-10 p-8 bg-white rounded-xl shadow-md">
        <div className="flex items-center gap-4">
          <label htmlFor="sort" className="text-base font-medium text-gray-700">
            정렬:
          </label>
          <select
            id="sort"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="border-2 border-gray-200 rounded-lg px-4 py-2.5 text-base focus:ring-2 focus:ring-purple-500 focus:border-transparent"
          >
            <option value="expirationDate">소비기한 임박순</option>
            <option value="name">이름순</option>
            <option value="createdAt">등록일순</option>
          </select>
        </div>
        <div className="flex items-center gap-3">
          <Link
            to="/refrigerator/recommendations"
            className="px-5 py-2.5 rounded-lg bg-gradient-to-r from-purple-600 to-indigo-600 text-white text-base hover:shadow-lg hover:scale-105 transition-all font-medium"
          >
            ✨ 레시피 추천
          </Link>
          <button
            onClick={() => setShowAddForm((v) => !v)}
            className="px-5 py-2.5 rounded-lg bg-gradient-to-r from-green-600 to-emerald-600 text-white text-base hover:shadow-lg hover:scale-105 transition-all font-medium"
          >
            ➕ 식재료 추가
          </button>
          <Link
            to="/refrigerator/receipt-scan"
            className="px-5 py-2.5 rounded-lg bg-gradient-to-r from-blue-600 to-cyan-600 text-white text-base hover:shadow-lg hover:scale-105 transition-all font-medium"
          >
            📄 영수증 추가
          </Link>
        </div>
      </div>

      {showAddForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white w-full max-w-lg rounded-2xl shadow-2xl p-8 border-2 border-gray-100">
            <h2 className="text-2xl font-bold gradient-text mb-6">➕ 식재료 추가</h2>
            <form onSubmit={handleAddSubmit} className="space-y-5" data-refrigerator-add-form="true">
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2">🥗 식재료명 *</label>
                <input
                  name="name"
                  ref={nameInputRef}
                  value={nameValue}
                  onChange={(e) => setNameValue(e.target.value)}
                  autoFocus
                  className={`w-full border-2 rounded-xl px-4 py-3 text-base focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all ${formErrors.name ? 'border-red-500' : 'border-gray-200'}`}
                />
                {formErrors.name && <p className="mt-2 text-sm text-red-600">⚠️ {formErrors.name}</p>}
                {!formErrors.name && (
                  <p className="mt-2 text-sm text-gray-500">
                    💡 같은 이름+같은 소비기한(또는 모두 미지정)은 수량이 합산됩니다.
                  </p>
                )}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-base font-semibold text-gray-700 mb-2">📊 수량</label>
                  <input
                    name="quantity"
                    type="number"
                    min={0}
                    className={`w-full border-2 rounded-xl px-4 py-3 text-base focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all ${formErrors.quantity ? 'border-red-500' : 'border-gray-200'}`}
                  />
                  {formErrors.quantity && <p className="mt-2 text-sm text-red-600">⚠️ {formErrors.quantity}</p>}
                </div>
                <div className="relative">
                  <label className="block text-base font-semibold text-gray-700 mb-2">📏 단위</label>
                  <input
                    name="unit"
                    value={unitValue}
                    onChange={(e) => setUnitValue(e.target.value)}
                    onFocus={() => setShowUnitSuggestions(true)}
                    onBlur={() => setTimeout(() => setShowUnitSuggestions(false), 150)}
                    className={`w-full border-2 rounded-xl px-4 py-3 text-base focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all ${formErrors.unit ? 'border-red-500' : 'border-gray-200'}`}
                    placeholder="예: 개, g, ml"
                    autoComplete="off"
                  />
                  {showUnitSuggestions && (
                    <ul className="absolute top-full left-0 right-0 bg-white border-2 border-purple-200 rounded-xl shadow-lg mt-2 max-h-40 overflow-auto text-base z-10">
                      {COMMON_UNITS.filter((u) => !unitValue || u.includes(unitValue)).map((u) => (
                        <li
                          key={u}
                          className="px-4 py-2 hover:bg-purple-50 cursor-pointer transition-colors"
                          onMouseDown={(e) => {
                            e.preventDefault();
                            setUnitValue(u);
                          }}
                        >
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
                <label className="block text-base font-semibold text-gray-700 mb-2">📅 소비기한</label>
                <DatePicker
                  selected={addExpirationDate}
                  onChange={(d) => setAddExpirationDate(d)}
                  locale={ko}
                  dateFormat="yyyy년 MM월 dd일"
                  placeholderText="소비기한 선택"
                  className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 text-base focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                />
                <p className="mt-1 text-sm text-gray-600">
                  {addExpirationDate ? `선택: ${formatDateYMDKorean(addExpirationDate)}` : '선택된 날짜 없음'}
                </p>
              </div>
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2">📝 메모</label>
                <textarea
                  name="memo"
                  rows={3}
                  className={`w-full border-2 rounded-xl px-4 py-3 text-base resize-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all ${formErrors.memo ? 'border-red-500' : 'border-gray-200'}`}
                />
                {formErrors.memo && <p className="mt-2 text-sm text-red-600">⚠️ {formErrors.memo}</p>}
              </div>
              <div className="flex justify-end gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowAddForm(false)}
                  className="px-5 py-2.5 border-2 border-gray-200 rounded-xl text-gray-600 font-medium hover:bg-gray-50 hover:border-gray-300 transition-all"
                >
                  취소
                </button>
                <button
                  type="submit"
                  disabled={createMutation.isPending}
                  className="px-6 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-xl font-semibold hover:shadow-lg hover:scale-105 disabled:opacity-50 disabled:hover:scale-100 transition-all"
                >
                  {createMutation.isPending ? '⏳ 저장 중...' : '💾 저장'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {isPending && <div className="p-6 text-center text-gray-600 text-base">불러오는 중...</div>}
      {isError && <div className="p-6 text-center text-red-600 text-base">목록을 불러오는 중 오류가 발생했습니다.</div>}

      {!isPending && !isError && items.length === 0 && (
        <div className="bg-white p-10 rounded shadow text-center">
          <p className="text-xl font-medium mb-2">냉장고가 비어있습니다. 첫 식재료를 추가해보세요!</p>
        </div>
      )}

      {items.length > 0 && (
        <div className="bg-white rounded shadow overflow-x-auto">
          <table className="min-w-full text-base">
            <thead>
              <tr className="bg-gray-100 text-left">
                <th className="px-4 py-3 font-semibold">식재료명</th>
                <th className="px-4 py-3 font-semibold">수량</th>
                <th className="px-4 py-3 font-semibold">단위</th>
                <th className="px-4 py-3 font-semibold">소비기한 (D-day)</th>
                <th className="px-4 py-3 font-semibold">메모</th>
                <th className="px-4 py-3 font-semibold">관리</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr
                  key={item.id}
                  className={`${classForItem(item)} border-t transition-opacity duration-300 ${removingIds.has(item.id) ? 'opacity-0' : 'opacity-100'}`}
                >
                  <td className="px-4 py-3 font-medium">{item.name}</td>
                  <td className="px-4 py-3">{item.quantity}</td>
                  <td className="px-4 py-3">{item.unit ?? '—'}</td>
                  <td className={`${dDayTextColor(item)} px-4 py-3`}>
                    {item.expirationDate
                      ? `${formatDateYMDKorean(item.expirationDate)} (${formatDDay(item.daysUntilExpiration)})`
                      : '—'}
                  </td>
                  <td className="px-4 py-3 max-w-xs truncate" title={item.memo ?? ''}>
                    {item.memo ?? '—'}
                  </td>
                  <td className="px-4 py-3 flex items-center gap-2">
                    <button
                      onClick={() => setEditingItem(item)}
                      className="px-3 py-1.5 bg-gray-200 rounded hover:bg-gray-300 text-sm"
                    >
                      수정
                    </button>
                    <button
                      onClick={() => setDeleteTarget(item)}
                      className="px-3 py-1.5 bg-red-600 text-white rounded hover:bg-red-700 flex items-center gap-1 text-sm"
                    >
                      <span>삭제</span>
                      <span aria-hidden>🗑️</span>
                    </button>
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
        </div>
      )}

      {editingItem && editingPrefill && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center p-4 z-50">
          <div className="bg-white w-full max-w-md rounded shadow p-6 space-y-4">
            <h2 className="text-lg font-semibold">식재료 수정</h2>
            {editErrors.global && <p className="text-xs text-red-600">{editErrors.global}</p>}
            <form onSubmit={handleEditSubmit} className="space-y-3">
              <div>
                <label className="block text-xs text-gray-600">식재료명</label>
                <input
                  name="name"
                  disabled
                  defaultValue={editingPrefill.name}
                  className="mt-1 w-full border rounded px-2 py-1 text-sm bg-gray-100 cursor-not-allowed"
                />
                <p className="mt-1 text-[10px] text-gray-500">
                  식재료명은 수정할 수 없습니다. 삭제 후 재등록하세요. (동일 정책: 이름+날짜 동일 시 추가 시 수량 합산)
                </p>
              </div>
              <div>
                <label className="block text-xs text-gray-600">수량</label>
                <input
                  name="quantity"
                  type="number"
                  min={0}
                  defaultValue={editingPrefill.quantity}
                  className={`mt-1 w-full border rounded px-2 py-1 text-sm ${editErrors.quantity ? 'border-red-500' : ''}`}
                />
                {editErrors.quantity && <p className="mt-1 text-xs text-red-600">{editErrors.quantity}</p>}
              </div>
              <div>
                <label className="block text-xs text-gray-600">단위</label>
                <input
                  name="unit"
                  defaultValue={editingPrefill.unit ?? ''}
                  className={`mt-1 w-full border rounded px-2 py-1 text-sm ${editErrors.unit ? 'border-red-500' : ''}`}
                />
                {editErrors.unit && <p className="mt-1 text-xs text-red-600">{editErrors.unit}</p>}
              </div>
              <div>
                <label className="block text-xs text-gray-600 mb-1">소비기한</label>
                <DatePicker
                  selected={editExpirationDate}
                  onChange={(d) => setEditExpirationDate(d)}
                  locale={ko}
                  dateFormat="yyyy년 MM월 dd일"
                  placeholderText="소비기한 선택"
                  className={`w-full border rounded px-2 py-1 text-sm ${editErrors.expirationDate ? 'border-red-500' : ''}`}
                />
                <p className="mt-1 text-xs text-gray-600">
                  {editExpirationDate ? `선택: ${formatDateYMDKorean(editExpirationDate)}` : '선택된 날짜 없음'}
                </p>
                {editErrors.expirationDate && <p className="mt-1 text-xs text-red-600">{editErrors.expirationDate}</p>}
              </div>
              <div>
                <label className="block text-xs text-gray-600">메모</label>
                <textarea
                  name="memo"
                  rows={2}
                  defaultValue={editingPrefill.memo ?? ''}
                  className={`mt-1 w-full border rounded px-2 py-1 text-sm ${editErrors.memo ? 'border-red-500' : ''}`}
                />
                {editErrors.memo && <p className="mt-1 text-xs text-red-600">{editErrors.memo}</p>}
              </div>
              <div className="flex gap-2 pt-2">
                <button
                  type="submit"
                  disabled={updateMutation.isPending}
                  className="px-4 py-2 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 disabled:opacity-50"
                >
                  수정 완료
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

      {deleteTarget && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white w-full max-w-md rounded shadow-lg p-6 space-y-4" role="dialog" aria-modal="true">
            <h2 className="text-lg font-semibold">식재료 삭제</h2>
            <p className="text-sm text-gray-700 leading-relaxed">
              정말로 '<span className="font-semibold">{deleteTarget.name}</span>' 항목을 냉장고에서 삭제하시겠습니까? 이
              작업은 되돌릴 수 없습니다.
            </p>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setDeleteTarget(null)}
                className="px-4 py-2 bg-gray-200 rounded text-sm hover:bg-gray-300"
              >
                취소
              </button>
              <button
                type="button"
                onClick={() => deleteMutation.mutate(deleteTarget.id)}
                disabled={deleteMutation.isPending}
                className="px-4 py-2 bg-red-600 text-white rounded text-sm hover:bg-red-700 disabled:opacity-50"
              >
                {deleteMutation.isPending ? '삭제 중...' : '삭제'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
