// The URL of the Spring Boot backend running on your machine
const BACKEND_URL = 'http://localhost:8080';

// The key used to store the JWT in the browser's localStorage
// Using a specific name avoids collisions with other apps on the same domain
const JWT_STORAGE_KEY = 'pokemonjagt_jwt';


// =============================================================================
// JWT storage helpers
// These are the only functions that know where and how the JWT is stored.
// Everything else goes through these functions.
// =============================================================================

function saveJwtToLocalStorage(jwtToken) {
    localStorage.setItem(JWT_STORAGE_KEY, jwtToken);
}

function getJwtFromLocalStorage() {
    return localStorage.getItem(JWT_STORAGE_KEY);
}

// Clears the JWT and sends the user back to the login page.
// Called on logout or when the server returns 401 (token expired).
function clearJwtAndRedirectToLogin() {
    localStorage.removeItem(JWT_STORAGE_KEY);
    window.location.href = 'index.html';
}


// =============================================================================
// API call helpers
// Two functions: one for public endpoints, one for protected endpoints.
// The protected version automatically attaches the JWT to every request.
// =============================================================================

/**
 * Calls a public backend endpoint (login or register) — no JWT needed.
 * Always uses POST because only auth endpoints are public.
 */
async function callPublicApi(endpoint, requestBody) {
    const response = await fetch(`${BACKEND_URL}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
    });
    return response;
}

/**
 * Calls a protected backend endpoint with the stored JWT attached.
 *
 * Automatically redirects to login if:
 *   - There is no JWT in storage (user was never logged in)
 *   - The server returns 401 (token expired or tampered with)
 *
 * Returns the raw Response object so callers can check status and parse JSON themselves.
 */
async function callAuthenticatedApi(endpoint, httpMethod = 'GET', requestBody = null) {
    const jwtToken = getJwtFromLocalStorage();

    // No token means the user is not logged in — send them to the login page
    if (!jwtToken) {
        clearJwtAndRedirectToLogin();
        return null;
    }

    const fetchOptions = {
        method: httpMethod,
        headers: {
            // This is how the backend knows who is making the request
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json',
        },
    };

    // Only attach a body for requests that carry data (POST, PUT, etc.)
    if (requestBody) {
        fetchOptions.body = JSON.stringify(requestBody);
    }

    const response = await fetch(`${BACKEND_URL}${endpoint}`, fetchOptions);

    // 401 means the JWT is no longer valid — log the user out automatically
    if (response.status === 401) {
        clearJwtAndRedirectToLogin();
        return null;
    }

    return response;
}
