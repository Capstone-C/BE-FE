// src/utils/recipe.ts
// Utility functions for handling recipe (post) references via URL.

/**
 * Attempt to extract a numeric recipe (post) ID from a user provided URL or path.
 * Supported examples:
 *   123
 *   /boards/123
 *   https://example.com/boards/123
 *   https://example.com/boards/123?x=1
 *   https://example.com/boards/123/#section
 * Returns undefined when not parsable.
 */
export function parseRecipeUrlToId(input: string | undefined | null): number | undefined {
  if (!input) return undefined;
  const raw = input.trim();
  if (raw.length === 0) return undefined;

  // Direct number
  if (/^\d+$/.test(raw)) return Number(raw);

  try {
    // Remove query/hash portion first for easier regex
    const cleaned = raw.replace(/[?#].*$/, '');
    // Find '/boards/<id>' pattern
    const match = cleaned.match(/\/boards\/(\d+)(?:\/)?$/);
    if (match) {
      const idNum = Number(match[1]);
      if (Number.isFinite(idNum) && idNum > 0) return idNum;
    }
  } catch {
    // noop
  }
  return undefined;
}

/** Build a canonical relative path for a recipe id. */
export function recipeIdToPath(id: number | undefined | null): string {
  if (!id) return '';
  return `/boards/${id}`;
}
