import { apiFetch } from './client';
import type { CurrentUser, LoginRequest, LoginResponse, RegisterRequest, User } from './types';

export const authApi = {
    login: (request: LoginRequest) =>
        apiFetch<LoginResponse>('/api/v1/auth/login', {
            method: 'POST',
            body: JSON.stringify(request),
        }),

    register: (request: RegisterRequest) =>
        apiFetch<User>('/api/v1/auth/register', {
            method: 'POST',
            body: JSON.stringify(request),
        }),

    me: () =>
        apiFetch<CurrentUser>('/api/v1/users/me'),
};