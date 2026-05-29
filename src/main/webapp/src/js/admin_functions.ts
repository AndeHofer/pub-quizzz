export {};

import type {QuizDTO, TeamDTO, UserDTO} from './types';
import {quizDisplayTitle, sortQuizzesNewestFirst} from './quiz-utils';
import {withEnsuredCsrfHeaders} from './csrf';

const API_BASE = '/admin';

// Helper for API fetch with basic network error handling
async function apiFetch(url: string, options?: RequestInit): Promise<Response> {
    try {
        const method = (options?.method ?? 'GET').toUpperCase();
        const requiresCsrf = !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method);
        const requestOptions: RequestInit = options ?? {};
        if (requiresCsrf) {
            requestOptions.headers = await withEnsuredCsrfHeaders(options?.headers);
        }
        return await fetch(url, requestOptions);
    } catch (error) {
        console.error('Netzwerkfehler:', error);
        throw error;
    }
}

function closeModal() {
    const modal = document.getElementById('dataModal') as HTMLElement | null;
    if (modal) modal.style.display = 'none';
}

function showModal(title: string, content: string) {
    const modal = document.getElementById('dataModal') as HTMLElement | null;
    const modalContent = document.getElementById('modalContent') as HTMLElement | null;
    if (modal && modalContent) {
        modalContent.innerHTML = `<h2>${title}</h2>${content}`;
        modal.style.display = 'block';
    }
}

function showLoading() {
    return '<div class="loading">Laden...</div>';
}

function loadCreateQuizPage(quiz: QuizDTO) {
    sessionStorage.setItem('editingQuiz', JSON.stringify(quiz));
    window.location.href = 'create_quiz.html';
}

function showError(message: string) {
    return `<div class="error">❌ ${message}</div>`;
}

// Wire up admin page handlers and modal click-outside behavior
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
    document.getElementById('viewLogsBtn')?.addEventListener('click', () => {
        location.href = 'logs.html';
    });
});

// ==================== Quiz Management ====================

