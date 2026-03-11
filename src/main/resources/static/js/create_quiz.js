const questionsContainer = document.getElementById('questionsContainer');

// Global variable to track edit mode
let editingQuizId = null;

// Check for editing quiz data in sessionStorage on page load
document.addEventListener('DOMContentLoaded', function () {
    const editingData = sessionStorage.getItem('editingQuiz');
    if (editingData) {
        const quiz = JSON.parse(editingData);
        sessionStorage.removeItem('editingQuiz'); // Clear after reading
        populateFormForEdit(quiz);
    }
});

function populateFormForEdit(quiz) {
    editingQuizId = quiz.quizId;

    // Set the pubDate
    const pubDateInput = document.getElementById('pubDate');
    if (pubDateInput) {
        pubDateInput.value = quiz.pubDate;
    }

    // Set the page title and submit button
    const title = document.querySelector('h1');
    if (title) {
        title.textContent = 'Quiz bearbeiten';
    }
    const submitBtn = document.querySelector('#quizForm button[type="submit"]');
    if (submitBtn) {
        submitBtn.textContent = 'Speichern';
    }

    // Fill in questions
    if (quiz.questions) {
        quiz.questions.forEach(q => {
            const qNum = q.number;
            const quizInput = document.getElementById(`quiz${qNum}`);
            if (quizInput) quizInput.value = q.questionText || '';

            const answerInput = document.getElementById(`answer${qNum}`);
            if (answerInput) answerInput.value = q.answer || '';

            const noteInput = document.getElementById(`note${qNum}`);
            if (noteInput) noteInput.value = q.note || '';

            // Fill hints
            if (q.hints) {
                q.hints.forEach((h, idx) => {
                    const hNum = idx + 1;
                    const hintInput = document.getElementById(`hint${qNum}_${hNum}`);
                    if (hintInput && h.hintText) {
                        hintInput.value = h.hintText;
                    }
                    // Show existing image if present
                    if (h.imageUrl) {
                        const preview = document.getElementById(`preview_q${qNum}_${hNum}`);
                        if (preview) {
                            preview.src = h.imageUrl;
                            preview.style.display = 'block';
                        }
                    }
                });
            }
        });
    }
}

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
                    <label>Hinweis ${i}.${j}:</label>
                    <input type="text" id="hint${i}_${j}" placeholder="Text (optional)">
                    <input type="file" id="hint_image_q${i}_h${j}" accept="image/*"
                           onchange="previewHintImage(this, 'preview_q${i}_h${j}')">
                    <img id="preview_q${i}_h${j}" src="" alt="" style="display:none; max-height:80px; margin-top:4px;">
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

        const quizData = {
            pubDate: document.getElementById('pubDate').value || null,
            questions: []
        };

        const formData = new FormData();

        // Collect all 8 questions
        for (let i = 1; i <= 8; i++) {
            const numHints = i <= 4 ? 4 : 3;
            const hints = [];

            for (let j = 1; j <= numHints; j++) {
                hints.push({
                    hintText: document.getElementById(`hint${i}_${j}`).value || null,
                    imageUrl: null  // will be set by backend after file save
                });

                // Attach image file if selected
                const fileInput = document.getElementById(`hint_image_q${i}_h${j}`);
                if (fileInput && fileInput.files[0]) {
                    formData.append(`hint_image_q${i}_h${j}`, fileInput.files[0]);
                }
            }

            quizData.questions.push({
                number: i,
                questionText: document.getElementById(`quiz${i}`).value,
                answer: document.getElementById(`answer${i}`).value,
                note: document.getElementById(`note${i}`).value || null,
                hints: hints
            });
        }

        formData.append('quiz', new Blob([JSON.stringify(quizData)], {type: 'application/json'}));
        console.log('Sending quiz data:', JSON.stringify(quizData, null, 2));

        const url = editingQuizId ? `/admin/quiz/${editingQuizId}` : '/admin/create-quiz';
        const method = editingQuizId ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            body: formData
            // No Content-Type header — browser sets it with boundary automatically
        })
            .then(response => {
                if (response.ok) {
                    return response.text().then(text => {
                        if (editingQuizId) {
                            // In edit mode, redirect to admin page
                            showMessage('✅ Quiz erfolgreich aktualisiert!', 'success');
                            setTimeout(() => {
                                window.location.href = 'admin_main.html';
                            }, 1000);
                        } else {
                            // In create mode, clear form
                            showMessage('✅ ' + text, 'success');
                            // Clear form immediately after successful save
                            document.getElementById('quizForm').reset();
                            // Clear image previews
                            document.querySelectorAll('img[id^="preview_q"]').forEach(img => {
                                img.src = '';
                                img.style.display = 'none';
                            });
                            // Hide success message after 3 seconds
                            setTimeout(() => {
                                document.getElementById('message').style.display = 'none';
                            }, 3000);
                        }
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

function previewHintImage(input, previewId) {
    const preview = document.getElementById(previewId);
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = e => {
            preview.src = e.target.result;
            preview.style.display = 'block';
        };
        reader.readAsDataURL(input.files[0]);
    } else {
        preview.src = '';
        preview.style.display = 'none';
    }
}

function goBack() {
    window.location.href = 'admin_main.html';
}
