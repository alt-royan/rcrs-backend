/* ===== ARTIST SEARCH — ALBUM LEVEL ===== */
let albumSearchTimeout = null;

function onAlbumArtistSearch(query) {
    clearTimeout(albumSearchTimeout);
    const dropdown = document.getElementById('album-artist-dropdown');
    if (!query || query.trim().length === 0) {
        dropdown.classList.add('hidden');
        return;
    }
    albumSearchTimeout = setTimeout(async () => {
        const results = await searchArtists(query);
        renderArtistDropdown(results, dropdown, (artist) => {
            if (!state.albumArtists.find(a => a.id === artist.id)) {
                state.albumArtists.push({ ...artist });
                renderAlbumArtistTags();
                syncTrackLockedArtists();
            }
            document.getElementById('album-artist-search').value = '';
            dropdown.classList.add('hidden');
        });
    }, 200);
}

function renderAlbumArtistTags() {
    const container = document.getElementById('album-artists-tags');
    if (!container) return;
    container.innerHTML = state.albumArtists.map(a => `
        <span class="artist-tag" data-id="${a.id}">
            <span class="artist-avatar">${a.name[0]}</span>
            ${escapeHtml(a.name)}
            <button class="artist-tag-remove" onclick="removeAlbumArtist(${a.id})" title="Remove">
                <span class="material-icons-round">close</span>
            </button>
        </span>
    `).join('');
}

function removeAlbumArtist(id) {
    state.albumArtists = state.albumArtists.filter(a => a.id !== id);
    renderAlbumArtistTags();
    syncTrackLockedArtists();
}

function syncTrackLockedArtists() {
    if (typeof tracksData === 'undefined' || !tracksData) return;

    tracksData.forEach(track => {
        // 1. Обновляем статус locked для существующих артистов
        track.artists.forEach(artist => {
            const isAlbumArtist = state.albumArtists.some(aa => aa.id === artist.id);
            if (isAlbumArtist && !artist.locked) {
                artist.locked = true;
            } else if (!isAlbumArtist && artist.locked) {
                // Если артист был locked, но его убрали из альбома, разлочиваем
                artist.locked = false;
            }
        });

        // 2. Добавляем новых артистов альбома, которых ещё нет в треке
        state.albumArtists.forEach(aa => {
            if (!track.artists.find(ta => ta.id === aa.id)) {
                track.artists.unshift({ ...aa, locked: true });
            }
        });

        // 3. (Опционально) Удаляем locked-артистов, которых больше нет в альбоме
        // Раскомментировать, если нужно удалять, а не разлочивать:
        // track.artists = track.artists.filter(ta =>
        //     !ta.locked || state.albumArtists.find(aa => aa.id === ta.id)
        // );
    });

    // Перерисовываем теги у всех треков
    if (typeof window.updateAllTrackArtistTags === 'function') {
        window.updateAllTrackArtistTags();
    } else {
        // fallback
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
}