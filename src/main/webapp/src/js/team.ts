import {TeamResultEntry} from './types';
import {getMedal} from './leaderboard-common';
import {escapeHtml} from './html-utils';
import {buildToggleButtonHtml, numberBadge, wireDetailToggleButtons} from './results-table-common';

function renderResults(teamName: string, entries: TeamResultEntry[]): void {
    const tbody = document.getElementById('resultsBody') as HTMLTableSectionElement;
    const heading = document.getElementById('teamHeading') as HTMLHeadingElement;
    heading.textContent = `\uD83C\uDFF5\uFE0F Team: ${teamName}`;

    if (entries.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-8 text-gray-500">Noch keine Ergebnisse für dieses Team.</td></tr>';
        return;
    }

    const rows: string[] = [];
    entries.forEach((entry, index) => {
        const detailRowId = `detail-${index}`;
        const medal = entry.quizRank <= 3 ? `${getMedal(entry.quizRank)} ` : '';
        const rankLabel = `${medal}${entry.quizRank}/${entry.participantCount}`;

        // Summary row
        rows.push(`
            <tr class="border-b border-gray-200 hover:bg-gray-50">
                <td class="py-2 px-2 sm:py-3 sm:px-4 text-xs sm:text-base"><a href="/quiz.html?id=${entry.quizId}" class="text-blue-600 hover:underline">${escapeHtml(entry.quizTitle)}</a></td>
                <td class="py-2 px-2 sm:py-3 sm:px-4 text-center font-bold text-gray-900 text-xs sm:text-base">${entry.totalPoints}</td>
                <td class="py-2 px-2 sm:py-3 sm:px-4 text-center font-semibold text-xs sm:text-base">${rankLabel}</td>
                <td class="py-2 px-2 sm:py-3 sm:px-4 text-center">
                    ${buildToggleButtonHtml(detailRowId)}
                </td>
            </tr>
        `);

        // Detail row (hidden by default)
        const sortedAnswers = [...entry.answers].sort((a, b) => a.questionNumber - b.questionNumber);
        const answerHeaders = sortedAnswers
            .map(a => `<th class="py-2 px-3 text-center">${numberBadge(a.questionNumber)}</th>`)
            .join('');
        const answerCells = sortedAnswers
            .map(a => `<td class="py-2 px-3 text-center text-sm sm:text-base font-medium">${a.points}</td>`)
            .join('');

        rows.push(`
            <tr id="${detailRowId}" style="display:none;" class="bg-gray-50">
                <td colspan="4" class="px-3 pb-4 pt-2">
                    <div class="overflow-x-auto">
                        <table class="w-full border-collapse">
                            <thead>
                                <tr class="border-b border-gray-200">${answerHeaders}</tr>
                            </thead>
                            <tbody>
                                <tr>${answerCells}</tr>
                            </tbody>
                        </table>
                    </div>
                </td>
            </tr>
        `);
    });

    tbody.innerHTML = rows.join('');
}

async function loadTeamResults(): Promise<void> {
    const loadingEl = document.getElementById('loading');
    const tableEl = document.getElementById('resultsTable');
    const errorEl = document.getElementById('errorMessage');
    const backLinkEl = document.getElementById('backToLeaderboardLink') as HTMLAnchorElement | null;

    const params = new URLSearchParams(window.location.search);
    const teamIdRaw = params.get('teamId');
    const source = params.get('source');

    if (backLinkEl) {
        if (source === 'medals') {
            backLinkEl.href = './medal-leaderboard.html';
            backLinkEl.textContent = '← Medaillenrangliste';
        } else if (source === 'average') {
            backLinkEl.href = './average-leaderboard.html';
            backLinkEl.textContent = '← Durchschnittsrangliste';
        } else {
            backLinkEl.href = './points-leaderboard.html';
            backLinkEl.textContent = '← Punkterangliste';
        }
    }

    const teamId = teamIdRaw ? Number(teamIdRaw) : NaN;
    if (!teamIdRaw || !Number.isInteger(teamId) || teamId <= 0) {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            errorEl.textContent = 'Kein Team angegeben.';
        }
        return;
    }

    // Set heading immediately so it shows during load
    const heading = document.getElementById('teamHeading');
    if (heading) heading.textContent = '\uD83C\uDFF5\uFE0F Team';

    try {
        const response = await fetch(`/api/teams/${encodeURIComponent(String(teamId))}/results`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const entries: TeamResultEntry[] = await response.json();
        if (loadingEl) loadingEl.style.display = 'none';
        if (tableEl) tableEl.style.display = 'table';
        const resolvedTeamName = entries[0]?.teamName ?? `#${teamId}`;
        renderResults(resolvedTeamName, entries);
    } catch {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            errorEl.textContent = 'Fehler beim Laden der Ergebnisse. Bitte Seite neu laden.';
        }
    }
}

window.addEventListener('load', () => {
    const tbody = document.getElementById('resultsBody') as HTMLTableSectionElement | null;
    if (tbody) {
        wireDetailToggleButtons(tbody);
    }
    void loadTeamResults();
});
