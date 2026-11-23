// Central Web Dashboard API client and utilities

async function apiFetch(endpoint, options = {}) {
  const token = localStorage.getItem('auth_token');
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...(options.headers || {})
  };

  try {
    const res = await fetch(endpoint, {
      ...options,
      headers
    });

    if (res.status === 401 || res.status === 403) {
      console.warn("Unauthorized / Token expired. Redirecting to login...");
      localStorage.clear();
      window.location.href = '/index.html';
      return null;
    }

    const data = await res.json();
    return data;
  } catch (err) {
    console.error("API error for " + endpoint, err);
    return null;
  }
}

function logout() {
  localStorage.clear();
  window.location.href = '/index.html';
}
