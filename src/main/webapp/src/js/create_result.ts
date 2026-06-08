import {goBack, showMessage} from './utils';
import type {QuizDTO, ResultDTO, TeamDTO} from './types';
import {quizDisplayTitle, sortQuizzesNewestFirst} from './quiz-utils';
import {getApiFetch} from './admin-api-loader';

type PointsValue = 0 | 1 | 2 | 3 | 5;

const ALLOWED_POINTS: PointsValue[] = [5, 3, 2, 1, 0];
const QUESTION_COUNT = 8;

const quizSelect = document.getElementById('resultQuiz') as HTMLSelectElement | null;
const teamSelect = document.getElementById('resultTeam') as HTMLSelectElement | null;
const resultQuestionsContainer = document.getElementById('resultQuestionsContainer') as HTMLDivElement | null;
const resultSaveBtn = document.getElementById('resultSaveBtn') as HTMLButtonElement | null;
const backToResultsBtn = document.getElementById('backToResultsBtn') as HTMLButtonElement | null;

let allQuizzes: QuizDTO[] = [];
let allTeams: TeamDTO[] = [];
let editingResult: ResultDTO | null = null;

function isAuthExpiredRedirectError(error: unknown): boolean {
    return error instanceof Error && error.message === 'AUTH_EXPIRED_REDIRECT';
}

function pointOptionsHtml(): string {
    return ALLOWED_POINTS.map(value => `<option value="${value}"${value === 0 ? ' selected' : ''}>${value}</option>`).join('');
}

function buildQuestionInput(questionNumber: number): string {
    return `<div class="field-group"><label for="result-q${questionNumber}">Frage ${questionNumber} Punkte:</label><select id="result-q${questionNumber}">${pointOptionsHtml()}</select></div>`;
}

function buildQuestionInputs(): void {
    if (!resultQuestionsContainer) return;
    let leftColumn = '';
    let rightColumn = '';
    for (let i = 1; i <= 4; i++) {
        leftColumn += buildQuestionInput(i);
    }
    for (let i = 5; i <= QUESTION_COUNT; i++) {
        rightColumn += buildQuestionInput(i);
    }
    resultQuestionsContainer.innerHTML = `
        <div class="result-points-grid">
            <div>${leftColumn}</div>
            <div>${rightColumn}</div>
        </div>
    `;
}

