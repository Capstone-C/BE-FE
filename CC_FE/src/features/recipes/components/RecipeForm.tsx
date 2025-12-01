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
        <div className="max-w-4xl mx-auto bg-white rounded-2xl shadow-lg border border-gray-200 p-8 space-y-8">
            <div className="border-b-2 border-gray-100 pb-6">
                <h1 className="text-3xl font-bold gradient-text mb-2">
                    {isEdit ? '🍳 레시피 수정' : '✨ 새 레시피 작성'}
                </h1>
                <p className="text-gray-600">
                    나만의 맛있는 레시피를 공유해보세요.
                </p>
            </div>

            {/* 기본 정보 */}
            <section className="space-y-4">
                <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                    <span className="text-purple-600">📝</span> 기본 정보
                </h2>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2" id="field-title">
                        <label className="block text-sm font-medium text-gray-700">레시피 제목 <span className="text-red-500">*</span></label>
                        <input
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            className={`w-full border-2 rounded-xl px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition-all ${errors.title ? 'border-red-500' : 'border-gray-200'}`}
                            placeholder="예: 소고기 미역국"
                        />
                        {errors.title && <p className="text-red-500 text-sm flex items-center gap-1">⚠️ {errors.title}</p>}
                    </div>

                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">카테고리</label>
                        <select
                            value={categoryId}
                            onChange={(e) => setCategoryId(Number(e.target.value))}
                            className="w-full border-2 border-gray-200 rounded-xl px-4 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none"
                        >
                            {categories.map(c => (
                                <option key={c.id} value={c.id}>{c.name}</option>
                            ))}
                        </select>
                    </div>
                </div>

                <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">요약 (선택)</label>
                    <textarea
                        value={summary}
                        onChange={(e) => setSummary(e.target.value)}
                        className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 h-24 resize-none focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none"
                        placeholder="레시피에 대한 간단한 설명을 적어주세요."
                    />
                </div>

                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">대표 이미지 (선택)</label>
                  <ImageUploader
                    value={thumbnail}
                    onChange={setThumbnail}
                    placeholder="대표 이미지 업로드"
                  />
                </div>
              </section>

            <hr className="border-gray-200" />

            {/* 상세 정보 */}
            <section className="space-y-4">
                <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                    <span className="text-purple-600">⚙️</span> 상세 정보
                </h2>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">식단 타입</label>
                        <select
                            value={dietType}
                            onChange={(e) => setDietType(e.target.value as DietType)}
                            className="w-full border-2 border-gray-200 rounded-xl px-3 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none"
                        >
                            <option value="">선택 안함</option>
                            {DIET_TYPES.map(t => (
                                <option key={t.value} value={t.value}>{t.label}</option>
                            ))}
                        </select>
                    </div>

                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700">난이도</label>
                        <select
                            value={difficulty}
                            onChange={(e) => setDifficulty(e.target.value as Difficulty)}
                            className="w-full border-2 border-gray-200 rounded-xl px-3 py-2.5 focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none"
                        >
                            <option value="">선택 안함</option>
                            {DIFFICULTIES.map(d => (
                                <option key={d.value} value={d.value}>{d.label}</option>
                            ))}
                        </select>
                    </div>

          <div className="space-y-1">
            <label className="block text-sm font-medium text-gray-700">조리 시간 (분)</label>
            <input
              type="number"
              value={cookTime}
              onChange={(e) => setCookTime(e.target.value ? Number(e.target.value) : '')}
              className="w-full border rounded-lg px-3 py-2 focus:ring-2 focus:ring-[#71853A] outline-none"
              min="0"
            />
          </div>

          <div className="space-y-1">
            <label className="block text-sm font-medium text-gray-700">분량 (인분)</label>
            <input
              type="number"
              value={servings}
              onChange={(e) => setServings(e.target.value ? Number(e.target.value) : '')}
              className="w-full border rounded-lg px-3 py-2 focus:ring-2 focus:ring-[#71853A] outline-none"
              min="1"
            />
          </div>
        </div>
      </section>

      <hr />

      <section className="space-y-4" id="field-ingredients">
        <IngredientsEditor items={ingredients} onChange={setIngredients} />
        {errors.ingredients && <p className="text-red-500 text-sm">{errors.ingredients}</p>}
      </section>

      <hr />

      <section className="space-y-4" id="field-steps">
        <StepsEditor steps={steps} onChange={setSteps} />
        {errors.steps && <p className="text-red-500 text-sm">{errors.steps}</p>}
      </section>

      <hr />

            {/* 상태 및 버튼 */}
            <div className="flex items-center justify-between pt-6">
                <div className="flex items-center gap-3">
                    <label className="text-sm font-semibold text-gray-700">📝 공개 상태:</label>
                    <select
                        value={status}
                        onChange={(e) => setStatus(e.target.value as UpsertPostStatus)}
                        className="border-2 border-gray-200 rounded-xl px-3 py-2 text-sm focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                    >
                        <option value="PUBLISHED">✅ 공개</option>
                        <option value="DRAFT">📦 비공개 (임시저장)</option>
                    </select>
                </div>

                <div className="flex gap-3">
                    <button
                        type="button"
                        onClick={() => nav(-1)}
                        className="px-5 py-2.5 border-2 border-gray-200 rounded-xl text-gray-600 font-medium hover:bg-gray-50 hover:border-gray-300 transition-all"
                    >
                        취소
                    </button>
                    <button
                        type="button"
                        onClick={handleSubmit}
                        disabled={submitting}
                        className="px-6 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-xl font-semibold hover:shadow-lg hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100 transition-all"
                    >
                        {submitting ? '⏳ 저장 중...' : (isEdit ? '✅ 수정 완료' : '🎉 레시피 등록')}
                    </button>
                </div>
            </div>
        </div>
      </div>
    </div>
  );
}