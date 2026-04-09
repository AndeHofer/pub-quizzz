import {QuizDetailResponse, QuizSummaryDTO} from './types';

let allDetailsExpanded = false;

function escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function questionSort(a: { number: number }, b: { number: number }): number {
    return a.number - b.number;
}

function hintImage(imageUrl: string | null | undefined): string {
    if (!imageUrl) {
        return '';
    }
    return `
        <div class="mt-2">
            <img src="${encodeURI(imageUrl)}" class="rounded-md border border-gray-200 max-h-64 w-auto">
        </div>
    `;
}

function createCollapsibleBlock(
    id: string,
    buttonLabel: string,
    content: string,
    wrapperClasses = 'rounded-md border border-gray-200 bg-gray-50 p-1',
    revealType: 'text' | 'media' = 'text'
): string {
    if (revealType === 'text') {
        return `
                <div class="${wrapperClasses} min-h-13">
                        <button type="button"
                                class="h-auto py-0.5 w-full text-left text-sm leading-5 font-medium bg-white text-black border border-gray-300 rounded-b-none px-2 flex items-center hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-300"
                                style="margin:0"
                                data-action="toggle-one"
                                data-target="${id}"
                                aria-controls="${id}"
                                aria-expanded="false">
                            ${buttonLabel} anzeigen
                        </button>
                        <div id="${id}" 
                            class="h-auto min-h-fit px-2 text-sm leading-normal flex items-center justify-start text-left text" 
                            data-reveal-type="text" 
                            data-expanded="false" 
                            style="display:none; opacity:0; visibility:hidden; overflow:visible;">
                            ${content}
                        </div>
                </div>
            `;
    }

    return `
        <div class="${wrapperClasses}">
            <button type="button"
                    class="w-full text-left text-sm font-medium bg-white text-black border border-gray-300 rounded-md px-2 py-1 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-300"
                    data-action="toggle-one"
                    data-target="${id}"
                    aria-controls="${id}"
                    aria-expanded="false">
                ${buttonLabel} anzeigen
            </button>
            <div id="${id}" data-reveal-type="media" data-expanded="false" style="display:none; opacity:0; overflow:hidden;">
                ${content}
            </div>
        </div>
    `;
}

function setToggleButtonText(button: HTMLButtonElement, expanded: boolean, baseLabel: string): void {
    button.textContent = `${baseLabel} ${expanded ? 'ausblenden' : 'anzeigen'}`;
    button.setAttribute('aria-expanded', expanded ? 'true' : 'false');
}

function setPerItemButtonsVisibility(show: boolean): void {
    const toggleButtons = document.querySelectorAll<HTMLButtonElement>('button[data-action="toggle-one"]');
    toggleButtons.forEach(button => {
        button.style.display = show ? 'block' : 'none';
    });
}

function expandContent(element: HTMLElement): void {
    const revealType = element.getAttribute('data-reveal-type');
    if (revealType === 'text') {
        element.style.display = 'block';
        element.style.visibility = 'visible';
        element.style.opacity = '1';
        element.style.maxHeight = '2.25rem';
        element.style.overflow = 'visible';
        element.style.transition = 'opacity 180ms ease';
        element.setAttribute('data-expanded', 'true');
        return;
    }

    element.style.display = 'block';
    element.style.overflow = 'hidden';
    element.style.maxHeight = '0px';
    element.style.opacity = '0';
    element.style.transition = 'max-height 220ms ease, opacity 220ms ease';
    void element.offsetHeight;
    element.style.maxHeight = `${element.scrollHeight}px`;
    element.style.opacity = '1';

    window.setTimeout(() => {
        element.style.maxHeight = 'none';
        element.style.overflow = 'visible';
    }, 240);
    element.setAttribute('data-expanded', 'true');
}

