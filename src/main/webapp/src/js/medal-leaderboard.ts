import {MedalLeaderboardEntry} from './types';
import {getMedal} from './leaderboard-common';
import {initLeaderboardPage} from './leaderboard-page';
import {escapeHtml} from './html-utils';

initLeaderboardPage<MedalLeaderboardEntry>({
    apiUrl: '/api/leaderboard/medals',
    rowRenderer: (e) => `
                    <tr class="border-b border-gray-200 hover:bg-gray-50">
                        <td class="py-3 px-4 font-semibold text-center">${getMedal(e.rank)}</td>
                        <td class="py-3 px-4 font-medium"><a href="/team.html?teamId=${encodeURIComponent(String(e.teamId))}&source=medals" class="text-blue-600 hover:underline">${escapeHtml(e.teamName)}</a></td>
                        <td class="py-3 px-4 text-center font-bold">${e.goldCount}</td>
                        <td class="py-3 px-4 text-center">${e.silverCount}</td>
                        <td class="py-3 px-4 text-center">${e.bronzeCount}</td>
                    </tr>
                `,
    fallbackMessage: 'Noch keine Ergebnisse vorhanden.'
});
