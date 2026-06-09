import {beforeEach, describe, expect, it, vi} from 'vitest';

import {wireLogoutButton} from './index';

describe('index logout button', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('writes XSRF-TOKEN cookie value into hidden logout csrf field', () => {
        const hiddenInput = {
            value: ''
        };
        const getElementById = vi.fn((id: string) => {
            if (id === 'logoutCsrfToken') {
                return hiddenInput;
            }
            return null;
        });

        const fakeDocument = {
            cookie: 'foo=bar; XSRF-TOKEN=abc123; x=y',
            getElementById,
        } as unknown as Document;

        wireLogoutButton(fakeDocument);

        expect(hiddenInput.value).toBe('abc123');
    });

    it('does nothing when hidden csrf field is absent', () => {
        const getElementById = vi.fn(() => null);
        const fakeDocument = {
            cookie: 'XSRF-TOKEN=abc123',
            getElementById
        } as unknown as Document;

        wireLogoutButton(fakeDocument);

        expect(getElementById).toHaveBeenCalledWith('logoutCsrfToken');
    });
});
