import {AverageLeaderboardEntry} from './types';
import {getMedal} from './leaderboard-common';
import {initLeaderboardPage} from './leaderboard-page';
import {escapeHtml} from './html-utils';

initLeaderboardPage<AverageLeaderboardEntry>({
    apiUrl: '/api/leaderboard/average',
    rowRenderer: (e) => `
                    <tr class="border-b border-gray-200 hover:bg-gray-50">
                        <td class="py-3 px-4 font-semibold text-center">${getMedal(e.rank)}</td>
                        <td class="py-3 px-4 font-medium"><a href="/team.html?teamId=${encodeURIComponent(String(e.teamId))}&source=average" class="text-blue-600 hover:underline">${escapeHtml(e.teamName)}</a></td>
                        <td class="py-3 px-4 text-center font-bold text-gray-900">${e.averagePoints.toFixed(2)}</td>
                        <td class="py-3 px-4 text-center text-gray-600">${e.quizCount}</td>
                    </tr>
                `,
    fallbackMessage: 'Noch keine Ergebnisse vorhanden.'
});
