import axios, {type AxiosResponse} from 'axios';

export const httpClient = axios.create({
    withCredentials: true,
    xsrfCookieName: 'XSRF-TOKEN',
    xsrfHeaderName: 'X-XSRF-TOKEN',
    validateStatus: () => true
});

function toBodyText(data: unknown): string {
    if (typeof data === 'string') {
        return data;
    }
    if (data === null || data === undefined) {
        return '';
    }
    if (typeof data === 'object') {
        try {
            return JSON.stringify(data);
        } catch {
            return String(data);
        }
    }
    return String(data);
}

export function toResponse(response: AxiosResponse): Response {
    const body = (response.status === 204 || response.status === 205)
        ? null
        : toBodyText(response.data);
    return new Response(body, {
        status: response.status,
        statusText: response.statusText
    });
}

export function readResponseText(data: unknown): string {
    return toBodyText(data).trim();
}
