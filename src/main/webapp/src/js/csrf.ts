function readCookieValue(name: string, cookieString: string): string | null {
    const prefix = `${name}=`;
    const cookies = cookieString.split(';');
    for (const cookie of cookies) {
        const trimmed = cookie.trim();
        if (!trimmed.startsWith(prefix)) {
            continue;
        }
        const value = trimmed.slice(prefix.length);
        if (!value) {
            return null;
        }
        return decodeURIComponent(value);
    }
    return null;
}

function readMetaContent(doc: Document | null, selector: string): string | null {
    const value = doc?.querySelector(selector)?.getAttribute('content')?.trim();
    return value || null;
}

function toHeaderRecord(headers?: HeadersInit): Record<string, string> {
    if (!headers) {
        return {};
    }

    if (headers instanceof Headers) {
        const result: Record<string, string> = {};
        headers.forEach((value, key) => {
            result[key] = value;
        });
        return result;
    }

    if (Array.isArray(headers)) {
        return Object.fromEntries(headers);
    }

    return {...headers};
}

function hasCsrfHeader(headers: Record<string, string>): boolean {
    return Object.keys(headers).some(headerName => {
        const normalized = headerName.toLowerCase();
        return normalized === 'x-xsrf-token' || normalized.includes('csrf');
    });
}

async function defaultBootstrapToken(): Promise<void> {
    if (typeof fetch === 'undefined') {
        return;
    }
    await fetch('/api/bootstrap', {
        method: 'GET',
        credentials: 'same-origin',
        cache: 'no-store'
    });
}

type CsrfSources = {
    getCookieString?: () => string;
    getDocument?: () => Document | null;
    bootstrapToken?: () => Promise<void>;
};

export function withCsrfHeaders(
    existingHeaders?: HeadersInit,
    cookieString?: string,
    doc?: Document | null
): Record<string, string> {
    const resolvedCookieString = cookieString
        ?? (typeof document !== 'undefined' ? document.cookie : '');
    const resolvedDoc = doc
        ?? (typeof document !== 'undefined' ? document : null);

    const headers = toHeaderRecord(existingHeaders);
    const cookieToken = readCookieValue('XSRF-TOKEN', resolvedCookieString);
    if (cookieToken) {
        headers['X-XSRF-TOKEN'] = cookieToken;
        return headers;
    }

    const metaToken = readMetaContent(resolvedDoc, 'meta[name="_csrf"]');
    if (!metaToken) {
        return headers;
    }

    const headerName = readMetaContent(resolvedDoc, 'meta[name="_csrf_header"]') ?? 'X-CSRF-TOKEN';
    headers[headerName] = metaToken;
    return headers;
}

export async function withEnsuredCsrfHeaders(
    existingHeaders?: HeadersInit,
    sources?: CsrfSources
): Promise<Record<string, string>> {
    const getCookieString = sources?.getCookieString
        ?? (() => (typeof document !== 'undefined' ? document.cookie : ''));
    const getDocument = sources?.getDocument
        ?? (() => (typeof document !== 'undefined' ? document : null));

    let headers = withCsrfHeaders(existingHeaders, getCookieString(), getDocument());
    if (hasCsrfHeader(headers)) {
        return headers;
    }

    const bootstrapToken = sources?.bootstrapToken ?? defaultBootstrapToken;
    try {
        await bootstrapToken();
    } catch {
        return headers;
    }

    headers = withCsrfHeaders(existingHeaders, getCookieString(), getDocument());
    return headers;
}

export async function withRefreshedCsrfHeaders(
    existingHeaders?: HeadersInit,
    sources?: CsrfSources
): Promise<Record<string, string>> {
    const getCookieString = sources?.getCookieString
        ?? (() => (typeof document !== 'undefined' ? document.cookie : ''));
    const getDocument = sources?.getDocument
        ?? (() => (typeof document !== 'undefined' ? document : null));
    const bootstrapToken = sources?.bootstrapToken ?? defaultBootstrapToken;

    try {
        await bootstrapToken();
    } catch {
        return withCsrfHeaders(existingHeaders, getCookieString(), getDocument());
    }

    return withCsrfHeaders(existingHeaders, getCookieString(), getDocument());
}
