import {describe, expect, it} from 'vitest';

describe('403 module exports', () => {
    it('does not expose triggerRelogin directly', async () => {
        const moduleExports = await import('./403');

        expect(Object.prototype.hasOwnProperty.call(moduleExports, 'triggerRelogin')).toBe(false);
    });
});
