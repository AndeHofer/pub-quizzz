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

        const assignMock = vi.fn();
        vi.useFakeTimers();
        const originalWindow = globalThis.window;
        const fakeWindow = {
            location: {
                assign: assignMock
            }
        } as unknown as Window & typeof globalThis;
        Object.defineProperty(globalThis, 'window', {
            configurable: true,
            writable: true,
            value: fakeWindow
        });

        let handled = false;
        try {
            handled = await handleAuthExpiredIfNeeded(response, {
                redirectUrl: '/login'
            });
            expect(assignMock).not.toHaveBeenCalled();
            vi.runAllTimers();
        } finally {
            vi.useRealTimers();
            Object.defineProperty(globalThis, 'window', {
                configurable: true,
                writable: true,
                value: originalWindow
            });
        }

        expect(handled).toBe(true);
        expect(assignMock).toHaveBeenCalledWith('/login');
    });

    it('returns false when response is not auth-expired', async () => {
        const response = {
            status: 400,
            text: async () => '{"error":"Bad Request"}'
        } as Response;

        const assignMock = vi.fn();
        const originalWindow = globalThis.window;
        const fakeWindow = {
            location: {
                assign: assignMock
            }
        } as unknown as Window & typeof globalThis;
        Object.defineProperty(globalThis, 'window', {
            configurable: true,
            writable: true,
            value: fakeWindow
        });

        let handled = false;
        try {
            handled = await handleAuthExpiredIfNeeded(response);
        } finally {
            Object.defineProperty(globalThis, 'window', {
                configurable: true,
                writable: true,
                value: originalWindow
            });
        }

        expect(handled).toBe(false);
        expect(assignMock).not.toHaveBeenCalled();
    });
});
