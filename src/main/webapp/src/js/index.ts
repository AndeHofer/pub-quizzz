import {buildVersionBadgeMarkup} from './version-badge';

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
