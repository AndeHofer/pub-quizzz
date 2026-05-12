export {};

import { showMessage, goBack } from './utils';

window.addEventListener('load', () => {
    document.getElementById('registerUserBtn')?.addEventListener('click', registerUser);
    document.getElementById('backBtn')?.addEventListener('click', () => goBack('admin_main.html'));
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
        .then(async response => {
            if (response.ok) {
                showMessage('Benutzer erfolgreich registriert!', 'success');
                (document.getElementById('username') as HTMLInputElement).value = '';
                (document.getElementById('password') as HTMLInputElement).value = '';
                (document.getElementById('role') as HTMLSelectElement).value = '';
            } else {
                const errorText = await response.text();
                showMessage(errorText || 'Registrierung fehlgeschlagen. Bitte versuchen Sie es erneut.', 'error');
            }
        })
        .catch(error => {
            showMessage('Fehler: ' + error, 'error');
            console.error('Error:', error);
        });
}
