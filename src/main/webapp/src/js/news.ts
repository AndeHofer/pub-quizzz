import type {NewsDTO} from './types';
import {escapeHtml} from './html-utils';

const MAX_NEWS_ITEMS = 3;
const EVENT_META_PATTERN = /<!--\s*event\s+([\s\S]*?)\s*-->/i;
const EUROPE_VIENNA_TZ = 'Europe/Vienna';

interface RawEventMeta {
    title?: unknown;
    start?: unknown;
    end?: unknown;
    location?: unknown;
    text?: unknown;
}

interface ParsedEventMeta {
    title: string;
    start: string;
    end: string;
    location: string;
    text?: string;
}

interface EventRoot {
    events?: Record<string, RawEventMeta>;
}

type RenderEventInput = ParsedEventMeta;

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

function sanitizeIcsText(value: string): string {
    return value
        .replace(/\\/g, '\\\\')
        .replace(/;/g, '\\;')
        .replace(/,/g, '\\,')
        .replace(/\r?\n/g, '\\n');
}

function hasTimezoneOffset(value: string): boolean {
    return /(?:Z|[+-]\d{2}:\d{2})$/i.test(value);
}

function parseFloatingDateTime(value: string): {
    year: number;
    month: number;
    day: number;
    hour: number;
    minute: number;
    second: number
} | null {
    const match = value.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?$/);
    if (!match) {
        return null;
    }
    return {
        year: Number(match[1]),
        month: Number(match[2]),
        day: Number(match[3]),
        hour: Number(match[4]),
        minute: Number(match[5]),
        second: match[6] ? Number(match[6]) : 0
    };
}

function toCompactDateTime(value: string): string | null {
    if (hasTimezoneOffset(value)) {
        const parsed = new Date(value);
        if (Number.isNaN(parsed.getTime())) {
            return null;
        }
        const yyyy = parsed.getUTCFullYear();
        const mm = String(parsed.getUTCMonth() + 1).padStart(2, '0');
        const dd = String(parsed.getUTCDate()).padStart(2, '0');
        const hh = String(parsed.getUTCHours()).padStart(2, '0');
        const min = String(parsed.getUTCMinutes()).padStart(2, '0');
        const ss = String(parsed.getUTCSeconds()).padStart(2, '0');
        return `${yyyy}${mm}${dd}T${hh}${min}${ss}`;
    }

    const floating = parseFloatingDateTime(value);
    if (!floating) {
        return null;
    }

    return `${String(floating.year).padStart(4, '0')}${String(floating.month).padStart(2, '0')}${String(floating.day).padStart(2, '0')}T${String(floating.hour).padStart(2, '0')}${String(floating.minute).padStart(2, '0')}${String(floating.second).padStart(2, '0')}`;
}

function toOrderingValue(value: string): number {
    if (hasTimezoneOffset(value)) {
        const parsed = new Date(value).getTime();
        return Number.isNaN(parsed) ? Number.NaN : parsed;
    }

    const floating = parseFloatingDateTime(value);
    if (!floating) {
        return Number.NaN;
    }

    return Date.UTC(
        floating.year,
        floating.month - 1,
        floating.day,
        floating.hour,
        floating.minute,
        floating.second
    );
}

function asRequiredString(value: unknown): string | null {
    if (typeof value !== 'string') {
        return null;
    }
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : null;
}

function asOptionalString(value: unknown): string | undefined {
    if (typeof value !== 'string') {
        return undefined;
    }
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : undefined;
}

function validateEventMeta(raw: RawEventMeta | undefined): ParsedEventMeta | null {
    if (!raw) {
        return null;
    }

    const title = asRequiredString(raw.title);
    const start = asRequiredString(raw.start);
    const end = asRequiredString(raw.end);
    const location = asRequiredString(raw.location);
    const text = asOptionalString(raw.text);

    if (!title || !start || !end || !location) {
        return null;
    }

    const startOrder = toOrderingValue(start);
    const endOrder = toOrderingValue(end);
    if (Number.isNaN(startOrder) || Number.isNaN(endOrder) || endOrder <= startOrder) {
        return null;
    }

    const compactStart = toCompactDateTime(start);
    const compactEnd = toCompactDateTime(end);
    if (!compactStart || !compactEnd) {
        return null;
    }

    return {title, start, end, location, text};
}

function parseEventRoot(value: unknown): EventRoot {
    if (!value || typeof value !== 'object') {
        return {};
    }
    return value as EventRoot;
}

function buildIcsDataUrl(input: RenderEventInput): string {
    const ics = buildIcsContent(input);
    return `data:text/calendar;charset=utf-8,${encodeURIComponent(ics)}`;
}

function buildIcsFilename(title: string): string {
    const base = title
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-+/, '')
        .replace(/-+$/, '');
    return `${base || 'event'}.ics`;
}

