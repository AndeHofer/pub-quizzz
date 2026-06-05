import {escapeHtml} from './html-utils';

export type TrustedHtml = { html: string; trustedHtml: true };

export function trustedHtml(html: string): TrustedHtml {
    return {html, trustedHtml: true};
}

export function showModal(title: string, content: string): void {
    const modal = document.getElementById('dataModal') as HTMLElement | null;
    const modalContent = document.getElementById('modalContent') as HTMLElement | null;
    if (modal && modalContent) {
        modalContent.innerHTML = `<h2>${escapeHtml(title)}</h2>${content}`;
        modal.style.display = 'block';
    }
}

export function showLoading(): string {
    return '<div class="loading">Laden...</div>';
}

export function showError(message: string): string {
    return `<div class="error">❌ ${escapeHtml(message)}</div>`;
}

function renderCell(cell: string | TrustedHtml): string {
    if (typeof cell === 'string') {
        return escapeHtml(cell);
    }
    return cell.html;
}

export function renderTable(headers: string[], rows: unknown[], rowFn: (row: unknown) => Array<string | TrustedHtml>): string {
    let html = '<table><thead><tr>';
    headers.forEach(header => {
        html += `<th>${escapeHtml(header)}</th>`;
    });
    html += '</tr></thead><tbody>';
    rows.forEach(row => {
        html += '<tr>';
        rowFn(row).forEach(cell => {
            html += `<td>${renderCell(cell)}</td>`;
        });
        html += '</tr>';
    });
    html += '</tbody></table>';
    return `<div class="overflow-x-auto">${html}</div>`;
}
