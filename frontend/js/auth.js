function saveJwtToLocalStorage(jwtToken) {
    localStorage.setItem(JWT_STORAGE_KEY, jwtToken);
}

function getJwtFromLocalStorage() {
    return localStorage.getItem(JWT_STORAGE_KEY);
}

function clearJwtAndRedirectToLogin() {
    localStorage.removeItem(JWT_STORAGE_KEY);
    window.location.href = 'index.html';
}
