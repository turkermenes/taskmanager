import type { ProblemDetail } from './types';

const API_URL = import.meta.env.VITE_API_URL ?? '';

let accessToken: string | null = null;

export function setAccessToken(token: string | null): void {
    accessToken = token;
}

let unauthorizedHandler: (() => void) | null = null;

export function setUnauthorizedHandler(handler: (() => void) | null): void {
    unauthorizedHandler = handler;
}

export class ApiError extends Error {
    readonly status: number;
    readonly problem: ProblemDetail | null;

    constructor(status: number, problem: ProblemDetail | null) {
        super(problem?.detail ?? `İstek başarısız oldu (HTTP ${status})`);
        this.name = 'ApiError';
        this.status = status;
        this.problem = problem;
    }
}

export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
    const headers = new Headers(options.headers);

    if (options.body && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json');
    }
    if (accessToken) {
        headers.set('Authorization', `Bearer ${accessToken}`);
    }

    const response = await fetch(`${API_URL}${path}`, { ...options, headers });

    if (!response.ok) {
        if (response.status === 401 && accessToken && unauthorizedHandler) {
            unauthorizedHandler();
        }

        let problem: ProblemDetail | null = null;
        try {
            problem = await response.json();
        } catch {
            // Gövde JSON değilse (örneğin boş 401 cevabı) problem null kalır
        }
        throw new ApiError(response.status, problem);
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return (await response.json()) as T;
}