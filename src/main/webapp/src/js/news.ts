import type {NewsDTO} from './types';
import {escapeHtml} from './html-utils';

const MAX_NEWS_ITEMS = 3;

function formatNewsDate(createdAt: string): string {
    const parsed = new Date(createdAt);
    if (Number.isNaN(parsed.getTime())) {
        return '-';
    }
    return new Intl.DateTimeFormat('de-AT', {
        dateStyle: 'medium'
    }).format(parsed);
}

function withLineBreaks(text: string): string {
    return escapeHtml(text).replace(/\n/g, '<br>');
}

function dateValue(value: string): number {
    const parsed = new Date(value).getTime();
    return Number.isNaN(parsed) ? 0 : parsed;
}

export function sortAndLimitNews(items: NewsDTO[]): NewsDTO[] {
    return [...items]
        .sort((a, b) => {
            const byCreatedAt = dateValue(b.createdAt) - dateValue(a.createdAt);
            if (byCreatedAt !== 0) {
                return byCreatedAt;
            }
            return b.newsId - a.newsId;
        })
        .slice(0, MAX_NEWS_ITEMS);
}

export function buildNewsSectionMarkup(items: NewsDTO[]): string {
    const visibleItems = sortAndLimitNews(items);
    if (visibleItems.length === 0) {
        return '<p class="text-sm text-gray-500">Derzeit gibt es keine Neuigkeiten.</p>';
    }

    return visibleItems.map(item => `
        <article class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 sm:p-5">
            <div class="flex items-center justify-between gap-3 mb-2">
                <h3 class="font-semibold text-gray-800 text-base sm:text-lg">${escapeHtml(item.title)}</h3>
                <span class="text-xs text-gray-500">${escapeHtml(formatNewsDate(item.createdAt))}</span>
            </div>
            <p class="text-sm text-gray-700 leading-relaxed">${withLineBreaks(item.text)}</p>
        </article>
    `).join('');
}

export function buildNewsErrorMarkup(): string {
    return '<p class="text-sm text-gray-500">Neuigkeiten konnten nicht geladen werden.</p>';
}

export function renderNewsSection(container: HTMLElement, items: NewsDTO[]): void {
    container.innerHTML = buildNewsSectionMarkup(items);
}

export function renderNewsError(container: HTMLElement): void {
    container.innerHTML = buildNewsErrorMarkup();
}
