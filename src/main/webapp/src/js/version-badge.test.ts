import {describe, expect, it} from 'vitest';
import {buildVersionBadgeMarkup} from './version-badge';

describe('buildVersionBadgeMarkup', () => {
    it('renders a version label as GitHub link', () => {
        const markup = buildVersionBadgeMarkup('1.2.3');

        expect(markup).toContain('href="https://github.com/AndeHofer/pub-quizzz"');
        expect(markup).toContain('target="_blank"');
        expect(markup).toContain('rel="noopener noreferrer"');
        expect(markup).toContain('>v1.2.3<');
    });
});
