function goToAdmin() {
    window.location.href = '/admin/admin_main.html';
}

// Check if user is admin
fetch('/api/is-admin')
    .then(response => response.json())
    .then(isAdmin => {
        const adminBtn = document.getElementById('adminBtn');
        if (isAdmin && adminBtn) {
            adminBtn.style.display = 'inline-block';
        }
    });
