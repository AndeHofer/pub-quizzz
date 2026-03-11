const API_BASE = '/admin';

// Helper for API fetch with basic network error handling
async function apiFetch(url, options) {
    try {
        return await fetch(url, options);
    } catch (error) {
        alert('Netzwerkfehler: ' + error.message);
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
            alert('Quiz nicht gefunden');
            return;
        }

        const quiz = await response.json();
        // Open create quiz form in edit mode with pre-filled data
        loadCreateQuizPage(quiz);
    } catch (error) {
        alert('Fehler: ' + error.message);
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
            alert('Quiz erfolgreich gelöscht!');
            viewQuizzes(); // Refresh the list
        } else {
            alert('Fehler beim Löschen des Quiz');
        }
    } catch (error) {
        alert('Fehler: ' + error.message);
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

        if (response.ok) {
            alert('Team erfolgreich erstellt!');
        } else {
            const message = await response.text();
            alert('Fehler: ' + message);
        }
    } catch (error) {
        alert('Fehler: ' + error.message);
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
            alert('Team erfolgreich gelöscht!');
            viewTeams(); // Refresh the list
        } else {
            alert('Fehler beim Löschen des Teams');
        }
    } catch (error) {
        alert('Fehler: ' + error.message);
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
