import {QuizSummaryDTO} from './types';
import {compactQuizTitle, sortFinishedQuizzesNewestFirst} from './quiz-utils';
import {escapeHtml} from './html-utils';

function renderQuizzes(quizzes: QuizSummaryDTO[]): void {
    const tbody = document.getElementById('quizzesBody') as HTMLTableSectionElement;

    if (quizzes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center py-8 text-gray-500">Keine fertigen Quizze verfügbar.</td></tr>';
        return;
    }

    tbody.innerHTML = quizzes.map(q => {
        const fullQuizTitle = escapeHtml(q.quizTitle);
        const mobileQuizTitle = escapeHtml(compactQuizTitle(q.quizTitle));
        const winnerCell = q.winnerTeamName && q.winnerTeamId != null
            ? `<a href="/team.html?teamId=${encodeURIComponent(String(q.winnerTeamId))}" class="text-blue-700 hover:underline">${escapeHtml(q.winnerTeamName)}</a>`
            : `<span class="text-gray-400">&mdash;</span>`;
        return `
        <tr class="border-b border-gray-200 hover:bg-gray-50">
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 break-words font-medium text-sm sm:text-base">
                <a href="/quiz.html?id=${q.quizId}" class="text-blue-600 hover:underline" aria-label="${fullQuizTitle}" title="${fullQuizTitle}"><span class="sm:hidden block truncate" aria-hidden="true">${mobileQuizTitle}</span><span class="hidden sm:inline" aria-hidden="true">${fullQuizTitle}</span></a>
            </td>
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 break-words text-left text-gray-600 font-medium">${winnerCell}</td>
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 text-center text-gray-600 text-xs sm:text-base">${q.teamCount}</td>
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
        const finishedQuizzes = sortFinishedQuizzesNewestFirst(quizzes);
        if (loadingEl) loadingEl.style.display = 'none';
        if (tableEl) tableEl.style.display = 'table';
        renderQuizzes(finishedQuizzes);
    } catch {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            errorEl.textContent = 'Fehler beim Laden der Quizze. Bitte Seite neu laden.';
        }
    }
}

window.addEventListener('load', loadQuizzes);
