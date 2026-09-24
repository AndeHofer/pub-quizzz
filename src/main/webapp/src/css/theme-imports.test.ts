import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {describe, expect, it} from 'vitest';

const cssDirectory = path.resolve(__dirname);

describe('theme CSS structure', () => {
    it('imports isolated shared tokens and individual theme files from the base stylesheet', async () => {
        const styles = await readFile(path.join(cssDirectory, 'styles.css'), 'utf-8');

        expect(styles).toContain('@import "./themes/tokens.css";');
        expect(styles).toContain('@import "./themes/november.css";');
        expect(styles).toContain('@import "./themes/december.css";');
        expect(styles).toContain('@import "./themes/january.css";');
        expect(styles).toContain('@import "./themes/halloween.css";');
    });

    it('keeps each seasonal selector in its own theme file', async () => {
        await expectThemeFile('november.css', 'html[data-theme="november"]');
        await expectThemeFile('december.css', 'html[data-theme="december"]');
        await expectThemeFile('january.css', 'html[data-theme="january"]');
        await expectThemeFile('halloween.css', 'html[data-theme="halloween"]');
    });

    it('adds a shared non-standard decoration layer inside the public container', async () => {
        const tokens = await readFile(path.join(cssDirectory, 'themes', 'tokens.css'), 'utf-8');

        expect(tokens).toContain('html[data-theme]:not([data-theme="standard"]) body > .container::before');
        expect(tokens).toContain('html[data-theme]:not([data-theme="standard"]) body > .container::after');
        expect(tokens).toContain('pointer-events: none;');
        expect(tokens).toContain('z-index: 2;');
        expect(tokens).toContain('padding-top: 0;');
    });

    it('configures a Unicode motif and CSS atmosphere per seasonal theme', async () => {
        await expectThemeFile('november.css', '--pq-decoration-primary-motifs:');
        await expectThemeFile('november.css', '🍂');
        await expectThemeFile('december.css', '🌲');
        await expectThemeFile('january.css', '◇');
        await expectThemeFile('halloween.css', '🎃');
    });

    it('uses explicit high-contrast halloween text and field colors', async () => {
        const halloween = await readFile(path.join(cssDirectory, 'themes', 'halloween.css'), 'utf-8');

        expect(halloween).toContain('--pq-text-main: #fffaff;');
        expect(halloween).toContain('input[type="text"]');
        expect(halloween).toContain('background-color: #1d1726;');
    });

    it('maps gray-900 dynamic score text to the active theme primary color', async () => {
        const tokens = await readFile(path.join(cssDirectory, 'themes', 'tokens.css'), 'utf-8');

        expect(tokens).toContain('html[data-theme] .text-gray-900,');
    });

    it('adds an equally configured desktop body decoration below the public container', async () => {
        const tokens = await readFile(path.join(cssDirectory, 'themes', 'tokens.css'), 'utf-8');

        expect(tokens).toContain('html[data-theme]:not([data-theme="standard"]) body::before');
        expect(tokens).toContain('html[data-theme]:not([data-theme="standard"]) body::after');
        expect(tokens).toContain('html[data-theme]:not([data-theme="standard"]) body > .container {');
        expect(tokens).toContain('z-index: 2;');
        expect(tokens).toContain('display: none;');
    });

    it('renders distinct primary and secondary motif clusters with a compact mobile primary cluster', async () => {
        const tokens = await readFile(path.join(cssDirectory, 'themes', 'tokens.css'), 'utf-8');

        expect(tokens).toContain('content: var(--pq-decoration-primary-motifs);');
        expect(tokens).toContain('content: var(--pq-decoration-secondary-motifs);');
        expect(tokens).toContain('inset: var(--pq-decoration-primary-position);');
        expect(tokens).toContain('inset: var(--pq-decoration-secondary-position);');
        expect(tokens).toContain('content: var(--pq-decoration-body-primary-motifs);');
        expect(tokens).toContain('content: var(--pq-decoration-body-secondary-motifs);');
        expect(tokens).toContain('body > .container::after {\n        display: none;');
        expect(tokens).toContain('font-size: var(--pq-decoration-primary-mobile-size);');
        expect(tokens).toContain('opacity: var(--pq-decoration-primary-mobile-opacity);');
    });

    it('gives every non-standard theme distinct primary and secondary motif mixes', async () => {
        const themes = [
            ['january', '❄', '⌁'], ['february', '☾', '♡'], ['march', '🌱', '🌦'], ['april', '☔', '🌷'],
            ['may', '✿', '☘'], ['june', '☀', '🌿'], ['july', '☀', '⛱'], ['august', '🌾', '🍑'],
            ['september', '🍃', '🌻'], ['october', '🍂', '🌫'], ['november', '🍁', '☕'], ['december', '🌲', '☾'],
            ['easter', '🐣', '🐇'], ['halloween', '🎃', '🕸'], ['christmas', '🎄', '🔔'], ['new-year', '🎆', '🎉']
        ] as const;

        for (const [themeId, primaryMotif, secondaryMotif] of themes) {
            const themeCss = await readFile(path.join(cssDirectory, 'themes', `${themeId}.css`), 'utf-8');
            expect(themeCss).toContain('--pq-decoration-primary-motifs:');
            expect(themeCss).toContain('--pq-decoration-secondary-motifs:');
            expect(themeCss).toContain(primaryMotif);
            expect(themeCss).toContain(secondaryMotif);
        }
    });

    it('imports and configures every remaining month and event theme', async () => {
        const styles = await readFile(path.join(cssDirectory, 'styles.css'), 'utf-8');
        const themes = [
            ['february', '☾'], ['march', '🌱'], ['april', '☔'], ['may', '✿'],
            ['june', '☀'], ['july', '≈'], ['august', '🌾'], ['september', '🍃'],
            ['october', '☁'], ['easter', '🐣'], ['christmas', '🎄'], ['new-year', '🎆']
        ] as const;

        for (const [themeId, motif] of themes) {
            expect(styles).toContain(`@import "./themes/${themeId}.css";`);
            await expectThemeFile(`${themeId}.css`, `html[data-theme="${themeId}"]`);
            await expectThemeFile(`${themeId}.css`, motif);
        }
    });
});

async function expectThemeFile(fileName: string, selector: string): Promise<void> {
    const themeCss = await readFile(path.join(cssDirectory, 'themes', fileName), 'utf-8');
    expect(themeCss).toContain(selector);
}