function renderEventDateLabel(label: string, event: ParsedEventMeta): string {
    const data: RenderEventInput = event;

    return `<details class="inline-block align-baseline relative">` +
        `<summary class="inline list-none cursor-pointer underline decoration-dotted text-blue-700 hover:text-blue-800">${withLineBreaks(label)}</summary>` +
        `<div class="absolute z-10 mt-1 min-w-44 rounded-md border border-gray-200 bg-white shadow-lg p-1">` +
        `<a class="block rounded px-2 py-1 text-sm text-gray-700 hover:bg-gray-100" href="${escapeHtml(buildGoogleCalendarUrl(data))}" target="_blank" rel="noopener noreferrer">Google Kalender</a>` +
        `<a class="block rounded px-2 py-1 text-sm text-gray-700 hover:bg-gray-100" href="${escapeHtml(buildIcsDataUrl(data))}" download="${escapeHtml(buildIcsFilename(event.title))}">ICS herunterladen</a>` +
        `</div>` +
        `</details>`;
}

function renderNewsText(text: string): string {
    const extracted = extractEventMetaFromText(text);
    const source = extracted.visibleText;
    const parts: string[] = [];
    const markerRegex = /\[event-date:([a-zA-Z0-9_-]+)]([\s\S]*?)\[\/event-date]/g;
    let cursor = 0;
    let match: RegExpExecArray | null;

    while ((match = markerRegex.exec(source)) !== null) {
        const fullMatch = match[0];
        const markerId = match[1];
        const label = match[2];
        const startIndex = match.index;

        parts.push(withLineBreaks(source.slice(cursor, startIndex)));
        const validEvent = validateEventMeta(extracted.events[markerId]);
        if (validEvent) {
            parts.push(renderEventDateLabel(label, validEvent));
        } else {
            parts.push(withLineBreaks(label));
        }
        cursor = startIndex + fullMatch.length;
    }

    parts.push(withLineBreaks(source.slice(cursor)));
    return parts.join('');
}

export function extractEventMetaFromText(text: string): { visibleText: string; events: Record<string, RawEventMeta> } {
    const match = text.match(EVENT_META_PATTERN);
    if (!match) {
        return {
            visibleText: text,
            events: {}
        };
    }

    const visibleText = text.replace(EVENT_META_PATTERN, '');
    const rawJson = match[1]?.trim();
    if (!rawJson) {
        return {
            visibleText,
            events: {}
        };
    }

    try {
        const parsed = JSON.parse(rawJson);
        const root = parseEventRoot(parsed);
        if (!root.events || typeof root.events !== 'object') {
            return {
                visibleText,
                events: {}
            };
        }
        return {
            visibleText,
            events: root.events
        };
    } catch {
        return {
            visibleText,
            events: {}
        };
    }
}

export function buildGoogleCalendarUrl(input: RenderEventInput): string {
    const startCompact = toCompactDateTime(input.start);
    const endCompact = toCompactDateTime(input.end);
    if (!startCompact || !endCompact) {
        return 'https://calendar.google.com/calendar/render?action=TEMPLATE';
    }

    const isFloating = !hasTimezoneOffset(input.start) && !hasTimezoneOffset(input.end);
    const datesValue = isFloating
        ? `${startCompact}/${endCompact}`
        : `${startCompact}Z/${endCompact}Z`;

    const params = new URLSearchParams({
        action: 'TEMPLATE',
        text: input.title,
        location: input.location,
        dates: datesValue
    });

    if (input.text) {
        params.set('details', input.text);
    }

    if (isFloating) {
        params.set('ctz', EUROPE_VIENNA_TZ);
    }

    return `https://calendar.google.com/calendar/render?${params.toString()}`;
}

export function buildIcsContent(input: RenderEventInput): string {
    const stamp = new Date().toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '');
    const startCompact = toCompactDateTime(input.start);
    const endCompact = toCompactDateTime(input.end);
    if (!startCompact || !endCompact) {
        return 'BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//pub-quizzz//News Event//DE\r\nEND:VCALENDAR\r\n';
    }

    const isFloating = !hasTimezoneOffset(input.start) && !hasTimezoneOffset(input.end);
    const dtStart = isFloating
        ? `DTSTART;TZID=${EUROPE_VIENNA_TZ}:${startCompact}`
        : `DTSTART:${startCompact}Z`;
    const dtEnd = isFloating
        ? `DTEND;TZID=${EUROPE_VIENNA_TZ}:${endCompact}`
        : `DTEND:${endCompact}Z`;

    const lines = [
        'BEGIN:VCALENDAR',
        'VERSION:2.0',
        'PRODID:-//pub-quizzz//News Event//DE',
        'BEGIN:VEVENT',
        `UID:${Date.now()}@pub-quizzz`,
        `DTSTAMP:${stamp}`,
        dtStart,
        dtEnd,
        `SUMMARY:${sanitizeIcsText(input.title)}`,
        `LOCATION:${sanitizeIcsText(input.location)}`,
        'END:VEVENT',
        'END:VCALENDAR'
    ];

    if (input.text) {
        lines.splice(9, 0, `DESCRIPTION:${sanitizeIcsText(input.text)}`);
    }

    return lines.join('\r\n') + '\r\n';
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
            <div class="text-sm text-gray-700 leading-relaxed">${renderNewsText(item.text)}</div>
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
