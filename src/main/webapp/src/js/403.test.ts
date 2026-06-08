import {describe, expect, it, vi} from 'vitest';
import {triggerRelogin} from './403';

describe('403 relogin action', () => {
    it('tries logout and then redirects to /login', async () => {
        const logoutMock = vi.fn(async () => ({ok: true, status: 200}));
        const redirectMock = vi.fn();
        const refreshCsrfMock = vi.fn(async () => {
        });

        await triggerRelogin({
            logoutExecutor: logoutMock,
            redirect: redirectMock,
            refreshCsrfExecutor: refreshCsrfMock
        });

        expect(logoutMock).toHaveBeenCalledTimes(1);
        expect(refreshCsrfMock).not.toHaveBeenCalled();
        expect(redirectMock).toHaveBeenCalledWith('/login?relogin=1');
    });

    it('still redirects to /login when logout request fails', async () => {
        const logoutMock = vi.fn(async () => {
            throw new Error('network');
        });
        const redirectMock = vi.fn();
        const refreshCsrfMock = vi.fn(async () => {
        });

        await triggerRelogin({
            logoutExecutor: logoutMock,
            redirect: redirectMock,
            refreshCsrfExecutor: refreshCsrfMock
        });

        expect(redirectMock).toHaveBeenCalledWith('/login?relogin=1');
    });

    it('retries logout after csrf refresh when first logout returns 403', async () => {
        const logoutMock = vi.fn()
            .mockResolvedValueOnce({ok: false, status: 403})
            .mockResolvedValueOnce({ok: true, status: 200});
        const redirectMock = vi.fn();
        const refreshCsrfMock = vi.fn(async () => {
        });

        await triggerRelogin({
            logoutExecutor: logoutMock,
            redirect: redirectMock,
            refreshCsrfExecutor: refreshCsrfMock
        });

        expect(logoutMock).toHaveBeenCalledTimes(2);
        expect(refreshCsrfMock).toHaveBeenCalledTimes(1);
        expect(redirectMock).toHaveBeenCalledWith('/login?relogin=1');
    });
});
