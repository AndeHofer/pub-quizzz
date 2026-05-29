import {describe, expect, it} from 'vitest';
import {buildDocumentListMarkup} from './create_quiz_documents';

describe('buildDocumentListMarkup', () => {
    it('escapes untrusted filenames in text and download attribute', () => {
        const markup = buildDocumentListMarkup(42, [
            {
                id: 7,
                quizId: 42,
                originalFilename: 'evil" onmouseover="alert(1).pdf<script>alert(2)</script>',
                fileSize: 1234,
                contentType: 'application/pdf',
                uploadedAt: '2026-01-01T00:00:00Z'
            }
        ]);

        expect(markup).not.toContain('onmouseover="alert(1)');
        expect(markup).not.toContain('<script>alert(2)</script>');
        expect(markup).toContain('evil&quot; onmouseover=&quot;alert(1).pdf&lt;script&gt;alert(2)&lt;/script&gt;');
    });
});
