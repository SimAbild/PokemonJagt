document.addEventListener('DOMContentLoaded', function () {
    if (getJwtFromLocalStorage()) {
        window.location.href = 'dashboard.html';
    }
});

function showLoginForm() {
    document.getElementById('loginFormSection').classList.remove('hidden');
    document.getElementById('registerFormSection').classList.add('hidden');
}

function showRegisterForm() {
    document.getElementById('loginFormSection').classList.add('hidden');
    document.getElementById('registerFormSection').classList.remove('hidden');
}

async function submitLoginForm() {
    const email    = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value;
    const errorDisplay = document.getElementById('loginErrorMessage');

    errorDisplay.style.display = 'none';

    const response = await callPublicApi('/api/auth/login', { email, password });

    if (!response.ok) {
        errorDisplay.textContent = 'Wrong email or password. Demo accounts: ash@pokemon.com / password123';
        errorDisplay.style.display = 'block';
        return;
    }

    const tokenData = await response.json();
    saveJwtToLocalStorage(tokenData.access_token);
    window.location.href = 'dashboard.html';
}

async function submitRegisterForm() {
    const email    = document.getElementById('registerEmail').value.trim();
    const password = document.getElementById('registerPassword').value;
    const errorDisplay = document.getElementById('registerErrorMessage');

    errorDisplay.style.display = 'none';

    const response = await callPublicApi('/api/auth/register', { email, password });

    if (!response.ok) {
        errorDisplay.textContent = 'Registration failed. This email may already be in use.';
        errorDisplay.style.display = 'block';
        return;
    }

    alert('Account created! Please log in.');
    showLoginForm();
}
