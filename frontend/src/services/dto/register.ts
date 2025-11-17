export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
    roles: string[];
}

export interface RegisterResponse {
    email: string;
}
