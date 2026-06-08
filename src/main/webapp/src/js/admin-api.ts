import {handleAuthExpiredIfNeeded} from './auth-session';
import {httpClient, toResponse} from './http-client';

function isAuthExpiredRedirectError(error: unknown): boolean {
    return error instanceof Error && error.message === 'AUTH_EXPIRED_REDIRECT';
}

function toAxiosHeaders(headers?: HeadersInit): Record<string, string> | undefined {
    if (!headers) {
        return undefined;
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

export async function apiFetch(url: string, options?: RequestInit): Promise<Response> {
    try {
        const method = (options?.method ?? 'GET').toUpperCase();
        const response = await httpClient.request({
            url,
            method,
            data: options?.body,
            headers: toAxiosHeaders(options?.headers)
        });

        const mappedResponse = toResponse(response);
        if (await handleAuthExpiredIfNeeded(mappedResponse.clone())) {
            throw new Error('AUTH_EXPIRED_REDIRECT');
        }

        return mappedResponse;
    } catch (error) {
        if (isAuthExpiredRedirectError(error)) {
            throw error;
        }
        console.error('Netzwerkfehler:', error);
        throw error;
    }
}
