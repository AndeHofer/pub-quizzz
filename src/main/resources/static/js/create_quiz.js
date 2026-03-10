const questionsContainer = document.getElementById('questionsContainer');

// Questions 1-4 have 4 hints, 5-8 have 3 hints.
if (questionsContainer) {
    for (let i = 1; i <= 8; i++) {
        const section = document.createElement('div');
        section.className = 'section';

        let hintsHtml = '';
        const numHints = i <= 4 ? 4 : 3;
        for (let j = 1; j <= numHints; j++) {
            hintsHtml += `
                <div class="field-group">
                    <label for="hint${i}_${j}">Hinweis ${i}.${j}:</label>
                    <input type="text" id="hint${i}_${j}" required>
                </div>`;
        }

        section.innerHTML = `
            <h2>Frage ${i}</h2>
            <div class="field-group">
                <label for="quiz${i}">Frage ${i}:</label>
                <input type="text" id="quiz${i}" required>
            </div>
            ${hintsHtml}
            <div class="field-group">
                <label for="answer${i}">Antwort ${i}:</label>
                <input type="text" id="answer${i}" required>
            </div>
            <div class="field-group">
                <label for="note${i}">Anmerkung ${i} (optional):</label>
                <input type="text" id="note${i}">
            </div>
        `;
        questionsContainer.appendChild(section);
    }
}

const quizForm = document.getElementById('quizForm');
if (quizForm) {
    quizForm.addEventListener('submit', function (e) {
        e.preventDefault();

        // Build proper JSON structure matching CreateQuizRequest
        const quizData = {
            pubDate: document.getElementById('pubDate').value || null,
            questions: []
        };

        // Collect all 8 questions
        for (let i = 1; i <= 8; i++) {
            const numHints = i <= 4 ? 4 : 3;
            const hints = [];

            // Collect hints for this question
            for (let j = 1; j <= numHints; j++) {
                hints.push(document.getElementById(`hint${i}_${j}`).value);
            }

            quizData.questions.push({
                number: i,
                question: document.getElementById(`quiz${i}`).value,
                answer: document.getElementById(`answer${i}`).value,
                note: document.getElementById(`note${i}`).value || null,
                hints: hints
            });
        }

        console.log('Sending quiz data:', JSON.stringify(quizData, null, 2));

        fetch('/admin/create-quiz', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(quizData)
        })
            .then(response => {
                if (response.ok) {
                    return response.text().then(text => {
                        showMessage('✅ ' + text, 'success');
                        // Clear form immediately after successful save
                        document.getElementById('quizForm').reset();
                        // Hide success message after 3 seconds
                        setTimeout(() => {
                            document.getElementById('message').style.display = 'none';
                        }, 3000);
                    });
                } else {
                    return response.text().then(text => {
                        showMessage('❌ Fehler: ' + text, 'error');
                    });
                }
            })
            .catch(error => {
                showMessage('❌ Netzwerkfehler: ' + error, 'error');
                console.error('Error:', error);
            });
    });
}

function showMessage(text, type) {
    const messageDiv = document.getElementById('message');
    if (messageDiv) {
        messageDiv.textContent = text;
        messageDiv.className = 'message ' + type;
        messageDiv.style.display = 'block';
    }
}

function goBack() {
    window.location.href = 'admin_main.html';
}
