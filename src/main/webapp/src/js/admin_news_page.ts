import type {NewsDTO} from './types';
import {escapeHtml} from './html-utils';
import {apiFetch} from './admin-api';
import {readHttpErrorMessage} from './http-utils';

interface NewsPayload {
    title: string;
    text: string;
    showOnHomePage: boolean;
}

interface NewsPageState {
    editingId: number | null;
    items: NewsDTO[];
}

export function buildNewsAuthoringHintMarkup(): string {
    return '<details class="mt-2 text-sm text-gray-600">' +
        '<summary class="cursor-pointer select-none">Kalender-Event-Hinweis</summary>' +
        '<div class="mt-2 space-y-2">' +
        '<p>Im Neuigkeiten-Text können klickbare Kalender-Termine gesetzt werden.</p>' +
        '<p><strong>Marker im sichtbaren Text:</strong> <code>[event-date:sept]2. September 2026[/event-date]</code></p>' +
        '<p><strong>Versteckte Event-Metadaten:</strong></p>' +
        '<pre class="whitespace-pre-wrap rounded bg-gray-100 p-2 text-xs"><code>&lt;!--event {&quot;events&quot;:{&quot;sept&quot;:{&quot;title&quot;:&quot;Pub Quiz September&quot;,&quot;start&quot;:&quot;2026-09-02T19:00&quot;,&quot;end&quot;:&quot;2026-09-02T22:00&quot;,&quot;location&quot;:&quot;Pub XY, Wien&quot;,&quot;text&quot;:&quot;Optionaler Kalendertext&quot;}}}--&gt;</code></pre>' +
        '<p>Pflichtfelder pro Event: <code>title</code>, <code>start</code>, <code>end</code>, <code>location</code>.</p>' +
        '<p><code>text</code> ist optional und wird nur für den Kalendertext verwendet. Ohne <code>text</code> wird kein Kalendertext gesetzt.</p>' +
        '</div>' +
        '</details>';
}

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

export function buildNewsPayload(title: string, text: string, showOnHomePage: boolean): NewsPayload {
    return {
        title: title.trim(),
        text: text.trim(),
        showOnHomePage
    };
}

export function validateNewsPayload(payload: NewsPayload): string | null {
    if (!payload.title || !payload.text) {
        return 'Titel und Text sind erforderlich.';
    }
    return null;
}

export function toSubmitButtonText(editingId: number | null): string {
    return editingId === null ? 'Neuigkeit erstellen' : 'Neuigkeit aktualisieren';
}

export function buildNewsRowsMarkup(items: NewsDTO[]): string {
    if (items.length === 0) {
        return '<tr><td colspan="5" class="text-sm text-gray-500">Keine Neuigkeiten gefunden.</td></tr>';
    }

    return items.map(item => {
        const status = item.showOnHomePage ? 'Ja' : 'Nein';
        return '<tr>' +
            `<td>${escapeHtml(item.title)}</td>` +
            `<td>${escapeHtml(formatNewsDate(item.createdAt))}</td>` +
            `<td>${status}</td>` +
            '<td class="whitespace-nowrap">' +
            `<button type="button" class="icon-btn edit-news-btn" data-id="${escapeHtml(String(item.newsId))}" title="Neuigkeit bearbeiten">✏️</button>` +
            `<button type="button" class="icon-btn delete-news-btn" data-id="${escapeHtml(String(item.newsId))}" title="Neuigkeit löschen">🗑️</button>` +
            '</td>' +
            `<td class="hidden">${escapeHtml(item.text)}</td>` +
            '</tr>';
    }).join('');
}

async function loadNews(): Promise<NewsDTO[]> {
    const response = await apiFetch('/admin/news');
    if (!response.ok) {
        throw new Error(await readHttpErrorMessage(response, 'Neuigkeiten konnten nicht geladen werden.'));
    }
    return await response.json() as NewsDTO[];
}

async function saveNews(editingId: number | null, payload: NewsPayload): Promise<void> {
    const url = editingId === null ? '/admin/news' : `/admin/news/${editingId}`;
    const method = editingId === null ? 'POST' : 'PUT';
    const response = await apiFetch(url, {
        method,
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });
    if (!response.ok) {
        throw new Error(await readHttpErrorMessage(response, 'Speichern fehlgeschlagen.'));
    }
}

async function removeNews(id: number): Promise<void> {
    const response = await apiFetch(`/admin/news/${id}`, {method: 'DELETE'});
    if (!response.ok) {
        throw new Error(await readHttpErrorMessage(response, 'Löschen fehlgeschlagen.'));
    }
}

function setMessage(text: string, kind: 'success' | 'error' | 'neutral'): void {
    const message = document.getElementById('newsMessage');
    if (!message) {
        return;
    }

    message.textContent = text;
    message.className = 'message';
    message.classList.add('block');

    if (kind === 'success') {
        message.classList.add('success');
    } else if (kind === 'error') {
        message.classList.add('error');
    }
}

