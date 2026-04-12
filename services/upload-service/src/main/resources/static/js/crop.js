/* ===== CROP DIALOG ===== */
function openCropDialog(target) {
    const imageData = target === 'artist' ? state.artist.imageBlob : state.album.imageBlob;
    if (!imageData) { showToast('Please select an image first.', 'error'); return; }
    state.cropTarget = target;

    const cropImg = document.getElementById('crop-image');
    cropImg.src = imageData;

    document.getElementById('crop-overlay').classList.remove('hidden');
    document.getElementById('crop-dialog').classList.remove('hidden');

    if (cropperInstance) { cropperInstance.destroy(); cropperInstance = null; }

    cropImg.onload = () => {
        cropperInstance = new Cropper(cropImg, {
            aspectRatio: 1,
            viewMode: 2,
            dragMode: 'move',
            autoCropArea: 0.9,
            restore: false,
            guides: true,
            center: true,
            highlight: false,
            cropBoxMovable: true,
            cropBoxResizable: true,
            toggleDragModeOnDblclick: false,
        });
    };
}

function closeCropDialog() {
    document.getElementById('crop-overlay').classList.add('hidden');
    document.getElementById('crop-dialog').classList.add('hidden');
    if (cropperInstance) { cropperInstance.destroy(); cropperInstance = null; }
}

function applyCrop() {
    if (!cropperInstance) return;
    const size = 1000;
    const canvas = cropperInstance.getCroppedCanvas({
        width: size,
        height: size,
        imageSmoothingQuality: 'high',
    });
    const croppedDataUrl = canvas.toDataURL('image/jpeg', 0.92);
    const target = state.cropTarget;

    if (target === 'artist') {
        state.artist.imageBlob = croppedDataUrl;
        state.artist.cropped = true;
        document.getElementById('artist-image-preview').src = croppedDataUrl;
        document.getElementById('artist-image-status').textContent = '✓ Square crop applied.';
        document.getElementById('artist-image-status').className = 'image-status saved';
    } else {
        state.album.imageBlob = croppedDataUrl;
        state.album.cropped = true;
        document.getElementById('album-image-preview').src = croppedDataUrl;
        document.getElementById('album-image-status').textContent = '✓ Square crop applied.';
        document.getElementById('album-image-status').className = 'image-status saved';
    }

    closeCropDialog();
    showToast('Image cropped to square!', 'success');
}