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

    it('builds compact mobile quiz titles with Austrian German month names', async () => {
        const {compactQuizTitle} = await import('./top-results-leaderboard');

        expect(compactQuizTitle('2026 Jänner')).toBe('Jän. 26');
        expect(compactQuizTitle('2026 Mai')).toBe('Mai 26');
        expect(compactQuizTitle('2026 September')).toBe('Sep. 26');
    });

    it('keeps unrecognized quiz titles unchanged', async () => {
        const {compactQuizTitle} = await import('./top-results-leaderboard');

        expect(compactQuizTitle('Spezialquiz')).toBe('Spezialquiz');
        expect(compactQuizTitle('2026 Unbekannt')).toBe('2026 Unbekannt');
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
        };

        expect(buildTopResultsRowMarkup).toBeTypeOf('function');
        const markup = buildTopResultsRowMarkup!(entry);

        expect(markup).toContain('2026 Mai');
        expect(markup).toContain('Mai 26');
        expect(markup).toContain('aria-label="2026 Mai"');
        expect(markup).toContain('title="2026 Mai"');
        expect(markup).not.toContain('2026-05-01');
    });
});
