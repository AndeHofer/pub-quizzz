    function goToAdmin() {
    window.location.href = '/admin/admin_main.html';
}

(window as any).goToAdmin = goToAdmin;

// Check if user is admin
fetch('/api/is-admin')
    .then(response => response.json())
    .then((isAdmin: boolean) => {
        const adminBtn = document.getElementById('adminBtn') as HTMLButtonElement | null;
        if (isAdmin && adminBtn) {
            adminBtn.style.display = 'inline-block';
        }
    });
