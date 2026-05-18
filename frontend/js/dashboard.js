document.addEventListener('DOMContentLoaded', loadDashboard);

// =============================================================================
// Entry point — henter profil og viser det rigtige view baseret på rolle
// =============================================================================

async function loadDashboard() {
    const response = await callAuthenticatedApi('/api/gym/my-profile');
    if (!response) return;

    if (response.status === 404) {
        showSection('noProfileSection');
        return;
    }

    const profile = await response.json();
    document.getElementById('headerRoleLabel').textContent = profile.name + ' · ' + profile.role;

    switch (profile.role) {
        case 'GYM_LEADER': await loadGymLeaderView();  break;
        case 'TRAINER':    await loadTrainerView();     break;
        case 'FARMER':     await loadFarmerView();      break;
        case 'BREEDER':    await loadBreederView();     break;
    }
}

// =============================================================================
// GYM LEADER VIEW
// =============================================================================

async function loadGymLeaderView() {
    showSection('gymLeaderSection');
    await refreshMemberList();
}

async function refreshMemberList() {
    const response = await callAuthenticatedApi('/api/gym/members');
    if (!response || !response.ok) return;

    const members = await response.json();
    const grid    = document.getElementById('memberListGrid');
    grid.innerHTML = '';
    members.forEach(member => grid.appendChild(buildMemberCard(member)));
}

function buildMemberCard(member) {
    const card = document.createElement('div');
    card.className = 'pokemon-card';

    const rosterButton = member.role === 'TRAINER'
        ? member.inRoster
            ? `<button class="release-button" onclick="removeFromRoster('${member.id}')">Fjern fra gym</button>`
            : `<button class="catch-button"   onclick="addToRoster('${member.id}')">Tilføj til gym</button>`
        : '';

    card.innerHTML = `
        <h3>${member.name}</h3>
        <p>Rolle: ${member.role}</p>
        <p>Email: ${member.email}</p>
        <p>Tlf: ${member.phoneNumber || '—'}</p>
        <p>Adresse: ${member.address || '—'}</p>
        ${member.inRoster ? '<p style="color:#2a9d8f;font-weight:bold;">✓ I gymmet</p>' : ''}
        ${rosterButton}
    `;
    return card;
}

async function submitCreateMember() {
    const errorDisplay = document.getElementById('createMemberError');
    errorDisplay.style.display = 'none';

    const body = {
        name:        document.getElementById('newMemberName').value.trim(),
        email:       document.getElementById('newMemberEmail').value.trim(),
        password:    document.getElementById('newMemberPassword').value,
        phoneNumber: document.getElementById('newMemberPhone').value.trim(),
        address:     document.getElementById('newMemberAddress').value.trim(),
        role:        document.getElementById('newMemberRole').value,
    };

    const response = await callAuthenticatedApi('/api/gym/members', 'POST', body);

    if (!response) return;

    if (!response.ok) {
        const errorText = await response.text();
        errorDisplay.textContent = errorText || 'Oprettelse fejlede.';
        errorDisplay.style.display = 'block';
        return;
    }

    // Ryd formular og genindlæs listen
    ['newMemberName','newMemberEmail','newMemberPassword','newMemberPhone','newMemberAddress']
        .forEach(id => document.getElementById(id).value = '');
    await refreshMemberList();
}

async function addToRoster(memberId) {
    const response = await callAuthenticatedApi(`/api/gym/roster/${memberId}`, 'POST');
    if (response && response.ok) await refreshMemberList();
    else if (response) alert(await response.text());
}

async function removeFromRoster(memberId) {
    const response = await callAuthenticatedApi(`/api/gym/roster/${memberId}`, 'DELETE');
    if (response && response.ok) await refreshMemberList();
}

// =============================================================================
// TRAINER VIEW
// =============================================================================

async function loadTrainerView() {
    showSection('trainerSection');
    await refreshOwnTeam();
    await refreshAllTrainerTeams();
}

async function refreshOwnTeam() {
    const response = await callAuthenticatedApi('/api/trainer/team');
    if (!response || !response.ok) return;

    const team = await response.json();
    const grid  = document.getElementById('ownTeamGrid');
    grid.innerHTML = '';

    if (team.length === 0) {
        document.getElementById('ownTeamEmpty').classList.remove('hidden');
        return;
    }
    document.getElementById('ownTeamEmpty').classList.add('hidden');
    team.forEach(pokemon => grid.appendChild(buildOwnPokemonCard(pokemon)));
}

