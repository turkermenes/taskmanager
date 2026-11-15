import { ApiError } from './client';

export function toErrorMessage(error: unknown): string {
    if (error instanceof ApiError) {
        return error.message;
    }
    return 'Sunucuya ulaşılamıyor. Lütfen daha sonra tekrar deneyin.';
}

export function toFieldErrors(error: unknown): Record<string, string> {
    if (error instanceof ApiError) {
        return error.problem?.errors ?? {};
    }
    return {};
}