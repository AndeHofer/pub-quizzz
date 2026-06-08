import type {AdminLogEntryDTO, AdminLogResponseDTO} from './types';
import {goBack, showMessage} from './utils';
import {escapeHtml} from './html-utils';
import {getApiFetch} from './admin-api-loader';

const doc = typeof document === 'undefined' ? null : document;

const filterTextEl = (doc?.getElementById('logFilterText') as HTMLInputElement | null) ?? null;
const filterLevelEl = (doc?.getElementById('logFilterLevel') as HTMLSelectElement | null) ?? null;
const filterFromEl = (doc?.getElementById('logFilterFrom') as HTMLInputElement | null) ?? null;
const filterToEl = (doc?.getElementById('logFilterTo') as HTMLInputElement | null) ?? null;
const filterLimitEl = (doc?.getElementById('logFilterLimit') as HTMLSelectElement | null) ?? null;
const loadingEl = doc?.getElementById('loading') ?? null;
const errorEl = doc?.getElementById('errorMessage') ?? null;
const logMetaEl = doc?.getElementById('logMeta') ?? null;
const streamEl = doc?.getElementById('logStream') ?? null;

const DEFAULT_LIMIT = 200;

export function levelBadgeClass(level: string): string {
    if (level === 'ERROR') return 'bg-red-100 text-red-700 border border-red-200';
    if (level === 'WARN') return 'bg-amber-100 text-amber-700 border border-amber-200';
    if (level === 'INFO') return 'bg-blue-100 text-blue-700 border border-blue-200';
    if (level === 'DEBUG') return 'bg-gray-100 text-gray-700 border border-gray-200';
    return 'bg-slate-100 text-slate-700 border border-slate-200';
}

export function buildLogStream(entries: AdminLogEntryDTO[]): string {
    if (entries.length === 0) {
        return '<div class="bg-white rounded-lg border border-gray-200 px-4 py-6 text-gray-500 text-center">Keine Log-Eintraege gefunden.</div>';
    }

    return entries.map(entry => {
        const timestamp = escapeHtml(entry.timestamp ?? '-');
        const level = escapeHtml(entry.level || 'UNKNOWN');
        const source = escapeHtml(entry.source ?? '-');
        const message = escapeHtml(entry.message ?? entry.rawLine);
        const rawLine = escapeHtml(entry.rawLine);

        return `
            <article class="bg-white rounded-lg border border-gray-200 px-4 py-3 shadow-sm">
                <div class="flex flex-wrap items-center gap-2 text-sm mb-2">
                    <span class="text-gray-600 font-medium">${timestamp}</span>
                    <span class="inline-flex px-2 py-0.5 rounded text-xs font-semibold ${levelBadgeClass(entry.level)}">${level}</span>
                    <span class="text-gray-500">${source}</span>
                </div>
                <pre class="text-sm whitespace-pre-wrap break-words font-mono text-gray-800 mb-2">${message}</pre>
                <details>
                    <summary class="text-xs text-gray-500 cursor-pointer">Raw anzeigen</summary>
                    <pre class="text-xs whitespace-pre-wrap break-words font-mono text-gray-600 mt-2">${rawLine}</pre>
                </details>
            </article>
        `;
    }).join('');
}

function setLoading(loading: boolean): void {
    if (loadingEl) loadingEl.style.display = loading ? 'block' : 'none';
    if (streamEl) streamEl.style.display = loading ? 'none' : 'block';
    if (logMetaEl) logMetaEl.style.display = loading ? 'none' : 'block';
}

function setError(message: string): void {
    if (!errorEl) return;
    errorEl.textContent = message;
    errorEl.style.display = 'block';
}

function clearError(): void {
    if (!errorEl) return;
    errorEl.textContent = '';
    errorEl.style.display = 'none';
}

