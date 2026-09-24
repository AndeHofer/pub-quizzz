import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {describe, expect, it} from 'vitest';

describe('admin theme preview page markup', () => {
    it('contains a registry-driven preview table target without hard-coded theme buttons', async () => {
        const html = await readFile(path.resolve(__dirname, '../admin/theme_preview.html'), 'utf-8');

        expect(html).toContain('Designvorschau');
        expect(html).toContain('themePreviewTableBody');
        expect(html).toContain('Designvorschauen werden geladen...');
        expect(html).toContain('für öffentliche Seiten');
        expect(html).toContain('bleiben unverändert');
        expect(html).not.toContain('fuer oeffentliche Seiten');
        expect(html).not.toContain('bleiben unveraendert');
        expect(html).not.toContain('data-theme-id="halloween"');
        expect(html).not.toContain('data-theme-id="december"');
    });
});
