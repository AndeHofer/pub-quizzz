const API_BASE = '/admin';

// Helper for API fetch with basic network error handling
async function apiFetch(url, options) {
    try {
        return await fetch(url, options);
    } catch (error) {
        // show inline feedback if possible, otherwise rethrow
        console.error('Netzwerkfehler:', error);
        throw error;
    }
}

function goBack() {
    window.location.href = '/index.html';
}

function closeModal() {
    document.getElementById('dataModal').style.display = 'none';
}

function showModal(title, content) {
    const modal = document.getElementById('dataModal');
    const modalContent = document.getElementById('modalContent');
    modalContent.innerHTML = `<h2>${title}</h2>${content}`;
    modal.style.display = 'block';
}

function showLoading() {
    return '<div class="loading">Laden...</div>';
}

// Load create quiz page with pre-filled data for editing
function loadCreateQuizPage(quiz) {
    // Store quiz data in sessionStorage for the create_quiz page to pick up
    sessionStorage.setItem('editingQuiz', JSON.stringify(quiz));
    // Redirect to create quiz page
    window.location.href = 'create_quiz.html';
}

function showError(message) {
    return `<div class="error">❌ ${message}</div>`;
}

function showSuccess(message) {
    return `<div class="success">✅ ${message}</div>`;
}

// ==================== Quiz Management ====================

async function viewQuizzes() {
    showModal('Alle Quizze', showLoading());

    try {
        const response = await apiFetch(`${API_BASE}/quizzes`);
        const quizzes = await response.json();

        if (quizzes.length === 0) {
            showModal('Alle Quizze', '<p>Keine Quizze gefunden.</p>');
            return;
        }

        const headers = ['ID', 'Veröffentlicht am', 'Abgabedatum', 'Fragen', 'Aktionen'];
        const html = renderTable(headers, quizzes, quiz => {
            return [`${quiz.quizId}`, `${quiz.pubDate}`, `${quiz.submitDate}`, `${quiz.questionCount}`, `
                <button class="icon-btn" onclick="editQuiz(${quiz.quizId})" title="Quiz bearbeiten">✏️</button>
                <button class="icon-btn" onclick="deleteQuiz(${quiz.quizId})" title="Quiz löschen">🗑️</button>
            `];
        });

        showModal('Alle Quizze', html);
    } catch (error) {
        showModal('Fehler', showError('Fehler beim Laden der Quizze: ' + (error.message || error)));
    }
}

async function editQuiz(quizId) {
    try {
        const response = await fetch(`${API_BASE}/quiz/${quizId}/detail`);
        if (!response.ok) {
            // quiz not found — show modal feedback
            showModal('Fehler', showError('Quiz nicht gefunden'));
            return;
        }

        const quiz = await response.json();
        // Open create quiz form in edit mode with pre-filled data
        loadCreateQuizPage(quiz);
    } catch (error) {
        showModal('Fehler', showError('Fehler: ' + (error.message || error)));
    }
}

