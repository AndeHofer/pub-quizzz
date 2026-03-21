export {};

// Ensure functions are available globally
window.addEventListener('load', () => {
    (window as any).registerUser = registerUser;
    (window as any).goBack = goBack;
});

function registerUser() {
    const username = (document.getElementById('username') as HTMLInputElement).value;
    const password = (document.getElementById('password') as HTMLInputElement).value;
    const role = (document.getElementById('role') as HTMLSelectElement).value;

    if (!username || !password || !role) {
        showMessage('Bitte füllen Sie alle Felder aus', 'error');
        return;
    }

    const userData = {
        username: username,
        password: password,
        role: role
    };

    fetch('/admin/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData)
    })
        .then(response => {
            if (response.ok) {
                showMessage('Benutzer erfolgreich registriert!', 'success');
                (document.getElementById('username') as HTMLInputElement).value = '';
                (document.getElementById('password') as HTMLInputElement).value = '';
                (document.getElementById('role') as HTMLSelectElement).value = '';
            } else {
                showMessage('Registrierung fehlgeschlagen. Bitte versuchen Sie es erneut.', 'error');
            }
        })
        .catch(error => {
            showMessage('Fehler: ' + error, 'error');
            console.error('Error:', error);
        });
}

function showMessage(text: string, type: string) {
    const messageDiv = document.getElementById('message') as HTMLElement | null;
    if (messageDiv) {
        messageDiv.textContent = text;
        messageDiv.className = 'message ' + type;
        messageDiv.style.display = 'block';
    }
}

function goBack() {
    window.location.href = 'admin_main.html';
}
