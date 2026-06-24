export {};

import type {QuizDTO, TeamDTO, UserDTO} from './types';
import {quizDisplayTitle, sortQuizzesNewestFirst} from './quiz-utils';
import {escapeHtml} from './html-utils';
import {renderTable, showError, showLoading, showModal, trustedHtml} from './admin_ui';
import {readHttpErrorMessage} from './http-utils';
import {apiFetch} from './admin-api';

export {renderTable, showError, trustedHtml};

export async function invalidateCache(
    apiFetchFn: typeof apiFetch,
    confirmFn: (message: string) => boolean,
    msgDiv: HTMLElement | null
) {
    if (!confirmFn('Cache wirklich leeren?')) {
        return;
    }

    setStatusMessage(msgDiv, 'Cache wird geleert...', 'neutral');
    try {
        const response = await apiFetchFn('/admin/cache/invalidate', {method: 'POST'});
        if (response.ok) {
            const payload = await response.json() as { clearedCount?: number };
            const clearedCount = typeof payload.clearedCount === 'number' ? payload.clearedCount : 0;
            setStatusMessage(msgDiv, `Cache geleert (${clearedCount} Cache(s)).`, 'green');
            return;
        }
        const err = await response.json().catch(() => ({error: 'Unbekannter Fehler'}));
        setStatusMessage(msgDiv, `Fehler: ${String(err.error ?? 'Unbekannter Fehler')}`, 'red');
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        setStatusMessage(msgDiv, `Netzwerkfehler: ${error instanceof Error ? error.message : String(error)}`, 'red');
    }
}

function isAuthExpiredRedirectError(error: unknown): boolean {
    return error instanceof Error && error.message === 'AUTH_EXPIRED_REDIRECT';
}

const API_BASE = '/admin';

function closeModal() {
    const modal = document.getElementById('dataModal') as HTMLElement | null;
    if (modal) modal.style.display = 'none';
}

function loadCreateQuizPage(quiz: QuizDTO) {
    sessionStorage.setItem('editingQuiz', JSON.stringify(quiz));
    window.location.href = 'create_quiz.html';
}

function setStatusMessage(msgDiv: HTMLElement | null, text: string, color: 'red' | 'green' | 'neutral') {
    if (!msgDiv) return;

    if (color === 'neutral') {
        msgDiv.style.color = '';
    } else {
        msgDiv.style.color = color;
    }
    msgDiv.textContent = text;
}

// Wire up admin page handlers and modal click-outside behavior
if (typeof window !== 'undefined') {
window.addEventListener('load', () => {
    // Wire up admin_main.html buttons by ID
    document.getElementById('createQuizBtn')?.addEventListener('click', () => {
        location.href = 'create_quiz.html';
    });
    document.getElementById('viewQuizzesBtn')?.addEventListener('click', viewQuizzes);
    document.getElementById('createTeamBtn')?.addEventListener('click', createTeam);
    document.getElementById('viewTeamsBtn')?.addEventListener('click', viewTeams);
    document.getElementById('addResultBtn')?.addEventListener('click', () => {
        location.href = 'create_result.html';
    });
    document.getElementById('viewResultsBtn')?.addEventListener('click', () => {
        location.href = 'results.html';
    });
    document.getElementById('viewLoginStatsBtn')?.addEventListener('click', () => {
        location.href = 'login_stats.html';
    });
    document.getElementById('createUserBtn')?.addEventListener('click', () => {
        location.href = 'register_user.html';
    });
    document.getElementById('viewUsersBtn')?.addEventListener('click', viewUsers);
    document.getElementById('manageNewsBtn')?.addEventListener('click', () => {
        location.href = 'news.html';
    });
    document.getElementById('modalCloseBtn')?.addEventListener('click', closeModal);

    const modal = document.getElementById('dataModal');
    if (modal) {
        window.addEventListener('click', (event: MouseEvent) => {
            if (event.target === modal) closeModal();
        });
    }

    const exportBtn = document.getElementById('exportBackupBtn');
    if (exportBtn) exportBtn.addEventListener('click', exportBackup);

    const importBtn = document.getElementById('importBackupBtn');
    const importForm = document.getElementById('importBackupForm') as HTMLFormElement | null;
    if (importBtn && importForm) {
        importBtn.addEventListener('click', () => {
            importForm.style.display = importForm.style.display === 'none' ? 'block' : 'none';
        });
        importForm.addEventListener('submit', async (e: Event) => {
            e.preventDefault();
            await importBackup();
        });
    }

    document.getElementById('cleanupImagesBtn')?.addEventListener('click', cleanupImages);
    document.getElementById('invalidateCacheBtn')?.addEventListener('click', async () => {
        const msgDiv = document.getElementById('cacheInvalidateMessage') as HTMLElement | null;
        await invalidateCache(apiFetch, confirm, msgDiv);
    });
    document.getElementById('viewLogsBtn')?.addEventListener('click', () => {
        location.href = 'logs.html';
    });
});
}

// ==================== Quiz Management ====================

