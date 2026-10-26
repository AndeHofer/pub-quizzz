import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {describe, expect, it} from 'vitest';

const publicPages = [
    '/index.html',
    '/quizzes.html',
    '/quiz.html',
    '/quiz-details.html',
    '/points-leaderboard.html',
    '/medal-leaderboard.html',
    '/average-leaderboard.html',
    '/top-results-leaderboard.html',
    '/team.html',
    '/rules.html'
];

const adminPages = [
    '/admin/admin_main.html',
    '/admin/theme_preview.html'
];

describe('public theme loader markup', () => {
    it('includes public theme initialization script on all public pages', async () => {
        const pages = await Promise.all(publicPages.map(readPage));

        pages.forEach(page => {
            expect(page).toContain('public_theme_init.ts');
        });
    });

    it('bootstraps the saved theme before loading styles on every public page', async () => {
        const pages = await Promise.all(publicPages.map(readPage));

        pages.forEach(page => {
            const bootstrapPosition = page.indexOf('src="/public-theme-bootstrap.js"');
            const stylesheetPosition = page.indexOf('rel="stylesheet"');

            expect(page).toContain('<html lang="de-AT" style="background-color:var(--pq-page-bg,#f9fafb)">');
            expect(bootstrapPosition).toBeGreaterThan(-1);
            expect(stylesheetPosition).toBeGreaterThan(bootstrapPosition);
        });
    });

    it('keeps the early theme logic in one shared bootstrap file', async () => {
        const bootstrap = await readFile(
            path.resolve(__dirname, '../../public/public-theme-bootstrap.js'),
            'utf-8'
        );

        expect(bootstrap).toContain('pub-quizzz.theme-preview');
        expect(bootstrap).toContain('pub-quizzz.resolved-theme');
        expect(bootstrap).toContain('pub-quizzz.motif-layout');
    });

    it('does not include public theme initialization script on admin pages', async () => {
        const pages = await Promise.all(adminPages.map(readPage));

        pages.forEach(page => {
            expect(page).not.toContain('public_theme_init.ts');
            expect(page).not.toContain('public-theme-bootstrap.js');
        });
    });
});

async function readPage(webPath: string): Promise<string> {
    const fileName = webPath.startsWith('/') ? webPath.slice(1) : webPath;
    const absolutePath = path.resolve(__dirname, '../', fileName);
    return readFile(absolutePath, 'utf-8');
}
