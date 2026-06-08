import type {NewsDTO} from './types';
import {escapeHtml} from './html-utils';
import {renderTable, showError, showLoading, showModal, trustedHtml} from './admin_ui';
import {readHttpErrorMessage} from './http-utils';
import {handleAuthExpiredIfNeeded} from './auth-session';
import {httpClient, readResponseText, toResponse} from './http-client';

const API_BASE = '/admin/news';

function formatNewsDate(createdAt: string): string {
    const parsed = new Date(createdAt);
    if (Number.isNaN(parsed.getTime())) {
        return '-';
    }
    return new Intl.DateTimeFormat('de-AT', {
        dateStyle: 'medium',
        timeStyle: 'short'
    }).format(parsed);
}

function asText(error: unknown): string {
    return error instanceof Error ? error.message : String(error);
}

function isAuthExpiredRedirectError(error: unknown): boolean {
    return error instanceof Error && error.message === 'AUTH_EXPIRED_REDIRECT';
}

export function buildNewsPayload(title: string, text: string): { title: string; text: string } {
    return {
        title: title.trim(),
        text: text.trim()
    };
}

export function buildNewsUrl(newsId: number): string {
    return `${API_BASE}/${newsId}`;
}

export function buildNewsCreateRequestInit(payload: { title: string; text: string }): RequestInit {
    return {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    };
}

export function buildNewsUpdateRequestInit(payload: { title: string; text: string }): RequestInit {
    return {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    };
}

export function buildNewsDeleteRequestInit(): RequestInit {
    return {
        method: 'DELETE'
    };
}

export function buildNewsAuthoringHintMarkup(): string {
    return '<details class="mt-2 text-sm text-gray-600">' +
        '<summary class="cursor-pointer select-none">Kalender-Event-Hinweis</summary>' +
        '<div class="mt-2 space-y-2">' +
        '<p>Im Neuigkeiten-Text koennen Sie klickbare Kalender-Termine setzen.</p>' +
        '<p><strong>Marker im sichtbaren Text:</strong> <code>[event-date:sept]2. September 2026[/event-date]</code></p>' +
        '<p><strong>Versteckte Event-Metadaten:</strong></p>' +
        '<pre class="whitespace-pre-wrap rounded bg-gray-100 p-2 text-xs"><code>&lt;!--event {&quot;events&quot;:{&quot;sept&quot;:{&quot;title&quot;:&quot;Pub Quiz September&quot;,&quot;start&quot;:&quot;2026-09-02T19:00&quot;,&quot;end&quot;:&quot;2026-09-02T22:00&quot;,&quot;location&quot;:&quot;Pub XY, Wien&quot;,&quot;text&quot;:&quot;Optionaler Kalendertext&quot;}}}--&gt;</code></pre>' +
        '<p>Pflichtfelder pro Event: <code>title</code>, <code>start</code>, <code>end</code>, <code>location</code>.</p>' +
        '<p><code>text</code> ist optional und wird nur fuer den Kalendertext verwendet. Ohne <code>text</code> wird kein Kalendertext gesetzt.</p>' +
        '</div>' +
        '</details>';
}

export function buildAdminNewsTableMarkup(items: NewsDTO[]): string {
    if (items.length === 0) {
        return '<p>Keine Neuigkeiten gefunden.</p>';
    }

    const headers = ['ID', 'Titel', 'Text', 'Erstellt am', 'Aktionen'];
    return renderTable(headers, items, row => {
        const item = row as NewsDTO;
        return [
            String(item.newsId),
            item.title,
            item.text,
            formatNewsDate(item.createdAt),
            trustedHtml(
                `<button class="icon-btn edit-news-btn" data-id="${escapeHtml(String(item.newsId))}" title="Neuigkeit bearbeiten">✏️</button>` +
                `<button class="icon-btn delete-news-btn" data-id="${escapeHtml(String(item.newsId))}" title="Neuigkeit löschen">🗑️</button>`
            )
        ];
    });
}

async function loadAllNews(): Promise<NewsDTO[]> {
    const axiosResponse = await httpClient.get<NewsDTO[]>(API_BASE);
    const response = toResponse(axiosResponse);
    if (await handleAuthExpiredIfNeeded(response.clone())) {
        throw new Error('AUTH_EXPIRED_REDIRECT');
    }
    if (!response.ok) {
        throw new Error(await readHttpErrorMessage(response, 'Laden fehlgeschlagen'));
    }
    return axiosResponse.data;
}

