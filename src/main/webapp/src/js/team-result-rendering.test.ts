import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {describe, expect, it} from 'vitest';

describe('team result rendering', () => {
    it('renders expanded results as point values without a question-number header', async () => {
        const source = await readFile(path.resolve(__dirname, 'team.ts'), 'utf-8');

        expect(source).toContain(".map(a => `<td class=\"py-2 px-3 text-center text-sm sm:text-base font-medium\">${a.points}</td>`)");
        expect(source).not.toContain('numberBadge');
        expect(source).not.toContain('<thead>');
    });
});
