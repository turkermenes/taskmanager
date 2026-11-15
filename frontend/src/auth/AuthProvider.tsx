import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { authApi } from '../api/auth';
import { setAccessToken, setUnauthorizedHandler } from '../api/client';
import { AuthContext } from './authContext';
import { decodeToken, isTokenExpired, tokenStorage } from './tokenStorage';

function getInitialUsername(): string | null {
    const token = tokenStorage.get();
    if (!token) return null;

    const payload = decodeToken(token);
    if (!payload || isTokenExpired(payload)) {
        tokenStorage.clear();
        return null;
    }

    setAccessToken(token);
    return payload.sub;
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [username, setUsername] = useState<string | null>(getInitialUsername);

    const logout = useCallback(() => {
        tokenStorage.clear();
        setAccessToken(null);
        setUsername(null);
    }, []);

    const login = useCallback(async (user: string, password: string) => {
        const response = await authApi.login({ username: user, password });
        tokenStorage.set(response.accessToken);
        setAccessToken(response.accessToken);
        setUsername(decodeToken(response.accessToken)?.sub ?? user);
    }, []);

    useEffect(() => {
        setUnauthorizedHandler(logout);
        return () => setUnauthorizedHandler(null);
    }, [logout]);

    const value = useMemo(
        () => ({ username, isAuthenticated: username !== null, login, logout }),
        [username, login, logout],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}