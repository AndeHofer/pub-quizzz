export function numberBadge(n: number): string {
    return `<span class="inline-flex items-center justify-center w-7 h-7 rounded-full bg-blue-600 text-white text-xs font-bold">${n}</span>`;
}

export function buildToggleButtonHtml(detailRowId: string): string {
    return `<button type="button" data-action="toggle-detail" data-target-row="${detailRowId}" class="icon-btn w-6 h-6 text-gray-500 hover:bg-transparent hover:text-gray-700">+</button>`;
}

export function toggleDetailRow(button: HTMLButtonElement, container: ParentNode = document): void {
    const targetRowId = button.getAttribute('data-target-row');
    if (!targetRowId) return;

    const row = container.querySelector<HTMLElement>(`#${CSS.escape(targetRowId)}`);
    if (!row) return;

    const isHidden = row.style.display === 'none';
    row.style.display = isHidden ? 'table-row' : 'none';
    button.innerHTML = isHidden ? '&minus;' : '+';
}

export function wireDetailToggleButtons(container: HTMLElement): void {
    container.addEventListener('click', event => {
        const target = event.target as HTMLElement | null;
        if (!target) return;

        const button = target.closest('button[data-action="toggle-detail"]') as HTMLButtonElement | null;
        if (!button || !container.contains(button)) return;

        toggleDetailRow(button, container);
    });
}
