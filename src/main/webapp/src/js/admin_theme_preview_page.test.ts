import {describe, expect, it, vi} from 'vitest';
import {buildThemePreviewRowsMarkup, getSelectionMessage, initAdminThemePreviewPage} from './admin_theme_preview_page';

describe('admin theme preview helpers', () => {
    it('renders all selectable themes', () => {
        const html = buildThemePreviewRowsMarkup('standard');

        expect(html).toContain('Standard');
        expect(html).toContain('November');
        expect(html).toContain('Dezember');
        expect(html).toContain('Jänner');
        expect(html).toContain('Halloween');
    });

    it('does not render theme descriptions in preview rows', () => {
        const html = buildThemePreviewRowsMarkup('standard');

        expect(html).not.toContain('Aktuelles neutrales Design');
        expect(html).not.toContain('Nacht und Kürbisse');
    });

    it('marks selected theme as active', () => {
        const html = buildThemePreviewRowsMarkup('november');

        expect(html).toContain('data-theme-id="november"');
        expect(html).toContain('Aktiv');
    });

    it('returns a german confirmation message', () => {
        expect(getSelectionMessage('halloween')).toContain('Designvorschau aktiv: Halloween.');
    });

    it('explains that Standard clears the preview override', () => {
        expect(getSelectionMessage('standard'))
            .toBe('Keine Designvorschau aktiv. Öffentliche Seiten verwenden die automatische Auswahl.');
    });
});

describe('initAdminThemePreviewPage', () => {
    it('renders initial table from storage and updates after button click', () => {
        type Listener = () => void;
        type FakeButton = {
            dataset: { themeId: string };
            addEventListener: (event: string, listener: Listener) => void;
            click: () => void;
        };

        const listeners = new Map<FakeButton, Listener>();
        const createButton = (themeId: string): FakeButton => {
            const button: FakeButton = {
                dataset: {themeId},
                addEventListener: (_event, listener) => {
                    listeners.set(button, listener);
                },
                click: () => {
                    const listener = listeners.get(button);
                    if (listener) {
                        listener();
                    }
                }
            };
            return button;
        };

        const novemberButton = createButton('november');
        const standardButton = createButton('standard');

        const tableBody = {
            innerHTML: '',
            querySelectorAll: vi.fn(() => [standardButton, novemberButton])
        };

        const message = {textContent: ''};

        const documentMock = {
            getElementById: (id: string) => {
                if (id === 'themePreviewTableBody') {
                    return tableBody;
                }
                if (id === 'themePreviewMessage') {
                    return message;
                }
                return null;
            }
        } as unknown as Document;

        const store = new Map<string, string>();
        const storage = {
            getItem: (key: string) => store.get(key) ?? null,
            setItem: (key: string, value: string) => {
                store.set(key, value);
            },
            removeItem: (key: string) => {
                store.delete(key);
            }
        };

        initAdminThemePreviewPage(documentMock, storage as unknown as Storage);

        expect(message.textContent).toBe('Keine Designvorschau aktiv. Öffentliche Seiten verwenden die automatische Auswahl.');

        novemberButton.click();

        expect(message.textContent).toBe('Designvorschau aktiv: November.');
        expect(store.get('pub-quizzz.theme-preview')).toBe('november');
    });

    it('does nothing when required elements are missing', () => {
        const documentMock = {
            getElementById: vi.fn(() => null)
        } as unknown as Document;

        const storage = {
            getItem: vi.fn(),
            setItem: vi.fn(),
            removeItem: vi.fn()
        };

        initAdminThemePreviewPage(documentMock, storage as unknown as Storage);

        expect(storage.getItem).not.toHaveBeenCalled();
        expect(storage.setItem).not.toHaveBeenCalled();
    });
});
