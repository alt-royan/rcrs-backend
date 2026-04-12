// В state
const state = {
    currentPanel: 'home',
    artist: { imageBlob: null, imagePath: null, cropped: false },
    album: { imageBlob: null, imagePath: null, cropped: false },
    cropTarget: null,

    // Artist page
    socialLinks: [],          // [{name, url}]

    // Album page
    albumArtists: [],         // [{id, name}]
    // tracks: [],            // УДАЛИТЬ (больше не используется)
};

// let trackIdCounter = 0;    // УДАЛИТЬ
let socialIdCounter = 0;
let cropperInstance = null;