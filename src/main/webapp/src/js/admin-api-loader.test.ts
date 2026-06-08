import {describe, expect, it, vi} from 'vitest';
import {getApiFetchForTesting} from './admin-api-loader';

describe('admin api loader', () => {
    it('loads apiFetch lazily and caches loader result', async () => {
        const apiFetchMock = vi.fn();
        const importer = vi.fn(async () => ({apiFetch: apiFetchMock}));

        const first = await getApiFetchForTesting(importer);
        const second = await getApiFetchForTesting(importer);

        expect(first).toBe(apiFetchMock);
        expect(second).toBe(apiFetchMock);
        expect(importer).toHaveBeenCalledTimes(1);
    });
});