async function deleteQuiz(quizId) {
    if (!confirm(`Quiz ${quizId} wirklich löschen? Dies kann nicht rückgängig gemacht werden!`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/quiz/${quizId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            // refresh silently
            viewQuizzes(); // Refresh the list
        } else {
            showModal('Fehler', showError('Fehler beim Löschen des Quiz'));
        }
    } catch (error) {
        showModal('Fehler', showError('Fehler: ' + (error.message || error)));
    }
}

// ==================== Team Management ====================

async function createTeam() {
    const teamName = prompt('Team-Namen eingeben:');
    if (!teamName) return;

    try {
        const response = await fetch(`${API_BASE}/team?teamName=${encodeURIComponent(teamName)}`, {
            method: 'POST'
        });

        if (!response.ok) {
            const message = await response.text();
            showModal('Fehler', showError('Fehler: ' + message));
        }
    } catch (error) {
        showModal('Fehler', showError('Fehler: ' + (error.message || error)));
    }
}

async function viewTeams() {
    showModal('Alle Teams', showLoading());

    try {
        const response = await apiFetch(`${API_BASE}/teams`);
        const teams = await response.json();

        if (teams.length === 0) {
            showModal('Alle Teams', '<p>Keine Teams gefunden.</p>');
            return;
        }

        const headers = ['ID', 'Team-Name', 'Aktionen'];
        const html = renderTable(headers, teams, team => {
            return [`${team.teamsId}`, `${team.teamName}`, `
                <button class="icon-btn" onclick="deleteTeam(${team.teamsId}, '${team.teamName}')" title="Team löschen">🗑️</button>
            `];
        });

        showModal('Alle Teams', html);
    } catch (error) {
        showModal('Fehler', showError('Fehler beim Laden der Teams: ' + (error.message || error)));
    }
}

async function deleteTeam(teamId, teamName) {
    if (!confirm(`Team "${teamName}" wirklich löschen?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/team/${teamId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            viewTeams(); // Refresh the list
        } else {
            showModal('Fehler', showError('Fehler beim Löschen des Teams'));
        }
    } catch (error) {
        showModal('Fehler', showError('Fehler: ' + (error.message || error)));
    }
}

// ==================== Results Management ====================

async function viewResults() {
    const quizId = prompt('Quiz ID für Ergebnisse eingeben (leer = alle):');

    showModal('Ergebnisse', showLoading());

    try {
        const url = quizId ? `${API_BASE}/results?quizId=${quizId}` : `${API_BASE}/results`;
        const response = await apiFetch(url);
        const results = await response.json();

        if (results.length === 0) {
            showModal('Ergebnisse', '<p>Keine Ergebnisse gefunden.</p>');
            return;
        }

        let html = '<table><thead><tr><th>Team</th><th>Quiz Datum</th>';
        for (let i = 1; i <= 8; i++) {
            html += `<th>Q${i}</th>`;
        }
        html += '<th>Gesamt</th></tr></thead><tbody>';

        results.forEach(result => {
            // Build a lookup map from questionNumber -> answer object
            const answersMap = {};
            if (Array.isArray(result.answers)) {
                result.answers.forEach(a => {
                    answersMap[a.questionNumber] = a;
                });
            }

            html += `<tr>`;
            html += `<td>${result.teamName}</td>`;
            html += `<td>${result.quizDate}</td>`;

            for (let i = 1; i <= 8; i++) {
                const a = answersMap[i];
                const points = a && typeof a.points === 'number' ? a.points : 0;
                const changed = a && a.changed ? '*' : '';
                html += `<td>${points}${changed}</td>`;
            }

            html += `<td><strong>${result.totalPoints || 0}</strong></td>`;
            html += `</tr>`;
        });

        html += '</tbody></table>';
        html += '<p style="margin-top: 10px; color: #666;"><em>* = Antwort wurde geändert</em></p>';

        showModal('Ergebnisse', html);
    } catch (error) {
        showModal('Fehler', showError('Fehler beim Laden der Ergebnisse: ' + (error.message || error)));
    }
}

// ==================== Add Result Modal (client-side team filter) ====================

let _admin_quizzes_cache = null;
let _admin_teams_cache = null;
let _selectedTeamId = null;
// Accessibility / keyboard navigation state for team suggestions
// removed suggestion state (using select dropdown now)
let _team_matches = [];
let _team_suggestion_index = -1;

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

        // use a select dropdown for teams (no free text)
        const teamSelect = document.getElementById('add-result-team-select');
        // populate select with teams (in case cache changed)
        if (teamSelect && _admin_teams_cache) {
            teamSelect.innerHTML = '<option value="">-- Team auswählen --</option>' +
                _admin_teams_cache.map(t => `<option value="${t.teamsId}">${t.teamName}</option>`).join('');
        }

        document.getElementById('add-result-save-btn').addEventListener('click', onSaveAddResult);

    } catch (err) {
        showModal('Fehler', showError('Fehler beim Laden der Daten: ' + (err.message || err)));
    }
}

function buildAddResultForm() {
    let quizOptions = '<option value="">-- Quiz auswählen --</option>';
    _admin_quizzes_cache.forEach(q => {
        quizOptions += `<option value="${q.quizId}">ID ${q.quizId} — ${q.pubDate}</option>`;
    });

    let inputs = '';
    for (let i = 1; i <= 8; i++) {
        inputs += `<div class="form-row"><label>Frage ${i} Punkte</label><input type="number" id="add-result-q${i}" min="0" value="0"></div>`;
    }

    return `
      <div id="add-result-form">
        <div class="form-row"><label>Quiz auswählen</label>
          <select id="add-result-quiz">${quizOptions}</select>
        </div>
          <div class="form-row"><label>Team auswählen</label>
          <select id="add-result-team-select">
            <option value="">-- Team auswählen --</option>
          </select>
        </div>
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
    const quizId = Number(document.getElementById('add-result-quiz').value);
    const teamId = Number(document.getElementById('add-result-team-select').value);
    const feedback = document.getElementById('add-result-feedback');

    if (!quizId) {
        feedback.textContent = 'Bitte ein Quiz auswählen.';
        return;
    }
    if (!teamId) {
        feedback.textContent = 'Bitte ein Team auswählen (über Vorschläge).';
        return;
    }

    const answers = [];
    for (let i = 1; i <= 8; i++) {
        const val = Number(document.getElementById(`add-result-q${i}`).value);
        if (!Number.isFinite(val) || val < 0) {
            feedback.textContent = `Ungültige Punkte bei Frage ${i}.`;
            return;
        }
        answers.push({questionNumber: i, points: val});
    }

    const payload = {quizId, teamId, answers};

    try {
        const res = await apiFetch(`${API_BASE}/results`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const text = await res.text();
            feedback.textContent = 'Fehler: ' + text;
            return;
        }

        const created = await res.json();
        closeModal();
    } catch (err) {
        feedback.textContent = 'Fehler beim Speichern: ' + (err.message || err);
    }
}


async function exportResults() {
    const quizId = prompt('Quiz ID für Export eingeben (leer = alle):');

    try {
        const url = quizId ? `${API_BASE}/results/export?quizId=${quizId}` : `${API_BASE}/results/export`;
        window.open(url, '_blank');
    } catch (error) {
        alert('Fehler: ' + error.message);
    }
}

async function viewLeaderboard() {
    const quizId = prompt('Quiz ID für Rangliste eingeben (leer = alle):');

    showModal('Rangliste', showLoading());

    try {
        const url = quizId ? `${API_BASE}/leaderboard?quizId=${quizId}` : `${API_BASE}/leaderboard`;
        const response = await apiFetch(url);
        const leaderboard = await response.json();

        if (leaderboard.length === 0) {
            showModal('Rangliste', '<p>Keine Ergebnisse gefunden.</p>');
            return;
        }

        const headers = ['Rang', 'Team', 'Quiz Datum', 'Punkte'];
        const html = renderTable(headers, leaderboard, entry => {
            const rankEmoji = entry.rank === 1 ? '🥇' : entry.rank === 2 ? '🥈' : entry.rank === 3 ? '🥉' : entry.rank;
            return [`${rankEmoji}`, `${entry.teamName}`, `${entry.quizDate}`, `<strong>${entry.totalPoints}</strong>`];
        });

        showModal('🏆 Rangliste', html);
    } catch (error) {
        showModal('Fehler', showError('Fehler beim Laden der Rangliste: ' + (error.message || error)));
    }
}

// ==================== User Management ====================

async function viewUsers() {
    showModal('Alle Benutzer', showLoading());

    try {
        const response = await apiFetch(`${API_BASE}/users`);
        const users = await response.json();

        if (users.length === 0) {
            showModal('Alle Benutzer', '<p>Keine Benutzer gefunden.</p>');
            return;
        }

        const headers = ['ID', 'Benutzername', 'Rolle', ''];
        const html = renderTable(headers, users, user => {
            return [`${user.userId}`, `${user.username}`, `${user.role}`, `
                <button class="icon-btn" onclick="deleteUser(${user.userId}, '${user.username}')" title="Benutzer löschen">🗑️</button>
            `];
        });

        showModal('Alle Benutzer', html);
    } catch (error) {
        showModal('Fehler', showError('Fehler beim Laden der Benutzer: ' + (error.message || error)));
    }
}

// Helper to render a table from headers and data. rowFn returns an array of cell HTML/text.
function renderTable(headers, rows, rowFn) {
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

// Add deleteUser function
async function deleteUser(userId, username) {
    // Confirmation question
    if (!confirm(`Sind Sie sicher, dass Sie den Benutzer "${username}" löschen möchten?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/user/${userId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            // Refresh the user list after successful deletion
            await viewUsers();
        } else {
            const errorText = await response.text();
            alert('Fehler beim Löschen: ' + errorText);
        }
    } catch (error) {
        alert('Fehler: ' + error.message);
    }
}

// Close modal when clicking outside
window.onclick = function (event) {
    const modal = document.getElementById('dataModal');
    if (event.target === modal) {
        closeModal();
    }
}
