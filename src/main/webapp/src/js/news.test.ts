import {describe, expect, it} from 'vitest';
import {buildNewsErrorMarkup, buildNewsSectionMarkup, sortAndLimitNews} from './news';

describe('news helpers', () => {
    it('renders empty-state text when no entries exist', () => {
        const markup = buildNewsSectionMarkup([]);
        expect(markup).toContain('Derzeit gibt es keine Neuigkeiten.');
    });

    it('escapes untrusted title and text', () => {
        const markup = buildNewsSectionMarkup([
            {
                newsId: 1,
                title: '<script>alert(1)</script>',
                text: '<img src=x onerror=alert(1)>',
                createdAt: '2026-06-01T10:00:00Z'
            }
        ]);

        expect(markup).not.toContain('<script>alert(1)</script>');
        expect(markup).not.toContain('<img src=x onerror=alert(1)>');
        expect(markup).toContain('&lt;script&gt;alert(1)&lt;/script&gt;');
        expect(markup).toContain('&lt;img src=x onerror=alert(1)&gt;');
    });

    it('renders invalid createdAt as German fallback dash', () => {
        const markup = buildNewsSectionMarkup([
            {
                newsId: 10,
                title: 'Titel',
                text: 'Text',
                createdAt: 'kein-datum'
            }
        ]);

        expect(markup).toContain('<span class="text-xs text-gray-500">-</span>');
    });

    it('renders createdAt as date only without time', () => {
        const markup = buildNewsSectionMarkup([
            {
                newsId: 12,
                title: 'Datum',
                text: 'Nur Datum',
                createdAt: '2026-06-01T18:45:00Z'
            }
        ]);

        expect(markup).not.toMatch(/\d{1,2}:\d{2}/);
    });

    it('renders newlines as line breaks while keeping escaping', () => {
        const markup = buildNewsSectionMarkup([
            {
                newsId: 11,
                title: 'Zeilen',
                text: 'Erste Zeile\n<script>alert(1)</script>',
                createdAt: '2026-06-01T10:00:00Z'
            }
        ]);

        expect(markup).toContain('Erste Zeile<br>&lt;script&gt;alert(1)&lt;/script&gt;');
    });

    it('renders German fallback for fetch errors', () => {
        const markup = buildNewsErrorMarkup();
        expect(markup).toContain('Neuigkeiten konnten nicht geladen werden.');
    });

    it('sorts news newest-first and limits to three entries', () => {
        const result = sortAndLimitNews([
            {newsId: 1, title: 'Alt', text: 'A', createdAt: '2026-06-01T10:00:00Z'},
            {newsId: 2, title: 'Neu', text: 'B', createdAt: '2026-06-04T10:00:00Z'},
            {newsId: 3, title: 'Mitte', text: 'C', createdAt: '2026-06-03T10:00:00Z'},
            {newsId: 4, title: 'Sehr alt', text: 'D', createdAt: '2026-05-30T10:00:00Z'}
        ]);

        expect(result).toHaveLength(3);
        expect(result.map(item => item.newsId)).toEqual([2, 3, 1]);
    });

    it('does not mutate the original array when sorting and limiting', () => {
        const input = [
            {newsId: 1, title: 'A', text: 'A', createdAt: '2026-06-01T10:00:00Z'},
            {newsId: 2, title: 'B', text: 'B', createdAt: '2026-06-02T10:00:00Z'}
        ];

        const originalOrder = input.map(item => item.newsId);
        void sortAndLimitNews(input);

        expect(input.map(item => item.newsId)).toEqual(originalOrder);
    });

    it('uses newsId descending tie-break for equal createdAt', () => {
        const result = sortAndLimitNews([
            {newsId: 3, title: 'A', text: 'A', createdAt: '2026-06-01T10:00:00Z'},
            {newsId: 9, title: 'B', text: 'B', createdAt: '2026-06-01T10:00:00Z'},
            {newsId: 5, title: 'C', text: 'C', createdAt: '2026-06-01T10:00:00Z'}
        ]);

        expect(result.map(item => item.newsId)).toEqual([9, 5, 3]);
    });
});