function getResultIdFromQuery(): number | null {
    const params = new URLSearchParams(window.location.search);
    const idParam = params.get('resultId');
    if (!idParam) return null;
    const parsed = Number(idParam);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

function readResultsBackLinkFromQuery(): string | null {
    const params = new URLSearchParams(window.location.search);
    if (params.get('from') !== 'results') return null;

    const url = new URL('results.html', window.location.href);
    const quizId = params.get('quizId');
    const team = params.get('team');

    if (quizId && quizId.trim()) {
        url.searchParams.set('quizId', quizId.trim());
    }
    if (team && team.trim()) {
        url.searchParams.set('team', team.trim());
    }

    return `${url.pathname}${url.search}`;
}

async function fetchJson<T>(url: string): Promise<T> {
    const apiFetch = await getApiFetch();
    const response = await apiFetch(url);
    if (!response.ok) {
        const text = await response.text().catch(() => '');
        throw new Error(text || `Fehler beim Laden (${response.status})`);
    }
    return response.json() as Promise<T>;
}

function fillSelects(): void {
    if (!quizSelect || !teamSelect) return;

    const sortedQuizzes = sortQuizzesNewestFirst(allQuizzes);

    quizSelect.innerHTML = '<option value="">-- Quiz auswählen --</option>' +
        sortedQuizzes.map(quiz => `<option value="${quiz.quizId}">${quizDisplayTitle(quiz)}</option>`).join('');
    teamSelect.innerHTML = '<option value="">-- Team auswählen --</option>' +
        allTeams.map(team => `<option value="${team.teamsId}">${team.teamName}</option>`).join('');
}

function setEditModeUi(): void {
    const title = document.getElementById('resultPageTitle');
    if (title) title.textContent = 'Ergebnis bearbeiten';
    if (resultSaveBtn) resultSaveBtn.textContent = 'Speichern';
    if (quizSelect) {
        quizSelect.disabled = true;
        quizSelect.classList.add('locked-select');
    }
    if (teamSelect) {
        teamSelect.disabled = true;
        teamSelect.classList.add('locked-select');
    }
}

function prefillEditValues(result: ResultDTO): void {
    if (quizSelect) quizSelect.value = String(result.quizId);
    if (teamSelect) teamSelect.value = String(result.teamId);

    const answersMap = new Map<number, number>();
    if (Array.isArray(result.answers)) {
        result.answers.forEach(answer => {
            answersMap.set(answer.questionNumber, answer.points);
        });
    }

    for (let i = 1; i <= QUESTION_COUNT; i++) {
        const select = document.getElementById(`result-q${i}`) as HTMLSelectElement | null;
        if (!select) continue;
        const points = answersMap.get(i);
        if (points !== undefined && ALLOWED_POINTS.includes(points as PointsValue)) {
            select.value = String(points);
        } else {
            select.value = '0';
        }
    }
}

function collectAnswers(): { questionNumber: number; points: number }[] {
    const answers: { questionNumber: number; points: number }[] = [];
    for (let i = 1; i <= QUESTION_COUNT; i++) {
        const select = document.getElementById(`result-q${i}`) as HTMLSelectElement | null;
        const raw = Number(select?.value ?? 0);
        const points = ALLOWED_POINTS.includes(raw as PointsValue) ? raw : 0;
        answers.push({questionNumber: i, points});
    }
    return answers;
}

async function saveResult(event: Event): Promise<void> {
    event.preventDefault();
    if (!quizSelect || !teamSelect || !resultSaveBtn) return;

    const quizId = Number(quizSelect.value);
    const teamId = Number(teamSelect.value);
    if (!editingResult && (!quizId || !teamId)) {
        showMessage('Bitte Quiz und Team auswählen.', 'error');
        return;
    }

    const answers = collectAnswers();
    resultSaveBtn.disabled = true;

    try {
        const apiFetch = await getApiFetch();
        const currentEditingResult = editingResult;
        const isEditMode = currentEditingResult !== null;
        const url = isEditMode ? `/admin/results/${currentEditingResult.resultsId}` : '/admin/results';
        const method = isEditMode ? 'PUT' : 'POST';
        const body = isEditMode ? {answers} : {quizId, teamId, answers};

        const response = await apiFetch(url, {
            method,
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });

        if (response.ok) {
            if (!isEditMode) {
                const createdResult = await response.json() as ResultDTO;
                editingResult = createdResult;
                setEditModeUi();
                prefillEditValues(createdResult);
                history.replaceState(null, '', `create_result.html?resultId=${createdResult.resultsId}`);
                showMessage('Ergebnis erfolgreich gespeichert!', 'success');
                return;
            }

            showMessage('Ergebnis erfolgreich aktualisiert!', 'success');
            return;
        }

        const raw = await response.text().catch(() => '');
        let errorMessage = 'Speichern fehlgeschlagen.';
        if (raw) {
            try {
                const payload = JSON.parse(raw) as { error?: string };
                errorMessage = payload.error || raw;
            } catch {
                errorMessage = raw;
            }
        }
        showMessage(errorMessage, 'error');
    } catch (error) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showMessage('Netzwerkfehler: ' + error, 'error');
    } finally {
        resultSaveBtn.disabled = false;
    }
}

async function loadPageData(): Promise<void> {
    buildQuestionInputs();
    const resultId = getResultIdFromQuery();

    const [quizzes, teams] = await Promise.all([
        fetchJson<QuizDTO[]>('/admin/quizzes'),
        fetchJson<TeamDTO[]>('/admin/teams')
    ]);
    allQuizzes = quizzes;
    allTeams = teams;
    fillSelects();

    if (!resultId) return;

    const results = await fetchJson<ResultDTO[]>('/admin/results');
    const result = results.find(r => r.resultsId === resultId);
    if (!result) {
        showMessage('Ergebnis nicht gefunden.', 'error');
        return;
    }

    editingResult = result;
    setEditModeUi();
    prefillEditValues(result);
}

window.addEventListener('load', () => {
    document.getElementById('backBtn')?.addEventListener('click', () => goBack('admin_main.html'));
    const resultsBackLink = readResultsBackLinkFromQuery();
    if (resultsBackLink && backToResultsBtn) {
        backToResultsBtn.style.display = 'inline';
        backToResultsBtn.addEventListener('click', () => goBack(resultsBackLink));
    }
    document.getElementById('resultForm')?.addEventListener('submit', event => {
        void saveResult(event);
    });

    void loadPageData().catch(error => {
        if (error instanceof Error && error.message === 'AUTH_EXPIRED_REDIRECT') {
            return;
        }
        showMessage('Fehler beim Laden der Seite: ' + error, 'error');
    });
});
