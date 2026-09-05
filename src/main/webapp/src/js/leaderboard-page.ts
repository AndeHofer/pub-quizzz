import {loadLeaderboard, renderLeaderboard} from './leaderboard-common';

type LeaderboardPageConfig<T> = {
    apiUrl: string;
    rowRenderer: (entry: T) => string;
    fallbackMessage: string;
    targetWindow?: Window;
};

const YEAR_TABS_ID = 'leaderboardYearTabs';

function readActiveYearFromUrl(targetWindow: Window): number | null {
    const params = new URLSearchParams(targetWindow.location.search);
    const yearValue = params.get('year');
    if (!yearValue) return null;

    const parsed = Number(yearValue);
    return Number.isInteger(parsed) ? parsed : null;
}

function buildLeaderboardUrl(apiUrl: string, year: number | null): string {
    if (year === null) return apiUrl;
    return `${apiUrl}?year=${encodeURIComponent(String(year))}`;
}

async function fetchLeaderboardYears(): Promise<number[]> {
    const response = await fetch('/api/leaderboard/years');
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }

    const years = await response.json() as number[];
    return years.filter(Number.isInteger);
}

function renderYearTabs(targetWindow: Window, years: number[], activeYear: number | null): void {
    const tabsEl = document.getElementById(YEAR_TABS_ID);
    if (!tabsEl) return;

    if (years.length < 2) {
        tabsEl.innerHTML = '';
        return;
    }

    const allTab = `
        <button type="button" class="rounded-full border px-4 py-2 text-sm font-medium whitespace-nowrap transition-colors ${activeYear === null ? 'bg-gray-900 text-white border-gray-900' : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-100'}" data-year="all" aria-pressed="${activeYear === null}">
            Alle
        </button>`;

    const yearTabs = years.map(year => `
        <button type="button" class="rounded-full border px-4 py-2 text-sm font-medium whitespace-nowrap transition-colors ${activeYear === year ? 'bg-gray-900 text-white border-gray-900' : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-100'}" data-year="${year}" aria-pressed="${activeYear === year}">
            ${year}
        </button>`).join('');

    tabsEl.innerHTML = allTab + yearTabs;
    tabsEl.setAttribute('aria-label', 'Jahresfilter');
    tabsEl.setAttribute('role', 'tablist');
}

function updateYearInUrl(targetWindow: Window, year: number | null): void {
    const url = new URL(targetWindow.location.href);
    if (year === null) {
        url.searchParams.delete('year');
    } else {
        url.searchParams.set('year', String(year));
    }
    targetWindow.history.replaceState({}, '', url.toString());
}

export function initLeaderboardPage<T>(config: LeaderboardPageConfig<T>): void {
    const targetWindow = config.targetWindow ?? window;

    targetWindow.addEventListener('load', async () => {
        let availableYears: number[] = [];
        try {
            availableYears = await fetchLeaderboardYears();
        } catch (error) {
            console.error(`Error loading leaderboard years: ${error}`);
        }

        let activeYear = readActiveYearFromUrl(targetWindow);
        if (availableYears.length < 2) {
            activeYear = null;
        }
        if (activeYear !== null && !availableYears.includes(activeYear)) {
            activeYear = null;
        }

        const reloadLeaderboard = (year: number | null): void => {
            loadLeaderboard<T>(
                buildLeaderboardUrl(config.apiUrl, year),
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
        };

        renderYearTabs(targetWindow, availableYears, activeYear);
        if (availableYears.length >= 2) {
            document.getElementById(YEAR_TABS_ID)?.addEventListener('click', event => {
                const target = event.target as HTMLElement | null;
                const tab = target?.closest('[data-year]') as HTMLElement | null;
                if (!tab) return;

                const yearValue = tab.dataset.year;
                const nextYear = yearValue === 'all' ? null : Number(yearValue);
                const normalizedYear = Number.isInteger(nextYear) ? nextYear : null;

                renderYearTabs(targetWindow, availableYears, normalizedYear);
                updateYearInUrl(targetWindow, normalizedYear);
                reloadLeaderboard(normalizedYear);
            });
        }

        reloadLeaderboard(activeYear);
    });
}
