import {httpClient} from './http-client';

async function logoutOnce(): Promise<{ ok: boolean; status: number }> {
    const response = await httpClient.post('/logout');
    return {
        ok: response.status >= 200 && response.status < 300,
        status: response.status
    };
}

async function refreshCsrfToken(): Promise<void> {
    await httpClient.get('/api/bootstrap', {headers: {'Cache-Control': 'no-store'}});
}

export async function triggerRelogin(): Promise<void> {
    try {
        const initialResponse = await logoutOnce();

        if (!initialResponse.ok && initialResponse.status === 403) {
            await refreshCsrfToken();
            await logoutOnce();
        }
    } catch {
        // Intentionally ignored: always continue to login.
    } finally {
        if (typeof window !== 'undefined') {
            window.location.replace('/login?relogin=1');
        }
    }
}
