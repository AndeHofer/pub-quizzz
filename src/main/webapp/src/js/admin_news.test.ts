import {afterEach, describe, expect, it, vi} from 'vitest';
import {
    buildAdminNewsTableMarkup,
    buildNewsAuthoringHintMarkup,
    buildNewsCreateRequestInit,
    buildNewsDeleteRequestInit,
    buildNewsPayload,
    buildNewsUpdateRequestInit,
    buildNewsUrl,
    confirmDeleteNews,
    createNews,
    deleteNews,
    promptForPayload,
    updateNews
} from './admin_news';
import {httpClient} from './http-client';

function mockAxiosResponse(overrides: { status: number; statusText?: string; data?: unknown }) {
    return {
        data: overrides.data ?? '',
        status: overrides.status,
        statusText: overrides.statusText ?? '',
        headers: {},
        config: {headers: {}}
    };
}

afterEach(() => {
    vi.restoreAllMocks();
});

describe('admin_news helpers', () => {
    it('renders german tooltip with event-date marker and hidden metadata example', () => {
        const markup = buildNewsAuthoringHintMarkup();

        expect(markup).toContain('Kalender-Event-Hinweis');
        expect(markup).toContain('[event-date:sept]2. September 2026[/event-date]');
        expect(markup).toContain('&lt;!--event {&quot;events&quot;:{&quot;sept&quot;:{&quot;title&quot;:&quot;Pub Quiz September&quot;');
        expect(markup).toContain('&quot;text&quot;:&quot;Optionaler Kalendertext&quot;');
        expect(markup).toContain('<code>text</code> ist optional');
    });

    it('escapes untrusted title/text and keeps trusted action buttons', () => {
        const markup = buildAdminNewsTableMarkup([
            {
                newsId: 9,
                title: '<script>alert(1)</script>',
                text: '<img src=x onerror=alert(1)>',
                createdAt: '2026-06-01T12:00:00Z'
            }
        ]);

        expect(markup).not.toContain('<script>alert(1)</script>');
        expect(markup).not.toContain('<img src=x onerror=alert(1)>');
        expect(markup).toContain('&lt;script&gt;alert(1)&lt;/script&gt;');
        expect(markup).toContain('&lt;img src=x onerror=alert(1)&gt;');
        expect(markup).toContain('class="icon-btn edit-news-btn"');
        expect(markup).toContain('class="icon-btn delete-news-btn"');
    });

    it('trims title/text for request payload', () => {
        const payload = buildNewsPayload('  Titel  ', '  Text  ');
        expect(payload).toEqual({title: 'Titel', text: 'Text'});
    });

    it('builds create request init with json content type and post method', () => {
        const init = buildNewsCreateRequestInit({title: 'Titel', text: 'Text'});
        expect(init.method).toBe('POST');
        expect(init.headers).toEqual({'Content-Type': 'application/json'});
        expect(init.body).toBe('{"title":"Titel","text":"Text"}');
    });

    it('builds update request init with json content type and put method', () => {
        const init = buildNewsUpdateRequestInit({title: 'Neu', text: 'Inhalt'});
        expect(init.method).toBe('PUT');
        expect(init.headers).toEqual({'Content-Type': 'application/json'});
        expect(init.body).toBe('{"title":"Neu","text":"Inhalt"}');
    });

    it('builds delete request init with delete method', () => {
        expect(buildNewsDeleteRequestInit()).toEqual({method: 'DELETE'});
    });

    it('builds resource url for a news id', () => {
        expect(buildNewsUrl(42)).toBe('/admin/news/42');
    });

    it('returns null and alerts on empty prompt payload', () => {
        const promptMock = vi.fn();
        promptMock.mockReturnValueOnce('   ');
        promptMock.mockReturnValueOnce('   ');
        const alertMock = vi.fn();

        const result = promptForPayload(
            '',
            '',
            promptMock as unknown as (message?: string, defaultValue?: string) => string | null,
            alertMock
        );

        expect(result).toBeNull();
        expect(alertMock).toHaveBeenCalledWith('Titel und Text sind erforderlich.');
    });

    it('uses confirm helper for delete confirmation', () => {
        const confirmMock = vi.fn().mockReturnValue(false);
        expect(confirmDeleteNews(11, confirmMock)).toBe(false);
        expect(confirmMock).toHaveBeenCalledWith('Neuigkeit 11 wirklich löschen?');
    });

    it('throws backend message when create fails', async () => {
        vi.spyOn(httpClient, 'post').mockResolvedValue(mockAxiosResponse({
            status: 400,
            statusText: 'Bad Request',
            data: 'Titel fehlt'
        }));

        await expect(createNews({title: 'T', text: 'X'})).rejects.toThrow('Titel fehlt');
    });

    it('does not bootstrap csrf endpoint when creating news', async () => {
        const postSpy = vi.spyOn(httpClient, 'post').mockResolvedValue(mockAxiosResponse({
            status: 201,
            statusText: 'Created',
            data: ''
        }));
        const fetchSpy = vi.spyOn(globalThis, 'fetch');

        await createNews({title: 'Titel', text: 'Text'});

        expect(postSpy).toHaveBeenCalledWith('/admin/news', {title: 'Titel', text: 'Text'}, {
            headers: {'Content-Type': 'application/json'}
        });
        expect(fetchSpy).not.toHaveBeenCalledWith('/api/bootstrap', expect.anything());
    });

    it('throws backend message when update fails', async () => {
        vi.spyOn(httpClient, 'put').mockResolvedValue(mockAxiosResponse({
            status: 404,
            statusText: 'Not Found',
            data: 'Neuigkeit nicht gefunden'
        }));

        await expect(updateNews(4, {title: 'Neu', text: 'Text'})).rejects.toThrow('Neuigkeit nicht gefunden');
    });

    it('throws backend message when delete fails', async () => {
        vi.spyOn(httpClient, 'delete').mockResolvedValue(mockAxiosResponse({
            status: 400,
            statusText: 'Bad Request',
            data: 'Löschen fehlgeschlagen'
        }));

        await expect(deleteNews(9)).rejects.toThrow('Löschen fehlgeschlagen');
    });
});
