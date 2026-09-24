import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {describe, expect, it} from 'vitest';

describe('homepage navigation cards', () => {
    it('uses the shared themed background hover for every primary navigation card', async () => {
        const page = await readFile(path.resolve(__dirname, '..', 'index.html'), 'utf-8');

        for (const href of ['./quizzes.html', './quiz-details.html', './rules.html', './admin/admin_main.html']) {
            const card = page.match(new RegExp(`<a[^>]*href="${href}"[^>]*class="([^"]+)"`, 's'));
            expect(card?.[1]).toContain('hover:bg-gray-50');
        }
    });
});
