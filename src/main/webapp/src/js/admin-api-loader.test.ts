import {describe, expect, it, vi} from 'vitest';

vi.mock('./admin-api', () => {
    const apiFetch = vi.fn();
    return {apiFetch};
});

import {apiFetch} from './admin-api';
import {getApiFetch} from './admin-api-loader';

describe('admin api loader', () => {
    it('returns the same cached apiFetch function', async () => {
        const first = await getApiFetch();
        const second = await getApiFetch();

        expect(first).toBe(apiFetch);
        expect(second).toBe(apiFetch);
        expect(second).toBe(first);
    });
});
