import {describe, expect, it, vi} from 'vitest';
import {
    DEFAULT_THEME_ID,
    THEME_OPTIONS,
    THEME_STORAGE_KEY,
    applyThemeToDocument,
    clearThemePreview,
    isThemeId,
    normalizeThemeId,
    readThemePreview,
    readThemePreviewOverride,
    saveThemePreview
} from './theme-preview';

describe('theme-preview ids', () => {
    it('exposes all month and event preview themes in display order', () => {
        expect(THEME_OPTIONS.map(option => option.id)).toEqual([
            'standard', 'january', 'february', 'march', 'april', 'may', 'june',
            'july', 'august', 'september', 'october', 'november', 'december',
            'easter', 'halloween', 'christmas', 'new-year'
        ]);
        expect(THEME_OPTIONS.map(option => option.label)).toEqual(expect.arrayContaining([
            'März', 'Ostern', 'Weihnachten', 'Neujahr'
        ]));
    });

    it('accepts only known ids', () => {
        expect(isThemeId('november')).toBe(true);
        expect(isThemeId('unknown')).toBe(false);
    });

    it('falls back to default for invalid ids', () => {
        expect(normalizeThemeId('unknown')).toBe(DEFAULT_THEME_ID);
        expect(normalizeThemeId(null)).toBe(DEFAULT_THEME_ID);
    });

});

describe('theme-preview storage and application', () => {
    it('returns null when no non-standard preview override is stored', () => {
        expect(readThemePreviewOverride({getItem: () => null})).toBeNull();
        expect(readThemePreviewOverride({getItem: () => 'standard'})).toBeNull();
        expect(readThemePreviewOverride({getItem: () => 'unknown'})).toBeNull();
    });

    it('returns valid non-standard preview overrides', () => {
        expect(readThemePreviewOverride({getItem: () => 'halloween'})).toBe('halloween');
    });

    it('stores and clears session preview state', () => {
        const store = new Map<string, string>();
        const storage = {
            getItem: (key: string) => store.get(key) ?? null,
            setItem: (key: string, value: string) => {
                store.set(key, value);
            },
            removeItem: (key: string) => {
                store.delete(key);
            }
        };

        expect(readThemePreview(storage)).toBe('standard');

        saveThemePreview(storage, 'november');
        expect(store.get(THEME_STORAGE_KEY)).toBe('november');
        expect(readThemePreview(storage)).toBe('november');

        saveThemePreview(storage, 'standard');
        expect(store.has(THEME_STORAGE_KEY)).toBe(false);
        expect(readThemePreview(storage)).toBe('standard');

        saveThemePreview(storage, 'halloween');
        expect(readThemePreview(storage)).toBe('halloween');
        clearThemePreview(storage);
        expect(readThemePreview(storage)).toBe('standard');
    });

    it('normalizes stale values in storage', () => {
        const storage = {
            getItem: vi.fn(() => 'legacy-theme')
        };

        expect(readThemePreview(storage)).toBe('standard');
    });

    it('applies selected theme on html data-theme attribute', () => {
        const setAttribute = vi.fn();
        const documentMock = {
            documentElement: {
                setAttribute
            }
        } as unknown as Document;

        applyThemeToDocument(documentMock, 'halloween');

        expect(setAttribute).toHaveBeenCalledWith('data-theme', 'halloween');
    });
});
