// Shared utility functions and modules for leaderboard rendering
import {escapeHtml as escapeHtmlShared} from './html-utils';

export function getMedal(rank: number): string {
    if (rank === 1) return '\uD83E\uDD47'; // Gold
    if (rank === 2) return '\uD83E\uDD48'; // Silver
    if (rank === 3) return '\uD83E\uDD49'; // Bronze
    return String(rank);
}

export function escapeHtml(text: string): string {
    return escapeHtmlShared(text);
}

export function renderLeaderboard<T>(
    entries: T[],
    tbodyId: string,
    customRowGenerator: (entry: T) => string,
    fallbackMessage: string
): void {
    const tbody = document.getElementById(tbodyId) as HTMLTableSectionElement;

    if (!tbody) {
        console.error(`Element with ID "${tbodyId}" not found!`);
        return;
    }

    if (entries.length === 0) {
        tbody.innerHTML = `<tr><td colspan="100%" class="text-center py-8 text-gray-500">${fallbackMessage}</td></tr>`;
        return;
    }

    tbody.innerHTML = entries.map(customRowGenerator).join('');
}

export async function loadLeaderboard<T>(
    apiUrl: string,
    onSuccess: (entries: T[]) => void,
    loadingElId: string,
    tableElId: string,
    errorElId: string
): Promise<void> {
    const loadingEl = document.getElementById(loadingElId);
    const tableEl = document.getElementById(tableElId);
    const errorEl = document.getElementById(errorElId);

    try {
        const response = await fetch(apiUrl);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const entries: T[] = await response.json();
        if (loadingEl) loadingEl.style.display = 'none';
        if (tableEl) tableEl.style.display = 'table';
        onSuccess(entries);
    } catch (error) {
        console.error(`Error loading leaderboard: ${error}`);
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            errorEl.textContent = 'Fehler beim Laden der Rangliste. Bitte Seite neu laden.';
        }
    }
}
