import type {TeamDTO} from './types';
import {escapeHtml} from './html-utils';

const teamNameCollator = new Intl.Collator('de-AT', {sensitivity: 'base'});

export function sortTeamsAlphabetically(teams: TeamDTO[]): TeamDTO[] {
    return [...teams].sort((a, b) => teamNameCollator.compare(a.teamName, b.teamName)
        || a.teamName.localeCompare(b.teamName, 'de-AT') || a.teamsId - b.teamsId);
}

export function renderTeamsMarkup(teams: TeamDTO[]): string {
    return sortTeamsAlphabetically(teams).map(team => `
        <li class="border-b border-gray-200 last:border-b-0">
            <a href="/team.html?teamId=${encodeURIComponent(String(team.teamsId))}"
               class="block px-4 py-3 sm:px-5 sm:py-4 text-blue-600 hover:bg-gray-50 hover:underline font-medium break-words">${escapeHtml(team.teamName)}</a>
        </li>`).join('');
}

export async function loadTeams(doc: Document = document, request: typeof fetch = fetch): Promise<void> {
    const loading = doc.getElementById('loading');
    const error = doc.getElementById('errorMessage');
    const empty = doc.getElementById('emptyState');
    const list = doc.getElementById('teamsList');
    if (!loading || !error || !empty || !list) return;

    try {
        const response = await request('/api/teams');
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const teams: TeamDTO[] = await response.json();
        if (teams.length === 0) {
            empty.style.display = 'block';
        } else {
            list.innerHTML = renderTeamsMarkup(teams);
            list.style.display = 'block';
        }
    } catch {
        error.textContent = 'Fehler beim Laden der Teams. Bitte Seite neu laden.';
        error.style.display = 'block';
    } finally {
        loading.style.display = 'none';
    }
}

if (typeof window !== 'undefined') {
    window.addEventListener('load', () => void loadTeams());
}
