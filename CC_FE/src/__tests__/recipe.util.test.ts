import { describe, it, expect } from 'vitest';
import { parseRecipeUrlToId } from '@/utils/recipe';

// Added explicit test suite function style to avoid detection issues
describe('recipe util: parseRecipeUrlToId', function () {
  it('parses direct number', () => {
    expect(parseRecipeUrlToId('42')).toBe(42);
  });
  it('parses boards path', () => {
    expect(parseRecipeUrlToId('/boards/123')).toBe(123);
  });
  it('parses full URL', () => {
    expect(parseRecipeUrlToId('https://domain.com/boards/555')).toBe(555);
  });
  it('parses full URL with query/hash', () => {
    expect(parseRecipeUrlToId('https://domain.com/boards/99?x=1#a')).toBe(99);
  });
  it('returns undefined on invalid path patterns', () => {
    expect(parseRecipeUrlToId('https://domain.com/recipes/77')).toBeUndefined();
  });
  it('returns undefined on non-numeric input', () => {
    expect(parseRecipeUrlToId('abc')).toBeUndefined();
    expect(parseRecipeUrlToId('')).toBeUndefined();
  });
});