function buildOwnPokemonCard(pokemon) {
    const card = document.createElement('div');
    card.className = 'pokemon-card';
    card.innerHTML = `
        <img src="${pokemon.spriteUrl}" alt="${pokemon.pokemonName}" />
        <h3>${pokemon.nickname || pokemon.pokemonName}</h3>
        <p>Type: ${pokemon.pokemonType}</p>
        <p>Level: ${pokemon.level}</p>
        <button class="release-button" onclick="releasePokemon('${pokemon.caughtPokemonId}')">
            Slip fri
        </button>
    `;
    return card;
}

async function releasePokemon(caughtPokemonId) {
    if (!confirm('Er du sikker på at du vil slippe denne Pokémon?')) return;
    const response = await callAuthenticatedApi(`/api/trainer/team/${caughtPokemonId}`, 'DELETE');
    if (response && response.ok) await refreshOwnTeam();
}

async function refreshAllTrainerTeams() {
    const response = await callAuthenticatedApi('/api/trainer/all-teams');
    if (!response || !response.ok) return;

    const trainerTeams = await response.json();
    const container    = document.getElementById('allTeamsContainer');
    container.innerHTML = '';

    trainerTeams.forEach(({ trainer, team }) => {
        const section = document.createElement('div');
        section.style.marginBottom = '1.5rem';
        section.innerHTML = `<h3>${trainer.name} ${trainer.inRoster ? '· <span style="color:#2a9d8f">I gymmet</span>' : ''}</h3>`;

        if (team.length === 0) {
            section.innerHTML += '<p class="empty-team-message">Intet hold endnu.</p>';
        } else {
            const grid = document.createElement('div');
            grid.className = 'pokemon-grid';
            team.forEach(pokemon => {
                const card = document.createElement('div');
                card.className = 'pokemon-card';
                card.innerHTML = `
                    <img src="${pokemon.spriteUrl}" alt="${pokemon.pokemonName}" />
                    <h3>${pokemon.nickname || pokemon.pokemonName}</h3>
                    <p>Type: ${pokemon.pokemonType}</p>
                    <p>Level: ${pokemon.level}</p>
                `;
                grid.appendChild(card);
            });
            section.appendChild(grid);
        }
        container.appendChild(section);
    });
}

// =============================================================================
// FARMER VIEW
// =============================================================================

async function loadFarmerView() {
    showSection('farmerSection');

    const response = await callAuthenticatedApi('/api/gym/leader-team');
    if (!response || !response.ok) return;

    const team = await response.json();
    const grid  = document.getElementById('leaderTeamGrid');
    grid.innerHTML = '';

    if (team.length === 0) {
        document.getElementById('leaderTeamEmpty').classList.remove('hidden');
        return;
    }

    team.forEach(pokemon => {
        const card = document.createElement('div');
        card.className = 'pokemon-card';
        card.innerHTML = `
            <img src="${pokemon.spriteUrl}" alt="${pokemon.pokemonName}" />
            <h3>${pokemon.nickname || pokemon.pokemonName}</h3>
            <p>Type: ${pokemon.pokemonType}</p>
            <p>Level: ${pokemon.level}</p>
        `;
        grid.appendChild(card);
    });
}

// =============================================================================
// BREEDER VIEW
// =============================================================================

async function loadBreederView() {
    showSection('breederSection');

    const response = await callAuthenticatedApi('/api/gym/money');
    if (!response || !response.ok) return;

    const data = await response.json();
    document.getElementById('gymMoneyDisplay').textContent =
        data.money.toLocaleString('da-DK');
}

// =============================================================================
// Hjælpefunktioner
// =============================================================================

function showSection(id) {
    ['noProfileSection','gymLeaderSection','trainerSection','farmerSection','breederSection']
        .forEach(s => document.getElementById(s).classList.add('hidden'));
    document.getElementById(id).classList.remove('hidden');
}

function logoutAndReturnToLogin() {
    clearJwtAndRedirectToLogin();
}
