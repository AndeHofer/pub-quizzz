import type {NewsDTO} from './types';
import {withEnsuredCsrfHeaders} from './csrf';
import {escapeHtml} from './html-utils';
import {renderTable, showError, showLoading, showModal, trustedHtml} from './admin_ui';
import {readHttpErrorMessage} from './http-utils';
import {handleAuthExpiredIfNeeded} from './auth-session';

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
    const response = await fetch(API_BASE);
    if (await handleAuthExpiredIfNeeded(response.clone())) {
        throw new Error('AUTH_EXPIRED_REDIRECT');
    }
    if (!response.ok) {
        throw new Error(await readHttpErrorMessage(response, 'Laden fehlgeschlagen'));
    }
    return response.json() as Promise<NewsDTO[]>;
}

export async function createNews(payload: { title: string; text: string }): Promise<void> {
    const requestInit = buildNewsCreateRequestInit(payload);
    requestInit.headers = await withEnsuredCsrfHeaders(requestInit.headers);
    const response = await fetch(API_BASE, requestInit);
    if (await handleAuthExpiredIfNeeded(response.clone())) {
        throw new Error('AUTH_EXPIRED_REDIRECT');
    }
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `HTTP ${response.status}`);
    }
}

export async function updateNews(id: number, payload: { title: string; text: string }): Promise<void> {
    const requestInit = buildNewsUpdateRequestInit(payload);
    requestInit.headers = await withEnsuredCsrfHeaders(requestInit.headers);
    const response = await fetch(buildNewsUrl(id), requestInit);
    if (await handleAuthExpiredIfNeeded(response.clone())) {
        throw new Error('AUTH_EXPIRED_REDIRECT');
    }
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `HTTP ${response.status}`);
    }
}

export async function deleteNews(id: number): Promise<void> {
    const requestInit = buildNewsDeleteRequestInit();
    requestInit.headers = await withEnsuredCsrfHeaders(requestInit.headers);
    const response = await fetch(buildNewsUrl(id), requestInit);
    if (await handleAuthExpiredIfNeeded(response.clone())) {
        throw new Error('AUTH_EXPIRED_REDIRECT');
    }
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `HTTP ${response.status}`);
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

    if (!createNewsButton || !viewNewsButton) {
        return;
    }

    createNewsButton.addEventListener('click', () => {
        void createNewsFromPrompt();
    });

    viewNewsButton.addEventListener('click', () => {
        void viewNews();
    });
}
