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

    it('loads available years, defaults to the newest year, and delegates to shared leaderboard functions', async () => {
        const addEventListener = vi.fn();
        const replaceState = vi.fn();
        const tabListeners = new Map<string, EventListener>();
        const elements = createFakeElements(tabListeners);
        const newestYear = new Date().getFullYear() - 1;
        const previousYear = newestYear - 1;

        vi.stubGlobal('document', {
            getElementById: (id: string) => elements[id] ?? null,
        });
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
            ok: true,
            json: async () => [newestYear, previousYear],
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
        expect(elements.leaderboardYearTabs.innerHTML).toContain(String(newestYear));
        expect(elements.leaderboardYearTabs.innerHTML).toContain(String(previousYear));
        expect(elements.leaderboardYearTabs.innerHTML.indexOf(String(newestYear)))
            .toBeLessThan(elements.leaderboardYearTabs.innerHTML.indexOf('Alle'));

        expect(loadLeaderboardMock).toHaveBeenCalledWith(
            `/api/leaderboard/points?year=${newestYear}`,
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
        elements.nextLeaderboardLink = {
            href: 'https://example.test/medal-leaderboard.html',
            innerHTML: '',
            style: {display: ''},
        };

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
        expect(elements.nextLeaderboardLink.href).toBe('https://example.test/medal-leaderboard.html?year=2025');
    });

    it('updates the URL and reloads when the Alle tab is clicked', async () => {
        const addEventListener = vi.fn();
        const replaceState = vi.fn();
        const tabListeners = new Map<string, EventListener>();
        const elements = createFakeElements(tabListeners);
        elements.nextLeaderboardLink = {
            href: 'https://example.test/medal-leaderboard.html',
            innerHTML: '',
            style: {display: ''},
        };

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
                closest: () => ({dataset: {year: 'all'}})
            }
        } as unknown as Event);

        expect(replaceState).toHaveBeenCalledWith({}, '', 'https://example.test/points-leaderboard.html?year=all');
        expect(elements.nextLeaderboardLink.href).toBe('https://example.test/medal-leaderboard.html?year=all');
        expect(loadLeaderboardMock).toHaveBeenLastCalledWith(
            '/api/leaderboard/points',
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

    it('switches between a separate empty state, results, and loading errors', async () => {
        const addEventListener = vi.fn();
        const tabListeners = new Map<string, EventListener>();
        const elements = createFakeElements(tabListeners);
        elements.emptyLeaderboard = {innerHTML: '', textContent: '', style: {display: 'none'}};
        vi.stubGlobal('document', {getElementById: (id: string) => elements[id] ?? null});
        vi.stubGlobal('fetch', vi.fn()
            .mockResolvedValueOnce({ok: true, json: async () => [2026, 2025]})
            .mockResolvedValueOnce({ok: true, json: async () => []})
            .mockResolvedValueOnce({ok: true, json: async () => [{rank: 1}]})
            .mockResolvedValueOnce({ok: true, json: async () => []})
            .mockResolvedValueOnce({ok: false, status: 500}));
        const actual = await vi.importActual<typeof import('./leaderboard-common')>('./leaderboard-common');
        loadLeaderboardMock.mockImplementation(actual.loadLeaderboard);
        const fakeWindow = {
            addEventListener,
            history: {replaceState: vi.fn()},
            location: {href: 'https://example.test/average-leaderboard.html', search: ''},
        } as unknown as Window;
        const module = await import('./leaderboard-page');
        module.initLeaderboardPage({
            apiUrl: '/api/leaderboard/average',
            fallbackMessage: 'Keine Teams mit mindestens zwei Teilnahmen in diesem Zeitraum.',
            emptyStateId: 'emptyLeaderboard',
            rowRenderer: () => '<tr></tr>',
            targetWindow: fakeWindow,
        });

        await (addEventListener.mock.calls[0][1] as () => Promise<void>)();
        await vi.waitFor(() => expect(elements.emptyLeaderboard.style.display).toBe('block'));
        expect(elements.emptyLeaderboard.textContent).toBe('Keine Teams mit mindestens zwei Teilnahmen in diesem Zeitraum.');
        expect(elements.leaderboardTable.style.display).toBe('none');
        expect(renderLeaderboardMock).not.toHaveBeenCalled();

        const click = (year: string) => tabListeners.get('leaderboardYearTabs')!({
            target: {closest: () => ({dataset: {year}})},
        } as unknown as Event);
        click('2025');
        await vi.waitFor(() => expect(elements.leaderboardTable.style.display).toBe('table'));
        expect(elements.emptyLeaderboard.style.display).toBe('none');
        expect(renderLeaderboardMock).toHaveBeenCalledOnce();

        click('2026');
        await vi.waitFor(() => expect(elements.emptyLeaderboard.style.display).toBe('block'));
        expect(elements.leaderboardTable.style.display).toBe('none');

        click('all');
        await vi.waitFor(() => expect(elements.errorMessage.style.display).toBe('block'));
        expect(elements.emptyLeaderboard.style.display).toBe('none');
        expect(elements.leaderboardTable.style.display).toBe('none');
        expect(elements.loading.style.display).toBe('none');
    });
});

function createFakeElements(tabListeners: Map<string, EventListener>): Record<string, {
    href?: string;
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