function collapseContent(element: HTMLElement): void {
    const revealType = element.getAttribute('data-reveal-type');

    if (revealType === 'text') {
        element.style.display = 'none';
        element.style.visibility = 'hidden';
        element.style.opacity = '0';
        element.style.maxHeight = '';
        element.style.overflow = 'hidden';
        element.style.transition = 'opacity 160ms ease';
        element.setAttribute('data-expanded', 'false');
        return;
    }

    if (element.style.display === 'none') {
        return;
    }
    element.style.overflow = 'hidden';
    element.style.maxHeight = `${element.scrollHeight}px`;
    element.style.opacity = '1';
    element.style.transition = 'max-height 200ms ease, opacity 180ms ease';
    void element.offsetHeight;
    element.style.maxHeight = '0px';
    element.style.opacity = '0';

    window.setTimeout(() => {
        element.style.display = 'none';
        element.style.maxHeight = '';
    }, 210);
    element.setAttribute('data-expanded', 'false');
}

function isExpanded(element: HTMLElement): boolean {
    return element.getAttribute('data-expanded') === 'true';
}

function updateGlobalToggleButton(): void {
    const button = document.getElementById('toggleAllButton') as HTMLButtonElement | null;
    if (!button) return;
    button.textContent = allDetailsExpanded ? 'Alle ausblenden' : 'Alle aufdecken';
}

function applyGlobalExpansion(expand: boolean): void {
    const contentNodes = document.querySelectorAll<HTMLElement>('[id^="collapse-"]');
    contentNodes.forEach(node => {
        if (expand) {
            expandContent(node);
        } else {
            collapseContent(node);
        }
    });

    const toggleButtons = document.querySelectorAll<HTMLButtonElement>('button[data-action="toggle-one"]');
    toggleButtons.forEach(button => {
        const baseLabel = button.getAttribute('data-base-label') ?? 'Details';
        setToggleButtonText(button, false, baseLabel);
    });

    setPerItemButtonsVisibility(!expand);

    allDetailsExpanded = expand;
    updateGlobalToggleButton();
}

function renderQuizDetail(quiz: QuizDetailResponse): void {
    const emptyEl = document.getElementById('emptyState');
    const questionsEl = document.getElementById('questionsContainer');
    const detailActionsEl = document.getElementById('detailActions');

    if (!emptyEl || !questionsEl || !detailActionsEl) {
        return;
    }

    const questions = (quiz.questions ?? []).slice().sort(questionSort);
    if (questions.length === 0) {
        emptyEl.style.display = 'block';
        emptyEl.textContent = 'Dieses Quiz enthält keine Fragen.';
        detailActionsEl.style.display = 'none';
        questionsEl.style.display = 'none';
        questionsEl.innerHTML = '';
        return;
    }

    emptyEl.style.display = 'none';
    detailActionsEl.style.display = 'block';
    allDetailsExpanded = false;
    updateGlobalToggleButton();

    const html = questions.map(question => {
        const hints = question.hints ?? [];
        const hintsHtml = hints.length === 0
            ? '<p class="text-gray-500 text-sm">Keine Hinweise vorhanden.</p>'
            : `<ol class="space-y-3">${hints.map((hint, idx) => {
                const blockId = `collapse-q${question.number}-hint-${idx + 1}`;
                const content = hint.hintText ? `
                    <span class="text-sm leading-5 text-gray-800">${escapeHtml(hint.hintText)}</span>
                    ${hintImage(hint.imageUrlAtStart)}
                    ${hintImage(hint.imageUrlAsHint)}
                ` : `
                    ${hintImage(hint.imageUrlAtStart)}
                    ${hintImage(hint.imageUrlAsHint)}
                `;

                return `
                    <li>
                        ${createCollapsibleBlock(
                    blockId,
                    `Hinweis ${idx + 1}`,
                    content,
                    'rounded-md border border-gray-200 bg-gray-50 p-3',
                    (hint.imageUrlAtStart || hint.imageUrlAsHint) ? 'media' : 'text'
                )}
                    </li>
                `;
            }).join('')}</ol>`;

        const answerText = question.answer?.trim()
            ? escapeHtml(question.answer)
            : '';
        const answerImageHtml = question.answerImageUrl
            ? `<img src="${encodeURI(question.answerImageUrl)}" alt="Antwortbild Frage ${question.number}" class="rounded-md border border-gray-200 max-h-72 w-auto"></div>`
            : '';
        const answerBlockId = `collapse-q${question.number}-answer`;
        const answerBlock = createCollapsibleBlock(
            answerBlockId,
            'Antwort',
            `<span class="text-sm leading-5 text-gray-900">${answerText}</span>${answerImageHtml}`,
            'rounded-md border border-green-200 bg-green-50 p-3',
            question.answerImageUrl ? 'media' : 'text'
        );

        const noteText = question.note?.trim();
        const noteBlock = noteText
            ? createCollapsibleBlock(
                `collapse-q${question.number}-note`,
                'Anmerkung',
                `<span class="text-sm leading-5 text-gray-900">${escapeHtml(noteText)}</span>`,
                'rounded-md border border-amber-200 bg-amber-50 p-3 mt-4',
                'text'
            )
            : '';

        return `
            <section class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-5">
                <h3 class="text-base sm:text-xl font-semibold text-gray-800 mb-2">Frage ${question.number}</h3>
                <p class="text-sm sm:text-base text-gray-900 mb-4">${escapeHtml(question.questionText ?? '')}</p>

                <div class="mb-4">
                    <h4 class="text-sm sm:text-base font-semibold text-gray-700 mb-2">Hinweise</h4>
                    ${hintsHtml}
                </div>

                <div>
                    <h4 class="text-sm sm:text-base font-semibold text-gray-700 mb-2">Antwort</h4>
                    ${answerBlock}
                </div>

                ${noteBlock}
            </section>
        `;
    }).join('');

    questionsEl.innerHTML = html;

    const toggleButtons = questionsEl.querySelectorAll<HTMLButtonElement>('button[data-action="toggle-one"]');
    toggleButtons.forEach(button => {
        const currentText = button.textContent?.trim() ?? 'Details anzeigen';
        const baseLabel = currentText.replace(/\s+anzeigen$/, '');
        button.setAttribute('data-base-label', baseLabel);
        setToggleButtonText(button, false, baseLabel);
    });

    questionsEl.style.display = 'block';
}

