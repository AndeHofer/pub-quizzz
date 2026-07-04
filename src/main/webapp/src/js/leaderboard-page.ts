import {loadLeaderboard, renderLeaderboard} from './leaderboard-common';

type LeaderboardPageConfig<T> = {
    apiUrl: string;
    rowRenderer: (entry: T) => string;
    fallbackMessage: string;
    targetWindow?: Window;
};

export function initLeaderboardPage<T>(config: LeaderboardPageConfig<T>): void {
    const targetWindow = config.targetWindow ?? window;

    targetWindow.addEventListener('load', () => {
        loadLeaderboard<T>(
            config.apiUrl,
            (entries: T[]) => {
                renderLeaderboard<T>(
                    entries,
                    'leaderboardBody',
                    config.rowRenderer,
                    config.fallbackMessage
                );
            },
            'loading',
            'leaderboardTable',
            'errorMessage'
        );
    });
}
