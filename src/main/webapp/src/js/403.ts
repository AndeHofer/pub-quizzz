import {httpClient} from './http-client';

type LogoutExecutor = () => Promise<{ ok: boolean; status: number }>;
type CsrfRefreshExecutor = () => Promise<void>;
type ReloginOptions = {
    logoutExecutor?: LogoutExecutor;
    redirect?: (url: string) => void;
    refreshCsrfExecutor?: CsrfRefreshExecutor;
};

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

export async function triggerRelogin(
    options: ReloginOptions = {}
): Promise<void> {
    const logoutExecutor = options.logoutExecutor ?? logoutOnce;
    const redirect = options.redirect ?? ((url: string) => {
        if (typeof window !== 'undefined') {
            window.location.replace(url);
        }
    });
    const refreshCsrfExecutor = options.refreshCsrfExecutor ?? refreshCsrfToken;

    try {
        const initialResponse = await logoutExecutor();

        if (!initialResponse.ok && initialResponse.status === 403) {
            await refreshCsrfExecutor();
            await logoutExecutor();
        }
    } catch {
        // Intentionally ignored: always continue to login.
    } finally {
        redirect('/login?relogin=1');
    }
}

if (typeof document !== 'undefined') {
    const reloginButton = document.getElementById('reloginBtn');
    if (reloginButton) {
        reloginButton.addEventListener('click', async (event) => {
            event.preventDefault();
            await triggerRelogin();
        });
    }
}
