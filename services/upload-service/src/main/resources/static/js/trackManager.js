/* ===== TRACK MANAGER (with file upload, presign, polling, drag-drop) ===== */
let nextTrackId = 0;           // уникальный идентификатор для треков (не конфликтует)
let tracksData = [];           // массив объектов треков

// Функции инициализации и работы с DOM

function initTrackManager() {
    const dropzone = document.getElementById('track-dropzone');
    const fileInput = document.getElementById('track-file-input');
    const addBtn = document.getElementById('add-tracks-btn');

    if (!dropzone || !fileInput) {
        console.warn('Track dropzone or file input not found');
        return;
    }

    // Открыть диалог выбора файлов по кнопке
    if (addBtn) {
        addBtn.onclick = (e) => {
            e.preventDefault();
            fileInput.click();
        };
    }

    // Drag & drop
    dropzone.ondragover = (e) => {
        e.preventDefault();
        dropzone.classList.add('drag-over');
    };
    dropzone.ondragleave = () => {
        dropzone.classList.remove('drag-over');
    };
    dropzone.ondrop = (e) => {
        e.preventDefault();
        dropzone.classList.remove('drag-over');
        const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('audio/'));
        if (files.length) {
            handleFiles(files);
        } else {
            showToast('Please drop audio files only', 'error');
        }
    };

    // Выбор файлов через input
    fileInput.onchange = (e) => {
        const files = Array.from(e.target.files);
        if (files.length) {
            handleFiles(files);
        }
        fileInput.value = ''; // сброс, чтобы можно было выбрать те же файлы снова
    };

    // Начальное состояние
    updateTrackListVisibility();
    updateSubmitButtonState();
}

async function handleFiles(files) {
    for (const file of files) {
        const id = ++nextTrackId;
        const title = file.name.replace(/\.[^/.]+$/, '');
        const track = {
            id,
            file,
            title,
            explicit: false,
            artists: [...(window.state?.albumArtists || []).map(a => ({ ...a, locked: true }))],
            uid: null,
            uploadStatus: 'pending',
            errorReason: '',
            presignHeaders: null,
            presignUrl: null,
        };
        tracksData.push(track);
        renderTrackCard(track);
        startUpload(track);
    }
    updateTrackListVisibility();
    updateTrackCount();
    updateSubmitButtonState();
    initSortable();
}

function renderTrackCard(track) {
    const container = document.getElementById('track-list');
    if (!container) return;

    const existingCard = document.querySelector(`.track-card[data-id="${track.id}"]`);
    if (existingCard) existingCard.remove();

    const card = document.createElement('div');
    card.className = 'track-card';
    card.setAttribute('data-id', track.id);
    card.setAttribute('data-uid', track.uid || '');

    const trackNumber = tracksData.findIndex(t => t.id === track.id) + 1;
    const artistsHtml = track.artists.map(a => `
        <span class="artist-tag ${a.locked ? 'locked' : ''}">
            ${a.locked ? '<span class="material-icons-round lock-icon">lock</span>' : ''}
            ${escapeHtml(a.name)}
            ${!a.locked ? `<button class="artist-tag-remove" onclick="removeTrackArtistFromCard(${track.id}, ${a.id})">✕</button>` : ''}
        </span>
    `).join('');

    card.innerHTML = `
        <div class="track-card-inner">
            <span class="track-drag-handle material-icons-round">drag_indicator</span>
            <span class="track-number">${trackNumber}</span>
            <input type="text" class="track-title-input" value="${escapeHtml(track.title)}"
                   onchange="updateTrackTitle(${track.id}, this.value)">

            <div class="track-artist-select">
                <div class="track-artist-tags" id="track-artist-tags-${track.id}">${artistsHtml}</div>
                <input type="text" class="track-artist-search" placeholder="Add artist..."
                       id="track-artist-search-${track.id}" autocomplete="off"
                       oninput="onTrackArtistSearch(${track.id}, this.value)">
                <div class="track-artist-dropdown hidden" id="track-artist-dropdown-${track.id}"></div>
            </div>

            <div class="track-explicit-toggle">
                <label class="toggle" style="width:32px; height:18px;">
                    <input type="checkbox" ${track.explicit ? 'checked' : ''}
                           onchange="toggleTrackExplicit(${track.id}, this.checked)">
                    <span class="toggle-slider"></span>
                </label>
                <span class="explicit-badge ${track.explicit ? 'active' : ''}">E</span>
            </div>

            <span class="track-filename" title="${escapeHtml(track.file.name)}">${escapeHtml(track.file.name)}</span>

            <div class="upload-icon-status" id="upload-icon-${track.id}"
                 data-status="${track.uploadStatus}"
                 title="${track.errorReason ? track.errorReason : (track.uploadStatus === 'half_uploaded' ? 'Uploaded to storage, waiting for confirmation' : '')}">
                ${getUploadIcon(track.uploadStatus)}
            </div>

            <button class="track-replace-btn" onclick="replaceTrackFile(${track.id})" title="Replace file">
                <span class="material-icons-round">refresh</span>
            </button>
            <button class="track-remove-btn" onclick="removeTrack(${track.id})" title="Remove track">
                <span class="material-icons-round">delete_outline</span>
            </button>
        </div>
    `;

    container.appendChild(card);

    // Привязка поиска артистов
    const searchInput = card.querySelector(`#track-artist-search-${track.id}`);
    const dropdown = card.querySelector(`#track-artist-dropdown-${track.id}`);
    if (searchInput && dropdown) {
        searchInput.onblur = () => setTimeout(() => dropdown.classList.add('hidden'), 200);
    }
}

