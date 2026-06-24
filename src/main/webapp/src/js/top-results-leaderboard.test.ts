import {beforeEach, describe, expect, it, vi} from 'vitest';

import type {TopResultLeaderboardEntry} from './types';

describe('top-results leaderboard row markup', () => {
    beforeEach(() => {
        vi.resetModules();
        (globalThis as { window?: { addEventListener: (type: string, listener: () => void) => void } }).window = {
            addEventListener: () => {
                // no-op in test environment
            },
        };
    });

    it('does not render quizDate when quizTitle is shown', async () => {
        const module = await import('./top-results-leaderboard');
        const buildTopResultsRowMarkup = (module as {
            buildTopResultsRowMarkup?: (entry: TopResultLeaderboardEntry) => string
        }).buildTopResultsRowMarkup;

        const entry: TopResultLeaderboardEntry = {
            rank: 1,
            teamId: 7,
            teamName: 'Test Team',
            quizId: 42,
            quizTitle: '2026 Mai',
            totalPoints: 50,
            quizRank: 2,
        };

        expect(buildTopResultsRowMarkup).toBeTypeOf('function');
        const markup = buildTopResultsRowMarkup!(entry);

        expect(markup).toContain('2026 Mai');
        expect(markup).not.toContain('2026-05-01');
    });
});
