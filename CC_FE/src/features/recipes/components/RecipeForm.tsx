import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { IngredientsEditor, type IngredientItem } from '@/features/recipes/components/IngredientsEditor';
import { StepsEditor } from '@/features/recipes/components/StepsEditor';
import { serializeRecipeContent, type RecipeStep } from '@/features/recipes/utils/recipeContent';
import { listCategories } from '@/apis/categories.api';
import type { UpsertPostDto, DietType, Difficulty, UpsertPostStatus } from '@/apis/boards.api';
import ImageUploader from '@/components/ui/ImageUploader';

export type RecipeFormProps = {
  initialData?: Partial<UpsertPostDto> & {
    steps?: RecipeStep[];
    summary?: string;
  };
  onSubmit: (dto: UpsertPostDto) => Promise<void>;
  isEdit?: boolean;
};

const DIET_TYPES: { value: DietType; label: string }[] = [
  { value: 'VEGAN', label: '비건' },
  { value: 'VEGETARIAN', label: '베지테리언' },
  { value: 'KETO', label: '키토제닉' },
  { value: 'PALEO', label: '팔레오' },
  { value: 'MEDITERRANEAN', label: '지중해식' },
  { value: 'LOW_CARB', label: '저탄수화물' },
  { value: 'HIGH_PROTEIN', label: '고단백' },
  { value: 'GENERAL', label: '일반식' },
];

const DIFFICULTIES: { value: Difficulty; label: string }[] = [
  { value: 'VERY_LOW', label: '매우 쉬움' },
  { value: 'LOW', label: '쉬움' },
  { value: 'MEDIUM', label: '보통' },
  { value: 'HIGH', label: '어려움' },
  { value: 'VERY_HIGH', label: '매우 어려움' },
];

