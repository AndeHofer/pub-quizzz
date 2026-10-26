import {TopResultLeaderboardEntry} from './types';
import {getMedal} from './leaderboard-common';
import {initLeaderboardPage} from './leaderboard-page';
import {escapeHtml} from './html-utils';

const COMPACT_MONTH_NAMES: Record<string, string> = {
    Jänner: 'Jän.',
    Februar: 'Feb.',
    März: 'März',
    April: 'Apr.',
    Mai: 'Mai',
    Juni: 'Juni',
    Juli: 'Juli',
    August: 'Aug.',
    September: 'Sep.',
    Oktober: 'Okt.',
    November: 'Nov.',
    Dezember: 'Dez.',
};

export function compactQuizTitle(quizTitle: string): string {
    const match = /^(\d{4}) (.+)$/.exec(quizTitle);
    if (!match) {
        return quizTitle;
    }

    const compactMonth = COMPACT_MONTH_NAMES[match[2]];
    return compactMonth ? `${compactMonth} ${match[1].slice(-2)}` : quizTitle;
}

export function buildTopResultsRowMarkup(e: TopResultLeaderboardEntry): string {
    const fullQuizTitle = escapeHtml(e.quizTitle);
    const mobileQuizTitle = escapeHtml(compactQuizTitle(e.quizTitle));

    return `
        <tr class="border-b border-gray-200 hover:bg-gray-50">
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 font-semibold text-center">${getMedal(e.rank)}</td>
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 font-medium break-words"><a href="/team.html?teamId=${encodeURIComponent(String(e.teamId))}&source=top-results" class="text-blue-600 hover:underline">${escapeHtml(e.teamName)}</a></td>
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 break-words text-sm sm:text-base"><a href="/quiz.html?id=${encodeURIComponent(String(e.quizId))}" class="text-blue-600 hover:underline font-medium" aria-label="${fullQuizTitle}" title="${fullQuizTitle}"><span class="sm:hidden block truncate" aria-hidden="true">${mobileQuizTitle}</span><span class="hidden sm:inline" aria-hidden="true">${fullQuizTitle}</span></a></td>
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 text-center font-bold text-gray-900">${e.totalPoints}</td>
        </tr>
    `;
}

initLeaderboardPage<TopResultLeaderboardEntry>({
    apiUrl: '/api/leaderboard/top-results',
    rowRenderer: buildTopResultsRowMarkup,
    fallbackMessage: 'Noch keine Ergebnisse vorhanden.'
});
