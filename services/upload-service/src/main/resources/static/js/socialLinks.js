/* ===== SOCIAL LINKS (Artist) ===== */
const MAX_SOCIAL_LINKS = 4;

function addSocialLink() {
    if (state.socialLinks.length >= MAX_SOCIAL_LINKS) return;
    const id = ++socialIdCounter;
    state.socialLinks.push({ id, name: '', url: '' });
    renderSocialLinks();
}

function removeSocialLink(id) {
    state.socialLinks = state.socialLinks.filter(l => l.id !== id);
    renderSocialLinks();
}

function updateSocialLink(id, field, value) {
    const link = state.socialLinks.find(l => l.id === id);
    if (link) link[field] = value;
}

function renderSocialLinks() {
    const list = document.getElementById('social-links-list');
    const addBtn = document.getElementById('add-social-btn');
    const limitNote = document.getElementById('social-limit-note');

    if (state.socialLinks.length === 0) {
        list.innerHTML = '<p class="social-empty">No social links added yet.</p>';
    } else {
        list.innerHTML = state.socialLinks.map(link => `
            <div class="social-link-item" data-id="${link.id}">
                <input type="text" class="field-input" placeholder="Platform (e.g. Instagram)"
                       value="${escapeHtml(link.name)}" oninput="updateSocialLink(${link.id}, 'name', this.value)" maxlength="40"/>
                <input type="url" class="field-input" placeholder="https://..."
                       value="${escapeHtml(link.url)}" oninput="updateSocialLink(${link.id}, 'url', this.value)"/>
                <button class="social-remove-btn" onclick="removeSocialLink(${link.id})" title="Remove">
                    <span class="material-icons-round">close</span>
                </button>
            </div>
        `).join('');
    }

    const atMax = state.socialLinks.length >= MAX_SOCIAL_LINKS;
    addBtn.disabled = atMax;
    limitNote.style.display = atMax ? 'block' : 'none';
}