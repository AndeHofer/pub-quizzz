import type {AdminMonthlyLoginStatDTO} from './types';
import {goBack} from './utils';
import {getApiFetch} from './admin-api-loader';

const doc = typeof document === 'undefined' ? null : document;
const loadingEl = doc?.getElementById('loading') ?? null;
const errorEl = doc?.getElementById('errorMessage') ?? null;
const tableEl = (doc?.getElementById('loginStatsTable') as HTMLTableElement | null) ?? null;
const tbodyEl = (doc?.getElementById('loginStatsBody') as HTMLTableSectionElement | null) ?? null;

export function roleLabel(role: string): string {
    if (role === 'ADMIN') return 'Admin';
    if (role === 'USER') return 'Benutzer';
    return role;
}

export function buildLoginStatsRows(stats: AdminMonthlyLoginStatDTO[]): string {
    if (stats.length === 0) {
        return '<tr><td colspan="3" class="text-center py-8 text-gray-500">Keine Login-Daten gefunden.</td></tr>';
    }

    return stats.map(stat => `
        <tr class="border-b border-gray-100">
            <td class="px-4 py-3">${stat.month}</td>
            <td class="px-4 py-3">${roleLabel(stat.role)}</td>
            <td class="px-4 py-3 text-right font-semibold">${stat.loginCount}</td>
        </tr>
    `).join('');
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

function setLoading(loading: boolean): void {
    if (loadingEl) loadingEl.style.display = loading ? 'block' : 'none';
    if (tableEl) tableEl.style.display = loading ? 'none' : 'table';
}

async function fetchLoginStats(): Promise<AdminMonthlyLoginStatDTO[]> {
    const apiFetch = await getApiFetch();
    const response = await apiFetch('/admin/login-stats/monthly');
    if (!response.ok) {
        const text = await response.text().catch(() => '');
        throw new Error(text || `HTTP ${response.status}`);
    }
    return response.json() as Promise<AdminMonthlyLoginStatDTO[]>;
}

async function loadLoginStats(): Promise<void> {
    if (!tbodyEl) return;

    setLoading(true);
    clearError();

    try {
        const stats = await fetchLoginStats();
        tbodyEl.innerHTML = buildLoginStatsRows(stats);
    } catch (error) {
        if (error instanceof Error && error.message === 'AUTH_EXPIRED_REDIRECT') {
            return;
        }
        setError('Fehler beim Laden der Login-Statistik. Bitte Seite neu laden.');
        tbodyEl.innerHTML = '';
        console.error(error);
    } finally {
        setLoading(false);
    }
}

if (typeof window !== 'undefined') {
    window.addEventListener('load', () => {
        document.getElementById('backBtn')?.addEventListener('click', () => goBack('admin_main.html'));
        void loadLoginStats();
    });
}
