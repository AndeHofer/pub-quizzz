import {AverageLeaderboardEntry} from './types';
import {getMedal, renderLeaderboard, loadLeaderboard} from './leaderboard-common';
import {escapeHtml} from './html-utils';

window.addEventListener('load', () => {
    loadLeaderboard<AverageLeaderboardEntry>(
        '/api/leaderboard/average',
        (entries: AverageLeaderboardEntry[]) => {
            renderLeaderboard<AverageLeaderboardEntry>(
                entries,
                'leaderboardBody',
                (e) => `
                    <tr class="border-b border-gray-200 hover:bg-gray-50">
                        <td class="py-3 px-4 font-semibold text-center">${getMedal(e.rank)}</td>
                        <td class="py-3 px-4 font-medium"><a href="/team.html?teamId=${encodeURIComponent(String(e.teamId))}&source=average" class="text-blue-600 hover:underline">${escapeHtml(e.teamName)}</a></td>
                        <td class="py-3 px-4 text-center font-bold text-gray-900">${e.averagePoints.toFixed(2)}</td>
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
