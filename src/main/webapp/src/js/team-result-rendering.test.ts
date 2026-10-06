import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {describe, expect, it} from 'vitest';

describe('team result rendering', () => {
    it('uses a fixed mobile-safe table with an unlabeled details column', async () => {
        const page = await readFile(path.resolve(__dirname, '../team.html'), 'utf-8');

        expect(page).toContain('class="w-full table-fixed bg-white rounded-lg shadow-sm"');
        expect(page).not.toContain('overflow-x-auto');
        expect(page).not.toContain('min-w-[320px]');
        expect(page).not.toContain('>Details</th>');
        expect(page).toContain('<th class="w-16');
        expect(page).toContain('<th class="w-14');
    });

    it('renders expanded results as point values without a question-number header', async () => {
        const source = await readFile(path.resolve(__dirname, 'team.ts'), 'utf-8');

        expect(source).toContain(".map(a => `<td class=\"py-2 px-3 text-center text-sm sm:text-base font-medium\">${a.points}</td>`)");
        expect(source).not.toContain('numberBadge');
        expect(source).not.toContain('<thead>');
    });

    it('renders quiz title links with the established medium weight', async () => {
        const source = await readFile(path.resolve(__dirname, 'team.ts'), 'utf-8');

        expect(source).toContain('class="text-blue-600 hover:underline font-medium">${escapeHtml(entry.quizTitle)}</a>');
    });
});