function setError(message: string): void {
    const errorEl = document.getElementById('errorMessage');
    if (!errorEl) return;
    errorEl.textContent = message;
    errorEl.style.display = 'block';
}

function clearError(): void {
    const errorEl = document.getElementById('errorMessage');
    if (!errorEl) return;
    errorEl.textContent = '';
    errorEl.style.display = 'none';
}

async function loadQuizDetail(quizId: number): Promise<void> {
    const loadingDetailEl = document.getElementById('loadingDetail');
    const questionsEl = document.getElementById('questionsContainer');
    const emptyEl = document.getElementById('emptyState');
    const detailActionsEl = document.getElementById('detailActions');

    clearError();
    if (loadingDetailEl) loadingDetailEl.style.display = 'block';
    if (questionsEl) {
        questionsEl.style.display = 'none';
        questionsEl.innerHTML = '';
    }
    if (detailActionsEl) detailActionsEl.style.display = 'none';
    if (emptyEl) emptyEl.style.display = 'none';

    try {
        const response = await fetch(`/api/quizzes/${encodeURIComponent(String(quizId))}/detail`);
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('Quiz nicht gefunden.');
            }
            throw new Error(`HTTP ${response.status}`);
        }
        const detail: QuizDetailResponse = await response.json();
        renderQuizDetail(detail);
    } catch (e) {
        const message = e instanceof Error && e.message === 'Quiz nicht gefunden.'
            ? e.message
            : 'Fehler beim Laden des Quiz. Bitte Seite neu laden.';
        setError(message);
        if (detailActionsEl) detailActionsEl.style.display = 'none';
        if (emptyEl) {
            emptyEl.style.display = 'block';
            emptyEl.textContent = 'Bitte ein anderes Quiz auswählen.';
        }
    } finally {
        if (loadingDetailEl) loadingDetailEl.style.display = 'none';
    }
}

function quizSort(a: QuizSummaryDTO, b: QuizSummaryDTO): number {
    const aDate = Date.parse(`${a.pubDate ?? ''}T00:00:00Z`);
    const bDate = Date.parse(`${b.pubDate ?? ''}T00:00:00Z`);
    const aValid = !Number.isNaN(aDate);
    const bValid = !Number.isNaN(bDate);

    if (aValid && bValid && aDate !== bDate) {
        return bDate - aDate;
    }
    if (aValid !== bValid) {
        return aValid ? -1 : 1;
    }
    return b.quizId - a.quizId;
}

