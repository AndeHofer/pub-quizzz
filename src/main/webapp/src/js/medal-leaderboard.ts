import {MedalLeaderboardEntry} from './types';

function getMedal(rank: number): string {
    if (rank === 1) return '\uD83E\uDD47';
    if (rank === 2) return '\uD83E\uDD48';
    if (rank === 3) return '\uD83E\uDD49';
    return String(rank);
}

function escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function renderLeaderboard(entries: MedalLeaderboardEntry[]): void {
    const tbody = document.getElementById('leaderboardBody') as HTMLTableSectionElement;

    if (entries.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center py-8 text-gray-500">Noch keine Ergebnisse vorhanden.</td></tr>';
        return;
    }

    tbody.innerHTML = entries.map(e => `
        <tr class="border-b border-gray-200 hover:bg-gray-50">
            <td class="py-3 px-4 font-semibold text-center">${getMedal(e.rank)}</td>
            <td class="py-3 px-4 font-medium"><a href="/team.html?team=${encodeURIComponent(e.teamName)}&source=medals" class="text-blue-600 hover:underline">${escapeHtml(e.teamName)}</a></td>
            <td class="py-3 px-4 text-center font-bold">${e.goldCount}</td>
            <td class="py-3 px-4 text-center">${e.silverCount}</td>
            <td class="py-3 px-4 text-center">${e.bronzeCount}</td>
        </tr>
    `).join('');
}

async function loadLeaderboard(): Promise<void> {
    const loadingEl = document.getElementById('loading');
    const tableEl = document.getElementById('leaderboardTable');
    const errorEl = document.getElementById('errorMessage');

    try {
        const response = await fetch('/api/leaderboard/medals');
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const entries: MedalLeaderboardEntry[] = await response.json();
        if (loadingEl) loadingEl.style.display = 'none';
        if (tableEl) tableEl.style.display = 'table';
        renderLeaderboard(entries);
    } catch {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            errorEl.textContent = 'Fehler beim Laden der Rangliste. Bitte Seite neu laden.';
        }
    }
}

window.addEventListener('load', loadLeaderboard);
