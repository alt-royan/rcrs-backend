/* ===== IMAGE UPLOAD ===== */
async function uploadImage(target) {
    const imgState = target === 'artist' ? state.artist : state.album;
    if (!imgState.imageBlob) return null;
    if (imgState.imagePath) return imgState.imagePath;

    const endpoint = '/upload/image';

    try {
        const res = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ image: imgState.imageBlob }),
        });
    
        if (res.ok) {const data = await res.json(); imgState.imagePath = data.uri; return data.uri; }
        else throw new Error(data.message || 'Upload failed');
    } catch (e) {
        throw new Error('Image upload failed: ' + e.message);
    }
}

/* ===== ARTIST FORM SUBMIT ===== */
async function submitArtistForm(event) {
    if (event) event.preventDefault();
    if (!validateField('artist-name', 'artist-name-error', 'Artist name is required')) return;

/*    if (!state.artist.imageBlob) {
        showToast('Please upload and crop an artist photo.', 'error');
        return;
    }*/

/*    if (!state.artist.cropped) {
        showToast('Please crop the artist photo first.', 'error');
        openCropDialog('artist');
        return;
    }*/

    showLoading(true);

    try {
        const imagePath = await uploadImage('artist');
        const payload = {
            name: document.getElementById('artist-name').value.trim(),
            avatarUri: imagePath,
            socialLinks: state.socialLinks.filter(l => l.name && l.url),
        };

        const res = await fetch('/upload/artist', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
        });

        if (res.ok) {
            const data = await res.json();
            showToast('Artist saved!', 'success', data.id);
            console.log(data.id);
            resetArtistForm();
            setTimeout(() => showHome(), 1200);
        } else {
            showToast(data.message || 'Failed to save artist.', 'error');
        }
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    } finally {
        showLoading(false);
    }
}

/* ===== ALBUM FORM SUBMIT ===== */
async function submitAlbumForm() {
    console.log('submitAlbumForm called'); // для отладки

    // Валидация названия альбома
    if (!validateField('album-title', 'album-title-error', 'Album title is required')) return;

    // Проверка: у альбома должен быть хотя бы один артист
    if (!state.albumArtists.length) {
        showToast('Please add at least one artist to the album', 'error');
        return;
    }

    // Проверка артистов у треков (если есть треки)
    for (const track of tracksData) {
        if (!track.artists.length) {
            showToast(`Track "${track.title}" has no artists. Please add at least one artist.`, 'error');
            return;
        }
    }

    // Проверка загрузки треков (только если есть треки)
    if (tracksData.length > 0 && tracksData.some(t => t.uploadStatus !== 'success')) {
        showToast('Wait for all tracks to finish uploading.', 'error');
        return;
    }

    showLoading(true);
    try {
        let imagePath = null;
        if (state.album.imageBlob) imagePath = await uploadImage('album');

        const payload = {
            title: document.getElementById('album-title').value.trim(),
            type: document.getElementById('album-type').value,
            releaseDate: document.getElementById('album-release').value || null,
            artists: state.albumArtists,
            coverImagePath: imagePath,
            tracks: tracksData.map(t => ({
                title: t.title,
                explicit: t.explicit,
                artists: t.artists.map(a => ({ id: a.id, name: a.name })),
                uid: t.uid
            })),
        };

        const res = await fetch('/upload/album', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Album saved!', 'success');
            resetAlbumForm();
            setTimeout(() => showHome(), 1200);
        } else {
            showToast(data.message || 'Failed to save album.', 'error');
        }
    } catch (e) {
        console.error(e);
        showToast('Error: ' + e.message, 'error');
    } finally {
        showLoading(false);
    }
}

/* ===== RESETS ===== */
function resetArtistForm() {
    document.getElementById('artist-name').value = '';
    document.getElementById('artist-name').classList.remove('invalid');
    document.getElementById('artist-name-error').textContent = '';

    ['artist'].forEach(t => {
        document.getElementById(t + '-image-preview').src = '';
        document.getElementById(t + '-image-preview').classList.add('hidden');
        document.getElementById(t + '-upload-placeholder').style.display = 'flex';
        document.getElementById(t + '-image-actions').classList.add('hidden');
        document.getElementById(t + '-image-status').textContent = '';
        document.getElementById(t + '-image-input').value = '';
    });

    state.artist = { imageBlob: null, imagePath: null, cropped: false };
    state.socialLinks = [];
    socialIdCounter = 0;
    renderSocialLinks();
}

function resetAlbumForm() {
    // ... остальной код сброса (очистка полей, image, albumArtists, tracksData и т.д.) ...
    tracksData = [];
    nextTrackId = 0;
    const trackList = document.getElementById('track-list');
    if (trackList) trackList.innerHTML = '<div class="track-empty-state" id="track-empty"><span class="material-icons-round">music_note</span><p>No tracks added yet.</p></div>';
    updateTrackCount();
    updateTrackListVisibility();
    updateSubmitButtonState();  // Важно: обновляем состояние кнопки после сброса
    initSortable();
}

document.addEventListener('DOMContentLoaded', () => {
    const submitBtn = document.getElementById('submit-album-btn');
    if (submitBtn) {
        submitBtn.addEventListener('click', submitAlbumForm);
    }
});