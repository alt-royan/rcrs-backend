/* ===== INIT ===== */
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('panel-home').style.display = 'block';

    // Escape key closes crop dialog
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeCropDialog();
    });

    // Close artist dropdowns on blur
    const albumSearch = document.getElementById('album-artist-search');
    if (albumSearch) {
        albumSearch.addEventListener('blur', () => {
            setTimeout(() => {
                document.getElementById('album-artist-dropdown')?.classList.add('hidden');
            }, 150);
        });
    }

    // Inline validation
    const artistName = document.getElementById('artist-name');
    if (artistName) {
        artistName.addEventListener('blur', () => validateField('artist-name', 'artist-name-error', 'Artist name is required'));
        artistName.addEventListener('input', () => {
            if (artistName.value.trim()) {
                artistName.classList.remove('invalid');
                document.getElementById('artist-name-error').textContent = '';
            }
        });
    }

    const albumTitle = document.getElementById('album-title');
    if (albumTitle) {
        albumTitle.addEventListener('blur', () => validateField('album-title', 'album-title-error', 'Album title is required'));
        albumTitle.addEventListener('input', () => {
            if (albumTitle.value.trim()) {
                albumTitle.classList.remove('invalid');
                document.getElementById('album-title-error').textContent = '';
            }
        });
    }

    // Initial social links render (empty state)
    renderSocialLinks();

    initTrackManager();
    updateSubmitButtonState();
});