function getUploadIcon(status) {
    switch(status) {
        case 'uploading': return '<span class="material-icons-round">autorenew</span>';
        case 'half_uploaded': return '<span class="material-icons-round">cloud_upload</span>';
        case 'success': return '<span class="material-icons-round">check_circle</span>';
        case 'failed': return '<span class="material-icons-round">error</span>';
        default: return '<span class="material-icons-round">cloud_upload</span>';
    }
}

function updateUploadIcon(trackId, status, reason = '') {
    const iconDiv = document.getElementById(`upload-icon-${trackId}`);
    if (!iconDiv) return;
    iconDiv.setAttribute('data-status', status);
    iconDiv.innerHTML = getUploadIcon(status);
    iconDiv.className = `upload-icon-status ${status === 'uploading' ? 'uploading' : status === 'half_uploaded' ? 'half-success' : status === 'success' ? 'success' : status === 'failed' ? 'failed' : ''}`;
    if (reason) iconDiv.title = reason;
    else if (status === 'half_uploaded') iconDiv.title = 'Uploaded to storage, waiting for confirmation';
    else if (status === 'success') iconDiv.title = 'Track ready';
    else iconDiv.title = '';
}

async function startUpload(track) {
    track.uploadStatus = 'uploading';
    updateUploadIcon(track.id, 'uploading');
    try {
        // 1. Получить presigned URL
        const presignRes = await fetch('/upload/preload', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: track.file.name,
                length: track.file.size
            })
        });
        if (!presignRes.ok) throw new Error(`Presign failed: ${presignRes.status}`);
        const presignData = await presignRes.json();
        if (!presignData.url) throw new Error('No presigned URL');

        track.uid = presignData.uid;
        track.presignUrl = presignData.url;
        track.presignHeaders = presignData.headers;

        // 2. Загрузить файл в S3 через PUT
        const uploadRes = await fetch(track.presignUrl, {
            method: 'PUT',
            headers: convertHeadersArrayToObj(track.presignHeaders),
            body: track.file
        });
        if (!uploadRes.ok) throw new Error(`Upload failed: ${uploadRes.status}`);

        track.uploadStatus = 'half_uploaded';
        updateUploadIcon(track.id, 'half_uploaded');

        // 3. Проверить статус (polling)
        await pollUploadStatus(track);

    } catch (err) {
        console.error(err);
        track.uploadStatus = 'failed';
        track.errorReason = err.message;
        updateUploadIcon(track.id, 'failed', err.message);
        updateSubmitButtonState();
    }
}

function convertHeadersArrayToObj(headersArray) {
    const headers = {};
    if (!headersArray) return headers;
    headersArray.forEach(h => {
        for (let [key, value] of Object.entries(h)) {
            headers[key] = value;
        }
    });
    return headers;
}

