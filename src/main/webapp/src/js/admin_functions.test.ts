import {describe, expect, it} from 'vitest';
import {renderTable, showError, trustedHtml} from './admin_functions';

describe('admin_functions rendering helpers', () => {
    it('escapes untrusted table headers and cell content', () => {
        const markup = renderTable(
            ['<script>alert(1)</script>'],
            [{name: '<img src=x onerror=alert(1)>'}],
            row => [String((row as { name: string }).name)]
        );

        expect(markup).not.toContain('<script>alert(1)</script>');
        expect(markup).not.toContain('<img src=x onerror=alert(1)>');
        expect(markup).toContain('&lt;script&gt;alert(1)&lt;/script&gt;');
        expect(markup).toContain('&lt;img src=x onerror=alert(1)&gt;');
    });

    it('keeps explicitly trusted action html unescaped', () => {
        const markup = renderTable(
            ['Aktionen'],
            [{id: 1}],
            () => [trustedHtml('<button class="x">OK</button>')]
        );

        expect(markup).toContain('<button class="x">OK</button>');
    });

    it('escapes error message content', () => {
        const markup = showError('<script>alert(1)</script>');
        expect(markup).not.toContain('<script>alert(1)</script>');
        expect(markup).toContain('&lt;script&gt;alert(1)&lt;/script&gt;');
    });
});
