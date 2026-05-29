export {};

import { showMessage, goBack } from './utils';
import type {QuizDTO, QuizDocumentDTO} from './types';
import {withEnsuredCsrfHeaders} from './csrf';
import {buildDocumentListMarkup} from './create_quiz_documents';

const questionsContainer = document.getElementById('questionsContainer') as HTMLDivElement | null;

// Global variable to track edit mode
let editingQuizId: number | null = null;

// Stores existing image URLs for hints when editing a quiz.
// Key format: "atstart_q{q}_h{h}" or "ashint_q{q}_h{h}"
const existingImageUrls = new Map<string, string>();
const existingAnswerImageUrls = new Map<number, string>();

// ── Readiness helpers ────────────────────────────────────────────────────────

function numHintsFor(q: number): number {
    return q <= 4 ? 4 : 3;
}

/**
 * Mirrors QuizFinishedChecker.isFinished() for a single question:
 * - questionText non-blank
 * - q1-4: answer non-blank
 * - q5-8: answer non-blank OR answer image present
 * - every hint has non-blank hintText OR a selected/existing imageUrlAsHint file
 */
function isQuestionReady(i: number): boolean {
    const questionInput = document.getElementById(`quiz${i}`) as HTMLInputElement | null;
    if (!questionInput || !questionInput.value.trim()) return false;

    const answerInput = document.getElementById(`answer${i}`) as HTMLInputElement | null;
    const hasAnswerText = !!(answerInput && answerInput.value.trim());
    const answerImageInput = document.getElementById(`answer_image_q${i}`) as HTMLInputElement | null;
    const hasAnswerFile = !!(answerImageInput?.files && answerImageInput.files[0]);
    const hasExistingAnswerImage = !!existingAnswerImageUrls.get(i);

    if (i <= 4) {
        if (!hasAnswerText) return false;
    } else {
        if (!hasAnswerText && !hasAnswerFile && !hasExistingAnswerImage) return false;
    }

    const n = numHintsFor(i);
    for (let j = 1; j <= n; j++) {
        const hintText = (document.getElementById(`hint${i}_${j}`) as HTMLInputElement | null)?.value.trim() ?? '';
        const asHintInput = document.getElementById(`hint_ashint_q${i}_h${j}`) as HTMLInputElement | null;
        const hasFile = !!(asHintInput?.files && asHintInput.files[0]);
        const hasExisting = !!existingImageUrls.get(`ashint_q${i}_h${j}`);
        if (!hintText && !hasFile && !hasExisting) return false;
    }
    return true;
}

function updateStatus(i: number): void {
    const statusEl = document.getElementById(`status${i}`);
    if (statusEl) {
        statusEl.textContent = isQuestionReady(i) ? '✅' : '❌';
    }
}

// ── Section generation ───────────────────────────────────────────────────────

