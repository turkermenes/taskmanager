import { useState, type FormEvent } from 'react';
import { toErrorMessage, toFieldErrors } from '../api/errors';
import type { TaskCreateRequest } from '../api/types';

interface TaskFormProps {
    onCreate: (request: TaskCreateRequest) => Promise<void>;
}

export function TaskForm({ onCreate }: TaskFormProps) {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError(null);
        setFieldErrors({});
        setSubmitting(true);
        try {
            await onCreate({ title, description: description || undefined });
            setTitle('');
            setDescription('');
        } catch (err) {
            setFieldErrors(toFieldErrors(err));
            setError(toErrorMessage(err));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <form onSubmit={handleSubmit} className="card" noValidate>
            <label>
                Başlık
                <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Ne yapılacak?" />
                {fieldErrors.title && <span className="field-error">{fieldErrors.title}</span>}
            </label>
            <label>
                Açıklama (opsiyonel)
                <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={2} />
                {fieldErrors.description && <span className="field-error">{fieldErrors.description}</span>}
            </label>
            {error && !fieldErrors.title && !fieldErrors.description && <p className="error">{error}</p>}
            <button type="submit" disabled={submitting}>
                {submitting ? 'Ekleniyor...' : 'Görev ekle'}
            </button>
        </form>
    );
}