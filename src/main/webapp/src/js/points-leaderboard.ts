import {AllTimeLeaderboardEntry} from './types';
import {getMedal, escapeHtml, renderLeaderboard, loadLeaderboard} from './leaderboard-common';

window.addEventListener('load', () => {
    loadLeaderboard<AllTimeLeaderboardEntry>(
        '/api/leaderboard/points',
        (entries: AllTimeLeaderboardEntry[]) => {
            renderLeaderboard<AllTimeLeaderboardEntry>(
                entries,
                'leaderboardBody',
                (e) => `
                    <tr class="border-b border-gray-200 hover:bg-gray-50">
                        <td class="py-3 px-4 font-semibold text-center">${getMedal(e.rank)}</td>
                        <td class="py-3 px-4 font-medium"><a href="/team.html?team=${encodeURIComponent(e.teamName)}&source=points" class="text-blue-600 hover:underline">${escapeHtml(e.teamName)}</a></td>
                        <td class="py-3 px-4 text-center font-bold text-blue-700">${e.totalPoints}</td>
                        <td class="py-3 px-4 text-center text-gray-600">${e.quizCount}</td>
                    </tr>
                `,
                'Noch keine Ergebnisse vorhanden.'
            );
        },
        'loading',
        'leaderboardTable',
        'errorMessage'
    );
});
