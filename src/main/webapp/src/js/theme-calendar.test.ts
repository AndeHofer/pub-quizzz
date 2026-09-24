import {describe, expect, it} from 'vitest';
import {
    calculateEasterSunday,
    getViennaCalendarDay,
    resolveCalendarTheme,
    type CalendarDay
} from './theme-calendar';

const calendarDay = (year: number, month: number, day: number): CalendarDay => ({year, month, day});

describe('resolveCalendarTheme', () => {
    it.each([
        [1, 'january'], [2, 'february'], [3, 'march'], [4, 'april'],
        [5, 'may'], [6, 'june'], [7, 'july'], [8, 'august'],
        [9, 'september'], [10, 'october'], [11, 'november'], [12, 'december']
    ])('uses the matching month theme outside events for month %i', (month, expected) => {
        expect(resolveCalendarTheme(calendarDay(2026, month, 15))).toBe(expected);
    });

    it('applies Halloween from 24 through 31 October inclusive', () => {
        expect(resolveCalendarTheme(calendarDay(2026, 10, 23))).toBe('october');
        expect(resolveCalendarTheme(calendarDay(2026, 10, 24))).toBe('halloween');
        expect(resolveCalendarTheme(calendarDay(2026, 10, 31))).toBe('halloween');
        expect(resolveCalendarTheme(calendarDay(2026, 11, 1))).toBe('november');
    });

    it('applies Christmas from 24 through 26 December inclusive', () => {
        expect(resolveCalendarTheme(calendarDay(2026, 12, 23))).toBe('december');
        expect(resolveCalendarTheme(calendarDay(2026, 12, 24))).toBe('christmas');
        expect(resolveCalendarTheme(calendarDay(2026, 12, 26))).toBe('christmas');
        expect(resolveCalendarTheme(calendarDay(2026, 12, 27))).toBe('december');
    });

    it('applies New Year across calendar years', () => {
        expect(resolveCalendarTheme(calendarDay(2026, 12, 30))).toBe('december');
        expect(resolveCalendarTheme(calendarDay(2026, 12, 31))).toBe('new-year');
        expect(resolveCalendarTheme(calendarDay(2027, 1, 1))).toBe('new-year');
        expect(resolveCalendarTheme(calendarDay(2027, 1, 2))).toBe('january');
    });

    it('applies Easter from Good Friday through Easter Monday inclusive', () => {
        expect(resolveCalendarTheme(calendarDay(2024, 3, 28))).toBe('march');
        expect(resolveCalendarTheme(calendarDay(2024, 3, 29))).toBe('easter');
        expect(resolveCalendarTheme(calendarDay(2024, 3, 31))).toBe('easter');
        expect(resolveCalendarTheme(calendarDay(2024, 4, 1))).toBe('easter');
        expect(resolveCalendarTheme(calendarDay(2024, 4, 2))).toBe('april');
    });
});

describe('calculateEasterSunday', () => {
    it.each([
        [2024, {year: 2024, month: 3, day: 31}],
        [2025, {year: 2025, month: 4, day: 20}],
        [2028, {year: 2028, month: 4, day: 16}],
        [2032, {year: 2032, month: 3, day: 28}]
    ])('calculates Gregorian Easter Sunday for %i', (year, expected) => {
        expect(calculateEasterSunday(year)).toEqual(expected);
    });
});

describe('getViennaCalendarDay', () => {
    it('uses the Vienna calendar day around midnight', () => {
        expect(getViennaCalendarDay(new Date('2026-12-31T22:30:00.000Z')))
            .toEqual({year: 2026, month: 12, day: 31});
        expect(getViennaCalendarDay(new Date('2026-12-31T23:30:00.000Z')))
            .toEqual({year: 2027, month: 1, day: 1});
    });

    it('remains correct across Vienna daylight-saving transitions', () => {
        expect(getViennaCalendarDay(new Date('2026-03-29T00:30:00.000Z')))
            .toEqual({year: 2026, month: 3, day: 29});
        expect(getViennaCalendarDay(new Date('2026-10-25T22:30:00.000Z')))
            .toEqual({year: 2026, month: 10, day: 25});
    });
});
