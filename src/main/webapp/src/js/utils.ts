export function showMessage(text: string, type: 'success' | 'error'): void {
    const messageDiv = document.getElementById('message') as HTMLElement | null;
    if (messageDiv) {
        messageDiv.textContent = text;
        messageDiv.className = 'message ' + type;
        messageDiv.style.display = 'block';
    }
}

export function goBack(target: string): void {
    window.location.href = target;
}
