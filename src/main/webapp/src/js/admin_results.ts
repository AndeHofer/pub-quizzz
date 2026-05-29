export {};

import type {QuizDTO, ResultDTO} from './types';
import {goBack, showMessage} from './utils';
import {quizDisplayTitle, sortQuizzesNewestFirst} from './quiz-utils';
import {escapeHtml} from './html-utils';
import {withEnsuredCsrfHeaders} from './csrf';

const quizFilterSelect = document.getElementById('resultFilterQuiz') as HTMLSelectElement | null;
const teamFilterSelect = document.getElementById('resultFilterTeam') as HTMLSelectElement | null;
const loadingEl = document.getElementById('loading');
const errorEl = document.getElementById('errorMessage');
const tableEl = document.getElementById('resultsTable') as HTMLTableElement | null;
const tbodyEl = document.getElementById('resultsBody') as HTMLTableSectionElement | null;

let allResults: ResultDTO[] = [];

function parseDateToMillis(value?: string): number | null {
    if (!value) return null;
    const parsed = Date.parse(`${value}T00:00:00Z`);
    return Number.isNaN(parsed) ? null : parsed;
}

function compareResultsNewestFirst(left: ResultDTO, right: ResultDTO): number {
    const leftDate = parseDateToMillis(left.quizDate);
    const rightDate = parseDateToMillis(right.quizDate);

    if (leftDate !== null && rightDate !== null && leftDate !== rightDate) {
        return rightDate - leftDate;
    }
    if (leftDate !== null && rightDate === null) return -1;
    if (leftDate === null && rightDate !== null) return 1;

    const leftTotalPoints = typeof left.totalPoints === 'number' ? left.totalPoints : 0;
    const rightTotalPoints = typeof right.totalPoints === 'number' ? right.totalPoints : 0;
    if (leftTotalPoints !== rightTotalPoints) {
        return rightTotalPoints - leftTotalPoints;
    }

    return right.resultsId - left.resultsId;
}

function currentQuizIdFilter(): string | null {
    if (!quizFilterSelect) return null;
    const value = quizFilterSelect.value.trim();
    return value || null;
}

function currentTeamFilter(): string | null {
    if (!teamFilterSelect) return null;
    const value = teamFilterSelect.value.trim();
    return value || null;
}

function updateUrlFromFilter(): void {
    const url = new URL(window.location.href);
    const quizId = currentQuizIdFilter();
    const team = currentTeamFilter();
    if (quizId) {
        url.searchParams.set('quizId', quizId);
    } else {
        url.searchParams.delete('quizId');
    }
    if (team) {
        url.searchParams.set('team', team);
    } else {
        url.searchParams.delete('team');
    }
    window.history.replaceState({}, '', url.toString());
}

function currentFilterSearchParams(): URLSearchParams {
    const params = new URLSearchParams();
    const quizId = currentQuizIdFilter();
    const team = currentTeamFilter();
    if (quizId) {
        params.set('quizId', quizId);
    }
    if (team) {
        params.set('team', team);
    }
    return params;
}

function readQuizIdFromUrl(): string | null {
    const params = new URLSearchParams(window.location.search);
    const value = params.get('quizId');
    return value && value.trim() ? value.trim() : null;
}

function readTeamFromUrl(): string | null {
    const params = new URLSearchParams(window.location.search);
    const value = params.get('team');
    return value && value.trim() ? value.trim() : null;
}

function setError(message: string): void {
    if (!errorEl) return;
    errorEl.textContent = message;
    errorEl.style.display = 'block';
}

function clearError(): void {
    if (!errorEl) return;
    errorEl.textContent = '';
    errorEl.style.display = 'none';
}

function setLoading(loading: boolean): void {
    if (loadingEl) loadingEl.style.display = loading ? 'block' : 'none';
    if (tableEl) tableEl.style.display = loading ? 'none' : 'table';
}

async function fetchJson<T>(url: string): Promise<T> {
    const response = await fetch(url);
    if (!response.ok) {
        const text = await response.text().catch(() => '');
        throw new Error(text || `HTTP ${response.status}`);
    }
    return response.json() as Promise<T>;
}

