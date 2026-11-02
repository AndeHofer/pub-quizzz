import type {TeamLeaderboardStatistics} from './types';
import {escapeHtml} from './html-utils';

function leaderboardLink(page: string, year: number | null): string {
    return `/${page}-leaderboard.html?year=${year === null ? 'all' : encodeURIComponent(String(year))}`;
}

export function renderTeamStatisticsMarkup(statistics: TeamLeaderboardStatistics[]): string {
    return statistics.map(period => {
        const label = period.year === null ? 'Alle' : String(period.year);
        const points = period.points
            ? `Platz ${period.points.rank} · ${period.points.totalPoints} Punkte`
            : 'Keine Platzierung';
        const average = period.average
            ? `Platz ${period.average.rank} · ${period.average.averagePoints.toFixed(2)} Punkte`
            : 'Keine Platzierung';
        const medals = period.medals;
        const hasMedals = medals !== null && (medals.goldCount + medals.silverCount + medals.bronzeCount) > 0;
        const topResults = period.topResults.length > 0
            ? `<div class="mt-4">
                <h4 class="font-medium mb-2"><a href="${leaderboardLink('top-results', period.year)}" class="text-blue-600 hover:underline">Top 10 Ergebnisse</a></h4>
                <ul class="space-y-2">${period.topResults.map(result => `
                <li class="flex flex-wrap items-baseline gap-x-2 text-sm text-gray-900">
                    <span class="font-semibold">Platz ${result.rank}</span>
                    <a href="/quiz.html?id=${encodeURIComponent(String(result.quizId))}" class="text-blue-600 hover:underline break-words">${escapeHtml(result.quizTitle)}</a>
                    <span>${result.totalPoints} Punkte</span>
                </li>`).join('')}</ul>
            </div>`
            : '';

        return `
            <section class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-5" aria-label="Ranglisten ${label}">
                <h3 class="text-lg font-semibold text-gray-900 mb-3">${label}</h3>
                <dl class="grid grid-cols-1 ${hasMedals ? 'sm:grid-cols-3' : 'sm:grid-cols-2'} gap-3">
                    <div class="min-w-0 break-words rounded-md bg-gray-50 p-3">
                        <dt><a href="${leaderboardLink('points', period.year)}" class="font-medium text-blue-600 hover:underline">Punkterangliste</a></dt>
                        <dd class="mt-1 text-sm text-gray-900">${points}</dd>
                    </div>
                    <div class="min-w-0 break-words rounded-md bg-gray-50 p-3">
                        <dt><a href="${leaderboardLink('average', period.year)}" class="font-medium text-blue-600 hover:underline">Durchschnittsrangliste</a></dt>
                        <dd class="mt-1 text-sm text-gray-900">${average}</dd>
                    </div>
                    ${hasMedals && medals ? `<div class="min-w-0 break-words rounded-md bg-gray-50 p-3">
                        <dt><a href="${leaderboardLink('medal', period.year)}" class="font-medium text-blue-600 hover:underline">Medaillenrangliste</a></dt>
                        <dd class="mt-1 text-sm text-gray-900">Platz ${medals.rank} · 🥇 ${medals.goldCount} 🥈 ${medals.silverCount} 🥉 ${medals.bronzeCount}</dd>
                    </div>` : ''}
                </dl>
                ${topResults}
            </section>`;
    }).join('');
}