async function viewQuizzes() {
    showModal('Alle Quizze', showLoading());
    try {
        const response = await apiFetch(`${API_BASE}/quizzes`);
        const quizzes: QuizDTO[] = await response.json();
        if (quizzes.length === 0) {
            showModal('Alle Quizze', '<p>Keine Quizze gefunden.</p>');
            return;
        }
        const sortedQuizzes = sortQuizzesNewestFirst(quizzes);
        const headers = ['ID', 'Titel', 'Pub Datum', 'Archiv Datum', 'Fertig', 'Aktionen'];
        const html = renderTable(headers, sortedQuizzes, (quiz: unknown) => {
            const q = quiz as QuizDTO;
            return [`${q.quizId}`, quizDisplayTitle(q), `${q.pubDate}`, `${q.submitDate}`, q.finished ? '✅' : '❌', `
                <button class="icon-btn edit-quiz-btn" data-id="${q.quizId}" title="Quiz bearbeiten">✏️</button>
                <button class="icon-btn delete-quiz-btn" data-id="${q.quizId}" title="Quiz löschen">🗑️</button>
            `];
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
        showModal('Fehler', showError('Fehler: ' + (error instanceof Error ? error.message : error)));
    }
}

async function viewTeams() {
    showModal('Alle Teams', showLoading());
    try {
        const response = await apiFetch(`${API_BASE}/teams`);
        const teams: TeamDTO[] = await response.json();
        if (teams.length === 0) {
            showModal('Alle Teams', '<p>Keine Teams gefunden.</p>');
            return;
        }
        const headers = ['ID', 'Team-Name', 'Aktionen'];
        const html = renderTable(headers, teams, (team: unknown) => {
            const t = team as TeamDTO;
            return [`${t.teamsId}`, `${t.teamName}`, `
                <button class="icon-btn rename-team-btn" data-id="${t.teamsId}" data-name="${t.teamName}" title="Team umbenennen">✏️</button>
                <button class="icon-btn delete-team-btn" data-id="${t.teamsId}" data-name="${t.teamName}" title="Team löschen">🗑️</button>
            `];
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
        showModal('Fehler', showError('Fehler: ' + (error instanceof Error ? error.message : error)));
    }
}

async function viewUsers() {
    showModal('Alle Benutzer', showLoading());
    try {
        const response = await apiFetch(`${API_BASE}/users`);
        const users: UserDTO[] = await response.json();
        const headers = ['ID', 'Benutzername', 'Rolle', ''];
        const html = renderTable(headers, users, (user: unknown) => {
            const u = user as UserDTO;
            return [`${u.userId}`, `${u.username}`, `${u.role}`, `
                <button class="icon-btn delete-user-btn" data-id="${u.userId}" data-name="${u.username}" title="Benutzer löschen">🗑️</button>
            `];
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
        showModal('Fehler', showError('Fehler beim Laden der Benutzer: ' + (error instanceof Error ? error.message : error)));
    }
}

async function deleteUser(userId: number, username: string) {
    if (!confirm(`Sind Sie sicher, dass Sie den Benutzer "${username}" löschen möchten?`)) return;
    try {
        const response = await apiFetch(`${API_BASE}/user/${userId}`, {method: 'DELETE'});
        if (response.ok) await viewUsers();
        else alert('Fehler beim Löschen');
    } catch (error: unknown) {
        alert('Fehler: ' + (error instanceof Error ? error.message : error));
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
        if (msgDiv) msgDiv.innerHTML = '<span style="color:red;">Bitte eine ZIP-Datei auswählen.</span>';
        return;
    }
    const file = input.files[0];
    const formData = new FormData();
    formData.append('file', file);

    if (msgDiv) msgDiv.innerHTML = '<span>Hochladen...</span>';

    try {
        const resp = await apiFetch('/admin/backup/import', { method: 'POST', body: formData });
        if (resp.ok) {
            const msg = await resp.text();
            if (msgDiv) msgDiv.innerHTML = `<span style="color:green;">${msg}</span>`;
        } else {
            const err = await resp.json().catch(() => ({ error: 'Unbekannter Fehler' }));
            if (msgDiv) msgDiv.innerHTML = `<span style="color:red;">Fehler: ${err.error}</span>`;
        }
    } catch (error: unknown) {
        if (msgDiv) msgDiv.innerHTML = `<span style="color:red;">Netzwerkfehler: ${error instanceof Error ? error.message : error}</span>`;
    }
}

async function cleanupImages() {
    const msgDiv = document.getElementById('cleanupMessage') as HTMLElement | null;
    if (msgDiv) msgDiv.innerHTML = '<span>Bereinigung läuft...</span>';

    try {
        const resp = await apiFetch('/admin/cleanup-images', {method: 'DELETE'});
        if (resp.ok) {
            const result = await resp.json() as { deletedCount: number; deletedFiles: string[] };
            if (msgDiv) {
                if (result.deletedCount === 0) {
                    msgDiv.innerHTML = '<span style="color:green;">Keine verwaisten Bilder gefunden.</span>';
                } else {
                    msgDiv.innerHTML = `<span style="color:green;">${result.deletedCount} verwaiste(s) Bild(er) gelöscht.</span>`;
                }
            }
        } else {
            const err = await resp.json().catch(() => ({error: 'Unbekannter Fehler'}));
            if (msgDiv) msgDiv.innerHTML = `<span style="color:red;">Fehler: ${err.error}</span>`;
        }
    } catch (error: unknown) {
        if (msgDiv) msgDiv.innerHTML = `<span style="color:red;">Netzwerkfehler: ${error instanceof Error ? error.message : error}</span>`;
    }
}

function renderTable(headers: string[], rows: unknown[], rowFn: (row: unknown) => string[]) {
    let html = '<table><thead><tr>';
    headers.forEach(h => html += `<th>${h}</th>`);
    html += '</tr></thead><tbody>';
    rows.forEach(r => {
        const cells = rowFn(r);
        html += '<tr>';
        cells.forEach(c => html += `<td>${c}</td>`);
        html += '</tr>';
    });
    html += '</tbody></table>';
    return `<div class="overflow-x-auto">${html}</div>`;
}
