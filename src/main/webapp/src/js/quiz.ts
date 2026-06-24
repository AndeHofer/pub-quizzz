import {QuizResultEntry, QuizResultsResponse} from './types';
import {getMedal} from './leaderboard-common';
import {escapeHtml} from './html-utils';
import {buildToggleButtonHtml, numberBadge, wireDetailToggleButtons} from './results-table-common';

function renderResults(entries: QuizResultEntry[]): void {
    const tbody = document.getElementById('resultsBody') as HTMLTableSectionElement;

    if (entries.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-8 text-gray-500">Noch keine Ergebnisse für dieses Quiz.</td></tr>';
        return;
    }

    const rows: string[] = [];
    entries.forEach((entry, index) => {
        const detailRowId = `detail-${index}`;

        // Summary row
        rows.push(`
            <tr class="border-b border-gray-200 hover:bg-gray-50">
                <td class="py-2 px-2 sm:py-3 sm:px-4 font-semibold text-center text-xs sm:text-base">${getMedal(entry.rank)}</td>
                <td class="py-2 px-2 sm:py-3 sm:px-4 font-medium text-xs sm:text-base">
                    <a href="/team.html?teamId=${encodeURIComponent(String(entry.teamId))}" class="text-blue-600 hover:underline">${escapeHtml(entry.teamName)}</a>
                </td>
                <td class="py-2 px-2 sm:py-3 sm:px-4 text-center font-bold text-gray-900 text-xs sm:text-base">${entry.totalPoints}</td>
                <td class="py-2 px-2 sm:py-3 sm:px-4 text-center">
                    ${buildToggleButtonHtml(detailRowId)}
                </td>
            </tr>
        `);

        // Detail row (hidden by default)
        const sortedAnswers = [...entry.answers].sort((a, b) => a.questionNumber - b.questionNumber);
        const answerCells = sortedAnswers
            .map(a => `<td class="py-2 px-3 text-center text-sm sm:text-base font-medium">${a.points}</td>`)
            .join('');

        rows.push(`
            <tr id="${detailRowId}" style="display:none;" class="bg-gray-50">
                <td colspan="4" class="px-3 pb-4 pt-2">
                    <div class="overflow-x-auto">
                        <table class="w-full border-collapse">
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

async function loadQuizResults(): Promise<void> {
    const loadingEl = document.getElementById('loading');
    const tableEl = document.getElementById('resultsTable');
    const errorEl = document.getElementById('errorMessage');
    const heading = document.getElementById('quizHeading');

    const params = new URLSearchParams(window.location.search);
    const quizId = params.get('id');

    if (!quizId) {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            errorEl.textContent = 'Kein Quiz angegeben.';
        }
        return;
    }

    try {
        const response = await fetch(`/api/quizzes/${encodeURIComponent(quizId)}/results`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const data: QuizResultsResponse = await response.json();
        if (loadingEl) loadingEl.style.display = 'none';
        if (tableEl) tableEl.style.display = 'table';
        if (heading) {
            heading.textContent = `\uD83C\uDFC6 ${data.quizTitle}`;
        }
        renderResults(data.entries);
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
    void loadQuizResults();
});
