import {TeamResultEntry} from './types';

function escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function numberBadge(n: number): string {
    return `<span class="inline-flex items-center justify-center w-7 h-7 rounded-full bg-blue-600 text-white text-xs font-bold">${n}</span>`;
}

function renderResults(teamName: string, entries: TeamResultEntry[]): void {
    const tbody = document.getElementById('resultsBody') as HTMLTableSectionElement;
    const heading = document.getElementById('teamHeading') as HTMLHeadingElement;
    heading.textContent = `\uD83C\uDFF5\uFE0F Team: ${escapeHtml(teamName)}`;

    if (entries.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center py-8 text-gray-500">Noch keine Ergebnisse für dieses Team.</td></tr>';
        return;
    }

    const rows: string[] = [];
    entries.forEach((entry, index) => {
        const detailRowId = `detail-${index}`;
        const btnId = `btn-${index}`;

        // Summary row
        rows.push(`
            <tr class="border-b border-gray-200 hover:bg-gray-50">
                <td class="py-2 px-2 sm:py-3 sm:px-4 text-xs sm:text-base"><a href="/quiz.html?id=${entry.quizId}" class="text-blue-600 hover:underline">${escapeHtml(entry.quizTitle)}</a></td>
                <td class="py-2 px-2 sm:py-3 sm:px-4 text-center font-bold text-blue-700 text-xs sm:text-base">${entry.totalPoints}</td>
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
                <td colspan="3" class="px-3 pb-4 pt-2">
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

async function loadTeamResults(): Promise<void> {
    const loadingEl = document.getElementById('loading');
    const tableEl = document.getElementById('resultsTable');
    const errorEl = document.getElementById('errorMessage');

    const params = new URLSearchParams(window.location.search);
    const teamName = params.get('team');

    if (!teamName) {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            errorEl.textContent = 'Kein Team angegeben.';
        }
        return;
    }

    // Set heading immediately so it shows during load
    const heading = document.getElementById('teamHeading');
    if (heading) heading.textContent = `\uD83C\uDFF5\uFE0F Team: ${teamName}`;

    try {
        const response = await fetch(`/api/teams/${encodeURIComponent(teamName)}/results`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const entries: TeamResultEntry[] = await response.json();
        if (loadingEl) loadingEl.style.display = 'none';
        if (tableEl) tableEl.style.display = 'table';
        renderResults(teamName, entries);
    } catch {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            errorEl.textContent = 'Fehler beim Laden der Ergebnisse. Bitte Seite neu laden.';
        }
    }
}

window.addEventListener('load', loadTeamResults);
