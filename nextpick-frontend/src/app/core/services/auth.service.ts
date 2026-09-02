import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, defer, finalize, Observable, of, shareReplay, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from '../models/user.model';

const ACCESS_TOKEN_KEY = 'nextpick_access_token';
const USER_KEY = 'nextpick_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  // Signal privado con el usuario actual (o null si no hay sesión)
  private readonly currentUserSignal = signal<AuthUser | null>(this.loadStoredUser());

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  private refreshInFlight$: Observable<AuthResponse> | null = null;

  constructor(private http: HttpClient, private router: Router) {}

  register(request: RegisterRequest) {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request).pipe(
      tap((response) => this.handleAuthSuccess(response))
    );
  }

  login(request: LoginRequest) {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(
      tap((response) => this.handleAuthSuccess(response))
    );
  }

  logout(): void {
    this.http.post<void>(`${this.baseUrl}/logout`, {}, { withCredentials: true })
      .pipe(finalize(() => this.clearSession(true)))
      .subscribe({ error: () => undefined });
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  refreshAccessToken(): Observable<AuthResponse> {
    if (!this.refreshInFlight$) {
      this.refreshInFlight$ = defer(() => this.http.post<AuthResponse>(
        `${this.baseUrl}/refresh`, {}, { withCredentials: true }
      )).pipe(
        tap((response) => this.handleAuthSuccess(response)),
        finalize(() => (this.refreshInFlight$ = null)),
        shareReplay({ bufferSize: 1, refCount: false })
      );
    }
    return this.refreshInFlight$;
  }

  restoreSession(): Observable<AuthResponse | null> {
    return this.refreshAccessToken().pipe(
      catchError(() => {
        this.clearSession(false);
        return of(null);
      })
    );
  }

  clearSession(navigate: boolean): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
    if (navigate) this.router.navigate(['/']);
  }

  private handleAuthSuccess(response: AuthResponse): void {
    const { accessToken, ...user } = response;
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  private loadStoredUser(): AuthUser | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  }
}
