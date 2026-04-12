/* ===== MOCK API ===== */
const MOCK_ARTISTS = [
    { id: 1,  name: 'Arctic Monkeys' },
    { id: 2,  name: 'Billie Eilish' },
    { id: 3,  name: 'Frank Ocean' },
    { id: 4,  name: 'Kendrick Lamar' },
    { id: 5,  name: 'Lana Del Rey' },
    { id: 6,  name: 'Mac Miller' },
    { id: 7,  name: 'Miles Davis' },
    { id: 8,  name: 'Radiohead' },
    { id: 9,  name: 'SZA' },
    { id: 10, name: 'Tyler, the Creator' },
];

async function searchArtists(query) {
    await new Promise(r => setTimeout(r, 80));
    if (!query || query.trim().length === 0) return [];
    const q = query.toLowerCase();
    return MOCK_ARTISTS.filter(a => a.name.toLowerCase().includes(q)).slice(0, 6);
}