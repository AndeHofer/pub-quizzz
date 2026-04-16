import {QuizResultEntry, QuizResultsResponse} from './types';

function escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getMedal(rank: number): string {
    if (rank === 1) return '\uD83E\uDD47'; // 🥇
    if (rank === 2) return '\uD83E\uDD48'; // 🥈
    if (rank === 3) return '\uD83E\uDD49'; // 🥉
    return String(rank);
}

function numberBadge(n: number): string {
    return `<span class="inline-flex items-center justify-center w-7 h-7 rounded-full bg-blue-600 text-white text-xs font-bold">${n}</span>`;
}

function renderResults(entries: QuizResultEntry[]): void {
    const tbody = document.getElementById('resultsBody') as HTMLTableSectionElement;

    if (entries.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-8 text-gray-500">Noch keine Ergebnisse für dieses Quiz.</td></tr>';
        return;
    }

    const rows: string[] = [];
    entries.forEach((entry, index) => {
        const detailRowId = `detail-${index}`;
        const btnId = `btn-${index}`;

        // Summary row
        rows.push(`
            <tr class="border-b border-gray-200 hover:bg-gray-50">
                <td class="py-2 px-2 sm:py-3 sm:px-4 font-semibold text-center text-xs sm:text-base">${getMedal(entry.rank)}</td>
                <td class="py-2 px-2 sm:py-3 sm:px-4 font-medium text-xs sm:text-base">
                    <a href="/team.html?team=${encodeURIComponent(entry.teamName)}" class="text-blue-600 hover:underline">${escapeHtml(entry.teamName)}</a>
                </td>
                <td class="py-2 px-2 sm:py-3 sm:px-4 text-center font-bold text-gray-900 text-xs sm:text-base">${entry.totalPoints}</td>
                <td class="py-2 px-2 sm:py-3 sm:px-4 text-center">
                    <button id="${btnId}" onclick="toggleDetail('${detailRowId}','${btnId}')"
                        class="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-600 hover:bg-gray-200 whitespace-nowrap">&#9658; anzeigen</button>
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

// Exposed to global scope for inline onclick handlers
(window as unknown as Record<string, unknown>)['toggleDetail'] = function(rowId: string, btnId: string): void {
    const row = document.getElementById(rowId);
    const btn = document.getElementById(btnId);
    if (!row || !btn) return;
    const isHidden = row.style.display === 'none';
    row.style.display = isHidden ? 'table-row' : 'none';
    btn.innerHTML = isHidden ? '&#9660; schlie&szlig;en' : '&#9658; anzeigen';
};

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

window.addEventListener('load', loadQuizResults);
