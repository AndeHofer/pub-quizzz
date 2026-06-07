import {describe, expect, it} from 'vitest';
import {withCsrfHeaders, withEnsuredCsrfHeaders, withRefreshedCsrfHeaders} from './csrf';

describe('csrf helper', () => {
    it('adds csrf header from XSRF-TOKEN cookie and keeps existing headers', () => {
        const headers = withCsrfHeaders(
            {'Content-Type': 'application/json'},
            'XSRF-TOKEN=abc123; other=value'
        );

        expect(headers).toMatchObject({
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': 'abc123'
        });
    });

    it('falls back to meta csrf token/header when cookie token is missing', () => {
        const fakeDocument = {
            querySelector: (selector: string) => {
                if (selector === 'meta[name="_csrf"]') {
                    return {
                        getAttribute: (name: string) => (name === 'content' ? 'meta-token' : null)
                    };
                }
                if (selector === 'meta[name="_csrf_header"]') {
                    return {
                        getAttribute: (name: string) => (name === 'content' ? 'X-CSRF-TOKEN' : null)
                    };
                }
                return null;
            }
        } as unknown as Document;

        const headers = withCsrfHeaders({}, '', fakeDocument);
        expect(headers).toMatchObject({'X-CSRF-TOKEN': 'meta-token'});
    });

    it('returns existing headers unchanged when no csrf token source exists', () => {
        const headers = withCsrfHeaders({'Content-Type': 'application/json'}, 'foo=bar');
        expect(headers).toMatchObject({'Content-Type': 'application/json'});
        expect(Object.keys(headers)).toHaveLength(1);
    });

    it('keeps headers unchanged when bootstrap cannot provide token', async () => {
        const headers = await withEnsuredCsrfHeaders(
            {'Content-Type': 'application/json'},
            {
                getCookieString: () => 'foo=bar',
                getDocument: () => null,
                bootstrapToken: async () => {
                    throw new Error('network');
                }
            }
        );

        expect(headers).toMatchObject({'Content-Type': 'application/json'});
        expect(headers['X-XSRF-TOKEN']).toBeUndefined();
    });

    it('bootstraps and then adds cookie csrf header when initially missing', async () => {
        let cookieValue = 'foo=bar';
        const headers = await withEnsuredCsrfHeaders(
            {'Content-Type': 'application/json'},
            {
                getCookieString: () => cookieValue,
                getDocument: () => null,
                bootstrapToken: async () => {
                    cookieValue = 'XSRF-TOKEN=bootstrapped123';
                }
            }
        );

        expect(headers).toMatchObject({
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': 'bootstrapped123'
        });
    });

    it('forces bootstrap and refreshes csrf header even when cookie token already exists', async () => {
        let cookieValue = 'XSRF-TOKEN=old-token';
        const headers = await withRefreshedCsrfHeaders(
            {'Content-Type': 'application/json'},
            {
                getCookieString: () => cookieValue,
                getDocument: () => null,
                bootstrapToken: async () => {
                    cookieValue = 'XSRF-TOKEN=new-token';
                }
            }
        );

        expect(headers).toMatchObject({
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': 'new-token'
        });
    });
});
