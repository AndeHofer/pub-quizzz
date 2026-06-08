import {beforeEach, describe, expect, it, vi} from 'vitest';
import {httpClient} from './http-client';
import {triggerRelogin} from './logout-action';

vi.mock('./http-client', () => ({
    httpClient: {
        post: vi.fn(),
        get: vi.fn()
    }
}));

describe('logout action', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('tries logout and then redirects to /login', async () => {
        const postMock = vi.mocked(httpClient.post);
        const getMock = vi.mocked(httpClient.get);
        const replaceMock = vi.fn();
        postMock.mockResolvedValue({status: 200});
        getMock.mockResolvedValue({status: 200});

        const originalWindow = globalThis.window;
        const fakeWindow = {
            location: {
                replace: replaceMock
            }
        } as unknown as Window & typeof globalThis;
        Object.defineProperty(globalThis, 'window', {
            configurable: true,
            writable: true,
            value: fakeWindow
        });

        try {
            await triggerRelogin();
        } finally {
            Object.defineProperty(globalThis, 'window', {
                configurable: true,
                writable: true,
                value: originalWindow
            });
        }

        expect(postMock).toHaveBeenCalledTimes(1);
        expect(getMock).not.toHaveBeenCalled();
        expect(replaceMock).toHaveBeenCalledWith('/login');
    });

    it('still redirects to /login when logout request fails', async () => {
        const postMock = vi.mocked(httpClient.post);
        const getMock = vi.mocked(httpClient.get);
        const replaceMock = vi.fn();
        postMock.mockRejectedValue(new Error('network'));

        const originalWindow = globalThis.window;
        const fakeWindow = {
            location: {
                replace: replaceMock
            }
        } as unknown as Window & typeof globalThis;
        Object.defineProperty(globalThis, 'window', {
            configurable: true,
            writable: true,
            value: fakeWindow
        });

        try {
            await triggerRelogin();
        } finally {
            Object.defineProperty(globalThis, 'window', {
                configurable: true,
                writable: true,
                value: originalWindow
            });
        }

        expect(postMock).toHaveBeenCalledTimes(1);
        expect(getMock).not.toHaveBeenCalled();
        expect(replaceMock).toHaveBeenCalledWith('/login');
    });

    it('retries logout after csrf refresh when first logout returns 403', async () => {
        const postMock = vi.mocked(httpClient.post);
        const getMock = vi.mocked(httpClient.get);
        const replaceMock = vi.fn();
        postMock
            .mockResolvedValueOnce({status: 403})
            .mockResolvedValueOnce({status: 200});
        getMock.mockResolvedValue({status: 200});

        const originalWindow = globalThis.window;
        const fakeWindow = {
            location: {
                replace: replaceMock
            }
        } as unknown as Window & typeof globalThis;
        Object.defineProperty(globalThis, 'window', {
            configurable: true,
            writable: true,
            value: fakeWindow
        });

        try {
            await triggerRelogin();
        } finally {
            Object.defineProperty(globalThis, 'window', {
                configurable: true,
                writable: true,
                value: originalWindow
            });
        }

        expect(postMock).toHaveBeenCalledTimes(2);
        expect(getMock).toHaveBeenCalledTimes(1);
        expect(replaceMock).toHaveBeenCalledWith('/login');
    });
});
