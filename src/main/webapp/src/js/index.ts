const VERSION_CACHE_KEY = 'pub-quizzz-version-badge';
const IS_ADMIN_CACHE_KEY = 'pub-quizzz-is-admin';

function setAdminCardVisible(isVisible: boolean): void {
    const adminCard = document.getElementById('adminCard') as HTMLAnchorElement | null;
    if (!adminCard) {
        return;
    }

    adminCard.style.display = isVisible ? 'block' : 'none';
}

function getCachedIsAdmin(): boolean | null {
    try {
        const cachedValue = sessionStorage.getItem(IS_ADMIN_CACHE_KEY);
        if (cachedValue === 'true') {
            return true;
        }
        if (cachedValue === 'false') {
            return false;
        }
        return null;
    } catch {
        return null;
    }
}

function cacheIsAdmin(isAdmin: boolean): void {
    try {
        sessionStorage.setItem(IS_ADMIN_CACHE_KEY, String(isAdmin));
    } catch {
        // Ignore storage failures in private/restricted contexts
    }
}

function setVersionBadge(version: string): void {
    const badge = document.getElementById('versionBadge');
    if (!badge) {
        return;
    }

    badge.textContent = version;
}

function getCachedVersion(): string | null {
    try {
        return sessionStorage.getItem(VERSION_CACHE_KEY);
    } catch {
        return null;
    }
}

function cacheVersion(version: string): void {
    try {
        sessionStorage.setItem(VERSION_CACHE_KEY, version);
    } catch {
        // Ignore storage failures in private/restricted contexts
    }
}

const cachedIsAdmin = getCachedIsAdmin();
if (cachedIsAdmin !== null) {
    setAdminCardVisible(cachedIsAdmin);
} else {
    fetch('/api/is-admin')
        .then(response => response.ok ? response.json() : false)
        .then((isAdmin: boolean) => {
            setAdminCardVisible(isAdmin);
            cacheIsAdmin(isAdmin);
        })
        .catch(() => {
            setAdminCardVisible(false);
        });
}

const cachedVersion = getCachedVersion();
if (cachedVersion) {
    setVersionBadge(cachedVersion);
} else {
    fetch('/api/version', {cache: 'no-store'})
        .then(res => res.ok ? res.text() : null)
        .then((version: string | null) => {
            if (!version) {
                return;
            }

            setVersionBadge(version);
            cacheVersion(version);
        })
        .catch(() => {
            // Keep badge empty if request fails
        });
}
