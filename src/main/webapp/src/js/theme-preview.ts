export type ThemeId =
    | 'standard'
    | 'january'
    | 'february'
    | 'march'
    | 'april'
    | 'may'
    | 'june'
    | 'july'
    | 'august'
    | 'september'
    | 'october'
    | 'november'
    | 'december'
    | 'easter'
    | 'halloween'
    | 'christmas'
    | 'new-year';

export type ThemeOption = {
    id: ThemeId;
    label: string;
    description: string;
};

export const DEFAULT_THEME_ID: ThemeId = 'standard';
export const THEME_STORAGE_KEY = 'pub-quizzz.theme-preview';

export const THEME_OPTIONS: ReadonlyArray<ThemeOption> = [
    {id: 'standard', label: 'Standard', description: 'Neutrales Design'},
    {id: 'january', label: 'Jänner', description: 'Eis und Bergwinter'},
    {id: 'february', label: 'Februar', description: 'Winterhimmel mit Mondlicht'},
    {id: 'march', label: 'März', description: 'Früher Frühling und neue Triebe'},
    {id: 'april', label: 'April', description: 'Regen und Aufbruch'},
    {id: 'may', label: 'Mai', description: 'Blüte und Wiese'},
    {id: 'june', label: 'Juni', description: 'Frühsommerabend'},
    {id: 'july', label: 'Juli', description: 'Hochsommer'},
    {id: 'august', label: 'August', description: 'Ernte und Spätsommer'},
    {id: 'september', label: 'September', description: 'Früher Herbst'},
    {id: 'october', label: 'Oktober', description: 'Nebel und Spätherbst'},
    {id: 'november', label: 'November', description: 'Ruhiges Spaetherbst-Design'},
    {id: 'december', label: 'Dezember', description: 'Winterwald mit Mondlicht'},
    {id: 'easter', label: 'Ostern', description: 'Helles Frühlingsfest'},
    {id: 'halloween', label: 'Halloween', description: 'Nacht und Kürbisse'},
    {id: 'christmas', label: 'Weihnachten', description: 'Festlicher Winterabend'},
    {id: 'new-year', label: 'Neujahr', description: 'Mitternacht und Aufbruch'}
];

const THEME_ID_SET = new Set<ThemeId>(THEME_OPTIONS.map(option => option.id));

type PreviewStorage = Pick<Storage, 'getItem' | 'setItem' | 'removeItem'>;

export function isThemeId(value: string): value is ThemeId {
    return THEME_ID_SET.has(value as ThemeId);
}

export function normalizeThemeId(value: string | null | undefined): ThemeId {
    if (!value) {
        return DEFAULT_THEME_ID;
    }
    return isThemeId(value) ? value : DEFAULT_THEME_ID;
}

export function readThemePreview(storage: Pick<PreviewStorage, 'getItem'>): ThemeId {
    return normalizeThemeId(storage.getItem(THEME_STORAGE_KEY));
}

export function readThemePreviewOverride(storage: Pick<PreviewStorage, 'getItem'>): ThemeId | null {
    const storedTheme = storage.getItem(THEME_STORAGE_KEY);
    if (!storedTheme) {
        return null;
    }

    const themeId = normalizeThemeId(storedTheme);
    return themeId === DEFAULT_THEME_ID ? null : themeId;
}

export function saveThemePreview(storage: Pick<PreviewStorage, 'setItem' | 'removeItem'>, themeId: ThemeId): void {
    if (themeId === DEFAULT_THEME_ID) {
        storage.removeItem(THEME_STORAGE_KEY);
        return;
    }
    storage.setItem(THEME_STORAGE_KEY, themeId);
}

export function clearThemePreview(storage: Pick<PreviewStorage, 'removeItem'>): void {
    storage.removeItem(THEME_STORAGE_KEY);
}

export function applyThemeToDocument(doc: Document, themeId: ThemeId): void {
    doc.documentElement.setAttribute('data-theme', themeId);
}