async function pollUploadStatus(track, attempt = 1) {
    const maxAttempts = 3;
    const delay = 1000;
    try {
        const statusRes = await fetch('/status', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify([track.uid])
        });
        if (!statusRes.ok) throw new Error(`Status check failed: ${statusRes.status}`);
        const statusData = await statusRes.json();
        const item = statusData.find(s => s.uid === track.uid);
        if (!item) throw new Error('No status info');

        if (item.status === 'UPLOAD') {
            track.uploadStatus = 'success';
            updateUploadIcon(track.id, 'success');
            updateSubmitButtonState();
            return;
        } else if (item.status === 'WAIT_FOR_UPLOAD' && attempt < maxAttempts) {
            setTimeout(() => pollUploadStatus(track, attempt + 1), delay);
        } else if (item.status === 'FAILED' || (item.status === 'WAIT_FOR_UPLOAD' && attempt >= maxAttempts)) {
            throw new Error(item.reason || 'Upload confirmation failed');
        } else {
            throw new Error('Unknown status');
        }
    } catch (err) {
        track.uploadStatus = 'failed';
        track.errorReason = err.message;
        updateUploadIcon(track.id, 'failed', err.message);
        updateSubmitButtonState();
    }
}

function updateSubmitButtonState() {
    const submitBtn = document.getElementById('submit-album-btn');
    if (!submitBtn) return;

    // Если треков нет – кнопка активна
    if (!tracksData.length) {
        submitBtn.disabled = false;
        return;
    }

    // Если треки есть – активна только когда все success
    const allSuccess = tracksData.every(t => t.uploadStatus === 'success');
    submitBtn.disabled = !allSuccess;
}

function updateTrackListVisibility() {
    const dropzone = document.getElementById('track-dropzone');
    const emptyState = document.querySelector('#track-list .track-empty-state');
    if (tracksData.length === 0) {
        if (dropzone) dropzone.style.display = 'block';
        if (emptyState) emptyState?.classList.remove('hidden');
    } else {
        if (dropzone) dropzone.style.display = 'none';
        if (emptyState) emptyState?.classList.add('hidden');
    }
}

function updateTrackCount() {
    const count = tracksData.length;
    const trackCountSpan = document.getElementById('track-count');
    if (trackCountSpan) trackCountSpan.textContent = `${count} ${count === 1 ? 'track' : 'tracks'}`;
}

function removeTrack(trackId) {
    tracksData = tracksData.filter(t => t.id !== trackId);
    const card = document.querySelector(`.track-card[data-id="${trackId}"]`);
    if (card) card.remove();
    renumberTracks();
    updateTrackCount();
    updateTrackListVisibility();
    updateSubmitButtonState();
    initSortable();
}

function replaceTrackFile(trackId) {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'audio/*';
    input.onchange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const track = tracksData.find(t => t.id === trackId);
        if (!track) return;
        // Сброс состояния трека
        track.file = file;
        track.title = file.name.replace(/\.[^/.]+$/, '');
        track.uid = null;
        track.uploadStatus = 'pending';
        track.errorReason = '';
        track.presignHeaders = null;
        track.presignUrl = null;
        // Перерисовать и начать загрузку заново
        renderTrackCard(track);
        startUpload(track);
        updateSubmitButtonState();
    };
    input.click();
}

function updateTrackTitle(trackId, newTitle) {
    const track = tracksData.find(t => t.id === trackId);
    if (track) track.title = newTitle;
}

function toggleTrackExplicit(trackId, checked) {
    const track = tracksData.find(t => t.id === trackId);
    if (track) track.explicit = checked;
    const badge = document.querySelector(`.track-card[data-id="${trackId}"] .explicit-badge`);
    if (badge) badge.classList.toggle('active', checked);
}

function renumberTracks() {
    document.querySelectorAll('.track-card').forEach((card, idx) => {
        const numSpan = card.querySelector('.track-number');
        if (numSpan) numSpan.textContent = idx + 1;
    });
}

