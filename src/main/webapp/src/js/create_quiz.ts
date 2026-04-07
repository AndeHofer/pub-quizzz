export {};

import { showMessage, goBack } from './utils';
import type {QuizDTO, QuizDocumentDTO} from './types';

const questionsContainer = document.getElementById('questionsContainer') as HTMLDivElement | null;

// Global variable to track edit mode
let editingQuizId: number | null = null;

// Stores existing image URLs for hints when editing a quiz.
// Key format: "atstart_q{q}_h{h}" or "ashint_q{q}_h{h}"
const existingImageUrls = new Map<string, string>();

// ── Readiness helpers ────────────────────────────────────────────────────────

function numHintsFor(q: number): number {
    return q <= 4 ? 4 : 3;
}

/**
 * Mirrors QuizFinishedChecker.isFinished() for a single question:
 * - questionText non-blank
 * - answer non-blank
 * - every hint has non-blank hintText OR a selected/existing imageUrlAsHint file
 */
function isQuestionReady(i: number): boolean {
    const questionInput = document.getElementById(`quiz${i}`) as HTMLInputElement | null;
    if (!questionInput || !questionInput.value.trim()) return false;

    const answerInput = document.getElementById(`answer${i}`) as HTMLInputElement | null;
    if (!answerInput || !answerInput.value.trim()) return false;

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

// ── Form submit ───────────────────────────────────────────────────────────────

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
                hints: { hintText: string | null; imageUrlAtStart: string | null; imageUrlAsHint: string | null }[]
            }[]
        } = {
            title: (document.getElementById('quizTitle') as HTMLInputElement).value || null,
            pubDate: (document.getElementById('pubDate') as HTMLInputElement).value || null,
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
                            quizForm.reset();
                            // Clear image previews
                            document.querySelectorAll('img[id^="preview_"]').forEach(img => {
                                (img as HTMLImageElement).src = '';
                                (img as HTMLImageElement).style.display = 'none';
                            });
                            // Reset all status indicators
                            for (let i = 1; i <= 8; i++) updateStatus(i);
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

// ── Global wiring ─────────────────────────────────────────────────────────────

window.addEventListener('load', () => {
    (window as any).goBack = () => goBack('admin_main.html');
    document.getElementById('backBtn')?.addEventListener('click', () => goBack('admin_main.html'));
    document.getElementById('uploadDocumentBtn')?.addEventListener('click', uploadDocument);
});

// ── Document management ───────────────────────────────────────────────────────

function formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

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
        let html = '<table style="width:100%; border-collapse:collapse; font-size:0.875rem;"><thead><tr>' +
            '<th style="text-align:left; padding:4px 8px; border-bottom:1px solid #e5e7eb;">Dateiname</th>' +
            '<th style="text-align:left; padding:4px 8px; border-bottom:1px solid #e5e7eb;">Größe</th>' +
            '<th style="padding:4px 8px; border-bottom:1px solid #e5e7eb;"></th>' +
            '</tr></thead><tbody>';
        docs.forEach(doc => {
            html += `<tr>
                <td style="padding:4px 8px;">
                    <a href="/admin/quiz/${editingQuizId}/documents/${doc.id}"
                       download="${doc.originalFilename}"
                       style="color:#374151; text-decoration:underline;">${doc.originalFilename}</a>
                </td>
                <td style="padding:4px 8px; color:#6b7280;">${formatFileSize(doc.fileSize)}</td>
                <td style="padding:4px 8px;">
                    <button type="button" class="delete-doc-btn"
                        data-id="${doc.id}"
                        style="background:none; border:none; cursor:pointer; color:#ef4444; font-size:1rem;"
                        title="Dokument löschen">🗑️</button>
                </td>
            </tr>`;
        });
        html += '</tbody></table>';
        listEl.innerHTML = html;
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
        const resp = await fetch(`/admin/quiz/${editingQuizId}/documents`, {method: 'POST', body: formData});
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
        const resp = await fetch(`/admin/quiz/${editingQuizId}/documents/${docId}`, {method: 'DELETE'});
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
