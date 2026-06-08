import {showMessage} from './utils';

export type AuthExpiredOptions = {
    redirectUrl?: string;
    delayMs?: number;
    message?: string;
};

const DEFAULT_REDIRECT_URL = '/login';
const DEFAULT_DELAY_MS = 1500;
const DEFAULT_MESSAGE = 'Sitzung abgelaufen. Du wirst zur Anmeldung weitergeleitet...';

export function looksLikeLoginHtml(bodyText: string): boolean {
    const normalized = bodyText.toLowerCase();
    return normalized.includes('<html') && normalized.includes('login');
}

export function isAuthExpiredResponse(response: Response, bodyText: string): boolean {
    if (response.status === 401 || response.status === 403) {
        return true;
    }
    return looksLikeLoginHtml(bodyText);
}

export async function handleAuthExpiredIfNeeded(
    response: Response,
    options: AuthExpiredOptions = {}
): Promise<boolean> {
    const bodyText = await response.text().catch(() => '');
    if (!isAuthExpiredResponse(response, bodyText)) {
        return false;
    }

    showMessage(options.message ?? DEFAULT_MESSAGE, 'error');
    const redirect = (url: string) => {
        if (typeof window !== 'undefined') {
            window.location.assign(url);
        }
    };

    setTimeout(
        () => redirect(options.redirectUrl ?? DEFAULT_REDIRECT_URL),
        options.delayMs ?? DEFAULT_DELAY_MS
    );
    return true;
}
