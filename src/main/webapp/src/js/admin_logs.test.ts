import {describe, expect, it} from 'vitest';
import {buildLogStream, levelBadgeClass, queryParamsFromFilters} from './admin_logs';

describe('admin_logs helpers', () => {
    it('renders empty-state in stream mode', () => {
        const markup = buildLogStream([]);
        expect(markup).toContain('Keine Log-Eintraege gefunden.');
    });

    it('maps level badges to deterministic classes', () => {
        expect(levelBadgeClass('ERROR')).toContain('text-red-700');
        expect(levelBadgeClass('WARN')).toContain('text-amber-700');
        expect(levelBadgeClass('INFO')).toContain('text-blue-700');
        expect(levelBadgeClass('DEBUG')).toContain('text-gray-700');
        expect(levelBadgeClass('ANYTHING')).toContain('text-slate-700');
    });

    it('escapes markup in message and source', () => {
        const markup = buildLogStream([
            {
                timestamp: '2026-05-29T10:00:00',
                level: 'INFO',
                source: '<script>alert(1)</script>',
                message: '<img src=x onerror=alert(1)>',
                rawLine: '<raw>'
            }
        ]);

        expect(markup).not.toContain('<script>alert(1)</script>');
        expect(markup).not.toContain('<img src=x onerror=alert(1)>');
        expect(markup).toContain('&lt;script&gt;alert(1)&lt;/script&gt;');
        expect(markup).toContain('&lt;img src=x onerror=alert(1)&gt;');
    });

    it('builds URL params from non-empty filters and keeps limit', () => {
        const params = queryParamsFromFilters({
            q: 'error',
            level: 'ERROR',
            from: '2026-05-29T08:00:00',
            to: '2026-05-29T09:00:00',
            limit: 200
        });

        expect(params.get('q')).toBe('error');
        expect(params.get('level')).toBe('ERROR');
        expect(params.get('from')).toBe('2026-05-29T08:00:00');
        expect(params.get('to')).toBe('2026-05-29T09:00:00');
        expect(params.get('limit')).toBe('200');
    });

    it('renders multiline raw content for stacktraces', () => {
        const markup = buildLogStream([
            {
                timestamp: '2026-05-29T10:00:00',
                level: 'ERROR',
                source: 'GlobalExceptionHandler.handleGenericException:70',
                message: 'Unexpected error',
                rawLine: '2026-05-29 10:00:00 ERROR GlobalExceptionHandler.handleGenericException:70 - Unexpected error\n'
                    + 'java.lang.RuntimeException: boom\n'
                    + '\tat com.ande.pubquizzz.SomeService.run(SomeService.java:42)'
            }
        ]);

        expect(markup).toContain('RuntimeException: boom');
        expect(markup).toContain('SomeService.run');
    });
});
