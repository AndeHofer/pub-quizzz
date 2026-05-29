import {escapeHtml} from './html-utils';
import type {QuizDocumentDTO} from './types';

function formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function buildDocumentListMarkup(editingQuizId: number, docs: QuizDocumentDTO[]): string {
    let html = '<table style="width:100%; border-collapse:collapse; font-size:0.875rem;"><thead><tr>' +
        '<th style="text-align:left; padding:4px 8px; border-bottom:1px solid #e5e7eb;">Dateiname</th>' +
        '<th style="text-align:left; padding:4px 8px; border-bottom:1px solid #e5e7eb;">Größe</th>' +
        '<th style="padding:4px 8px; border-bottom:1px solid #e5e7eb;"></th>' +
        '</tr></thead><tbody>';

    docs.forEach(doc => {
        const safeFilename = escapeHtml(doc.originalFilename ?? '');
        html += `<tr>
                <td style="padding:4px 8px;">
                    <a href="/admin/quiz/${editingQuizId}/documents/${doc.id}"
                       download="${safeFilename}"
                       style="color:#374151; text-decoration:underline;">${safeFilename}</a>
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
    return html;
}
