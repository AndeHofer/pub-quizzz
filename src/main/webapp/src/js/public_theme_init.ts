import {getViennaCalendarDay, resolveCalendarTheme} from './theme-calendar';
import {applyThemeToDocument, readThemePreviewOverride} from './theme-preview';

export const RESOLVED_THEME_STORAGE_KEY = 'pub-quizzz.resolved-theme';
export const MOTIF_LAYOUT_STORAGE_KEY = 'pub-quizzz.motif-layout';

export function initPublicTheme(
    doc: Document = document,
    storage: Storage | null = getSessionStorage(),
    now: Date = new Date(),
    random: () => number = Math.random
): void {
    let selectedTheme = resolveCalendarTheme(getViennaCalendarDay(now));
    if (storage) {
        try {
            selectedTheme = readThemePreviewOverride(storage) ?? selectedTheme;
        } catch {
            // Keep the calendar theme when browser storage is unavailable.
        }
    }

    applyThemeToDocument(doc, selectedTheme);

    let motifLayout: string | null = null;
    if (storage) {
        try {
            storage.setItem(RESOLVED_THEME_STORAGE_KEY, selectedTheme);
            const storedLayout = storage.getItem(MOTIF_LAYOUT_STORAGE_KEY);
            motifLayout = isMotifLayout(storedLayout) ? storedLayout : null;
        } catch {
            // Apply an in-memory layout when browser storage is unavailable.
        }
    }

    if (!motifLayout) {
        motifLayout = String(Math.min(4, Math.floor(random() * 4) + 1));
        if (storage) {
            try {
                storage.setItem(MOTIF_LAYOUT_STORAGE_KEY, motifLayout);
            } catch {
                // The visual theme still works without persisted storage.
            }
        }
    }

    doc.documentElement.setAttribute('data-motif-layout', motifLayout);
}

function isMotifLayout(value: string | null): boolean {
    return value === '1' || value === '2' || value === '3' || value === '4';
}

function getSessionStorage(): Storage | null {
    try {
        return typeof sessionStorage === 'undefined' ? null : sessionStorage;
    } catch {
        return null;
    }
}

if (typeof document !== 'undefined') {
    initPublicTheme(document);
}
