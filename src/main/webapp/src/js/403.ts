import {withEnsuredCsrfHeaders, withRefreshedCsrfHeaders} from './csrf';

type CsrfHeadersProvider = () => Promise<Record<string, string>>;

async function logoutOnce(fetchImpl: typeof fetch, headers: Record<string, string>): Promise<Response> {
    return fetchImpl('/logout', {
        method: 'POST',
        headers,
        credentials: 'same-origin'
    });
}

export async function triggerRelogin(
    fetchImpl: typeof fetch = fetch,
    redirect: (url: string) => void = (url) => {
        if (typeof window !== 'undefined') {
            window.location.replace(url);
        }
    },
    csrfHeadersProvider: CsrfHeadersProvider = () => withEnsuredCsrfHeaders(),
    refreshedCsrfHeadersProvider: CsrfHeadersProvider = () => withRefreshedCsrfHeaders()
): Promise<void> {
    try {
        const initialHeaders = await csrfHeadersProvider();
        const initialResponse = await logoutOnce(fetchImpl, initialHeaders);

        if (!initialResponse.ok && initialResponse.status === 403) {
            const refreshedHeaders = await refreshedCsrfHeadersProvider();
            await logoutOnce(fetchImpl, refreshedHeaders);
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
