import {describe, expect, it} from 'vitest';
import {buildLoginStatsRows, roleLabel} from './admin_login_stats';

describe('admin_login_stats helpers', () => {
    it('maps known role labels to German text', () => {
        expect(roleLabel('ADMIN')).toBe('Admin');
        expect(roleLabel('USER')).toBe('Benutzer');
    });

    it('renders empty-state row when no stats are present', () => {
        const markup = buildLoginStatsRows([]);

        expect(markup).toContain('Keine Login-Daten gefunden.');
        expect(markup).toContain('colspan="3"');
    });

    it('renders month, role, and login count columns', () => {
        const markup = buildLoginStatsRows([
            {month: '2026-05', role: 'USER', loginCount: 12},
            {month: '2026-05', role: 'ADMIN', loginCount: 3}
        ]);

        expect(markup).toContain('2026-05');
        expect(markup).toContain('Benutzer');
        expect(markup).toContain('Admin');
        expect(markup).toContain('12');
        expect(markup).toContain('3');
    });
});
