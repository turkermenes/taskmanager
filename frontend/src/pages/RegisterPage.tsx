import { useState, type FormEvent } from 'react';
import { Link, Navigate, useNavigate } from 'react-router';
import { authApi } from '../api/auth';
import { ApiError } from '../api/client';
import { useAuth } from '../auth/useAuth';

export function RegisterPage() {
    const { login, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const [form, setForm] = useState({ username: '', email: '', password: '' });
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    if (isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    function updateField(field: keyof typeof form, value: string) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError(null);
        setFieldErrors({});
        setSubmitting(true);
        try {
            await authApi.register(form);
            await login(form.username, form.password);
            navigate('/', { replace: true });
        } catch (err) {
            if (err instanceof ApiError) {
                setFieldErrors(err.problem?.errors ?? {});
                setError(err.message);
            } else {
                setError('Sunucuya ulaşılamıyor. Lütfen daha sonra tekrar deneyin.');
            }
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="container narrow">
            <h1>Kayıt ol</h1>
            <form onSubmit={handleSubmit} className="card" noValidate>
                <label>
                    Kullanıcı adı
                    <input value={form.username} onChange={(e) => updateField('username', e.target.value)} autoComplete="username" />
                    {fieldErrors.username && <span className="field-error">{fieldErrors.username}</span>}
                </label>
                <label>
                    E-posta
                    <input type="email" value={form.email} onChange={(e) => updateField('email', e.target.value)} autoComplete="email" />
                    {fieldErrors.email && <span className="field-error">{fieldErrors.email}</span>}
                </label>
                <label>
                    Şifre
                    <input type="password" value={form.password} onChange={(e) => updateField('password', e.target.value)} autoComplete="new-password" />
                    {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
                </label>
                {error && <p className="error">{error}</p>}
                <button type="submit" disabled={submitting}>
                    {submitting ? 'Kaydediliyor...' : 'Kayıt ol'}
                </button>
            </form>
            <p>Zaten hesabın var mı? <Link to="/login">Giriş yap</Link></p>
        </main>
    );
}