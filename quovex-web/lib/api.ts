import { auth } from './firebase/config';

const BACKEND_BASE_URL = 'https://us-central1-quovex-f3104.cloudfunctions.net/api';

/**
 * Authenticated API Caller
 * Injects fresh Firebase ID token in Authorization header.
 * Interacts exclusively with secure Cloud Functions backend.
 */
export async function callBackend<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const user = auth.currentUser;
  let idToken = '';

  if (user) {
    idToken = await user.getIdToken();
  }

  const url = `${BACKEND_BASE_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> || {}),
  };

  if (idToken) {
    headers['Authorization'] = `Bearer ${idToken}`;
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.error || errorBody.message || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}
