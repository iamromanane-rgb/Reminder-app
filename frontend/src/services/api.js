import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:9090';

const api = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' }, //use json headers by default
});

// Attach JWT token to every request
api.interceptors.request.use( // Add auth token to headers
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Redirect to login on 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // Only redirect if not already on auth pages
      if (
        !window.location.pathname.startsWith('/login') &&
        !window.location.pathname.startsWith('/register')
      ) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