export async function createNews(payload: { title: string; text: string }): Promise<void> {
    const response = await httpClient.post(API_BASE, payload, {
        headers: {'Content-Type': 'application/json'}
    });
    const authResponse = toResponse(response);
    if (await handleAuthExpiredIfNeeded(authResponse.clone())) {
        throw new Error('AUTH_EXPIRED_REDIRECT');
    }
    if (response.status < 200 || response.status >= 300) {
        throw new Error(readResponseText(response.data) || `HTTP ${response.status}`);
    }
}

export async function updateNews(id: number, payload: { title: string; text: string }): Promise<void> {
    const response = await httpClient.put(buildNewsUrl(id), payload, {
        headers: {'Content-Type': 'application/json'}
    });
    const authResponse = toResponse(response);
    if (await handleAuthExpiredIfNeeded(authResponse.clone())) {
        throw new Error('AUTH_EXPIRED_REDIRECT');
    }
    if (response.status < 200 || response.status >= 300) {
        throw new Error(readResponseText(response.data) || `HTTP ${response.status}`);
    }
}

export async function deleteNews(id: number): Promise<void> {
    const response = await httpClient.delete(buildNewsUrl(id));
    const authResponse = toResponse(response);
    if (await handleAuthExpiredIfNeeded(authResponse.clone())) {
        throw new Error('AUTH_EXPIRED_REDIRECT');
    }
    if (response.status < 200 || response.status >= 300) {
        throw new Error(readResponseText(response.data) || `HTTP ${response.status}`);
    }
}

export function promptForPayload(
    initialTitle = '',
    initialText = '',
    promptFn: (message?: string, defaultValue?: string) => string | null = prompt,
    alertFn: (message?: string) => void = alert
): { title: string; text: string } | null {
    const title = promptFn('Titel der Neuigkeit:', initialTitle);
    if (title === null) {
        return null;
    }
    const text = promptFn('Text der Neuigkeit:', initialText);
    if (text === null) {
        return null;
    }
    const payload = buildNewsPayload(title, text);
    if (!payload.title || !payload.text) {
        alertFn('Titel und Text sind erforderlich.');
        return null;
    }
    return payload;
}

export function confirmDeleteNews(newsId: number, confirmFn: (message?: string) => boolean = confirm): boolean {
    return confirmFn(`Neuigkeit ${newsId} wirklich löschen?`);
}

async function viewNews(): Promise<void> {
    showModal('Neuigkeiten verwalten', showLoading());

    try {
        const items = await loadAllNews();
        showModal('Neuigkeiten verwalten', buildAdminNewsTableMarkup(items));
        wireRowActions(items);
    } catch (error) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError(`Fehler beim Laden der Neuigkeiten: ${asText(error)}`));
    }
}

function wireRowActions(items: NewsDTO[]): void {
    document.querySelectorAll('.edit-news-btn').forEach(button => {
        button.addEventListener('click', async () => {
            const id = Number((button as HTMLElement).dataset.id);
            const selected = items.find(item => item.newsId === id);
            if (!selected) {
                return;
            }

            const payload = promptForPayload(selected.title, selected.text);
            if (!payload) {
                return;
            }

            try {
                await updateNews(id, payload);
                await viewNews();
            } catch (error) {
                if (isAuthExpiredRedirectError(error)) {
                    return;
                }
                showModal('Fehler', showError(`Fehler beim Aktualisieren: ${asText(error)}`));
            }
        });
    });

    document.querySelectorAll('.delete-news-btn').forEach(button => {
        button.addEventListener('click', async () => {
            const id = Number((button as HTMLElement).dataset.id);
            if (Number.isNaN(id)) return;
            if (!confirmDeleteNews(id)) return;

            try {
                await deleteNews(id);
                await viewNews();
            } catch (error) {
                if (isAuthExpiredRedirectError(error)) {
                    return;
                }
                showModal('Fehler', showError(`Fehler beim Löschen: ${asText(error)}`));
            }
        });
    });
}

async function createNewsFromPrompt(): Promise<void> {
    const payload = promptForPayload();
    if (!payload) {
        return;
    }

    try {
        await createNews(payload);
        await viewNews();
    } catch (error) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError(`Fehler beim Erstellen: ${asText(error)}`));
    }
}

export function initAdminNewsActions(): void {
    const createNewsButton = document.getElementById('createNewsBtn');
    const viewNewsButton = document.getElementById('viewNewsBtn');
    const hintContainer = document.getElementById('newsAuthoringHint');

    if (!createNewsButton || !viewNewsButton) {
        return;
    }

    if (hintContainer) {
        hintContainer.innerHTML = buildNewsAuthoringHintMarkup();
    }

    createNewsButton.addEventListener('click', () => {
        void createNewsFromPrompt();
    });

    viewNewsButton.addEventListener('click', () => {
        void viewNews();
    });
}
