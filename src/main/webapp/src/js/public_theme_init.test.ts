import {describe, expect, it, vi} from 'vitest';
import {initPublicTheme} from './public_theme_init';

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
});
