import {TopResultLeaderboardEntry} from './types';
import {getMedal} from './leaderboard-common';
import {initLeaderboardPage} from './leaderboard-page';
import {escapeHtml} from './html-utils';

export function buildTopResultsRowMarkup(e: TopResultLeaderboardEntry): string {
    return `
        <tr class="border-b border-gray-200 hover:bg-gray-50">
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 font-semibold text-center">${getMedal(e.rank)}</td>
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 font-medium break-words"><a href="/team.html?teamId=${encodeURIComponent(String(e.teamId))}&source=top-results" class="text-blue-600 hover:underline">${escapeHtml(e.teamName)}</a></td>
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 break-words text-sm sm:text-base"><a href="/quiz.html?id=${encodeURIComponent(String(e.quizId))}" class="text-blue-600 hover:underline font-medium">${escapeHtml(e.quizTitle)}</a></td>
            <td class="py-2 px-1.5 sm:py-3 sm:px-4 text-center font-bold text-gray-900">${e.totalPoints}</td>
        </tr>
    `;
}

initLeaderboardPage<TopResultLeaderboardEntry>({
    apiUrl: '/api/leaderboard/top-results',
    rowRenderer: buildTopResultsRowMarkup,
    fallbackMessage: 'Noch keine Ergebnisse vorhanden.'
});
