import {QuizSummaryDTO} from './types';

function escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function renderQuizzes(quizzes: QuizSummaryDTO[]): void {
    const tbody = document.getElementById('quizzesBody') as HTMLTableSectionElement;

    if (quizzes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center py-8 text-gray-500">Noch keine Quizze vorhanden.</td></tr>';
        return;
    }

    tbody.innerHTML = quizzes.map(q => {
        const winnerCell = q.winnerTeamName
            ? `<a href="/team.html?team=${encodeURIComponent(q.winnerTeamName)}" class="text-blue-700 hover:underline">${escapeHtml(q.winnerTeamName)}</a>`
            : `<span class="text-gray-400">&mdash;</span>`;
        return `
        <tr class="border-b border-gray-200 hover:bg-gray-50">
            <td class="py-2 px-2 sm:py-3 sm:px-4 text-xs sm:text-base font-medium">
                <a href="/quiz.html?id=${q.quizId}" class="text-blue-600 hover:underline">${escapeHtml(q.quizTitle)}</a>
            </td>
            <td class="py-2 px-2 sm:py-3 sm:px-4 text-center text-gray-600 text-xs sm:text-base font-medium">${winnerCell}</td>
            <td class="py-2 px-2 sm:py-3 sm:px-4 text-center text-gray-600 text-xs sm:text-base">${q.teamCount}</td>
        </tr>
    `;
    }).join('');
}

async function loadQuizzes(): Promise<void> {
    const loadingEl = document.getElementById('loading');
    const tableEl = document.getElementById('quizzesTable');
    const errorEl = document.getElementById('errorMessage');

    try {
        const response = await fetch('/api/quizzes');
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const quizzes: QuizSummaryDTO[] = await response.json();
        if (loadingEl) loadingEl.style.display = 'none';
        if (tableEl) tableEl.style.display = 'table';
        renderQuizzes(quizzes);
    } catch {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            errorEl.textContent = 'Fehler beim Laden der Quizze. Bitte Seite neu laden.';
        }
    }
}

window.addEventListener('load', loadQuizzes);
