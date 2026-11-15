import { useState, type FormEvent } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router';
import { ApiError } from '../api/client';
import { useAuth } from '../auth/useAuth';

export function LoginPage() {
    const { login, isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const from = (location.state as { from?: string } | null)?.from ?? '/';

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    if (isAuthenticated) {
        return <Navigate to={from} replace />;
    }

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError(null);
        setSubmitting(true);
        try {
            await login(username, password);
            navigate(from, { replace: true });
        } catch (err) {
            setError(err instanceof ApiError ? err.message : 'Sunucuya ulaşılamıyor. Lütfen daha sonra tekrar deneyin.');
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="container narrow">
            <h1>Giriş yap</h1>
            <form onSubmit={handleSubmit} className="card">
                <label>
                    Kullanıcı adı
                    <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" required />
                </label>
                <label>
                    Şifre
                    <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required />
                </label>
                {error && <p className="error">{error}</p>}
                <button type="submit" disabled={submitting}>
                    {submitting ? 'Giriş yapılıyor...' : 'Giriş yap'}
                </button>
            </form>
            <p>Hesabın yok mu? <Link to="/register">Kayıt ol</Link></p>
        </main>
    );
}