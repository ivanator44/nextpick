export interface AuthUser {
  userId: number;
  name: string;
  email: string;
  avatarUrl: string | null;
}

export interface AuthResponse extends AuthUser {
  accessToken: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}
