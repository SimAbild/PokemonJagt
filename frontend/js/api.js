async function callPublicApi(endpoint, requestBody) {
    const response = await fetch(`${BACKEND_URL}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
    });
    return response;
}

async function callAuthenticatedApi(endpoint, httpMethod = 'GET', requestBody = null) {
    const jwtToken = getJwtFromLocalStorage();

    if (!jwtToken) {
        clearJwtAndRedirectToLogin();
        return null;
    }

    const fetchOptions = {
        method: httpMethod,
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json',
        },
    };

    if (requestBody) {
        fetchOptions.body = JSON.stringify(requestBody);
    }

    const response = await fetch(`${BACKEND_URL}${endpoint}`, fetchOptions);

    if (response.status === 401) {
        clearJwtAndRedirectToLogin();
        return null;
    }

    return response;
}
