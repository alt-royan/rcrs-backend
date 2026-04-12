/* ===== ARTIST IMAGE ===== */
function triggerArtistImageInput() { document.getElementById('artist-image-input').click(); }
function handleArtistImageSelect(event) {
    const file = event.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (e) => {
        state.artist.imageBlob = e.target.result;
        state.artist.cropped = false;
        state.artist.imagePath = null;
        showImagePreview('artist', e.target.result);
        openCropDialog('artist');
    };
    reader.readAsDataURL(file);
}

/* ===== ALBUM IMAGE ===== */
function triggerAlbumImageInput() { document.getElementById('album-image-input').click(); }
function handleAlbumImageSelect(event) {
    const file = event.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (e) => {
        state.album.imageBlob = e.target.result;
        state.album.cropped = false;
        state.album.imagePath = null;
        showImagePreview('album', e.target.result);
        openCropDialog('album');
    };
    reader.readAsDataURL(file);
}

function showImagePreview(target, src) {
    const preview = document.getElementById(target + '-image-preview');
    const placeholder = document.getElementById(target + '-upload-placeholder');
    const actions = document.getElementById(target + '-image-actions');
    const status = document.getElementById(target + '-image-status');
    preview.src = src;
    preview.classList.remove('hidden');
    placeholder.style.display = 'none';
    actions.classList.remove('hidden');
    status.textContent = 'Image selected — cropping to square...';
    status.className = 'image-status';
}