async function loadQuizOptions(): Promise<void> {
    const loadingQuizzesEl = document.getElementById('loadingQuizzes');
    const selectEl = document.getElementById('quizSelect') as HTMLSelectElement | null;

    if (!selectEl) return;

    clearError();

    try {
        const response = await fetch('/api/quizzes');
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const quizzes: QuizSummaryDTO[] = await response.json();
        const sorted = quizzes.slice().sort(quizSort);

        if (sorted.length === 0) {
            setError('Es sind keine Quizze verfügbar.');
            selectEl.innerHTML = '<option value="">Keine Quizze verfügbar</option>';
            return;
        }

        selectEl.innerHTML = '<option value="">Bitte auswählen...</option>'
            + sorted.map(quiz => `<option value="${quiz.quizId}">${escapeHtml(quiz.quizTitle)}</option>`).join('');

        const urlParams = new URLSearchParams(window.location.search);
        const preselected = urlParams.get('id');
        if (preselected) {
            const numeric = Number(preselected);
            if (!Number.isNaN(numeric) && sorted.some(q => q.quizId === numeric)) {
                selectEl.value = String(numeric);
                await loadQuizDetail(numeric);
            }
        }
    } catch {
        setError('Fehler beim Laden der Quizliste. Bitte Seite neu laden.');
    } finally {
        if (loadingQuizzesEl) loadingQuizzesEl.style.display = 'none';
    }
}

function wireEvents(): void {
    const selectEl = document.getElementById('quizSelect') as HTMLSelectElement | null;
    const toggleAllButton = document.getElementById('toggleAllButton') as HTMLButtonElement | null;
    const questionsEl = document.getElementById('questionsContainer') as HTMLDivElement | null;

    if (!selectEl || !questionsEl) return;

    selectEl.addEventListener('change', async () => {
        const selected = Number(selectEl.value);
        if (!selected || Number.isNaN(selected)) {
            clearError();
            const detailActionsEl = document.getElementById('detailActions');
            const questionsContainerEl = document.getElementById('questionsContainer');
            const emptyEl = document.getElementById('emptyState');
            if (detailActionsEl) detailActionsEl.style.display = 'none';
            if (questionsContainerEl) {
                questionsContainerEl.style.display = 'none';
                questionsContainerEl.innerHTML = '';
            }
            if (emptyEl) {
                emptyEl.style.display = 'block';
                emptyEl.textContent = 'Bitte zuerst ein Quiz auswählen.';
            }
            return;
        }
        const url = new URL(window.location.href);
        url.searchParams.set('id', String(selected));
        window.history.replaceState({}, '', url.toString());
        await loadQuizDetail(selected);
    });

    questionsEl.addEventListener('click', (event) => {
        const target = event.target as HTMLElement | null;
        if (!target) return;
        const button = target.closest('button[data-action="toggle-one"]') as HTMLButtonElement | null;
        if (!button) return;

        const targetId = button.getAttribute('data-target');
        const baseLabel = button.getAttribute('data-base-label') ?? 'Details';
        if (!targetId) return;

        const content = document.getElementById(targetId);
        if (!content) return;

        const currentlyExpanded = isExpanded(content);
        if (!currentlyExpanded) {
            expandContent(content);
        } else {
            collapseContent(content);
        }
        setToggleButtonText(button, false, baseLabel);
        button.style.display = 'none';

        const allContent = questionsEl.querySelectorAll<HTMLElement>('[id^="collapse-"]');
        allDetailsExpanded = allContent.length > 0
            && Array.from(allContent).every(node => isExpanded(node));
        updateGlobalToggleButton();
    });

    if (toggleAllButton) {
        toggleAllButton.addEventListener('click', () => {
            applyGlobalExpansion(!allDetailsExpanded);
        });
    }
}

window.addEventListener('load', async () => {
    wireEvents();
    await loadQuizOptions();
});
