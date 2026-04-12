/* ===== UTILITIES ===== */
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const icons = { success: 'check_circle', error: 'error', info: 'info' };
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span class="material-icons-round">${icons[type] || 'info'}</span><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('toast-out');
        setTimeout(() => toast.remove(), 250);
    }, 3500);
}

function showLoading(visible) {
    document.getElementById('loading-overlay').classList.toggle('hidden', !visible);
}

function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function validateField(inputId, errorId, message) {
    const input = document.getElementById(inputId);
    const error = document.getElementById(errorId);
    if (!input.value.trim()) {
        input.classList.add('invalid');
        if (error) error.textContent = message;
        return false;
    }
    input.classList.remove('invalid');
    if (error) error.textContent = '';
    return true;
}