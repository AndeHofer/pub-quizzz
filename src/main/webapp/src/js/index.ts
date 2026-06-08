import {buildVersionBadgeMarkup} from './version-badge';
import type {NewsDTO} from './types';
import {renderNewsError, renderNewsSection} from './news';
import {triggerRelogin} from './logout-action';

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
    const logoutButton = doc.getElementById('logoutBtn');
    if (!logoutButton) {
        return;
    }

    logoutButton.addEventListener('click', async (event) => {
        event.preventDefault();
        await triggerRelogin();
    });
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
