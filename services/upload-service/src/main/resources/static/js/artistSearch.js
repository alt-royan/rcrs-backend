/* ===== GENERIC DROPDOWN RENDERER ===== */
function renderArtistDropdown(results, dropdownEl, onSelect) {
    if (results.length === 0) {
        dropdownEl.innerHTML = `<div class="artist-dropdown-item" style="color:var(--on-bg-4);cursor:default;">No results found</div>`;
    } else {
        dropdownEl.innerHTML = results.map(a => `
            <div class="artist-dropdown-item">
                <span class="artist-avatar">${a.name[0]}</span>
                ${escapeHtml(a.name)}
            </div>
        `).join('');
        dropdownEl.querySelectorAll('.artist-dropdown-item').forEach((el, i) => {
            el.addEventListener('mousedown', (e) => {
                e.preventDefault();
                onSelect(results[i]);
            });
        });
    }
    dropdownEl.classList.remove('hidden');
}