function clearMessage(): void {
    const message = document.getElementById('newsMessage');
    if (!message) {
        return;
    }
    message.className = 'message';
    message.textContent = '';
}

function setFormMode(state: NewsPageState): void {
    const submit = document.getElementById('newsSubmitBtn');
    const cancel = document.getElementById('newsCancelEditBtn');
    const heading = document.getElementById('newsFormHeading');
    if (submit) {
        submit.textContent = toSubmitButtonText(state.editingId);
    }
    if (cancel) {
        cancel.classList.toggle('hidden', state.editingId === null);
    }
    if (heading) {
        heading.textContent = state.editingId === null ? 'Neuigkeit erstellen' : 'Neuigkeit bearbeiten';
    }
}

function resetForm(state: NewsPageState): void {
    const title = document.getElementById('newsTitle') as HTMLInputElement | null;
    const text = document.getElementById('newsText') as HTMLTextAreaElement | null;
    const show = document.getElementById('newsShowOnHomePage') as HTMLInputElement | null;

    if (title) {
        title.value = '';
    }
    if (text) {
        text.value = '';
    }
    if (show) {
        show.checked = false;
    }
    state.editingId = null;
    setFormMode(state);
}

function fillFormFromItem(state: NewsPageState, item: NewsDTO): void {
    const title = document.getElementById('newsTitle') as HTMLInputElement | null;
    const text = document.getElementById('newsText') as HTMLTextAreaElement | null;
    const show = document.getElementById('newsShowOnHomePage') as HTMLInputElement | null;

    if (title) {
        title.value = item.title;
    }
    if (text) {
        text.value = item.text;
    }
    if (show) {
        show.checked = item.showOnHomePage;
    }

    state.editingId = item.newsId;
    setFormMode(state);
}

function readFormPayload(): NewsPayload {
    const title = (document.getElementById('newsTitle') as HTMLInputElement | null)?.value ?? '';
    const text = (document.getElementById('newsText') as HTMLTextAreaElement | null)?.value ?? '';
    const showOnHomePage = (document.getElementById('newsShowOnHomePage') as HTMLInputElement | null)?.checked ?? false;
    return buildNewsPayload(title, text, showOnHomePage);
}

function wireRowButtons(state: NewsPageState): void {
    document.querySelectorAll<HTMLButtonElement>('.edit-news-btn').forEach(button => {
        button.addEventListener('click', () => {
            const id = Number(button.dataset.id);
            const selected = state.items.find(item => item.newsId === id);
            if (!selected) {
                return;
            }
            fillFormFromItem(state, selected);
            clearMessage();
        });
    });

    document.querySelectorAll<HTMLButtonElement>('.delete-news-btn').forEach(button => {
        button.addEventListener('click', async () => {
            const id = Number(button.dataset.id);
            if (Number.isNaN(id)) {
                return;
            }

            if (!confirm(`Neuigkeit ${id} wirklich löschen?`)) {
                return;
            }

            try {
                await removeNews(id);
                await reloadTable(state);
                if (state.editingId === id) {
                    resetForm(state);
                }
                setMessage('Neuigkeit wurde gelöscht.', 'success');
            } catch (error) {
                const text = error instanceof Error ? error.message : String(error);
                setMessage(text, 'error');
            }
        });
    });
}

async function reloadTable(state: NewsPageState): Promise<void> {
    const tableBody = document.getElementById('newsTableBody');
    if (!tableBody) {
        return;
    }

    state.items = await loadNews();
    tableBody.innerHTML = buildNewsRowsMarkup(state.items);
    wireRowButtons(state);
}

function wireForm(state: NewsPageState): void {
    const form = document.getElementById('newsForm') as HTMLFormElement | null;
    const cancel = document.getElementById('newsCancelEditBtn');
    if (!form || !cancel) {
        return;
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        clearMessage();

        const payload = readFormPayload();
        const validationMessage = validateNewsPayload(payload);
        if (validationMessage) {
            setMessage(validationMessage, 'error');
            return;
        }

        try {
            await saveNews(state.editingId, payload);
            await reloadTable(state);
            resetForm(state);
            setMessage('Neuigkeit wurde gespeichert.', 'success');
        } catch (error) {
            const text = error instanceof Error ? error.message : String(error);
            setMessage(text, 'error');
        }
    });

    cancel.addEventListener('click', () => {
        resetForm(state);
        clearMessage();
    });
}

export function initAdminNewsPage(): void {
    const tableBody = document.getElementById('newsTableBody');
    if (!tableBody) {
        return;
    }

    const hintContainer = document.getElementById('newsAuthoringHint');
    if (hintContainer) {
        hintContainer.innerHTML = buildNewsAuthoringHintMarkup();
    }

    const state: NewsPageState = {
        editingId: null,
        items: []
    };

    setFormMode(state);
    wireForm(state);

    void reloadTable(state).catch(error => {
        const text = error instanceof Error ? error.message : String(error);
        setMessage(text, 'error');
    });
}

if (typeof document !== 'undefined') {
    initAdminNewsPage();
}
