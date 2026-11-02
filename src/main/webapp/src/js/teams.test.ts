import {describe, expect, it, vi} from 'vitest';
import {loadTeams, renderTeamsMarkup, sortTeamsAlphabetically} from './teams';
import type {TeamDTO} from './types';

describe('participating teams directory', () => {
    it('sorts names alphabetically using German collation without changing the source', () => {
        const teams: TeamDTO[] = [
            {teamsId: 1, teamName: 'Zulu'},
            {teamsId: 2, teamName: 'Äpfel'},
            {teamsId: 3, teamName: 'Biene'},
            {teamsId: 4, teamName: 'alpha'},
        ];
        expect(sortTeamsAlphabetically(teams).map(team => team.teamName)).toEqual(['alpha', 'Äpfel', 'Biene', 'Zulu']);
        expect(teams[0].teamName).toBe('Zulu');
    });

    it('links each team to its detail page and escapes names', () => {
        const markup = renderTeamsMarkup([{teamsId: 7, teamName: '<Team & Co>'}]);
        expect(markup).toContain('/team.html?teamId=7');
        expect(markup).toContain('&lt;Team &amp; Co&gt;');
        expect(markup).not.toContain('<Team & Co>');
    });

    it('renders the list after loading teams', async () => {
        const {doc, elements} = fakeDocument();
        const request = vi.fn().mockResolvedValue({ok: true, json: async () => [{teamsId: 7, teamName: 'Alpha'}]});
        await loadTeams(doc, request as typeof fetch);
        expect(request).toHaveBeenCalledWith('/api/teams');
        expect(elements.teamsList.innerHTML).toContain('/team.html?teamId=7');
        expect(elements.teamsList.style.display).toBe('block');
        expect(elements.loading.style.display).toBe('none');
    });

    it('shows an empty message when no team has participated', async () => {
        const {doc, elements} = fakeDocument();
        const request = vi.fn().mockResolvedValue({ok: true, json: async () => []});
        await loadTeams(doc, request as typeof fetch);
        expect(elements.emptyState.style.display).toBe('block');
        expect(elements.teamsList.style.display).not.toBe('block');
        expect(elements.loading.style.display).toBe('none');
    });

    it('shows an error when the request fails', async () => {
        const {doc, elements} = fakeDocument();
        const request = vi.fn().mockResolvedValue({ok: false, status: 500});
        await loadTeams(doc, request as typeof fetch);
        expect(elements.errorMessage.textContent).toContain('Fehler beim Laden der Teams');
        expect(elements.errorMessage.style.display).toBe('block');
        expect(elements.loading.style.display).toBe('none');
    });
});

function fakeDocument() {
    const makeElement = () => ({style: {display: ''}, innerHTML: '', textContent: ''});
    const elements: Record<string, ReturnType<typeof makeElement>> = {
        loading: makeElement(), errorMessage: makeElement(), emptyState: makeElement(), teamsList: makeElement(),
    };
    const doc = {getElementById: (id: string) => elements[id] ?? null} as unknown as Document;
    return {doc, elements};
}
