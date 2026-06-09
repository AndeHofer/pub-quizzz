import {describe, expect, it} from 'vitest';
import {readFileSync} from 'node:fs';
import {fileURLToPath} from 'node:url';
import {wireReloginForm} from './403';

describe('403 page', () => {
    it('renders relogin link to /login', () => {
        const pagePath = fileURLToPath(new URL('../403.html', import.meta.url));
        const pageHtml = readFileSync(pagePath, 'utf8');
        expect(pageHtml).toContain('id="reloginForm"');
        expect(pageHtml).toContain('method="post"');
        expect(pageHtml).toContain('action="/logout"');
        expect(pageHtml).toContain('id="reloginCsrfToken"');
    });

    it('writes XSRF-TOKEN cookie value into hidden relogin csrf field', () => {
        const hiddenInput = {
            value: ''
        };
        const getElementById = (id: string) => id === 'reloginCsrfToken' ? hiddenInput : null;
        const fakeDocument = {
            cookie: 'foo=bar; XSRF-TOKEN=csrf-403; x=y',
            getElementById
        } as unknown as Document;

        wireReloginForm(fakeDocument);

        expect(hiddenInput.value).toBe('csrf-403');
    });
});