// Questions 1-4 have 4 hints, 5-8 have 3 hints.
if (questionsContainer) {
    for (let i = 1; i <= 8; i++) {
        const section = document.createElement('div');
        section.className = 'section';

        const n = numHintsFor(i);

        let hintsHtml = '';
        for (let j = 1; j <= n; j++) {
            hintsHtml += `
                <div class="field-group">
                    <label>Hinweis ${i}.${j}:</label>
                    <input type="text" id="hint${i}_${j}" placeholder="Text (optional)">
                    <label style="font-size:0.85em; margin-top:4px;">Bild: Am Anfang</label>
                    <input type="file" id="hint_atstart_q${i}_h${j}" accept="image/*">
                    <img id="preview_atstart_q${i}_h${j}" src="" alt="" style="display:none; max-height:80px; margin-top:4px;">
                    <label style="font-size:0.85em; margin-top:4px;">Bild: Als Hinweis</label>
                    <input type="file" id="hint_ashint_q${i}_h${j}" accept="image/*">
                    <img id="preview_ashint_q${i}_h${j}" src="" alt="" style="display:none; max-height:80px; margin-top:4px;">
                </div>`;
        }

        const answerImageHtml = i >= 5 ? `
                <div class="field-group">
                    <label style="font-size:0.85em; margin-top:4px;">Antwortbild ${i} (optional):</label>
                    <input type="file" id="answer_image_q${i}" accept="image/*">
                    <img id="preview_answer_q${i}" src="" alt="" style="display:none; max-height:80px; margin-top:4px;">
                </div>` : '';

        section.innerHTML = `
            <div class="question-toggle" id="toggle${i}">
                <span id="status${i}">❌</span>
                <h2>Frage ${i}</h2>
                <span class="question-arrow" id="arrow${i}">▶</span>
            </div>
            <div class="question-body" id="body${i}" style="display:none">
                <div class="field-group">
                    <label for="quiz${i}">Frage ${i}:</label>
                    <input type="text" id="quiz${i}">
                </div>
                ${hintsHtml}
                <div class="field-group">
                    <label for="answer${i}">Antwort ${i}:</label>
                    <input type="text" id="answer${i}">
                </div>
                ${answerImageHtml}
                <div class="field-group">
                    <label for="note${i}">Anmerkung ${i} (optional):</label>
                    <input type="text" id="note${i}">
                </div>
            </div>
        `;
        questionsContainer.appendChild(section);

        // ── Fold toggle ──────────────────────────────────────────────────────
        const toggle = document.getElementById(`toggle${i}`) as HTMLDivElement;
        const body = document.getElementById(`body${i}`) as HTMLDivElement;
        const arrow = document.getElementById(`arrow${i}`) as HTMLSpanElement;

        toggle.addEventListener('click', () => {
            const isOpen = body.style.display !== 'none';
            body.style.display = isOpen ? 'none' : 'block';
            arrow.classList.toggle('open', !isOpen);
        });

        // ── Status update on text input ──────────────────────────────────────
        const textIds = [`quiz${i}`, `answer${i}`, `note${i}`];
        for (let j = 1; j <= n; j++) textIds.push(`hint${i}_${j}`);
        textIds.forEach(id => {
            document.getElementById(id)?.addEventListener('input', () => updateStatus(i));
        });

        // ── Image preview + status update on file change ─────────────────────
        for (let j = 1; j <= n; j++) {
            const atStartInput = document.getElementById(`hint_atstart_q${i}_h${j}`) as HTMLInputElement | null;
            if (atStartInput) {
                atStartInput.addEventListener('change', () => {
                    previewHintImage(atStartInput, `preview_atstart_q${i}_h${j}`);
                    // atstart does not affect readiness — no updateStatus needed
                });
            }

            const asHintInput = document.getElementById(`hint_ashint_q${i}_h${j}`) as HTMLInputElement | null;
            if (asHintInput) {
                asHintInput.addEventListener('change', () => {
                    previewHintImage(asHintInput, `preview_ashint_q${i}_h${j}`);
                    updateStatus(i);
                });
            }
        }

        if (i >= 5) {
            const answerImageInput = document.getElementById(`answer_image_q${i}`) as HTMLInputElement | null;
            if (answerImageInput) {
                answerImageInput.addEventListener('change', () => {
                    previewHintImage(answerImageInput, `preview_answer_q${i}`);
                    updateStatus(i);
                });
            }
        }
    }
}

