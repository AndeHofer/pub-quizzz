import {describe, expect, it, vi} from 'vitest';

vi.mock('./utils', () => ({
    showMessage: vi.fn(),
    goBack: vi.fn()
}));

import {handleAuthExpiredIfNeeded, isAuthExpiredResponse, looksLikeLoginHtml} from './auth-session';

describe('auth-session', () => {
    it('detects Spring login html payload', () => {
        expect(looksLikeLoginHtml('<html><body><h1>Login</h1></body></html>')).toBe(true);
    });

    it('treats 401 and 403 as auth-expired responses', () => {
        const unauthorized = {status: 401} as Response;
        const forbidden = {status: 403} as Response;

        expect(isAuthExpiredResponse(unauthorized, '')).toBe(true);
        expect(isAuthExpiredResponse(forbidden, '')).toBe(true);
    });

    it('shows message and schedules redirect when auth expired', async () => {
        const response = {
            status: 401,
            text: async () => '{"error":"Nicht authentifiziert"}'
        } as Response;

        const redirect = vi.fn();
        const scheduler = vi.fn((callback: () => void) => callback());

        const handled = await handleAuthExpiredIfNeeded(response, {
            redirect,
            scheduler,
            redirectUrl: '/login'
        });

        expect(handled).toBe(true);
        expect(scheduler).toHaveBeenCalledOnce();
        expect(redirect).toHaveBeenCalledWith('/login');
    });

    it('returns false when response is not auth-expired', async () => {
        const response = {
            status: 400,
            text: async () => '{"error":"Bad Request"}'
        } as Response;

        const redirect = vi.fn();
        const scheduler = vi.fn();

        const handled = await handleAuthExpiredIfNeeded(response, {
            redirect,
            scheduler
        });

        expect(handled).toBe(false);
        expect(redirect).not.toHaveBeenCalled();
        expect(scheduler).not.toHaveBeenCalled();
    });
});
