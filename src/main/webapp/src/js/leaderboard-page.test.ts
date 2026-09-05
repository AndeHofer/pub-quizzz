import {beforeEach, describe, expect, it, vi} from 'vitest';

const loadLeaderboardMock = vi.hoisted(() => vi.fn());
const renderLeaderboardMock = vi.hoisted(() => vi.fn());

vi.mock('./leaderboard-common', () => ({
    loadLeaderboard: loadLeaderboardMock,
    renderLeaderboard: renderLeaderboardMock,
}));

describe('leaderboard page init helper', () => {
    beforeEach(() => {
        vi.resetModules();
        vi.clearAllMocks();
    });

    it('loads available years, defaults to Alle, and delegates to shared leaderboard functions', async () => {
        const addEventListener = vi.fn();
        const replaceState = vi.fn();
        const tabListeners = new Map<string, EventListener>();
        const elements = createFakeElements(tabListeners);

        vi.stubGlobal('document', {
            getElementById: (id: string) => elements[id] ?? null,
        });
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
            ok: true,
            json: async () => [2026, 2025],
        }));

        const fakeWindow = {
            addEventListener,
            history: {replaceState},
            location: {href: 'https://example.test/points-leaderboard.html', search: ''},
        } as unknown as Window;

        const module = await import('./leaderboard-page');

        module.initLeaderboardPage({
            apiUrl: '/api/leaderboard/points',
            fallbackMessage: 'Noch keine Ergebnisse vorhanden.',
            rowRenderer: () => '<tr></tr>',
            targetWindow: fakeWindow,
        });

        expect(addEventListener).toHaveBeenCalledWith('load', expect.any(Function));

        const onLoad = addEventListener.mock.calls[0][1] as () => Promise<void>;
        await onLoad();

        expect(fetch).toHaveBeenCalledWith('/api/leaderboard/years');
        expect(elements.leaderboardYearTabs.innerHTML).toContain('Alle');
        expect(elements.leaderboardYearTabs.innerHTML).toContain('2026');
        expect(elements.leaderboardYearTabs.innerHTML).toContain('2025');

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

    it('honors a valid year from the URL and fetches the year-specific leaderboard', async () => {
        const addEventListener = vi.fn();
        const tabListeners = new Map<string, EventListener>();
        const elements = createFakeElements(tabListeners);

        vi.stubGlobal('document', {
            getElementById: (id: string) => elements[id] ?? null,
        });
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
            ok: true,
            json: async () => [2026, 2025],
        }));

        const fakeWindow = {
            addEventListener,
            history: {replaceState: vi.fn()},
            location: {
                href: 'https://example.test/points-leaderboard.html?year=2025',
                search: '?year=2025'
            },
        } as unknown as Window;

        const module = await import('./leaderboard-page');

        module.initLeaderboardPage({
            apiUrl: '/api/leaderboard/points',
            fallbackMessage: 'Noch keine Ergebnisse vorhanden.',
            rowRenderer: () => '<tr></tr>',
            targetWindow: fakeWindow,
        });

        const onLoad = addEventListener.mock.calls[0][1] as () => Promise<void>;
        await onLoad();

        expect(loadLeaderboardMock).toHaveBeenCalledWith(
            '/api/leaderboard/points?year=2025',
            expect.any(Function),
            'loading',
            'leaderboardTable',
            'errorMessage'
        );
        expect(elements.leaderboardYearTabs.innerHTML).toContain('data-year="2025"');
        expect(elements.leaderboardYearTabs.innerHTML).toContain('aria-pressed="true"');
    });

    it('updates the URL and reloads when a year tab is clicked', async () => {
        const addEventListener = vi.fn();
        const replaceState = vi.fn();
        const tabListeners = new Map<string, EventListener>();
        const elements = createFakeElements(tabListeners);

        vi.stubGlobal('document', {
            getElementById: (id: string) => elements[id] ?? null,
        });
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
            ok: true,
            json: async () => [2026, 2025],
        }));

        const fakeWindow = {
            addEventListener,
            history: {replaceState},
            location: {href: 'https://example.test/points-leaderboard.html', search: ''},
        } as unknown as Window;

        const module = await import('./leaderboard-page');

        module.initLeaderboardPage({
            apiUrl: '/api/leaderboard/points',
            fallbackMessage: 'Noch keine Ergebnisse vorhanden.',
            rowRenderer: () => '<tr></tr>',
            targetWindow: fakeWindow,
        });

        const onLoad = addEventListener.mock.calls[0][1] as () => Promise<void>;
        await onLoad();

        const clickListener = tabListeners.get('leaderboardYearTabs');
        expect(clickListener).toBeDefined();

        await clickListener!({
            target: {
                closest: () => ({dataset: {year: '2026'}})
            }
        } as unknown as Event);

        expect(replaceState).toHaveBeenCalledWith({}, '', 'https://example.test/points-leaderboard.html?year=2026');
        expect(loadLeaderboardMock).toHaveBeenLastCalledWith(
            '/api/leaderboard/points?year=2026',
            expect.any(Function),
            'loading',
            'leaderboardTable',
            'errorMessage'
        );
    });

    it('does not render year tabs when fewer than two years are available', async () => {
        const addEventListener = vi.fn();
        const tabListeners = new Map<string, EventListener>();
        const elements = createFakeElements(tabListeners);

        vi.stubGlobal('document', {
            getElementById: (id: string) => elements[id] ?? null,
        });
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
            ok: true,
            json: async () => [2026],
        }));

        const fakeWindow = {
            addEventListener,
            history: {replaceState: vi.fn()},
            location: {href: 'https://example.test/points-leaderboard.html', search: ''},
        } as unknown as Window;

        const module = await import('./leaderboard-page');

        module.initLeaderboardPage({
            apiUrl: '/api/leaderboard/points',
            fallbackMessage: 'Noch keine Ergebnisse vorhanden.',
            rowRenderer: () => '<tr></tr>',
            targetWindow: fakeWindow,
        });

        const onLoad = addEventListener.mock.calls[0][1] as () => Promise<void>;
        await onLoad();

        expect(elements.leaderboardYearTabs.innerHTML).toBe('');
        expect(tabListeners.get('leaderboardYearTabs')).toBeUndefined();
        expect(loadLeaderboardMock).toHaveBeenCalledWith(
            '/api/leaderboard/points',
            expect.any(Function),
            'loading',
            'leaderboardTable',
            'errorMessage'
        );
    });
});

function createFakeElements(tabListeners: Map<string, EventListener>): Record<string, {
    innerHTML: string;
    style: { display: string };
    addEventListener?: (type: string, listener: EventListener) => void;
    textContent?: string;
    setAttribute?: (name: string, value: string) => void
}> {
    return {
        leaderboardYearTabs: {
            innerHTML: '',
            style: {display: ''},
            setAttribute: () => {
            },
            addEventListener: (_type: string, listener: EventListener) => {
                tabListeners.set('leaderboardYearTabs', listener);
            },
        },
        leaderboardTable: {innerHTML: '', style: {display: 'none'}},
        leaderboardBody: {innerHTML: '', style: {display: ''}},
        loading: {innerHTML: '', style: {display: 'block'}},
        errorMessage: {innerHTML: '', style: {display: 'none'}, textContent: ''},
    };
}
