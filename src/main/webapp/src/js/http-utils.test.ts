import {describe, expect, it} from 'vitest';
import {readHttpErrorMessage} from './http-utils';

describe('http-utils', () => {
    it('builds fallback with trimmed response body details', async () => {
        const response = {
            text: async () => '  Backend Fehler  ',
            status: 400,
            statusText: 'Bad Request'
        } as Response;

        await expect(readHttpErrorMessage(response, 'Laden fehlgeschlagen'))
            .resolves.toBe('Laden fehlgeschlagen: Backend Fehler');
    });

    it('falls back to status line when body is empty', async () => {
        const response = {
            text: async () => '   ',
            status: 404,
            statusText: 'Not Found'
        } as Response;

        await expect(readHttpErrorMessage(response, 'Fehler'))
            .resolves.toBe('Fehler: 404 Not Found');
    });
});
