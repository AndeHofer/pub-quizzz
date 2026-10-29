import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {describe, expect, it} from 'vitest';

describe('quiz page markup', () => {
    it('includes the next quiz navigation link', async () => {
        const quizPage = await readFile(path.resolve(__dirname, '../quiz.html'), 'utf-8');

        expect(quizPage).toContain('id="nextQuizLink"');
    });
});
