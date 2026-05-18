document.addEventListener('DOMContentLoaded', loadAndDisplayPokedex);

async function loadAndDisplayPokedex() {
    const pokedexResponse = await callAuthenticatedApi('/api/pokedex');
    if (!pokedexResponse) return;

    const allPokemon  = await pokedexResponse.json();
    const pokedexGrid = document.getElementById('pokedexGrid');

    allPokemon.forEach(pokemon => pokedexGrid.appendChild(buildPokedexCard(pokemon)));
}

function buildPokedexCard(pokemon) {
    const card = document.createElement('div');
    card.className = 'pokemon-card';
    card.innerHTML = `
        <img src="${pokemon.sprite_url}" alt="${pokemon.name}" />
        <h3>#${pokemon.id} ${pokemon.name}</h3>
        <p>Type: ${pokemon.type}</p>
        <button class="catch-button" onclick="catchPokemon(${pokemon.id}, this)">
            Catch!
        </button>
    `;
    return card;
}

async function catchPokemon(pokedexId, catchButton) {
    catchButton.disabled = true;
    catchButton.textContent = 'Catching...';

    const response = await callAuthenticatedApi(`/api/trainer/team/${pokedexId}`, 'POST');

    if (response && response.ok) {
        catchButton.outerHTML = '<p class="caught-confirmation-label">Added to team!</p>';
    } else {
        catchButton.disabled = false;
        catchButton.textContent = 'Catch!';
        alert('Could not catch this Pokemon. Make sure you have a trainer profile on your dashboard.');
    }
}

function logoutAndReturnToLogin() {
    clearJwtAndRedirectToLogin();
}