async function viewQuizzes() {
    showModal('Alle Quizze', showLoading());
    try {
        const response = await apiFetch(`${API_BASE}/quizzes`);
        if (!response.ok) {
            showModal('Fehler', showError(await readHttpErrorMessage(response, 'Fehler beim Laden der Quizze')));
            return;
        }
        const quizzes: QuizDTO[] = await response.json();
        if (quizzes.length === 0) {
            showModal('Alle Quizze', '<p>Keine Quizze gefunden.</p>');
            return;
        }
        const sortedQuizzes = sortQuizzesNewestFirst(quizzes);
        const headers = ['ID', 'Titel', 'Pub Datum', 'Archiv Datum', 'Fertig', 'Aktionen'];
        const html = renderTable(headers, sortedQuizzes, (quiz: unknown) => {
            const q = quiz as QuizDTO;
            const safeQuizId = escapeHtml(String(q.quizId));
            return [
                String(q.quizId),
                quizDisplayTitle(q),
                String(q.pubDate),
                String(q.submitDate),
                q.finished ? '✅' : '❌',
                trustedHtml(`
                <button class="icon-btn edit-quiz-btn" data-id="${safeQuizId}" title="Quiz bearbeiten">✏️</button>
                <button class="icon-btn delete-quiz-btn" data-id="${safeQuizId}" title="Quiz löschen">🗑️</button>
            `)
            ];
        });
        showModal('Alle Quizze', html);
        document.querySelectorAll('.edit-quiz-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number((btn as HTMLElement).dataset.id);
                editQuiz(id);
            });
        });
        document.querySelectorAll('.delete-quiz-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number((btn as HTMLElement).dataset.id);
                deleteQuiz(id);
            });
        });
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError('Fehler beim Laden der Quizze: ' + (error instanceof Error ? error.message : error)));
    }
}

async function editQuiz(quizId: number) {
    try {
        const response = await apiFetch(`${API_BASE}/quiz/${quizId}/detail`);
        if (!response.ok) {
            showModal('Fehler', showError('Quiz nicht gefunden'));
            return;
        }
        const quiz: QuizDTO = await response.json();
        loadCreateQuizPage(quiz);
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError('Fehler: ' + (error instanceof Error ? error.message : error)));
    }
}

async function deleteQuiz(quizId: number) {
    if (!confirm(`Quiz ${quizId} wirklich löschen?\n\nACHTUNG: Alle Ergebnisse dieses Quiz werden unwiderruflich gelöscht!`)) return;
    try {
        const response = await apiFetch(`${API_BASE}/quiz/${quizId}`, {method: 'DELETE'});
        if (response.ok) {
            await viewQuizzes();
        } else {
            showModal('Fehler', showError('Fehler beim Löschen des Quiz'));
        }
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError('Fehler: ' + (error instanceof Error ? error.message : error)));
    }
}

// ==================== Team Management ====================

async function createTeam() {
    const teamName = prompt('Team-Namen eingeben:');
    if (!teamName) return;
    try {
        const response = await apiFetch(`${API_BASE}/team`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({teamName})
        });
        if (response.ok) {
            await viewTeams();
        } else {
            const message = await response.text();
            showModal('Fehler', showError('Fehler: ' + message));
        }
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError('Fehler: ' + (error instanceof Error ? error.message : error)));
    }
}

async function viewTeams() {
    showModal('Alle Teams', showLoading());
    try {
        const response = await apiFetch(`${API_BASE}/teams`);
        if (!response.ok) {
            showModal('Fehler', showError(await readHttpErrorMessage(response, 'Fehler beim Laden der Teams')));
            return;
        }
        const teams: TeamDTO[] = await response.json();
        if (teams.length === 0) {
            showModal('Alle Teams', '<p>Keine Teams gefunden.</p>');
            return;
        }
        const headers = ['ID', 'Team-Name', 'Aktionen'];
        const html = renderTable(headers, teams, (team: unknown) => {
            const t = team as TeamDTO;
            return [
                String(t.teamsId),
                t.teamName,
                trustedHtml(`
                <button class="icon-btn rename-team-btn" data-id="${escapeHtml(String(t.teamsId))}" data-name="${escapeHtml(t.teamName)}" title="Team umbenennen">✏️</button>
                <button class="icon-btn delete-team-btn" data-id="${escapeHtml(String(t.teamsId))}" data-name="${escapeHtml(t.teamName)}" title="Team löschen">🗑️</button>
            `)
            ];
        });
        showModal('Alle Teams', html);
        document.querySelectorAll('.rename-team-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number((btn as HTMLElement).dataset.id);
                const name = (btn as HTMLElement).dataset.name ?? '';
                renameTeam(id, name);
            });
        });
        document.querySelectorAll('.delete-team-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number((btn as HTMLElement).dataset.id);
                const name = (btn as HTMLElement).dataset.name ?? '';
                deleteTeam(id, name);
            });
        });
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError('Fehler beim Laden der Teams: ' + (error instanceof Error ? error.message : error)));
    }
}

