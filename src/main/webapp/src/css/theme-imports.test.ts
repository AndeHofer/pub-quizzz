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

    it('defines explicit high-contrast halloween text and field tokens', async () => {
        const halloween = await readFile(path.join(cssDirectory, 'themes', 'halloween.css'), 'utf-8');

        expect(halloween).toContain('--pq-text-main: #fffaff;');
        expect(halloween).toContain('--pq-control-bg: #1d1726;');
        expect(halloween).toContain('--pq-control-text: #fffaff;');
    });

    it('maps gray-900 dynamic score text to the active theme primary color', async () => {
        const tokens = await readFile(path.join(cssDirectory, 'themes', 'tokens.css'), 'utf-8');

        expect(tokens).toContain('html[data-theme] .text-gray-900,');
    });

    it('maps public links, callouts, modals, and form controls through theme tokens', async () => {
        const tokens = await readFile(path.join(cssDirectory, 'themes', 'tokens.css'), 'utf-8');

        expect(tokens).toContain('--pq-link:');
        expect(tokens).toContain('html[data-theme] .text-blue-600,');
        expect(tokens).toContain('html[data-theme] .bg-yellow-50,');
        expect(tokens).toContain('html[data-theme] .modal-content');
        expect(tokens).toContain('html[data-theme] input[type="text"]');
        expect(tokens).toContain('color: var(--pq-control-placeholder);');
    });

    it('limits global table-row hover feedback to hover-capable devices', async () => {
        const styles = await readFile(path.join(cssDirectory, 'styles.css'), 'utf-8');

        expect(styles).toMatch(/@media \(hover: hover\) \{\r?\n\s+tr:hover \{/);
    });

    it('uses the theme interaction background for global table-row hover feedback', async () => {
        const styles = await readFile(path.join(cssDirectory, 'styles.css'), 'utf-8');

        expect(styles).toMatch(/tr:hover \{\r?\n\s+background-color: var\(--pq-highlight-bg\);/);
    });

    it('maps neutral hover utilities to the active theme interaction background', async () => {
        const tokens = await readFile(path.join(cssDirectory, 'themes', 'tokens.css'), 'utf-8');

        expect(tokens).toContain('html[data-theme] .hover\\:bg-gray-50:hover,');
        expect(tokens).toContain('html[data-theme] .hover\\:bg-gray-100:hover,');
        expect(tokens).toContain('html[data-theme] .hover\\:bg-gray-200:hover {');
        expect(tokens).toContain('background-color: var(--pq-highlight-bg) !important;');
    });

    it('defines shared contrast tokens for every dark public theme', async () => {
        for (const themeId of ['february', 'halloween', 'christmas', 'new-year']) {
            const themeCss = await readFile(path.join(cssDirectory, 'themes', `${themeId}.css`), 'utf-8');

            expect(themeCss).toContain('--pq-link:');
            expect(themeCss).toContain('--pq-callout-bg:');
            expect(themeCss).toContain('--pq-control-bg:');
        }
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
        expect(tokens).toMatch(/body > \.container::after \{\r?\n\s+display: none;/);
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

    it('keeps motif repetitions out of individual corner clusters', async () => {
        const themeIds = [
            'january', 'february', 'march', 'april', 'may', 'june', 'july', 'august', 'september', 'october',
            'november', 'december', 'easter', 'halloween', 'christmas', 'new-year'
        ];

        for (const themeId of themeIds) {
            const themeCss = await readFile(path.join(cssDirectory, 'themes', `${themeId}.css`), 'utf-8');
            expect(motifClusterHasUniqueSymbols(themeCss, 'primary')).toBe(true);
            expect(motifClusterHasUniqueSymbols(themeCss, 'secondary')).toBe(true);
        }
    });

    it('places the desktop body secondary cluster away from the container secondary cluster', async () => {
        const tokens = await readFile(path.join(cssDirectory, 'themes', 'tokens.css'), 'utf-8');

        expect(tokens).toContain('--pq-decoration-body-secondary-position: auto auto 10% 3%;');
        expect(tokens).toContain('--pq-decoration-secondary-position: auto -3% 3% auto;');
    });

    it('uses a stronger January decoration palette on pale surfaces', async () => {
        const january = await readFile(path.join(cssDirectory, 'themes', 'january.css'), 'utf-8');

        expect(january).toContain('--pq-decoration-color: #76b4dc;');
        expect(january).toContain('--pq-decoration-opacity: 0.36;');
        expect(january).toContain('--pq-decoration-mobile-opacity: 0.28;');
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

function motifClusterHasUniqueSymbols(themeCss: string, cluster: 'primary' | 'secondary'): boolean {
    const match = themeCss.match(new RegExp(`--pq-decoration-${cluster}-motifs: '([^']+)';`));
    const symbols = match?.[1].split(/\s+|\\A/).filter(Boolean) ?? [];
    return symbols.length > 0 && new Set(symbols).size === symbols.length;
}
