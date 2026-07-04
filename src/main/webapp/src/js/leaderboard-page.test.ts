import {beforeEach, describe, expect, it, vi} from 'vitest';

const loadLeaderboardMock = vi.hoisted(() => vi.fn());
const renderLeaderboardMock = vi.hoisted(() => vi.fn());

vi.mock('./leaderboard-common', () => ({
    loadLeaderboard: loadLeaderboardMock,
    renderLeaderboard: renderLeaderboardMock,
}));

describe('leaderboard page init helper', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('registers load handler and delegates to shared leaderboard functions', async () => {
        const addEventListener = vi.fn();
        const fakeWindow = {
            addEventListener,
        } as unknown as Window;

        const module = await import('./leaderboard-page');

        module.initLeaderboardPage({
            apiUrl: '/api/leaderboard/points',
            fallbackMessage: 'Noch keine Ergebnisse vorhanden.',
            rowRenderer: () => '<tr></tr>',
            targetWindow: fakeWindow,
        });

        expect(addEventListener).toHaveBeenCalledWith('load', expect.any(Function));

        const onLoad = addEventListener.mock.calls[0][1] as () => void;
        onLoad();

        expect(loadLeaderboardMock).toHaveBeenCalledWith(
            '/api/leaderboard/points',
            expect.any(Function),
            'loading',
            'leaderboardTable',
            'errorMessage'
        );

        const onSuccess = loadLeaderboardMock.mock.calls[0][1] as (entries: unknown[]) => void;
        const entries = [{rank: 1}];
        onSuccess(entries);

        expect(renderLeaderboardMock).toHaveBeenCalledWith(
            entries,
            'leaderboardBody',
            expect.any(Function),
            'Noch keine Ergebnisse vorhanden.'
        );
    });
});
