import {afterEach, describe, expect, it, vi} from 'vitest';
import {httpClient} from './http-client';
import * as authSession from './auth-session';
import {apiFetch} from './admin-api';

function mockAxiosResponse(overrides: { status: number; statusText?: string; data?: unknown }) {
    return {
        data: overrides.data ?? '',
        status: overrides.status,
        statusText: overrides.statusText ?? '',
        headers: {},
        config: {headers: {}}
    };
}

afterEach(() => {
    vi.restoreAllMocks();
});

describe('admin api fetch', () => {
    it('returns response mapped from axios request', async () => {
        vi.spyOn(httpClient, 'request').mockResolvedValue(mockAxiosResponse({
            status: 200,
            statusText: 'OK',
            data: '{"ok":true}'
        }));

        const response = await apiFetch('/admin/quizzes');

        expect(response.status).toBe(200);
        await expect(response.text()).resolves.toContain('"ok":true');
    });

    it('does not bootstrap csrf endpoint for mutating request', async () => {
        vi.spyOn(httpClient, 'request').mockResolvedValue(mockAxiosResponse({
            status: 204,
            statusText: 'No Content'
        }));
        const fetchSpy = vi.spyOn(globalThis, 'fetch');

        await apiFetch('/admin/team', {method: 'POST', body: '{"teamName":"A"}'});

        expect(fetchSpy).not.toHaveBeenCalledWith('/api/bootstrap', expect.anything());
    });

    it('throws auth expired redirect error when auth helper triggers redirect', async () => {
        vi.spyOn(httpClient, 'request').mockResolvedValue(mockAxiosResponse({
            status: 401,
            statusText: 'Unauthorized',
            data: '<html>login</html>'
        }));
        vi.spyOn(authSession, 'handleAuthExpiredIfNeeded').mockResolvedValue(true);

        await expect(apiFetch('/admin/users')).rejects.toThrow('AUTH_EXPIRED_REDIRECT');
    });
});