let sortableInstance = null;
function initSortable() {
    const container = document.getElementById('track-list');
    if (!container) return;
    if (sortableInstance) sortableInstance.destroy();
    sortableInstance = new Sortable(container, {
        handle: '.track-drag-handle',
        animation: 150,
        onEnd: function() {
            // Переупорядочиваем tracksData в соответствии с DOM
            const newOrder = [];
            document.querySelectorAll('.track-card').forEach(card => {
                const id = parseInt(card.getAttribute('data-id'));
                const track = tracksData.find(t => t.id === id);
                if (track) newOrder.push(track);
            });
            tracksData = newOrder;
            renumberTracks();
        }
    });
}

// Поиск артистов для конкретного трека
async function onTrackArtistSearch(trackId, query) {
    clearTimeout(window._trackArtistTimeout);
    const dropdown = document.getElementById(`track-artist-dropdown-${trackId}`);
    if (!dropdown) return;
    if (!query || query.trim() === '') {
        dropdown.classList.add('hidden');
        return;
    }
    window._trackArtistTimeout = setTimeout(() => {
        fetchAndRenderArtists(query, dropdown, (artist) => {
            const track = tracksData.find(t => t.id === trackId);
            if (track && !track.artists.find(a => a.id === artist.id)) {
                track.artists.push({ ...artist, locked: false });
                // Перерисовываем теги артистов у этого трека
                const tagsContainer = document.getElementById(`track-artist-tags-${trackId}`);
                if (tagsContainer) {
                    tagsContainer.innerHTML = track.artists.map(a => `
                        <span class="artist-tag ${a.locked ? 'locked' : ''}">
                            ${a.locked ? '<span class="material-icons-round lock-icon">lock</span>' : ''}
                            ${escapeHtml(a.name)}
                            ${!a.locked ? `<button class="artist-tag-remove" onclick="removeTrackArtistFromCard(${trackId}, ${a.id})">✕</button>` : ''}
                        </span>
                    `).join('');
                }
            }
            const searchInput = document.getElementById(`track-artist-search-${trackId}`);
            if (searchInput) searchInput.value = '';
            dropdown.classList.add('hidden');
        });
    }, 200);
}

function removeTrackArtistFromCard(trackId, artistId) {
    const track = tracksData.find(t => t.id === trackId);
    if (track) {
        track.artists = track.artists.filter(a => a.id !== artistId || a.locked);
        const tagsContainer = document.getElementById(`track-artist-tags-${trackId}`);
        if (tagsContainer) {
            tagsContainer.innerHTML = track.artists.map(a => `
                <span class="artist-tag ${a.locked ? 'locked' : ''}">
                    ${a.locked ? '<span class="material-icons-round lock-icon">lock</span>' : ''}
                    ${escapeHtml(a.name)}
                    ${!a.locked ? `<button class="artist-tag-remove" onclick="removeTrackArtistFromCard(${trackId}, ${a.id})">✕</button>` : ''}
                </span>
            `).join('');
        }
    }
}

// Глобальные функции для вызова из onclick
window.initTrackManager = initTrackManager;
window.handleFiles = handleFiles;
window.removeTrack = removeTrack;
window.replaceTrackFile = replaceTrackFile;
window.updateTrackTitle = updateTrackTitle;
window.toggleTrackExplicit = toggleTrackExplicit;
window.onTrackArtistSearch = onTrackArtistSearch;
window.removeTrackArtistFromCard = removeTrackArtistFromCard;

// Обновить теги артистов у всех треков (вызывается из albumArtists.js)
function updateAllTrackArtistTags() {
    if (typeof tracksData === 'undefined') return;
    tracksData.forEach(track => {
        const tagsContainer = document.getElementById(`track-artist-tags-${track.id}`);
        if (tagsContainer) {
            tagsContainer.innerHTML = track.artists.map(a => `
                <span class="artist-tag ${a.locked ? 'locked' : ''}">
                    ${a.locked ? '<span class="material-icons-round lock-icon">lock</span>' : ''}
                    ${escapeHtml(a.name)}
                    ${!a.locked ? `<button class="artist-tag-remove" onclick="removeTrackArtistFromCard(${track.id}, ${a.id})">✕</button>` : ''}
                </span>
            `).join('');
        }
    });
}

window.updateAllTrackArtistTags = updateAllTrackArtistTags;