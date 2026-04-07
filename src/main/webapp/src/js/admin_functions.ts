export {};

import type { QuizDTO, TeamDTO, ResultDTO, UserDTO } from './types';

const API_BASE = '/admin';

// Module-level caches
let _admin_quizzes_cache: QuizDTO[] | null = null;
let _admin_teams_cache: TeamDTO[] | null = null;
let _admin_results_cache: ResultDTO[] | null = null;
let _admin_last_quiz_id_filter: string | null = null;

// Helper for API fetch with basic network error handling
async function apiFetch(url: string, options?: RequestInit): Promise<Response> {
    try {
        return await fetch(url, options);
    } catch (error) {
        console.error('Netzwerkfehler:', error);
        throw error;
    }
}

function goBack() {
    window.location.href = '/index.html';
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

// Ensure functions are available globally and wire up modal click-outside handler
window.addEventListener('load', () => {
    (window as any).goBack = goBack;
    (window as any).closeModal = closeModal;
    (window as any).viewQuizzes = viewQuizzes;
    (window as any).editQuiz = editQuiz;
    (window as any).deleteQuiz = deleteQuiz;
    (window as any).createTeam = createTeam;
    (window as any).viewTeams = viewTeams;
    (window as any).deleteTeam = deleteTeam;
    (window as any).renameTeam = renameTeam;
    (window as any).viewResults = viewResults;
    (window as any).showAddResultModal = showAddResultModal;
    (window as any).deleteResult = deleteResult;
    (window as any).editResult = editResult;
    (window as any).viewUsers = viewUsers;
    (window as any).deleteUser = deleteUser;

    // Wire up admin_main.html buttons by ID
    document.getElementById('createQuizBtn')?.addEventListener('click', () => {
        location.href = 'create_quiz.html';
    });
    document.getElementById('viewQuizzesBtn')?.addEventListener('click', viewQuizzes);
    document.getElementById('createTeamBtn')?.addEventListener('click', createTeam);
    document.getElementById('viewTeamsBtn')?.addEventListener('click', viewTeams);
    document.getElementById('addResultBtn')?.addEventListener('click', showAddResultModal);
    document.getElementById('viewResultsBtn')?.addEventListener('click', () => viewResults());
    document.getElementById('createUserBtn')?.addEventListener('click', () => {
        location.href = 'register_user.html';
    });
    document.getElementById('viewUsersBtn')?.addEventListener('click', viewUsers);
    document.getElementById('backBtn')?.addEventListener('click', goBack);
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
});

// ==================== Quiz Management ====================

const GERMAN_MONTHS = ['Jänner', 'Februar', 'März', 'April', 'Mai', 'Juni',
    'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'];

function quizDisplayTitle(quiz: QuizDTO): string {
    if (quiz.title) return quiz.title;
    if (quiz.pubDate) {
        const parts = quiz.pubDate.split('-');
        if (parts.length >= 2) {
            const year = parts[0];
            const month = parseInt(parts[1], 10);
            if (month >= 1 && month <= 12) {
                return `${year} ${GERMAN_MONTHS[month - 1]}`;
            }
        }
    }
    return `Quiz ${quiz.quizId}`;
}

function parsePubDateToMillis(pubDate?: string): number | null {
    if (!pubDate) return null;
    const parsed = Date.parse(`${pubDate}T00:00:00Z`);
    return Number.isNaN(parsed) ? null : parsed;
}

async function viewQuizzes() {
    showModal('Alle Quizze', showLoading());
    try {
        const response = await apiFetch(`${API_BASE}/quizzes`);
        const quizzes: QuizDTO[] = await response.json();
        if (quizzes.length === 0) {
            showModal('Alle Quizze', '<p>Keine Quizze gefunden.</p>');
            return;
        }
        const sortedQuizzes = [...quizzes].sort((a: QuizDTO, b: QuizDTO) => {
            const aPubDate = parsePubDateToMillis(a.pubDate);
            const bPubDate = parsePubDateToMillis(b.pubDate);

            if (aPubDate !== null && bPubDate !== null) {
                if (aPubDate !== bPubDate) return bPubDate - aPubDate;
            } else if (aPubDate !== null) {
                return -1;
            } else if (bPubDate !== null) {
                return 1;
            }

            return b.quizId - a.quizId;
        });
        const headers = ['ID', 'Titel', 'Pub Datum', 'Archiv Datum', 'Fertig', 'Aktionen'];
        const html = renderTable(headers, sortedQuizzes, (quiz: unknown) => {
            const q = quiz as QuizDTO;
            return [`${q.quizId}`, quizDisplayTitle(q), `${q.pubDate}`, `${q.submitDate}`, q.finished ? '✅' : '❌', `
                <button class="icon-btn" onclick="editQuiz(${q.quizId})" title="Quiz bearbeiten">✏️</button>
                <button class="icon-btn" onclick="deleteQuiz(${q.quizId})" title="Quiz löschen">🗑️</button>
            `];
        });
        showModal('Alle Quizze', html);
    } catch (error: unknown) {
        showModal('Fehler', showError('Fehler beim Laden der Quizze: ' + (error instanceof Error ? error.message : error)));
    }
}

async function editQuiz(quizId: number) {
    try {
        const response = await fetch(`${API_BASE}/quiz/${quizId}/detail`);
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
        const response = await fetch(`${API_BASE}/quiz/${quizId}`, {method: 'DELETE'});
        if (response.ok) {
            _admin_quizzes_cache = null;
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
        const response = await fetch(`${API_BASE}/team`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({teamName})
        });
        if (response.ok) {
            _admin_teams_cache = null;
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
        const response = await fetch(`${API_BASE}/team/${teamId}`, {method: 'DELETE'});
        if (response.ok) {
            _admin_teams_cache = null;
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
        const response = await fetch(`${API_BASE}/team/${teamId}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({teamName: newName})
        });
        if (response.ok) {
            _admin_teams_cache = null;
            await viewTeams();
        } else {
            const message = await response.text();
            showModal('Fehler', showError('Fehler: ' + message));
        }
    } catch (error: unknown) {
        showModal('Fehler', showError('Fehler: ' + (error instanceof Error ? error.message : error)));
    }
}

// ==================== Results Management ====================

async function viewResults(quizIdOverride?: string | null) {
    const quizId = quizIdOverride !== undefined ? quizIdOverride : prompt('Quiz ID für Ergebnisse eingeben (leer = alle):');
    _admin_last_quiz_id_filter = quizId ?? null;
    showModal('Ergebnisse', showLoading());
    try {
        const url = quizId ? `${API_BASE}/results?quizId=${quizId}` : `${API_BASE}/results`;
        const response = await apiFetch(url);
        const results: ResultDTO[] = await response.json();
        _admin_results_cache = results;
        if (results.length === 0) {
            showModal('Ergebnisse', '<p>Keine Ergebnisse gefunden.</p>');
            return;
        }
        let html = '<div class="overflow-x-auto"><table><thead><tr><th>Team</th><th>Quiz Datum</th>';
        for (let i = 1; i <= 8; i++) html += `<th>Q${i}</th>`;
        html += '<th>Gesamt</th><th>Aktionen</th></tr></thead><tbody>';
        results.forEach((result: ResultDTO) => {
            const answersMap: Record<number, { points: number; changed: boolean }> = {};
            if (Array.isArray(result.answers)) {
                result.answers.forEach(a => { answersMap[a.questionNumber] = a; });
            }
            html += `<tr><td>${result.teamName}</td><td>${result.quizDate}</td>`;
            for (let i = 1; i <= 8; i++) {
                const a = answersMap[i];
                const points = a && typeof a.points === 'number' ? a.points : 0;
                const changed = a && a.changed ? '*' : '';
                html += `<td>${points}${changed}</td>`;
            }
            html += `<td><strong>${result.totalPoints || 0}</strong></td>`;
            html += `<td>
    <button class="icon-btn edit-result-btn"
        data-id="${result.resultsId}"
        data-name="${result.teamName}"
        title="Ergebnis bearbeiten">✏️</button>
    <button class="icon-btn delete-result-btn"
        data-id="${result.resultsId}"
        data-name="${result.teamName}"
        title="Ergebnis löschen">🗑️</button>
</td>`;
            html += '</tr>';
        });
        html += '</tbody></table></div>';
        showModal('Ergebnisse', html);
        document.querySelectorAll('.delete-result-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number((btn as HTMLElement).dataset.id);
                const name = (btn as HTMLElement).dataset.name ?? '';
                deleteResult(id, name);
            });
        });
        document.querySelectorAll('.edit-result-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number((btn as HTMLElement).dataset.id);
                const name = (btn as HTMLElement).dataset.name ?? '';
                editResult(id, name);
            });
        });
    } catch (error: unknown) {
        showModal('Fehler', showError('Fehler beim Laden der Ergebnisse: ' + (error instanceof Error ? error.message : error)));
    }
}

async function deleteResult(resultId: number, teamName: string) {
    if (!confirm(`Ergebnis für Team "${teamName}" wirklich löschen?`)) return;
    try {
        const response = await fetch(`${API_BASE}/results/${resultId}`, {method: 'DELETE'});
        if (response.ok) {
            await viewResults(_admin_last_quiz_id_filter);
        } else {
            showModal('Fehler', showError('Fehler beim Löschen des Ergebnisses'));
        }
    } catch (error: unknown) {
        showModal('Fehler', showError('Fehler: ' + (error instanceof Error ? error.message : error)));
    }
}

async function editResult(resultId: number, teamName: string) {
    const result = _admin_results_cache?.find(r => r.resultsId === resultId);
    if (!result) {
        showModal('Fehler', showError('Ergebnis nicht gefunden'));
        return;
    }

    const answersMap: Record<number, number> = {};
    if (Array.isArray(result.answers)) {
        result.answers.forEach(a => { answersMap[a.questionNumber] = a.points; });
    }

    const pointOptions = [5, 3, 2, 1, 0];

    let formHtml = `<h3>Ergebnis bearbeiten: ${teamName}</h3><form id="editResultForm">`;
    for (let i = 1; i <= 8; i++) {
        const current = answersMap[i] ?? 0;
        const opts = pointOptions.map(v => `<option value="${v}"${v === current ? ' selected' : ''}>${v}</option>`).join('');
        formHtml += `<div><label>Frage ${i}: <select name="q${i}">${opts}</select></label></div>`;
    }
    formHtml += `<button type="button" id="saveEditResultBtn">Speichern</button></form>`;

    showModal(`Ergebnis bearbeiten`, formHtml);

    document.getElementById('saveEditResultBtn')?.addEventListener('click', async () => {
        const form = document.getElementById('editResultForm') as HTMLFormElement;
        const answers = [];
        for (let i = 1; i <= 8; i++) {
            const select = form.querySelector(`[name="q${i}"]`) as HTMLSelectElement;
            answers.push({questionNumber: i, points: Number(select.value)});
        }
        try {
            const response = await fetch(`${API_BASE}/results/${resultId}`, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({answers})
            });
            if (response.ok) {
                closeModal();
                await viewResults(_admin_last_quiz_id_filter);
            } else {
                const message = await response.text();
                showModal('Fehler', showError('Fehler beim Speichern: ' + message));
            }
        } catch (error: unknown) {
            showModal('Fehler', showError('Fehler: ' + (error instanceof Error ? error.message : error)));
        }
    });
}

// ==================== Add Result ====================

async function showAddResultModal() {
    showModal('Ergebnis hinzufügen', showLoading());
    try {
        if (!_admin_quizzes_cache) {
            const res = await apiFetch(`${API_BASE}/quizzes`);
            _admin_quizzes_cache = await res.json();
        }
        if (!_admin_teams_cache) {
            const res = await apiFetch(`${API_BASE}/teams`);
            _admin_teams_cache = await res.json();
        }
        const html = buildAddResultForm();
        showModal('Ergebnis hinzufügen', html);
        const teamSelect = document.getElementById('add-result-team-select') as HTMLSelectElement | null;
        if (teamSelect && _admin_teams_cache) {
            teamSelect.innerHTML = '<option value="">-- Team auswählen --</option>' +
                _admin_teams_cache.map(t => `<option value="${t.teamsId}">${t.teamName}</option>`).join('');
        }
        const saveBtn = document.getElementById('add-result-save-btn');
        if (saveBtn) saveBtn.addEventListener('click', onSaveAddResult);
        const cancelBtn = document.getElementById('add-result-cancel-btn');
        if (cancelBtn) cancelBtn.addEventListener('click', closeModal);
    } catch (err: unknown) {
        showModal('Fehler', showError('Fehler beim Laden der Daten: ' + (err instanceof Error ? err.message : err)));
    }
}

function buildAddResultForm() {
    let quizOptions = '<option value="">-- Quiz auswählen --</option>';
    if (_admin_quizzes_cache) {
        _admin_quizzes_cache.forEach(q => {
            quizOptions += `<option value="${q.quizId}">${quizDisplayTitle(q)}</option>`;
        });
    }
    let inputs = '';
    const pointOptions = [5, 3, 2, 1, 0].map(v => `<option value="${v}"${v === 0 ? ' selected' : ''}>${v}</option>`).join('');
    for (let i = 1; i <= 8; i++) {
        inputs += `<div class="form-row"><label>Frage ${i} Punkte</label><select id="add-result-q${i}">${pointOptions}</select></div>`;
    }
    return `
      <div id="add-result-form">
        <div class="form-row"><label>Quiz auswählen</label><select id="add-result-quiz">${quizOptions}</select></div>
        <div class="form-row"><label>Team auswählen</label><select id="add-result-team-select"><option value="">-- Team auswählen --</option></select></div>
        ${inputs}
        <div class="form-actions">
          <button id="add-result-save-btn" class="primary-btn">Speichern</button>
          <button id="add-result-cancel-btn" class="secondary-btn">Abbrechen</button>
        </div>
        <div id="add-result-feedback" class="form-feedback"></div>
      </div>
    `;
}

async function onSaveAddResult() {
    const quizId = Number((document.getElementById('add-result-quiz') as HTMLSelectElement).value);
    const teamId = Number((document.getElementById('add-result-team-select') as HTMLSelectElement).value);
    const feedback = document.getElementById('add-result-feedback') as HTMLElement | null;
    if (!quizId || !teamId) {
        if (feedback) feedback.textContent = 'Bitte Quiz und Team auswählen.';
        return;
    }
    const answers: { questionNumber: number; points: number }[] = [];
    for (let i = 1; i <= 8; i++) {
        const val = Number((document.getElementById(`add-result-q${i}`) as HTMLInputElement).value);
        answers.push({questionNumber: i, points: val});
    }
    try {
        const res = await apiFetch(`${API_BASE}/results`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({quizId, teamId, answers})
        });
        if (res.ok) {
            closeModal();
        } else {
            const text = await res.text();
            if (feedback) feedback.textContent = 'Fehler: ' + text;
        }
    } catch (err: unknown) {
        if (feedback) feedback.textContent = 'Fehler beim Speichern: ' + (err instanceof Error ? err.message : err);
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
        const response = await fetch(`${API_BASE}/user/${userId}`, {method: 'DELETE'});
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