async function deleteTeam(teamId: number, teamName: string) {
    if (!confirm(`Team "${teamName}" wirklich löschen?\n\nACHTUNG: Alle Ergebnisse dieses Teams werden unwiderruflich gelöscht!`)) return;
    try {
        const response = await apiFetch(`${API_BASE}/team/${teamId}`, {method: 'DELETE'});
        if (response.ok) {
            await viewTeams();
        } else {
            showModal('Fehler', showError('Fehler beim Löschen des Teams'));
        }
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError('Fehler: ' + (error instanceof Error ? error.message : error)));
    }
}

async function renameTeam(teamId: number, currentName: string) {
    const newName = prompt('Team-Namen ändern:', currentName);
    if (!newName || newName === currentName) return;
    try {
        const response = await apiFetch(`${API_BASE}/team/${teamId}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({teamName: newName})
        });
        if (response.ok) {
            await viewTeams();
        } else {
            const message = await response.text();
            showModal('Fehler', showError('Fehler: ' + message));
        }
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError('Fehler: ' + (error instanceof Error ? error.message : error)));
    }
}

async function viewUsers() {
    showModal('Alle Benutzer', showLoading());
    try {
        const response = await apiFetch(`${API_BASE}/users`);
        if (!response.ok) {
            showModal('Fehler', showError(await readHttpErrorMessage(response, 'Fehler beim Laden der Benutzer')));
            return;
        }
        const users: UserDTO[] = await response.json();
        const headers = ['ID', 'Benutzername', 'Rolle', ''];
        const html = renderTable(headers, users, (user: unknown) => {
            const u = user as UserDTO;
            return [
                String(u.userId),
                u.username,
                u.role,
                trustedHtml(`
                <button class="icon-btn delete-user-btn" data-id="${escapeHtml(String(u.userId))}" data-name="${escapeHtml(u.username)}" title="Benutzer löschen">🗑️</button>
            `)
            ];
        });
        showModal('Alle Benutzer', html);
        document.querySelectorAll('.delete-user-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number((btn as HTMLElement).dataset.id);
                const name = (btn as HTMLElement).dataset.name ?? '';
                deleteUser(id, name);
            });
        });
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError('Fehler beim Laden der Benutzer: ' + (error instanceof Error ? error.message : error)));
    }
}

async function deleteUser(userId: number, username: string) {
    if (!confirm(`Sind Sie sicher, dass Sie den Benutzer "${username}" löschen möchten?`)) return;
    try {
        const response = await apiFetch(`${API_BASE}/user/${userId}`, {method: 'DELETE'});
        if (response.ok) {
            await viewUsers();
            return;
        }
        showModal('Fehler', showError(await readHttpErrorMessage(response, 'Fehler beim Löschen des Benutzers')));
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        showModal('Fehler', showError('Fehler beim Löschen des Benutzers: ' + (error instanceof Error ? error.message : error)));
    }
}

// ==================== Backup Management ====================

function exportBackup() {
    window.location.href = '/admin/backup/export';
}

async function importBackup() {
    const input = document.getElementById('backupFileInput') as HTMLInputElement | null;
    const msgDiv = document.getElementById('backupMessage') as HTMLElement | null;
    if (!input || !input.files || input.files.length === 0) {
        setStatusMessage(msgDiv, 'Bitte eine ZIP-Datei auswählen.', 'red');
        return;
    }
    const file = input.files[0];
    const formData = new FormData();
    formData.append('file', file);

    setStatusMessage(msgDiv, 'Hochladen...', 'neutral');

    try {
        const resp = await apiFetch('/admin/backup/import', { method: 'POST', body: formData });
        if (resp.ok) {
            const msg = await resp.text();
            setStatusMessage(msgDiv, msg, 'green');
        } else {
            const err = await resp.json().catch(() => ({ error: 'Unbekannter Fehler' }));
            setStatusMessage(msgDiv, `Fehler: ${String(err.error ?? 'Unbekannter Fehler')}`, 'red');
        }
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        setStatusMessage(msgDiv, `Netzwerkfehler: ${error instanceof Error ? error.message : String(error)}`, 'red');
    }
}

async function cleanupImages() {
    const msgDiv = document.getElementById('cleanupMessage') as HTMLElement | null;
    setStatusMessage(msgDiv, 'Bereinigung läuft...', 'neutral');

    try {
        const resp = await apiFetch('/admin/cleanup-images', {method: 'DELETE'});
        if (resp.ok) {
            const result = await resp.json() as { deletedCount: number; deletedFiles: string[] };
            if (msgDiv) {
                if (result.deletedCount === 0) {
                    setStatusMessage(msgDiv, 'Keine verwaisten Bilder gefunden.', 'green');
                } else {
                    setStatusMessage(msgDiv, `${result.deletedCount} verwaiste(s) Bild(er) gelöscht.`, 'green');
                }
            }
        } else {
            const err = await resp.json().catch(() => ({error: 'Unbekannter Fehler'}));
            setStatusMessage(msgDiv, `Fehler: ${String(err.error ?? 'Unbekannter Fehler')}`, 'red');
        }
    } catch (error: unknown) {
        if (isAuthExpiredRedirectError(error)) {
            return;
        }
        setStatusMessage(msgDiv, `Netzwerkfehler: ${error instanceof Error ? error.message : String(error)}`, 'red');
    }
}