export function queryParamsFromFilters(filters: {
    q: string;
    level: string;
    from: string;
    to: string;
    limit: number;
}): URLSearchParams {
    const params = new URLSearchParams();
    if (filters.q.trim()) params.set('q', filters.q.trim());
    if (filters.level.trim()) params.set('level', filters.level.trim());
    if (filters.from.trim()) params.set('from', filters.from.trim());
    if (filters.to.trim()) params.set('to', filters.to.trim());
    params.set('limit', String(filters.limit));
    return params;
}

function currentFilters() {
    return {
        q: filterTextEl?.value ?? '',
        level: filterLevelEl?.value ?? '',
        from: filterFromEl?.value ?? '',
        to: filterToEl?.value ?? '',
        limit: Number(filterLimitEl?.value ?? DEFAULT_LIMIT)
    };
}

function applyFiltersToUrl(): URLSearchParams {
    const params = queryParamsFromFilters(currentFilters());
    const url = new URL(window.location.href);
    url.search = params.toString();
    window.history.replaceState({}, '', url.toString());
    return params;
}

function hydrateFiltersFromUrl(): void {
    const params = new URLSearchParams(window.location.search);
    if (filterTextEl) filterTextEl.value = params.get('q') ?? '';
    if (filterLevelEl) filterLevelEl.value = params.get('level') ?? '';
    if (filterFromEl) filterFromEl.value = params.get('from') ?? '';
    if (filterToEl) filterToEl.value = params.get('to') ?? '';

    const limitParam = Number(params.get('limit') ?? DEFAULT_LIMIT);
    if (filterLimitEl) {
        if ([100, 200, 500, 1000].includes(limitParam)) {
            filterLimitEl.value = String(limitParam);
        } else {
            filterLimitEl.value = String(DEFAULT_LIMIT);
        }
    }
}

async function fetchLogs(params: URLSearchParams): Promise<AdminLogResponseDTO> {
    const apiFetch = await getApiFetch();
    const response = await apiFetch(`/admin/logs?${params.toString()}`);
    if (!response.ok) {
        const errorBody = await response.json().catch(() => ({error: ''}));
        const message = typeof errorBody.error === 'string' && errorBody.error.trim()
            ? errorBody.error
            : `HTTP ${response.status}`;
        throw new Error(message);
    }
    return response.json() as Promise<AdminLogResponseDTO>;
}

async function loadLogs(): Promise<void> {
    if (!streamEl) return;

    clearError();
    setLoading(true);

    try {
        const params = applyFiltersToUrl();
        const response = await fetchLogs(params);
        streamEl.innerHTML = buildLogStream(response.entries);
        if (logMetaEl) {
            logMetaEl.textContent = `${response.returnedCount} Eintraege angezeigt (Limit: ${response.appliedLimit})`;
        }
    } catch (error) {
        if (error instanceof Error && error.message === 'AUTH_EXPIRED_REDIRECT') {
            return;
        }
        const message = error instanceof Error ? error.message : 'Unbekannter Fehler';
        setError(`Fehler beim Laden der Log-Eintraege: ${message}`);
        streamEl.innerHTML = '';
        if (logMetaEl) {
            logMetaEl.textContent = '';
        }
        showMessage('Log-Eintraege konnten nicht geladen werden.', 'error');
        console.error(error);
    } finally {
        setLoading(false);
    }
}

if (typeof window !== 'undefined') {
    window.addEventListener('load', () => {
        document.getElementById('backBtn')?.addEventListener('click', () => goBack('admin_main.html'));
        document.getElementById('applyFiltersBtn')?.addEventListener('click', () => {
            void loadLogs();
        });
        document.getElementById('resetFiltersBtn')?.addEventListener('click', () => {
            if (filterTextEl) filterTextEl.value = '';
            if (filterLevelEl) filterLevelEl.value = '';
            if (filterFromEl) filterFromEl.value = '';
            if (filterToEl) filterToEl.value = '';
            if (filterLimitEl) filterLimitEl.value = String(DEFAULT_LIMIT);
            void loadLogs();
        });

        hydrateFiltersFromUrl();
        void loadLogs();
    });
}
