import {buildVersionBadgeMarkup} from './version-badge';
import type {NewsDTO} from './types';
import {renderNewsError, renderNewsSection} from './news';

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

function setAdminCardVisible(isVisible: boolean, doc: Document = document): void {
    const adminCard = doc.getElementById('adminCard') as HTMLAnchorElement | null;
    if (!adminCard) {
        return;
    }

    adminCard.style.display = isVisible ? 'block' : 'none';
}

function setVersionBadge(version: string, doc: Document = document): void {
    const badge = doc.getElementById('versionBadge');
    if (!badge) {
        return;
    }

    badge.innerHTML = buildVersionBadgeMarkup(version);
}

function getNewsContainer(doc: Document = document): HTMLElement | null {
    return doc.getElementById('newsList');
}

async function loadNews(doc: Document = document): Promise<void> {
    const container = getNewsContainer(doc);
    if (!container) {
        return;
    }

    try {
        const response = await fetch('/api/news?limit=3');
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const news = await response.json() as NewsDTO[];
        renderNewsSection(container, news);
    } catch {
        renderNewsError(container);
    }
}

export function wireLogoutButton(doc: Document = document): void {
    const csrfTokenInput = doc.getElementById('logoutCsrfToken') as HTMLInputElement | null;
    if (!csrfTokenInput) {
        return;
    }

    csrfTokenInput.value = readCookieValue(doc, 'XSRF-TOKEN') ?? '';
}

export function initIndex(doc: Document = document): void {
    setAdminCardVisible(false, doc);
    wireLogoutButton(doc);

    fetch('/api/bootstrap')
        .then(response => response.ok ? response.json() : null)
        .then((bootstrap: { isAdmin: boolean; version: string } | null) => {
            if (!bootstrap) {
                return;
            }
            setAdminCardVisible(bootstrap.isAdmin, doc);
            setVersionBadge(bootstrap.version, doc);
        })
        .catch(() => {
            setAdminCardVisible(false, doc);
            // Keep badge empty if request fails
        });

    void loadNews(doc);
}

if (typeof document !== 'undefined') {
    initIndex(document);
}
