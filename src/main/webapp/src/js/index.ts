import {buildVersionBadgeMarkup} from './version-badge';
import type {NewsDTO} from './types';
import {renderNewsError, renderNewsSection} from './news';

function setAdminCardVisible(isVisible: boolean): void {
    const adminCard = document.getElementById('adminCard') as HTMLAnchorElement | null;
    if (!adminCard) {
        return;
    }

    adminCard.style.display = isVisible ? 'block' : 'none';
}

function setVersionBadge(version: string): void {
    const badge = document.getElementById('versionBadge');
    if (!badge) {
        return;
    }

    badge.innerHTML = buildVersionBadgeMarkup(version);
}

function getNewsContainer(): HTMLElement | null {
    return document.getElementById('newsList');
}

async function loadNews(): Promise<void> {
    const container = getNewsContainer();
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

setAdminCardVisible(false);

fetch('/api/bootstrap')
    .then(response => response.ok ? response.json() : null)
    .then((bootstrap: { isAdmin: boolean; version: string } | null) => {
        if (!bootstrap) {
            return;
        }
        setAdminCardVisible(bootstrap.isAdmin);
        setVersionBadge(bootstrap.version);
    })
    .catch(() => {
        setAdminCardVisible(false);
        // Keep badge empty if request fails
    });

void loadNews();
