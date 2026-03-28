export {};

import { showMessage, goBack } from './utils';
import type { QuizDTO } from './types';

const questionsContainer = document.getElementById('questionsContainer') as HTMLDivElement | null;

// Global variable to track edit mode
let editingQuizId: number | null = null;

// Check for editing quiz data in sessionStorage on page load
document.addEventListener('DOMContentLoaded', function () {
    const editingData = sessionStorage.getItem('editingQuiz');
    if (editingData) {
        const quiz: QuizDTO = JSON.parse(editingData);
        sessionStorage.removeItem('editingQuiz'); // Clear after reading
        populateFormForEdit(quiz);
    }
});

function populateFormForEdit(quiz: QuizDTO) {
    editingQuizId = quiz.quizId;

    // Set the title
    const titleInput = document.getElementById('quizTitle') as HTMLInputElement | null;
    if (titleInput) {
        titleInput.value = quiz.title ?? '';
    }

    // Set the pubDate
    const pubDateInput = document.getElementById('pubDate') as HTMLInputElement | null;
    if (pubDateInput) {
        pubDateInput.value = quiz.pubDate ?? '';
    }

    // Set the page title and submit button
    const title = document.querySelector('h1');
    if (title) {
        title.textContent = 'Quiz bearbeiten';
    }
    const submitBtn = document.querySelector('#quizForm button[type="submit"]') as HTMLButtonElement | null;
    if (submitBtn) {
        submitBtn.textContent = 'Speichern';
    }

    // Fill in questions
    if (quiz.questions) {
        quiz.questions.forEach(q => {
            const qNum = q.number;
            const quizInput = document.getElementById(`quiz${qNum}`) as HTMLInputElement | null;
            if (quizInput) quizInput.value = q.questionText || '';

            const answerInput = document.getElementById(`answer${qNum}`) as HTMLInputElement | null;
            if (answerInput) answerInput.value = q.answer || '';

            const noteInput = document.getElementById(`note${qNum}`) as HTMLInputElement | null;
            if (noteInput) noteInput.value = q.note || '';

            // Fill hints
            if (q.hints) {
                q.hints.forEach((h, idx) => {
                    const hNum = idx + 1;
                    const hintInput = document.getElementById(`hint${qNum}_${hNum}`) as HTMLInputElement | null;
                    if (hintInput && h.hintText) {
                        hintInput.value = h.hintText;
                    }
                    // Show existing "am Anfang" image if present
                    if (h.imageUrlAtStart) {
                        const preview = document.getElementById(`preview_atstart_q${qNum}_h${hNum}`) as HTMLImageElement | null;
                        if (preview) {
                            preview.src = h.imageUrlAtStart;
                            preview.style.display = 'block';
                        }
                    }
                    // Show existing "als Hinweis" image if present
                    if (h.imageUrlAsHint) {
                        const preview = document.getElementById(`preview_ashint_q${qNum}_h${hNum}`) as HTMLImageElement | null;
                        if (preview) {
                            preview.src = h.imageUrlAsHint;
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
                    <label style="font-size:0.85em; margin-top:4px;">Bild: Am Anfang</label>
                    <input type="file" id="hint_atstart_q${i}_h${j}" accept="image/*"
                           onchange="previewHintImage(this, 'preview_atstart_q${i}_h${j}')">
                    <img id="preview_atstart_q${i}_h${j}" src="" alt="" style="display:none; max-height:80px; margin-top:4px;">
                    <label style="font-size:0.85em; margin-top:4px;">Bild: Als Hinweis</label>
                    <input type="file" id="hint_ashint_q${i}_h${j}" accept="image/*"
                           onchange="previewHintImage(this, 'preview_ashint_q${i}_h${j}')">
                    <img id="preview_ashint_q${i}_h${j}" src="" alt="" style="display:none; max-height:80px; margin-top:4px;">
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

const quizForm = document.getElementById('quizForm') as HTMLFormElement | null;
if (quizForm) {
    quizForm.addEventListener('submit', function (e) {
        e.preventDefault();

        const quizData: {
            title: string | null;
            pubDate: string | null;
            questions: {
                number: number;
                questionText: string;
                answer: string;
                note: string | null;
                hints: { hintText: string | null; imageUrlAtStart: null; imageUrlAsHint: null }[]
            }[]
        } = {
            title: (document.getElementById('quizTitle') as HTMLInputElement).value || null,
            pubDate: (document.getElementById('pubDate') as HTMLInputElement).value || null,
            questions: []
        };

        const formData = new FormData();

        // Collect all 8 questions
        for (let i = 1; i <= 8; i++) {
            const numHints = i <= 4 ? 4 : 3;
            const hints: { hintText: string | null; imageUrlAtStart: null; imageUrlAsHint: null }[] = [];

            for (let j = 1; j <= numHints; j++) {
                hints.push({
                    hintText: (document.getElementById(`hint${i}_${j}`) as HTMLInputElement).value || null,
                    imageUrlAtStart: null,  // will be set by backend after file save
                    imageUrlAsHint: null    // will be set by backend after file save
                });

                // Attach "am Anfang" image file if selected
                const atStartInput = document.getElementById(`hint_atstart_q${i}_h${j}`) as HTMLInputElement;
                if (atStartInput && atStartInput.files && atStartInput.files[0]) {
                    formData.append(`hint_atstart_q${i}_h${j}`, atStartInput.files[0]);
                }

                // Attach "als Hinweis" image file if selected
                const asHintInput = document.getElementById(`hint_ashint_q${i}_h${j}`) as HTMLInputElement;
                if (asHintInput && asHintInput.files && asHintInput.files[0]) {
                    formData.append(`hint_ashint_q${i}_h${j}`, asHintInput.files[0]);
                }
            }

            quizData.questions.push({
                number: i,
                questionText: (document.getElementById(`quiz${i}`) as HTMLInputElement).value,
                answer: (document.getElementById(`answer${i}`) as HTMLInputElement).value,
                note: (document.getElementById(`note${i}`) as HTMLInputElement).value || null,
                hints: hints
            });
        }

        formData.append('quiz', new Blob([JSON.stringify(quizData)], {type: 'application/json'}));

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
                            quizForm.reset();
                            // Clear image previews
                            document.querySelectorAll('img[id^="preview_"]').forEach(img => {
                                (img as HTMLImageElement).src = '';
                                (img as HTMLImageElement).style.display = 'none';
                            });
                            // Hide success message after 3 seconds
                            setTimeout(() => {
                                const msg = document.getElementById('message');
                                if (msg) msg.style.display = 'none';
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

// Ensure functions are available globally
window.addEventListener('load', () => {
    (window as any).previewHintImage = previewHintImage;
    (window as any).goBack = () => goBack('admin_main.html');
});

function previewHintImage(input: HTMLInputElement, previewId: string) {
    const preview = document.getElementById(previewId) as HTMLImageElement | null;
    if (preview && input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = e => {
            if (e.target && typeof e.target.result === 'string') {
                preview.src = e.target.result;
                preview.style.display = 'block';
            }
        };
        reader.readAsDataURL(input.files[0]);
    } else if (preview) {
        preview.src = '';
        preview.style.display = 'none';
    }
}
