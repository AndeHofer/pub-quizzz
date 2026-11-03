import {describe, expect, it} from 'vitest';
import {renderTeamStatisticsMarkup} from './team-statistics';
import type {TeamLeaderboardStatistics} from './types';

const emptyPeriod = (year: number | null): TeamLeaderboardStatistics => ({
    year, points: null, average: null, medals: null, topResults: [],
});

describe('team statistics rendering', () => {
    it('renders each period in supplied order and links to the corresponding rankings', () => {
        const html = renderTeamStatisticsMarkup([emptyPeriod(2026), emptyPeriod(2025), emptyPeriod(null)]);
        expect(html.indexOf('>2026</h3>')).toBeLessThan(html.indexOf('>2025</h3>'));
        expect(html.indexOf('>2025</h3>')).toBeLessThan(html.indexOf('>Alle</h3>'));
        expect(html).toContain('/points-leaderboard.html?year=2026');
        expect(html).not.toContain('Durchschnittsrangliste');
        expect(html).not.toContain('Medaillenrangliste');
        expect(html).not.toContain('Top 10 Ergebnisse');
        expect(html).not.toContain('Keine Medaillenplatzierung');
        expect(html).not.toContain('Keine Top-10-Platzierung');
    });

    it('renders the given ranks and every qualifying result with escaped quiz names', () => {
        const html = renderTeamStatisticsMarkup([{
            year: 2026,
            points: {rank: 2, teamId: 7, teamName: 'Team', totalPoints: 55, quizCount: 2},
            average: {rank: 1, teamId: 7, teamName: 'Team', averagePoints: 27.5, quizCount: 2},
            medals: {rank: 3, teamId: 7, teamName: 'Team', goldCount: 1, silverCount: 0, bronzeCount: 2},
            topResults: [
                {rank: 1, teamId: 7, teamName: 'Team', quizId: 1, quizTitle: '<Quiz>', totalPoints: 30},
                {rank: 2, teamId: 7, teamName: 'Team', quizId: 2, quizTitle: 'Quiz 2', totalPoints: 25},
            ],
        }]);
        expect(html).toContain('Platz 2 · 55 Punkte');
        expect(html).toContain('Platz 1 · 27.50 Punkte');
        expect(html).toContain('/average-leaderboard.html?year=2026');
        expect(html).toContain('Platz 3 · 🥇 1 🥈 0 🥉 2');
        expect(html).toContain('/medal-leaderboard.html?year=2026');
        expect(html).toContain('/top-results-leaderboard.html?year=2026');
        expect(html).toContain('&lt;Quiz&gt;');
        expect(html).toContain('/quiz.html?id=1');
        expect(html).toContain('/quiz.html?id=2');
        expect(html).not.toContain('Keine Top-10-Platzierung');
    });

    it('shows the average only in periods with a qualifying placement', () => {
        const html = renderTeamStatisticsMarkup([
            emptyPeriod(2026),
            emptyPeriod(2025),
            {
                ...emptyPeriod(null),
                average: {rank: 1, teamId: 7, teamName: 'Team', averagePoints: 27.5, quizCount: 2},
            },
        ]);

        expect(html).not.toContain('/average-leaderboard.html?year=2026');
        expect(html).not.toContain('/average-leaderboard.html?year=2025');
        expect(html).toContain('/average-leaderboard.html?year=all');
        expect(html).toContain('Platz 1 · 27.50 Punkte');
        expect(html).toContain('/points-leaderboard.html?year=2026');
        expect(html).toContain('/points-leaderboard.html?year=2025');
    });

    it('determines medal and Top 10 visibility separately for each period', () => {
        const html = renderTeamStatisticsMarkup([
            {
                ...emptyPeriod(2026),
                medals: {rank: 1, teamId: 7, teamName: 'Team', goldCount: 0, silverCount: 1, bronzeCount: 0}
            },
            {
                ...emptyPeriod(2025),
                topResults: [{rank: 4, teamId: 7, teamName: 'Team', quizId: 2, quizTitle: 'Quiz', totalPoints: 25}]
            },
            {
                ...emptyPeriod(null),
                medals: {rank: 5, teamId: 7, teamName: 'Team', goldCount: 0, silverCount: 0, bronzeCount: 0}
            },
        ]);
        expect(html).toContain('/medal-leaderboard.html?year=2026');
        expect(html).not.toContain('/top-results-leaderboard.html?year=2026');
        expect(html).not.toContain('/medal-leaderboard.html?year=2025');
        expect(html).toContain('/top-results-leaderboard.html?year=2025');
        expect(html).not.toContain('/medal-leaderboard.html?year=all');
        expect(html).not.toContain('/top-results-leaderboard.html?year=all');
    });
});
