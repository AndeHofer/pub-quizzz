import type {ThemeId} from './theme-preview';

export type CalendarDay = {
    year: number;
    month: number;
    day: number;
};

type EventRule = {
    themeId: ThemeId;
    isActive: (day: CalendarDay) => boolean;
};

const MONTH_THEMES: Readonly<Record<number, ThemeId>> = {
    1: 'january',
    2: 'february',
    3: 'march',
    4: 'april',
    5: 'may',
    6: 'june',
    7: 'july',
    8: 'august',
    9: 'september',
    10: 'october',
    11: 'november',
    12: 'december'
};

const EVENT_RULES: ReadonlyArray<EventRule> = [
    {themeId: 'easter', isActive: isEasterWeekend},
    {themeId: 'halloween', isActive: day => day.month === 10 && day.day >= 24 && day.day <= 31},
    {themeId: 'christmas', isActive: day => day.month === 12 && day.day >= 24 && day.day <= 26},
    {themeId: 'new-year', isActive: day => (day.month === 12 && day.day === 31) || (day.month === 1 && day.day === 1)}
];

export function getViennaCalendarDay(now: Date): CalendarDay {
    const parts = new Intl.DateTimeFormat('en-CA', {
        timeZone: 'Europe/Vienna',
        year: 'numeric',
        month: 'numeric',
        day: 'numeric'
    }).formatToParts(now);

    return {
        year: getDatePart(parts, 'year'),
        month: getDatePart(parts, 'month'),
        day: getDatePart(parts, 'day')
    };
}

export function calculateEasterSunday(year: number): CalendarDay {
    const a = year % 19;
    const b = Math.floor(year / 100);
    const c = year % 100;
    const d = Math.floor(b / 4);
    const e = b % 4;
    const f = Math.floor((b + 8) / 25);
    const g = Math.floor((b - f + 1) / 3);
    const h = (19 * a + b - d - g + 15) % 30;
    const i = Math.floor(c / 4);
    const k = c % 4;
    const l = (32 + 2 * e + 2 * i - h - k) % 7;
    const m = Math.floor((a + 11 * h + 22 * l) / 451);
    const month = Math.floor((h + l - 7 * m + 114) / 31);
    const day = (h + l - 7 * m + 114) % 31 + 1;
    return {year, month, day};
}

export function resolveCalendarTheme(day: CalendarDay): ThemeId {
    const activeEvent = EVENT_RULES.find(rule => rule.isActive(day));
    return activeEvent?.themeId ?? MONTH_THEMES[day.month] ?? 'standard';
}

function isEasterWeekend(day: CalendarDay): boolean {
    const easterSunday = calculateEasterSunday(day.year);
    return isSameDay(day, addDays(easterSunday, -2))
        || isSameDay(day, easterSunday)
        || isSameDay(day, addDays(easterSunday, 1));
}

function addDays(day: CalendarDay, amount: number): CalendarDay {
    const date = new Date(Date.UTC(day.year, day.month - 1, day.day + amount));
    return {
        year: date.getUTCFullYear(),
        month: date.getUTCMonth() + 1,
        day: date.getUTCDate()
    };
}

function isSameDay(left: CalendarDay, right: CalendarDay): boolean {
    return left.year === right.year && left.month === right.month && left.day === right.day;
}

function getDatePart(parts: Intl.DateTimeFormatPart[], type: Intl.DateTimeFormatPartTypes): number {
    const value = parts.find(part => part.type === type)?.value;
    if (!value) {
        throw new Error(`Missing ${type} in Vienna calendar date.`);
    }
    return Number(value);
}