function renderRows(results: ResultDTO[]): void {
    if (!tbodyEl) return;

    if (results.length === 0) {
        tbodyEl.innerHTML = '<tr><td colspan="3" class="text-center py-8 text-gray-500">Keine Ergebnisse gefunden.</td></tr>';
        return;
    }

    const sorted = results.slice().sort(compareResultsNewestFirst);
    tbodyEl.innerHTML = sorted.map(result => {
        const answersMap: Record<number, { points: number }> = {};
        if (Array.isArray(result.answers)) {
            result.answers.forEach(answer => {
                answersMap[answer.questionNumber] = answer;
            });
        }

        let questionValueCells = '';
        for (let i = 1; i <= 8; i++) {
            const answer = answersMap[i];
            const points = answer && typeof answer.points === 'number' ? answer.points : 0;
            questionValueCells += `<td class="text-center">${points}</td>`;
        }

        return `
            <tr class="result-group-main">
                <td class="team-cell">${escapeHtml(result.teamName)}</td>
                <td>${escapeHtml(result.quizDate)}</td>
                <td class="text-center actions-cell">
                    <button type="button" class="icon-btn edit-result-btn" data-id="${result.resultsId}" title="Ergebnis bearbeiten">✏️</button>
                    <button type="button" class="icon-btn delete-result-btn" data-id="${result.resultsId}" data-name="${escapeHtml(result.teamName)}" title="Ergebnis löschen">🗑️</button>
                </td>
                <td class="text-center"><strong>${result.totalPoints || 0}</strong></td>
            </tr>
            <tr class="result-group-detail">
                <td colspan="4" class="result-points-cell">
                    <div class="result-points-wrap">
                        <table class="result-points-table">
                            <tbody>
                                <tr>
                                    ${questionValueCells}
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function resultsFilteredByQuiz(results: ResultDTO[]): ResultDTO[] {
    const quizId = currentQuizIdFilter();
    if (!quizId) return results;
    return results.filter(result => String(result.quizId) === quizId);
}

function applyActiveFilters(results: ResultDTO[]): ResultDTO[] {
    const byQuiz = resultsFilteredByQuiz(results);
    const team = currentTeamFilter();
    if (!team) return byQuiz;
    return byQuiz.filter(result => result.teamName === team);
}

function uniqueSortedTeamNames(results: ResultDTO[]): string[] {
    return Array.from(new Set(results.map(result => result.teamName)))
        .sort((left, right) => left.localeCompare(right, 'de-AT'));
}

function rebuildTeamFilterOptions(selectedTeam?: string | null): void {
    if (!teamFilterSelect) return;

    const teamNames = uniqueSortedTeamNames(resultsFilteredByQuiz(allResults));
    teamFilterSelect.innerHTML = '';
    teamFilterSelect.append(new Option('Alle Teams', ''));
    teamNames.forEach(teamName => {
        teamFilterSelect.append(new Option(teamName, teamName));
    });

    if (selectedTeam && teamNames.includes(selectedTeam)) {
        teamFilterSelect.value = selectedTeam;
        return;
    }

    teamFilterSelect.value = '';
}

async function loadResults(): Promise<void> {
    setLoading(true);
    clearError();

    try {
        if (allResults.length === 0) {
            allResults = await fetchJson<ResultDTO[]>('/admin/results');
            rebuildTeamFilterOptions(readTeamFromUrl());
        }

        const filtered = applyActiveFilters(allResults);
        renderRows(filtered);
    } catch (error) {
        setError('Fehler beim Laden der Ergebnisse. Bitte Seite neu laden.');
        if (tbodyEl) {
            tbodyEl.innerHTML = '';
        }
        console.error(error);
    } finally {
        setLoading(false);
    }
}

async function loadQuizFilterOptions(): Promise<void> {
    if (!quizFilterSelect) return;

    const selectedFromUrl = readQuizIdFromUrl();
    const quizzes = await fetchJson<QuizDTO[]>('/admin/quizzes');
    const sorted = sortQuizzesNewestFirst(quizzes);

    quizFilterSelect.innerHTML = '<option value="">Alle Quizze</option>'
        + sorted.map(quiz => `<option value="${quiz.quizId}">${escapeHtml(quizDisplayTitle(quiz))}</option>`).join('');

    if (selectedFromUrl && sorted.some(quiz => String(quiz.quizId) === selectedFromUrl)) {
        quizFilterSelect.value = selectedFromUrl;
    }
}

async function deleteResult(resultId: number, teamName: string): Promise<void> {
    if (!confirm(`Ergebnis für Team "${teamName}" wirklich löschen?`)) return;

    try {
        const response = await fetch(`/admin/results/${resultId}`, {
            method: 'DELETE',
            headers: await withEnsuredCsrfHeaders()
        });
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        allResults = allResults.filter(result => result.resultsId !== resultId);
        rebuildTeamFilterOptions(currentTeamFilter());
        showMessage('Ergebnis gelöscht.', 'success');
        await loadResults();
    } catch (error) {
        showMessage('Fehler beim Löschen des Ergebnisses.', 'error');
        console.error(error);
    }
}

function wireTableActions(): void {
    if (!tbodyEl) return;
    tbodyEl.addEventListener('click', event => {
        const target = event.target as HTMLElement | null;
        if (!target) return;

        const editButton = target.closest('.edit-result-btn') as HTMLButtonElement | null;
        if (editButton) {
            const resultId = Number(editButton.dataset.id);
            if (!Number.isNaN(resultId) && resultId > 0) {
                const params = currentFilterSearchParams();
                params.set('resultId', String(resultId));
                params.set('from', 'results');
                location.href = `create_result.html?${params.toString()}`;
            }
            return;
        }

        const deleteButton = target.closest('.delete-result-btn') as HTMLButtonElement | null;
        if (!deleteButton) return;

        const resultId = Number(deleteButton.dataset.id);
        const teamName = deleteButton.dataset.name ?? '';
        if (!Number.isNaN(resultId) && resultId > 0) {
            void deleteResult(resultId, teamName);
        }
    });
}

function wireFilter(): void {
    quizFilterSelect?.addEventListener('change', () => {
        rebuildTeamFilterOptions(currentTeamFilter());
        updateUrlFromFilter();
        void loadResults();
    });

    teamFilterSelect?.addEventListener('change', () => {
        updateUrlFromFilter();
        void loadResults();
    });
}

window.addEventListener('load', () => {
    document.getElementById('backBtn')?.addEventListener('click', () => goBack('admin_main.html'));
    wireFilter();
    wireTableActions();

    void loadQuizFilterOptions()
        .then(() => {
            rebuildTeamFilterOptions(readTeamFromUrl());
            return loadResults();
        })
        .catch(error => {
            setLoading(false);
            setError('Fehler beim Laden der Seite. Bitte Seite neu laden.');
            console.error(error);
        });
});
