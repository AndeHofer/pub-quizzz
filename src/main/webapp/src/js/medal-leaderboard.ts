import {MedalLeaderboardEntry} from './types';
import {getMedal, escapeHtml, renderLeaderboard, loadLeaderboard} from './leaderboard-common';

window.addEventListener('load', () => {
    loadLeaderboard<MedalLeaderboardEntry>(
        '/api/leaderboard/medals',
        (entries: MedalLeaderboardEntry[]) => {
            renderLeaderboard<MedalLeaderboardEntry>(
                entries,
                'leaderboardBody',
                (e) => `
                    <tr class="border-b border-gray-200 hover:bg-gray-50">
                        <td class="py-3 px-4 font-semibold text-center">${getMedal(e.rank)}</td>
                        <td class="py-3 px-4 font-medium"><a href="/team.html?team=${encodeURIComponent(e.teamName)}&source=medals" class="text-blue-600 hover:underline">${escapeHtml(e.teamName)}</a></td>
                        <td class="py-3 px-4 text-center font-bold">${e.goldCount}</td>
                        <td class="py-3 px-4 text-center">${e.silverCount}</td>
                        <td class="py-3 px-4 text-center">${e.bronzeCount}</td>
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