export default function RecipeForm({ initialData, onSubmit, isEdit = false }: RecipeFormProps) {
  const nav = useNavigate();

  const [categoryId, setCategoryId] = useState<number>(0);
  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [thumbnail, setThumbnail] = useState('');
  const [dietType, setDietType] = useState<DietType | ''>('');
  const [cookTime, setCookTime] = useState<number | ''>('');
  const [servings, setServings] = useState<number | ''>('');
  const [difficulty, setDifficulty] = useState<Difficulty | ''>('');
  const [status, setStatus] = useState<UpsertPostStatus>('PUBLISHED');

  const [ingredients, setIngredients] = useState<IngredientItem[]>([]);
  const [steps, setSteps] = useState<RecipeStep[]>([]);

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [categories, setCategories] = useState<{ id: number; name: string }[]>([]);

  useEffect(() => {
    if (initialData) {
      if (initialData.categoryId) setCategoryId(Number(initialData.categoryId));
      if (initialData.title) setTitle(initialData.title);
      if (initialData.summary) setSummary(initialData.summary);
      if (initialData.thumbnailUrl) setThumbnail(initialData.thumbnailUrl);
      if (initialData.dietType) setDietType(initialData.dietType);
      if (initialData.cookTimeInMinutes) setCookTime(initialData.cookTimeInMinutes);
      if (initialData.servings) setServings(initialData.servings);
      if (initialData.difficulty) setDifficulty(initialData.difficulty);
      if (initialData.status) setStatus(initialData.status);

      if (initialData.ingredients) {
        setIngredients(initialData.ingredients.map(i => ({
          name: i.name,
          quantity: i.quantity,
          unit: i.unit,
          memo: i.memo
        })));
      }
      if (initialData.steps) setSteps(initialData.steps);
    }
  }, [initialData]);

  useEffect(() => {
    (async () => {
      try {
        const list = await listCategories();
        const recipeCats = list.filter((c) => c.type === 'RECIPE');
        const targetCats = recipeCats.length ? recipeCats : list;
        setCategories(targetCats);

        if (!isEdit && !categoryId && targetCats.length > 0 && targetCats[0]) {
          setCategoryId(targetCats[0].id);
        }
      } catch (e) {
        console.error('Failed to load categories', e);
      }
    })();
  }, [isEdit, categoryId]);

  const validate = () => {
    const e: Record<string, string> = {};
    if (!title.trim()) e.title = '레시피 제목을 입력해주세요.';
    else if (title.length < 2) e.title = '제목은 2자 이상 입력해주세요.';

    if (ingredients.length === 0) e.ingredients = '재료를 최소 1개 이상 입력해주세요.';
    if (steps.length === 0) e.steps = '조리 순서를 최소 1개 이상 입력해주세요.';

    if (ingredients.some(i => !i.name.trim())) e.ingredients = '재료명을 모두 입력해주세요.';
    if (steps.some(s => !s.description.trim() && !s.imageUrl)) e.steps = '조리 단계 내용을 입력해주세요.';

    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async () => {
    if (!validate()) {
      const firstError = Object.keys(errors)[0];
      if (firstError) {
        const el = document.getElementById(`field-${firstError}`);
        el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
      return;
    }

    try {
      setSubmitting(true);

      const contentHtml = serializeRecipeContent(summary, steps);

      const dto: UpsertPostDto = {
        title,
        content: contentHtml,
        categoryId,
        status,
        isRecipe: true,
        dietType: dietType || undefined,
        cookTimeInMinutes: typeof cookTime === 'number' ? cookTime : undefined,
        servings: typeof servings === 'number' ? servings : undefined,
        difficulty: difficulty || undefined,
        ingredients: ingredients.map(i => ({
          name: i.name,
          quantity: i.quantity,
          unit: i.unit,
          memo: i.memo
        })),
        thumbnailUrl: thumbnail || undefined,
      };

      await onSubmit(dto);
    } catch (e) {
      console.error(e);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto bg-white rounded-xl shadow-sm border border-gray-100 p-8 space-y-10">
      <div className="border-b border-gray-100 pb-6">
        <h1 className="text-3xl font-bold text-gray-900">
          {isEdit ? '레시피 수정' : '새 레시피 작성'}
        </h1>
        <p className="text-gray-500 mt-2 text-lg">
          나만의 맛있는 레시피를 공유해보세요.
        </p>
      </div>

      <section className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <span className="w-1.5 h-6 bg-[#4E652F] rounded-full"></span>
            기본 정보
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-2" id="field-title">
            <label className="block text-sm font-bold text-gray-700">
              레시피 제목 <span className="text-red-500">*</span>
            </label>
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className={`w-full border rounded-lg px-4 py-3 text-lg focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A] outline-none transition-all ${errors.title ? 'border-red-500 bg-red-50' : 'border-gray-300'}`}
              placeholder="예: 소고기 미역국"
            />
            {errors.title && <p className="text-red-500 text-sm mt-1">{errors.title}</p>}
          </div>

          <div className="space-y-2">
            <label className="block text-sm font-bold text-gray-700">카테고리</label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(Number(e.target.value))}
              className="w-full border border-gray-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A] outline-none transition-all bg-white"
            >
              {categories.map(c => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="space-y-2">
          <label className="block text-sm font-bold text-gray-700">요약 (선택)</label>
          <textarea
            value={summary}
            onChange={(e) => setSummary(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-4 py-3 h-24 resize-none focus:ring-2 focus:ring-[#71853A] focus:border-[#71853A] outline-none transition-all"
            placeholder="이 레시피의 특징이나 꿀팁을 간단히 적어주세요."
          />
        </div>

        {/* [수정] 대표 이미지 위치 이동 및 비율(aspect-video) 고정 */}
        <div className="space-y-2">
          <label className="block text-sm font-bold text-gray-700">대표 이미지 (선택)</label>
          <div className="w-full aspect-video bg-gray-50 rounded-lg border border-gray-200 overflow-hidden">
            <ImageUploader
              value={thumbnail}
              onChange={setThumbnail}
              placeholder="레시피를 대표할 이미지를 업로드해주세요"
              className="w-full h-full"
            />
          </div>
          <p className="text-xs text-gray-500 mt-1 text-right">권장 비율: 16:9 (가로형 이미지)</p>
        </div>
      </section>

      <hr className="border-gray-100" />

      <section className="space-y-6">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <span className="w-1.5 h-6 bg-[#4E652F] rounded-full"></span>
          상세 정보
        </h2>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 bg-[#F7F9F2] p-6 rounded-xl border border-[#E4E9D9]">
          <div className="space-y-2">
            <label className="block text-sm font-medium text-gray-600">식단 타입</label>
            <select
              value={dietType}
              onChange={(e) => setDietType(e.target.value as DietType)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-[#71853A] outline-none bg-white text-sm"
            >
              <option value="">선택 안함</option>
              {DIET_TYPES.map(t => (
                <option key={t.value} value={t.value}>{t.label}</option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <label className="block text-sm font-medium text-gray-600">난이도</label>
            <select
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value as Difficulty)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-[#71853A] outline-none bg-white text-sm"
            >
              <option value="">선택 안함</option>
              {DIFFICULTIES.map(d => (
                <option key={d.value} value={d.value}>{d.label}</option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <label className="block text-sm font-medium text-gray-600">조리 시간 (분)</label>
            <div className="relative">
              <input
                type="number"
                value={cookTime}
                onChange={(e) => setCookTime(e.target.value ? Number(e.target.value) : '')}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 pr-8 focus:ring-2 focus:ring-[#71853A] outline-none text-sm"
                min="0"
                placeholder="0"
              />
              <span className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 text-xs">분</span>
            </div>
          </div>

          <div className="space-y-2">
            <label className="block text-sm font-medium text-gray-600">분량 (인분)</label>
            <div className="relative">
              <input
                type="number"
                value={servings}
                onChange={(e) => setServings(e.target.value ? Number(e.target.value) : '')}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 pr-10 focus:ring-2 focus:ring-[#71853A] outline-none text-sm"
                min="1"
                placeholder="1"
              />
              <span className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 text-xs">인분</span>
            </div>
          </div>
        </div>
      </section>

      <hr className="border-gray-100" />

      <section className="space-y-6" id="field-ingredients">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <span className="w-1.5 h-6 bg-[#4E652F] rounded-full"></span>
          재료
        </h2>
        <div className="bg-gray-50 p-6 rounded-xl border border-gray-100">
          <IngredientsEditor items={ingredients} onChange={setIngredients} />
        </div>
        {errors.ingredients && <p className="text-red-500 text-sm px-2">{errors.ingredients}</p>}
      </section>

      <hr className="border-gray-100" />

      <section className="space-y-6" id="field-steps">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <span className="w-1.5 h-6 bg-[#4E652F] rounded-full"></span>
          조리 순서
        </h2>
        <div className="bg-gray-50 p-6 rounded-xl border border-gray-100">
          <StepsEditor steps={steps} onChange={setSteps} />
        </div>
        {errors.steps && <p className="text-red-500 text-sm px-2">{errors.steps}</p>}
      </section>

      <hr className="border-gray-100" />

      <div className="flex items-center justify-between pt-6 sticky bottom-0 bg-white border-t border-gray-100 py-4 -mx-6 px-6 sm:mx-0 sm:px-0 sm:static sm:border-t-0 sm:py-0 shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)] sm:shadow-none z-10">
        <div className="flex items-center gap-3">
          <label className="text-sm font-bold text-gray-700">공개 상태</label>
          <select
            value={status}
            onChange={(e) => setStatus(e.target.value as UpsertPostStatus)}
            className="border border-gray-300 rounded-md px-3 py-1.5 text-sm bg-white focus:ring-2 focus:ring-[#71853A] outline-none"
          >
            <option value="PUBLISHED">공개</option>
            <option value="DRAFT">비공개 (임시저장)</option>
          </select>
        </div>

        <div className="flex gap-3">
          <button
            type="button"
            onClick={() => nav(-1)}
            className="px-5 py-2.5 border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50 transition-colors font-medium"
          >
            취소
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={submitting}
            className="px-8 py-2.5 bg-[#4E652F] text-white rounded-lg font-bold hover:bg-[#425528] disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-sm hover:shadow-md transform hover:-translate-y-0.5"
          >
            {submitting ? '저장 중...' : (isEdit ? '수정 완료' : '레시피 등록')}
          </button>
        </div>
      </div>
    </div>
  );
}