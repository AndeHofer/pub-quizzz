import {describe, expect, it, vi} from 'vitest';
import {triggerRelogin} from './403';

describe('403 relogin action', () => {
    it('tries logout with csrf headers and then redirects to /login', async () => {
        const fetchMock = vi.fn(async () => ({ok: true}));
        const redirectMock = vi.fn();
        const csrfHeadersMock = vi.fn(async () => ({'X-XSRF-TOKEN': 'token123'}));
        const refreshedCsrfHeadersMock = vi.fn(async () => ({'X-XSRF-TOKEN': 'token456'}));

        await triggerRelogin(
            fetchMock as unknown as typeof fetch,
            redirectMock,
            csrfHeadersMock,
            refreshedCsrfHeadersMock
        );

        expect(csrfHeadersMock).toHaveBeenCalledTimes(1);
        expect(refreshedCsrfHeadersMock).not.toHaveBeenCalled();
        expect(fetchMock).toHaveBeenCalledWith('/logout', {
            method: 'POST',
            headers: {'X-XSRF-TOKEN': 'token123'},
            credentials: 'same-origin'
        });
        expect(redirectMock).toHaveBeenCalledWith('/login?relogin=1');
    });

    it('still redirects to /login when logout request fails', async () => {
        const fetchMock = vi.fn(async () => {
            throw new Error('network');
        });
        const redirectMock = vi.fn();
        const csrfHeadersMock = vi.fn(async () => ({'X-XSRF-TOKEN': 'token123'}));
        const refreshedCsrfHeadersMock = vi.fn(async () => ({'X-XSRF-TOKEN': 'token456'}));

        await triggerRelogin(
            fetchMock as unknown as typeof fetch,
            redirectMock,
            csrfHeadersMock,
            refreshedCsrfHeadersMock
        );

        expect(redirectMock).toHaveBeenCalledWith('/login?relogin=1');
    });

    it('retries logout after forced csrf refresh when first logout returns 403', async () => {
        const fetchMock = vi.fn()
            .mockResolvedValueOnce({ok: false, status: 403})
            .mockResolvedValueOnce({ok: true, status: 200});
        const redirectMock = vi.fn();
        const csrfHeadersMock = vi.fn(async () => ({'X-XSRF-TOKEN': 'stale-token'}));
        const refreshedCsrfHeadersMock = vi.fn(async () => ({'X-XSRF-TOKEN': 'fresh-token'}));

        await triggerRelogin(
            fetchMock as unknown as typeof fetch,
            redirectMock,
            csrfHeadersMock,
            refreshedCsrfHeadersMock
        );

        expect(csrfHeadersMock).toHaveBeenCalledTimes(1);
        expect(refreshedCsrfHeadersMock).toHaveBeenCalledTimes(1);
        expect(fetchMock).toHaveBeenCalledTimes(2);
        expect(fetchMock).toHaveBeenNthCalledWith(1, '/logout', {
            method: 'POST',
            headers: {'X-XSRF-TOKEN': 'stale-token'},
            credentials: 'same-origin'
        });
        expect(fetchMock).toHaveBeenNthCalledWith(2, '/logout', {
            method: 'POST',
            headers: {'X-XSRF-TOKEN': 'fresh-token'},
            credentials: 'same-origin'
        });
        expect(redirectMock).toHaveBeenCalledWith('/login?relogin=1');
    });
});
