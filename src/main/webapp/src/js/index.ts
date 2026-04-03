fetch('/api/is-admin')
    .then(response => response.json())
    .then((isAdmin: boolean) => {
        const adminCard = document.getElementById('adminCard') as HTMLAnchorElement | null;
        if (isAdmin && adminCard) {
            adminCard.style.display = 'block';
        }
    })
    .catch(() => {
        // Admin card remains hidden if request fails — no action needed
    });

fetch('/api/version')
    .then(res => res.ok ? res.json() : null)
    .then((data: { version: string } | null) => {
        if (!data) return;
        const badge = document.getElementById('versionBadge');
        if (badge) badge.textContent = data.version;
    })
    .catch(() => { /* silent */
    });
