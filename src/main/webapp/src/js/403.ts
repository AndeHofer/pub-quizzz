function readCookieValue(doc: Document, name: string): string | null {
    const cookies = (doc.cookie ?? '').split(';');
    for (const rawCookie of cookies) {
        const cookie = rawCookie.trim();
        if (!cookie.startsWith(`${name}=`)) {
            continue;
        }
        return decodeURIComponent(cookie.substring(name.length + 1));
    }
    return null;
}

export function wireReloginForm(doc: Document = document): void {
    const csrfTokenInput = doc.getElementById('reloginCsrfToken') as HTMLInputElement | null;
    if (!csrfTokenInput) {
        return;
    }

    csrfTokenInput.value = readCookieValue(doc, 'XSRF-TOKEN') ?? '';
}

if (typeof document !== 'undefined') {
    wireReloginForm(document);
}
