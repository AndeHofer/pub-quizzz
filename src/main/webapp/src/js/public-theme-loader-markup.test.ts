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

    it('does not include public theme initialization script on admin pages', async () => {
        const pages = await Promise.all(adminPages.map(readPage));

        pages.forEach(page => {
            expect(page).not.toContain('public_theme_init.ts');
        });
    });
});

async function readPage(webPath: string): Promise<string> {
    const fileName = webPath.startsWith('/') ? webPath.slice(1) : webPath;
    const absolutePath = path.resolve(__dirname, '../', fileName);
    return readFile(absolutePath, 'utf-8');
}