// ── Edit mode population ─────────────────────────────────────────────────────

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

    // Set the pubDate
    const pubDateInput = document.getElementById('pubDate') as HTMLInputElement | null;
    if (pubDateInput) {
        pubDateInput.value = quiz.pubDate ?? '';
    }

    const creatorInput = document.getElementById('creator') as HTMLInputElement | null;
    if (creatorInput) {
        creatorInput.value = quiz.creator ?? '';
    }

    setEditModeUi();

    // Fill in questions
    if (quiz.questions) {
        quiz.questions.forEach(q => {
            const qNum = q.number;
            const quizInput = document.getElementById(`quiz${qNum}`) as HTMLInputElement | null;
            if (quizInput) quizInput.value = q.questionText || '';

            const answerInput = document.getElementById(`answer${qNum}`) as HTMLInputElement | null;
            if (answerInput) answerInput.value = q.answer || '';

            if (qNum >= 5 && q.answerImageUrl) {
                existingAnswerImageUrls.set(qNum, q.answerImageUrl);
                const preview = document.getElementById(`preview_answer_q${qNum}`) as HTMLImageElement | null;
                if (preview) {
                    preview.src = q.answerImageUrl;
                    preview.style.display = 'block';
                }
            }

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
                        existingImageUrls.set(`atstart_q${qNum}_h${hNum}`, h.imageUrlAtStart);
                        const preview = document.getElementById(`preview_atstart_q${qNum}_h${hNum}`) as HTMLImageElement | null;
                        if (preview) {
                            preview.src = h.imageUrlAtStart;
                            preview.style.display = 'block';
                        }
                    }
                    // Show existing "als Hinweis" image if present
                    if (h.imageUrlAsHint) {
                        existingImageUrls.set(`ashint_q${qNum}_h${hNum}`, h.imageUrlAsHint);
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

    // Update all status indicators after filling in values
    for (let i = 1; i <= 8; i++) updateStatus(i);

    // Show the documents section and load existing documents
    const docsSection = document.getElementById('documentsSection');
    if (docsSection) docsSection.style.display = 'block';
    loadDocuments();
}

function setEditModeUi(): void {
    const title = document.querySelector('h1');
    if (title) {
        title.textContent = 'Quiz bearbeiten';
    }
    const submitBtn = document.querySelector('#quizForm button[type="submit"]') as HTMLButtonElement | null;
    if (submitBtn) {
        submitBtn.textContent = 'Speichern';
    }
}

// ── Form submit ───────────────────────────────────────────────────────────────

const quizForm = document.getElementById('quizForm') as HTMLFormElement | null;
if (quizForm) {
    quizForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const quizData: {
            pubDate: string | null;
            creator: string | null;
            questions: {
                number: number;
                questionText: string;
                answer: string;
                answerImageUrl: string | null;
                note: string | null;
                hints: { hintText: string | null; imageUrlAtStart: string | null; imageUrlAsHint: string | null }[]
            }[]
        } = {
            pubDate: (document.getElementById('pubDate') as HTMLInputElement).value || null,
            creator: (document.getElementById('creator') as HTMLInputElement).value.trim() || null,
            questions: []
        };

        const formData = new FormData();

        // Collect all 8 questions
        for (let i = 1; i <= 8; i++) {
            const n = numHintsFor(i);
            const hints: {
                hintText: string | null;
                imageUrlAtStart: string | null;
                imageUrlAsHint: string | null
            }[] = [];

            const answerImageInput = document.getElementById(`answer_image_q${i}`) as HTMLInputElement | null;
            const answerImageUrl = (answerImageInput && answerImageInput.files && answerImageInput.files[0])
                ? null
                : (existingAnswerImageUrls.get(i) ?? null);

            for (let j = 1; j <= n; j++) {
                const atStartInput = document.getElementById(`hint_atstart_q${i}_h${j}`) as HTMLInputElement;
                const asHintInput = document.getElementById(`hint_ashint_q${i}_h${j}`) as HTMLInputElement;

                // If no new file selected, carry forward the existing URL (edit mode)
                const imageUrlAtStart = (atStartInput && atStartInput.files && atStartInput.files[0])
                    ? null  // will be set by backend after file save
                    : (existingImageUrls.get(`atstart_q${i}_h${j}`) ?? null);

                const imageUrlAsHint = (asHintInput && asHintInput.files && asHintInput.files[0])
                    ? null  // will be set by backend after file save
                    : (existingImageUrls.get(`ashint_q${i}_h${j}`) ?? null);

                hints.push({
                    hintText: (document.getElementById(`hint${i}_${j}`) as HTMLInputElement).value || null,
                    imageUrlAtStart,
                    imageUrlAsHint
                });

                // Attach "am Anfang" image file if selected
                if (atStartInput && atStartInput.files && atStartInput.files[0]) {
                    formData.append(`hint_atstart_q${i}_h${j}`, atStartInput.files[0]);
                }

                // Attach "als Hinweis" image file if selected
                if (asHintInput && asHintInput.files && asHintInput.files[0]) {
                    formData.append(`hint_ashint_q${i}_h${j}`, asHintInput.files[0]);
                }
            }

            quizData.questions.push({
                number: i,
                questionText: (document.getElementById(`quiz${i}`) as HTMLInputElement).value,
                answer: (document.getElementById(`answer${i}`) as HTMLInputElement).value,
                answerImageUrl,
                note: (document.getElementById(`note${i}`) as HTMLInputElement).value || null,
                hints: hints
            });

            if (answerImageInput && answerImageInput.files && answerImageInput.files[0]) {
                formData.append(`answer_image_q${i}`, answerImageInput.files[0]);
            }
        }

        formData.append('quiz', new Blob([JSON.stringify(quizData)], {type: 'application/json'}));

        const url = editingQuizId ? `/admin/quiz/${editingQuizId}` : '/admin/create-quiz';
        const method = editingQuizId ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            headers: await withEnsuredCsrfHeaders(),
            body: formData
            // No Content-Type header — browser sets it with boundary automatically
        })
            .then(response => {
                if (response.ok) {
                    if (editingQuizId) {
                        showMessage('✅ Quiz erfolgreich aktualisiert!', 'success');
                        setTimeout(() => {
                            const msg = document.getElementById('message');
                            if (msg) msg.style.display = 'none';
                        }, 3000);
                        return;
                    }

                    return response.json()
                        .then((createdQuiz: QuizDTO) => {
                            if (typeof createdQuiz.quizId === 'number') {
                                editingQuizId = createdQuiz.quizId;
                                setEditModeUi();
                                const docsSection = document.getElementById('documentsSection');
                                if (docsSection) docsSection.style.display = 'block';
                                loadDocuments();
                            }
                            showMessage('✅ Quiz erfolgreich gespeichert!', 'success');
                            setTimeout(() => {
                                const msg = document.getElementById('message');
                                if (msg) msg.style.display = 'none';
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

// ── Global wiring ─────────────────────────────────────────────────────────────

window.addEventListener('load', () => {
    document.getElementById('backBtn')?.addEventListener('click', () => goBack('admin_main.html'));
    document.getElementById('uploadDocumentBtn')?.addEventListener('click', uploadDocument);
});

// ── Document management ───────────────────────────────────────────────────────

async function loadDocuments(): Promise<void> {
    if (!editingQuizId) return;
    const listEl = document.getElementById('documentList');
    if (!listEl) return;
    try {
        const resp = await fetch(`/admin/quiz/${editingQuizId}/documents`);
        if (!resp.ok) {
            listEl.innerHTML = '<span style="color:red;">Fehler beim Laden der Dokumente.</span>';
            return;
        }
        const docs: QuizDocumentDTO[] = await resp.json();
        if (docs.length === 0) {
            listEl.innerHTML = '<p style="color:#888; font-size:0.875rem;">Keine Dokumente vorhanden.</p>';
            return;
        }
        listEl.innerHTML = buildDocumentListMarkup(editingQuizId, docs);
        listEl.querySelectorAll('.delete-doc-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number((btn as HTMLElement).dataset.id);
                deleteDocument(id);
            });
        });
    } catch (err) {
        if (listEl) listEl.innerHTML = '<span style="color:red;">Netzwerkfehler beim Laden.</span>';
        console.error('loadDocuments error:', err);
    }
}

async function uploadDocument(): Promise<void> {
    if (!editingQuizId) return;
    const fileInput = document.getElementById('documentFileInput') as HTMLInputElement | null;
    const msgEl = document.getElementById('documentMessage') as HTMLElement | null;
    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
        if (msgEl) {
            msgEl.textContent = 'Bitte eine Datei auswählen.';
            msgEl.style.display = 'block';
        }
        return;
    }
    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('file', file);
    if (msgEl) {
        msgEl.textContent = 'Hochladen...';
        msgEl.style.display = 'block';
    }
    try {
        const resp = await fetch(`/admin/quiz/${editingQuizId}/documents`, {
            method: 'POST',
            headers: await withEnsuredCsrfHeaders(),
            body: formData
        });
        if (resp.ok) {
            fileInput.value = '';
            if (msgEl) {
                msgEl.textContent = '';
                msgEl.style.display = 'none';
            }
            await loadDocuments();
        } else {
            const text = await resp.text();
            if (msgEl) {
                msgEl.textContent = 'Fehler: ' + text;
                msgEl.style.display = 'block';
            }
        }
    } catch (err) {
        if (msgEl) {
            msgEl.textContent = 'Netzwerkfehler: ' + err;
            msgEl.style.display = 'block';
        }
        console.error('uploadDocument error:', err);
    }
}

async function deleteDocument(docId: number): Promise<void> {
    if (!editingQuizId) return;
    if (!confirm('Dokument wirklich löschen?')) return;
    try {
        const resp = await fetch(`/admin/quiz/${editingQuizId}/documents/${docId}`, {
            method: 'DELETE',
            headers: await withEnsuredCsrfHeaders()
        });
        if (resp.ok) {
            await loadDocuments();
        } else {
            const text = await resp.text();
            console.error('deleteDocument error:', text);
        }
    } catch (err) {
        console.error('deleteDocument network error:', err);
    }
}

// ── Image preview helper ──────────────────────────────────────────────────────

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
