import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {describe, expect, it} from 'vitest';

describe('leaderboard page markup', () => {
    it('includes the year tabs container on every leaderboard page', async () => {
        const pages = await Promise.all([
            readPage('/points-leaderboard.html'),
            readPage('/average-leaderboard.html'),
            readPage('/medal-leaderboard.html'),
            readPage('/top-results-leaderboard.html'),
        ]);

        pages.forEach(page => {
            expect(page).toContain('id="leaderboardYearTabs"');
        });
    });

    it('overrides the global table top margin so no extra 20px gap appears above the leaderboard table', async () => {
        const pages = await Promise.all([
            readPage('/points-leaderboard.html'),
            readPage('/average-leaderboard.html'),
            readPage('/medal-leaderboard.html'),
            readPage('/top-results-leaderboard.html'),
        ]);

        pages.forEach(page => {
            const tableTagMatch = page.match(/<table id="leaderboardTable"[^>]*>/);
            expect(tableTagMatch).not.toBeNull();
            expect(tableTagMatch![0]).toMatch(/class="[^"]*\bmt-0\b[^"]*"/);
        });
    });
});

async function readPage(path: string): Promise<string> {
    const absolutePath = nodePathFromWebPath(path);
    return readFile(absolutePath, 'utf-8');
}

function nodePathFromWebPath(webPath: string): string {
    const fileName = webPath.startsWith('/') ? webPath.slice(1) : webPath;
    return path.resolve(__dirname, '../', fileName);
}
