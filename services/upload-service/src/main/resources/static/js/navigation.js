/* ===== PANEL NAVIGATION ===== */
function showHome() { switchPanel('home');}
function showSection(section) { switchPanel(section); }

function switchPanel(name) {
    const current = document.querySelector('.panel.active');
    const next = document.getElementById('panel-' + name);
    if (!next || current === next) return;

    if (current) {
        current.classList.remove('active');
        current.style.display = 'none';
    }

    next.style.display = 'block';
    requestAnimationFrame(() => next.classList.add('active'));

    state.currentPanel = name;
    window.scrollTo({ top: 0, behavior: 'smooth' });
}
