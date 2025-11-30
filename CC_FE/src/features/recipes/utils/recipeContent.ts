export type RecipeStep = {
  description: string;
  imageUrl?: string;
};

export type RecipeContent = {
  summary: string;
  steps: RecipeStep[];
};

/**
 * 레시피 요약과 조리 순서를 HTML 문자열로 변환합니다.
 * 데이터 보존을 위해 data-attribute를 적극 활용합니다.
 */
export function serializeRecipeContent(summary: string, steps: RecipeStep[]): string {
  const esc = (s: string) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

  let html = '';

  // 1. 요약 (Summary)
  if (summary.trim()) {
    html += `<div data-type="summary" class="recipe-summary"><p>${esc(summary.trim())}</p></div>\n`;
  }

  // 2. 조리 순서 (Steps)
  if (steps.length > 0) {
    html += `<div data-type="recipe-steps" class="recipe-steps">\n`;
    html += `<h3>조리 순서</h3>\n`;
    steps.forEach((step, index) => {
      if (!step.description.trim() && !step.imageUrl) return;

      html += `<div data-type="recipe-step" data-step-index="${index}" class="recipe-step mb-4">\n`;
      html += `<h4>STEP ${index + 1}</h4>\n`;

      if (step.imageUrl) {
        html += `<div class="step-image"><img src="${esc(step.imageUrl)}" alt="step-${index + 1}" /></div>\n`;
      }

      if (step.description.trim()) {
        html += `<p class="step-description">${esc(step.description.trim())}</p>\n`;
      }

      html += `</div>\n`;
    });
    html += `</div>`;
  }

  return html;
}

/**
 * HTML 문자열에서 레시피 요약과 조리 순서를 추출합니다.
 */
export function parseRecipeContent(html: string): RecipeContent {
  if (!html) return { summary: '', steps: [{ description: '', imageUrl: '' }] };

  const parser = new DOMParser();
  const doc = parser.parseFromString(html, 'text/html');

  let summary = '';
  const steps: RecipeStep[] = [];

  // 1. Try parsing with data-attributes (New Format)
  const summaryDiv = doc.querySelector('[data-type="summary"]');
  if (summaryDiv) {
    summary = summaryDiv.textContent?.trim() ?? '';
  }

  const stepDivs = doc.querySelectorAll('[data-type="recipe-step"]');
  if (stepDivs.length > 0) {
    stepDivs.forEach((div) => {
      const img = div.querySelector('img');
      const p = div.querySelector('.step-description') || div.querySelector('p');

      let desc = '';
      if (p) {
        desc = p.textContent?.trim() ?? '';
      } else {
        const clone = div.cloneNode(true) as HTMLElement;
        const h4 = clone.querySelector('h4');
        if (h4) h4.remove();
        const imgInClone = clone.querySelector('img');
        if (imgInClone) imgInClone.remove();
        desc = clone.textContent?.trim() ?? '';
      }

      steps.push({
        description: desc,
        imageUrl: img?.getAttribute('src') ?? undefined,
      });
    });
  } else {
    // 2. Fallback for Legacy Format (Old Data)
    const ps = Array.from(doc.querySelectorAll('p'));
    if (ps.length > 0 && !ps[0]?.querySelector('img')) {
      summary = ps[0]?.textContent?.trim() ?? '';
    }

    const h4s = Array.from(doc.querySelectorAll('h4'));
    h4s.forEach((h4) => {
      let desc = '';
      let img: string | undefined;

      let next = h4.nextElementSibling;
      while (next && next.tagName !== 'H4') {
        if (next.tagName === 'P') {
          const imgEl = next.querySelector('img');
          if (imgEl) {
            img = imgEl.getAttribute('src') ?? undefined;
          } else {
            desc += (next.textContent ?? '') + '\n';
          }
        } else if (next.tagName === 'DIV' && next.querySelector('img')) {
          const imgEl = next.querySelector('img');
          if (imgEl) img = imgEl.getAttribute('src') ?? undefined;
        }
        next = next.nextElementSibling;
      }

      if (desc.trim() || img) {
        steps.push({ description: desc.trim(), imageUrl: img });
      }
    });

    if (steps.length === 0 && ps.length > 1) {
      const rest = ps.slice(summary ? 1 : 0).map(p => p.textContent).join('\n').trim();
      if (rest) steps.push({ description: rest });
    }
  }

  if (steps.length === 0) {
    steps.push({ description: '', imageUrl: '' });
  }

  return { summary, steps };
}