import {escapeHtml} from './html-utils';
import {
    THEME_OPTIONS,
    type ThemeId,
    normalizeThemeId,
    readThemePreview,
    saveThemePreview
} from './theme-preview';

export function buildThemePreviewRowsMarkup(selectedTheme: ThemeId): string {
    return THEME_OPTIONS.map(theme => {
        const isActive = theme.id === selectedTheme;
        const status = isActive ? 'Aktiv' : 'Nicht aktiv';
        const statusClass = isActive ? 'text-green-700 font-semibold' : 'text-gray-500';
        const actionLabel = isActive ? 'Erneut setzen' : 'Auswählen';

        return `<tr>
            <td>${escapeHtml(theme.label)}</td>
            <td class="${statusClass}">${status}</td>
            <td><button type="button" class="secondary-btn my-0" data-theme-id="${theme.id}">${actionLabel}</button></td>
        </tr>`;
    }).join('');
}

export function getSelectionMessage(selectedTheme: ThemeId): string {
    if (selectedTheme === 'standard') {
        return 'Keine Designvorschau aktiv. Öffentliche Seiten verwenden die automatische Auswahl.';
    }

    const selected = THEME_OPTIONS.find(theme => theme.id === selectedTheme) ?? THEME_OPTIONS[0];
    return `Designvorschau aktiv: ${selected.label}.`;
}

export function initAdminThemePreviewPage(doc: Document = document, storage: Storage = sessionStorage): void {
    const tableBody = doc.getElementById('themePreviewTableBody');
    const message = doc.getElementById('themePreviewMessage');
    if (!tableBody || !message) {
        return;
    }

    const render = () => {
        const selected = readThemePreview(storage);
        tableBody.innerHTML = buildThemePreviewRowsMarkup(selected);
        message.textContent = getSelectionMessage(selected);

        tableBody.querySelectorAll<HTMLButtonElement>('button[data-theme-id]').forEach(button => {
            button.addEventListener('click', () => {
                const themeId = normalizeThemeId(button.dataset.themeId ?? null);
                saveThemePreview(storage, themeId);
                render();
            });
        });
    };

    render();
}

if (typeof document !== 'undefined' && typeof sessionStorage !== 'undefined') {
    initAdminThemePreviewPage(document, sessionStorage);
}
