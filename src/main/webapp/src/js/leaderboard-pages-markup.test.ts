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

    it('links every leaderboard to the next leaderboard', async () => {
        const pages = await Promise.all([
            readPage('/points-leaderboard.html'),
            readPage('/medal-leaderboard.html'),
            readPage('/average-leaderboard.html'),
            readPage('/top-results-leaderboard.html'),
        ]);

        const nextPageUrls = [
            'medal-leaderboard.html',
            'average-leaderboard.html',
            'top-results-leaderboard.html',
            'points-leaderboard.html',
        ];

        pages.forEach((page, index) => {
            expect(page).toContain('id="nextLeaderboardLink"');
            expect(page).toContain(`href="${nextPageUrls[index]}"`);
        });
    });

    it('explains the minimum participation requirement on the average leaderboard', async () => {
        const page = await readPage('/average-leaderboard.html');

        expect(page).toContain('mindestens zwei Teilnahmen im ausgewählten Zeitraum');
        expect(page).not.toContain('Hat in einem Jahr kein Team zwei Teilnahmen');
    });

    it('provides a separate empty state for the average leaderboard', async () => {
        const page = await readPage('/average-leaderboard.html');
        const script = await readPage('/js/average-leaderboard.ts');

        expect(page).toContain('id="emptyLeaderboard"');
        expect(script).toContain("emptyStateId: 'emptyLeaderboard'");
        expect(script).toContain('Keine Teams mit mindestens zwei Teilnahmen in diesem Zeitraum.');
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
