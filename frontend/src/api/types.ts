// ===== Görevler =====
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface Task {
    id: number;
    title: string;
    description: string | null;
    status: TaskStatus;
    createdAt: string;
    updatedAt: string;
}

export interface TaskCreateRequest {
    title: string;
    description?: string;
}

export interface TaskUpdateRequest {
    title: string;
    description?: string;
    status: TaskStatus;
}

// ===== Kimlik doğrulama =====
export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
}

export interface User {
    id: number;
    username: string;
    email: string;
    role: 'USER' | 'ADMIN';
    createdAt: string;
}

export interface CurrentUser {
    username: string;
    userId: number;
    authorities: string[];
    expiresAt: string;
}

// ===== Hatalar (RFC 9457) =====
export interface ProblemDetail {
    type?: string;
    title?: string;
    status?: number;
    detail?: string;
    instance?: string;
    errors?: Record<string, string>;
    errorId?: string;
}