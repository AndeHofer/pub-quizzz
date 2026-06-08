import {describe, expect, it} from 'vitest';
import {
    buildNewsAuthoringHintMarkup,
    buildNewsPayload,
    buildNewsRowsMarkup,
    toSubmitButtonText,
    validateNewsPayload
} from './admin_news_page';

describe('admin_news_page helpers', () => {
    it('trims title and text and keeps showOnHomePage in payload', () => {
        const payload = buildNewsPayload('  Titel  ', '  Text  ', true);
        expect(payload).toEqual({title: 'Titel', text: 'Text', showOnHomePage: true});
    });

    it('returns german validation error for empty fields', () => {
        expect(validateNewsPayload({
            title: '',
            text: 'Text',
            showOnHomePage: true
        })).toBe('Titel und Text sind erforderlich.');
        expect(validateNewsPayload({
            title: 'Titel',
            text: '',
            showOnHomePage: false
        })).toBe('Titel und Text sind erforderlich.');
    });

    it('renders status column with Ja and Nein labels', () => {
        const markup = buildNewsRowsMarkup([
            {newsId: 1, title: 'Sichtbar', text: 'A', createdAt: '2026-06-01T10:00:00Z', showOnHomePage: true},
            {newsId: 2, title: 'Versteckt', text: 'B', createdAt: '2026-06-01T10:00:00Z', showOnHomePage: false}
        ]);

        expect(markup).toContain('Ja');
        expect(markup).toContain('Nein');
        expect(markup).toContain('edit-news-btn');
        expect(markup).toContain('delete-news-btn');
    });

    it('returns create and edit submit labels', () => {
        expect(toSubmitButtonText(null)).toBe('Neuigkeit erstellen');
        expect(toSubmitButtonText(5)).toBe('Neuigkeit aktualisieren');
    });

    it('renders calendar event authoring hint markup with marker and metadata examples', () => {
        const markup = buildNewsAuthoringHintMarkup();

        expect(markup).toContain('Kalender-Event-Hinweis');
        expect(markup).toContain('[event-date:sept]2. September 2026[/event-date]');
        expect(markup).toContain('&lt;!--event {&quot;events&quot;:{&quot;sept&quot;:{&quot;title&quot;:&quot;Pub Quiz September&quot;');
        expect(markup).toContain('&quot;text&quot;:&quot;Optionaler Kalendertext&quot;');
        expect(markup).toContain('<code>text</code> ist optional');
    });
});
