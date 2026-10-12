import axios from 'axios';
import type { ApiErrorBody } from '../types';

export const TOKEN_KEY = 'helpdesk.token';

const PRODUCTION_API = 'https://helpdesk-api-wo8z.onrender.com';
const LOCAL_API = 'http://localhost:8081';

const configured = import.meta.env.VITE_API_BASE_URL?.trim();
const apiOrigin = (configured || (import.meta.env.PROD ? PRODUCTION_API : LOCAL_API)).replace(/\/$/, '');

export const api = axios.create({
  baseURL: `${apiOrigin}/api`,
});

export function setAuthToken(token: string | null) {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    delete api.defaults.headers.common.Authorization;
  }
}

const existing = localStorage.getItem(TOKEN_KEY);
if (existing) {
  setAuthToken(existing);
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const url: string = error.config?.url ?? '';
    const isAuthAttempt = url.includes('/auth/login') || url.includes('/auth/register');
    if (axios.isAxiosError(error) && error.response?.status === 401 && !isAuthAttempt) {
      localStorage.removeItem(TOKEN_KEY);
      setAuthToken(null);
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  },
);

export function errorMessage(error: unknown, fallback: string) {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    const data = error.response?.data;
    if (data?.fieldErrors?.length) {
      return data.fieldErrors.map((field) => field.message).join(' ');
    }
    if (data?.message) {
      return data.message;
    }
  }
  return fallback;
}
