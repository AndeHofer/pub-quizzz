import {describe, expect, it} from 'vitest';
import {
    buildGoogleCalendarUrl,
    buildIcsContent,
    buildNewsErrorMarkup,
    buildNewsSectionMarkup,
    extractEventMetaFromText,
    sortAndLimitNews
} from './news';

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
                createdAt: '2026-06-01T10:00:00Z',
                showOnHomePage: true
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
                createdAt: 'kein-datum',
                showOnHomePage: true
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
                createdAt: '2026-06-01T18:45:00Z',
                showOnHomePage: true
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
                createdAt: '2026-06-01T10:00:00Z',
                showOnHomePage: true
            }
        ]);

        expect(markup).toContain('Erste Zeile<br>&lt;script&gt;alert(1)&lt;/script&gt;');
    });

    it('extracts hidden event metadata and strips it from visible text', () => {
        const input = 'Text davor.<!--event {"events":{"sept":{"title":"Quiz","start":"2026-09-02T19:00","end":"2026-09-02T22:00","location":"Wien"}}}-->Text danach.';

        const result = extractEventMetaFromText(input);

        expect(result.visibleText).toBe('Text davor.Text danach.');
        expect(result.events.sept?.title).toBe('Quiz');
    });

    it('renders clickable inline event-date label in text body when metadata is valid', () => {
        const markup = buildNewsSectionMarkup([
            {
                newsId: 21,
                title: 'Ankuendigung',
                text: 'Unser naechstes Quiz ist am [event-date:sept]2. September 2026[/event-date].<!--event {"events":{"sept":{"title":"Pub Quiz September","start":"2026-09-02T19:00","end":"2026-09-02T22:00","location":"Pub XY, Wien"}}}-->',
                createdAt: '2026-08-30T10:00:00Z',
                showOnHomePage: true
            }
        ]);

        expect(markup).not.toContain('[event-date:sept]');
        expect(markup).toContain('2. September 2026');
        expect(markup).toContain('Google Kalender');
        expect(markup).toContain('ICS herunterladen');
    });

    it('renders plain inline label when marker id has no valid event mapping', () => {
        const markup = buildNewsSectionMarkup([
            {
                newsId: 22,
                title: 'Ankuendigung',
                text: 'Termin: [event-date:missing]2. September 2026[/event-date].<!--event {"events":{"other":{"title":"Pub Quiz September","start":"2026-09-02T19:00","end":"2026-09-02T22:00","location":"Pub XY, Wien"}}}-->',
                createdAt: '2026-08-30T10:00:00Z',
                showOnHomePage: true
            }
        ]);

        expect(markup).toContain('2. September 2026');
        expect(markup).not.toContain('Google Kalender');
        expect(markup).not.toContain('ICS herunterladen');
    });

    it('requires event title and does not fallback to news title', () => {
        const markup = buildNewsSectionMarkup([
            {
                newsId: 23,
                title: 'Fallback Titel',
                text: 'Termin: [event-date:sept]2. September 2026[/event-date].<!--event {"events":{"sept":{"start":"2026-09-02T19:00","end":"2026-09-02T22:00","location":"Pub XY, Wien"}}}-->',
                createdAt: '2026-08-30T10:00:00Z',
                showOnHomePage: true
            }
        ]);

        expect(markup).toContain('2. September 2026');
        expect(markup).not.toContain('Google Kalender');
        expect(markup).not.toContain('ICS herunterladen');
    });

    it('builds google calendar URL using Europe/Vienna for floating local times', () => {
        const url = buildGoogleCalendarUrl({
            title: 'Pub Quiz September',
            start: '2026-09-02T19:00',
            end: '2026-09-02T22:00',
            location: 'Pub XY, Wien',
            text: 'Quizabend'
        });

        expect(url).toContain('https://calendar.google.com/calendar/render');
        expect(url).toContain('ctz=Europe%2FVienna');
        expect(url).toContain('dates=20260902T190000%2F20260902T220000');
    });

    it('builds ICS content with Europe/Vienna timezone for floating local times', () => {
        const ics = buildIcsContent({
            title: 'Pub Quiz September',
            start: '2026-09-02T19:00',
            end: '2026-09-02T22:00',
            location: 'Pub XY, Wien',
            text: 'Quizabend'
        });

        expect(ics).toContain('BEGIN:VCALENDAR');
        expect(ics).toContain('BEGIN:VEVENT');
        expect(ics).toContain('SUMMARY:Pub Quiz September');
        expect(ics).toContain('DTSTART;TZID=Europe/Vienna:20260902T190000');
        expect(ics).toContain('DTEND;TZID=Europe/Vienna:20260902T220000');
        expect(ics).toContain('LOCATION:Pub XY\\, Wien');
    });

    it('omits calendar description when metadata text is missing', () => {
        const url = buildGoogleCalendarUrl({
            title: 'Pub Quiz September',
            start: '2026-09-02T19:00',
            end: '2026-09-02T22:00',
            location: 'Pub XY, Wien'
        });

        const ics = buildIcsContent({
            title: 'Pub Quiz September',
            start: '2026-09-02T19:00',
            end: '2026-09-02T22:00',
            location: 'Pub XY, Wien'
        });

        expect(url).not.toContain('details=');
        expect(ics).not.toContain('DESCRIPTION:');
    });

    it('renders German fallback for fetch errors', () => {
        const markup = buildNewsErrorMarkup();
        expect(markup).toContain('Neuigkeiten konnten nicht geladen werden.');
    });

    it('sorts news newest-first and limits to three entries', () => {
        const result = sortAndLimitNews([
            {newsId: 1, title: 'Alt', text: 'A', createdAt: '2026-06-01T10:00:00Z', showOnHomePage: true},
            {newsId: 2, title: 'Neu', text: 'B', createdAt: '2026-06-04T10:00:00Z', showOnHomePage: true},
            {newsId: 3, title: 'Mitte', text: 'C', createdAt: '2026-06-03T10:00:00Z', showOnHomePage: true},
            {newsId: 4, title: 'Sehr alt', text: 'D', createdAt: '2026-05-30T10:00:00Z', showOnHomePage: false}
        ]);

        expect(result).toHaveLength(3);
        expect(result.map(item => item.newsId)).toEqual([2, 3, 1]);
    });

    it('does not mutate the original array when sorting and limiting', () => {
        const input = [
            {newsId: 1, title: 'A', text: 'A', createdAt: '2026-06-01T10:00:00Z', showOnHomePage: true},
            {newsId: 2, title: 'B', text: 'B', createdAt: '2026-06-02T10:00:00Z', showOnHomePage: true}
        ];

        const originalOrder = input.map(item => item.newsId);
        void sortAndLimitNews(input);

        expect(input.map(item => item.newsId)).toEqual(originalOrder);
    });

    it('uses newsId descending tie-break for equal createdAt', () => {
        const result = sortAndLimitNews([
            {newsId: 3, title: 'A', text: 'A', createdAt: '2026-06-01T10:00:00Z', showOnHomePage: true},
            {newsId: 9, title: 'B', text: 'B', createdAt: '2026-06-01T10:00:00Z', showOnHomePage: true},
            {newsId: 5, title: 'C', text: 'C', createdAt: '2026-06-01T10:00:00Z', showOnHomePage: false}
        ]);

        expect(result.map(item => item.newsId)).toEqual([9, 5, 3]);
    });
});
