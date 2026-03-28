export {};

import type { QuizDTO, TeamDTO, ResultDTO, LeaderboardEntry, UserDTO } from './types';

const API_BASE = '/admin';

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
    (window as any).viewResults = viewResults;
    (window as any).showAddResultModal = showAddResultModal;
    (window as any).exportResults = exportResults;
    (window as any).viewLeaderboard = viewLeaderboard;
    (window as any).viewUsers = viewUsers;
    (window as any).deleteUser = deleteUser;

    const modal = document.getElementById('dataModal');
    if (modal) {
        window.addEventListener('click', (event: MouseEvent) => {
            if (event.target === modal) closeModal();
        });
    }
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
        const headers = ['ID', 'Veröffentlicht am', 'Abgabedatum', 'Fragen', 'Aktionen'];
        const html = renderTable(headers, quizzes, (quiz: unknown) => {
            const q = quiz as QuizDTO;
            return [`${q.quizId}`, `${q.pubDate}`, `${q.submitDate}`, `${q.questionCount}`, `
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
    if (!confirm(`Quiz ${quizId} wirklich löschen? Dies kann nicht rückgängig gemacht werden!`)) return;
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
                <button class="icon-btn delete-team-btn" data-id="${t.teamsId}" data-name="${t.teamName}" title="Team löschen">🗑️</button>
            `];
        });
        showModal('Alle Teams', html);
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
    if (!confirm(`Team "${teamName}" wirklich löschen?`)) return;
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

// ==================== Results Management ====================

async function viewResults() {
    const quizId = prompt('Quiz ID für Ergebnisse eingeben (leer = alle):');
    showModal('Ergebnisse', showLoading());
    try {
        const url = quizId ? `${API_BASE}/results?quizId=${quizId}` : `${API_BASE}/results`;
        const response = await apiFetch(url);
        const results: ResultDTO[] = await response.json();
        if (results.length === 0) {
            showModal('Ergebnisse', '<p>Keine Ergebnisse gefunden.</p>');
            return;
        }
        let html = '<table><thead><tr><th>Team</th><th>Quiz Datum</th>';
        for (let i = 1; i <= 8; i++) html += `<th>Q${i}</th>`;
        html += '<th>Gesamt</th></tr></thead><tbody>';
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
            html += `<td><strong>${result.totalPoints || 0}</strong></td></tr>`;
        });
        html += '</tbody></table>';
        showModal('Ergebnisse', html);
    } catch (error: unknown) {
        showModal('Fehler', showError('Fehler beim Laden der Ergebnisse: ' + (error instanceof Error ? error.message : error)));
    }
}

// ==================== Add Result ====================

let _admin_quizzes_cache: QuizDTO[] | null = null;
let _admin_teams_cache: TeamDTO[] | null = null;

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
    } catch (err: unknown) {
        showModal('Fehler', showError('Fehler beim Laden der Daten: ' + (err instanceof Error ? err.message : err)));
    }
}

function buildAddResultForm() {
    let quizOptions = '<option value="">-- Quiz auswählen --</option>';
    if (_admin_quizzes_cache) {
        _admin_quizzes_cache.forEach(q => {
            quizOptions += `<option value="${q.quizId}">ID ${q.quizId} — ${q.pubDate}</option>`;
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
          <button onclick="closeModal()" class="secondary-btn">Abbrechen</button>
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

async function exportResults() {
    const quizId = prompt('Quiz ID für Export eingeben (leer = alle):');
    const url = quizId ? `${API_BASE}/results/export?quizId=${quizId}` : `${API_BASE}/results/export`;
    window.open(url, '_blank');
}

async function viewLeaderboard() {
    const quizId = prompt('Quiz ID für Rangliste eingeben (leer = alle):');
    showModal('Rangliste', showLoading());
    try {
        const url = quizId ? `${API_BASE}/leaderboard?quizId=${quizId}` : `${API_BASE}/leaderboard`;
        const response = await apiFetch(url);
        const leaderboard: LeaderboardEntry[] = await response.json();
        const headers = ['Rang', 'Team', 'Quiz Datum', 'Punkte'];
        const html = renderTable(headers, leaderboard, (entry: unknown) => {
            const e = entry as LeaderboardEntry;
            const rankEmoji = e.rank === 1 ? '🥇' : e.rank === 2 ? '🥈' : e.rank === 3 ? '🥉' : e.rank;
            return [`${rankEmoji}`, `${e.teamName}`, `${e.quizDate}`, `<strong>${e.totalPoints}</strong>`];
        });
        showModal('🏆 Rangliste', html);
    } catch (error: unknown) {
        showModal('Fehler', showError('Fehler beim Laden der Rangliste: ' + (error instanceof Error ? error.message : error)));
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
    return html;
}
