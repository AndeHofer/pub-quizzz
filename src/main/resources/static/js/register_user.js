function registerUser() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const role = document.getElementById('role').value;

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
                document.getElementById('username').value = '';
                document.getElementById('password').value = '';
                document.getElementById('role').value = '';
            } else {
                console.log(response);
                showMessage('Registrierung fehlgeschlagen. Bitte versuchen Sie es erneut.', 'error');
            }
        })
        .catch(error => {
            showMessage('Fehler: ' + error, 'error');
            console.error('Error:', error);
        });
}

function showMessage(text, type) {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = text;
    messageDiv.className = 'message ' + type;
    messageDiv.style.display = 'block';
}

function goBack() {
    window.location.href = 'admin_main.html';
}
