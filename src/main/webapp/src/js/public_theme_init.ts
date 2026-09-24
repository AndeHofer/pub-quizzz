import {getViennaCalendarDay, resolveCalendarTheme} from './theme-calendar';
import {applyThemeToDocument, readThemePreviewOverride} from './theme-preview';

export function initPublicTheme(
    doc: Document = document,
    storage: Storage = sessionStorage,
    now: Date = new Date(),
    random: () => number = Math.random
): void {
    const selectedTheme = readThemePreviewOverride(storage) ?? resolveCalendarTheme(getViennaCalendarDay(now));
    applyThemeToDocument(doc, selectedTheme);
    doc.documentElement.setAttribute('data-motif-layout', String(Math.floor(random() * 4) + 1));
}

if (typeof document !== 'undefined' && typeof sessionStorage !== 'undefined') {
    initPublicTheme(document, sessionStorage);
}
