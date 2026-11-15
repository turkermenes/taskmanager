const TOKEN_KEY = 'taskmanager.accessToken';

export interface TokenPayload {
    sub: string;
    userId: number;
    role: string;
    exp: number;
}

export const tokenStorage = {
    get(): string | null {
        try {
            return localStorage.getItem(TOKEN_KEY);
        } catch {
            return null;
        }
    },
    set(token: string): void {
        localStorage.setItem(TOKEN_KEY, token);
    },
    clear(): void {
        localStorage.removeItem(TOKEN_KEY);
    },
};

export function decodeToken(token: string): TokenPayload | null {
    try {
        const payload = token.split('.')[1];
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
        const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
        return JSON.parse(atob(padded)) as TokenPayload;
    } catch {
        return null;
    }
}

export function isTokenExpired(payload: TokenPayload): boolean {
    return payload.exp * 1000 <= Date.now();
}