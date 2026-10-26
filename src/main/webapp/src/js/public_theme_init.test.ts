import {describe, expect, it, vi} from 'vitest';
import {
    initPublicTheme,
    MOTIF_LAYOUT_STORAGE_KEY,
    RESOLVED_THEME_STORAGE_KEY
} from './public_theme_init';

describe('initPublicTheme', () => {
    it('uses a tab-local preview override ahead of an active calendar event', () => {
        const setAttribute = vi.fn();
        const doc = {documentElement: {setAttribute}} as unknown as Document;
        const storage = {getItem: () => 'halloween'} as unknown as Storage;

        initPublicTheme(doc, storage, new Date('2026-12-25T12:00:00.000Z'));

        expect(setAttribute).toHaveBeenCalledWith('data-theme', 'halloween');
    });

    it('uses the active calendar event when no preview override exists', () => {
        const setAttribute = vi.fn();
        const doc = {documentElement: {setAttribute}} as unknown as Document;
        const storage = {getItem: () => null} as unknown as Storage;

        initPublicTheme(doc, storage, new Date('2026-12-25T12:00:00.000Z'));

        expect(setAttribute).toHaveBeenCalledWith('data-theme', 'christmas');
    });

    it('uses the Vienna month theme when no event or preview override applies', () => {
        const setAttribute = vi.fn();
        const doc = {documentElement: {setAttribute}} as unknown as Document;
        const storage = {getItem: () => null} as unknown as Storage;

        initPublicTheme(doc, storage, new Date('2026-08-15T12:00:00.000Z'));

        expect(setAttribute).toHaveBeenCalledWith('data-theme', 'august');
    });

    it('assigns one safe motif layout for the current public page load', () => {
        const setAttribute = vi.fn();
        const doc = {documentElement: {setAttribute}} as unknown as Document;
        const storage = {getItem: () => null} as unknown as Storage;

        initPublicTheme(doc, storage, new Date('2026-08-15T12:00:00.000Z'), () => 0.75);

        expect(setAttribute).toHaveBeenCalledWith('data-motif-layout', '4');
    });

    it('persists the resolved theme and generated motif for later page loads', () => {
        const setAttribute = vi.fn();
        const setItem = vi.fn();
        const doc = {documentElement: {setAttribute}} as unknown as Document;
        const storage = {getItem: () => null, setItem} as unknown as Storage;

        initPublicTheme(doc, storage, new Date('2026-08-15T12:00:00.000Z'), () => 0.25);

        expect(setItem).toHaveBeenCalledWith(RESOLVED_THEME_STORAGE_KEY, 'august');
        expect(setItem).toHaveBeenCalledWith(MOTIF_LAYOUT_STORAGE_KEY, '2');
    });

    it('restores the session motif instead of randomizing it on every page', () => {
        const setAttribute = vi.fn();
        const random = vi.fn(() => 0.75);
        const doc = {documentElement: {setAttribute}} as unknown as Document;
        const storage = {
            getItem: (key: string) => key === MOTIF_LAYOUT_STORAGE_KEY ? '2' : null,
            setItem: vi.fn()
        } as unknown as Storage;

        initPublicTheme(doc, storage, new Date('2026-08-15T12:00:00.000Z'), random);

        expect(setAttribute).toHaveBeenCalledWith('data-motif-layout', '2');
        expect(random).not.toHaveBeenCalled();
    });

    it('falls back safely when session storage is unavailable', () => {
        const setAttribute = vi.fn();
        const doc = {documentElement: {setAttribute}} as unknown as Document;
        const storage = {
            getItem: () => {
                throw new Error('blocked');
            },
            setItem: () => {
                throw new Error('blocked');
            }
        } as unknown as Storage;

        expect(() => initPublicTheme(
            doc,
            storage,
            new Date('2026-08-15T12:00:00.000Z'),
            () => 0
        )).not.toThrow();
        expect(setAttribute).toHaveBeenCalledWith('data-theme', 'august');
        expect(setAttribute).toHaveBeenCalledWith('data-motif-layout', '1');
    });
});
