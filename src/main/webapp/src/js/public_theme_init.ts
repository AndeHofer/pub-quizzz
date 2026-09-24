import {getViennaCalendarDay, resolveCalendarTheme} from './theme-calendar';
import {applyThemeToDocument, readThemePreviewOverride} from './theme-preview';

export function initPublicTheme(
    doc: Document = document,
    storage: Storage = sessionStorage,
    now: Date = new Date()
): void {
    const selectedTheme = readThemePreviewOverride(storage) ?? resolveCalendarTheme(getViennaCalendarDay(now));
    applyThemeToDocument(doc, selectedTheme);
}

if (typeof document !== 'undefined' && typeof sessionStorage !== 'undefined') {
    initPublicTheme(document, sessionStorage);
}
