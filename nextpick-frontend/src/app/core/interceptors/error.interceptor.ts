import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error) => {
      const isAuthRequest = req.url.includes('/auth/login') || req.url.includes('/auth/register')
        || req.url.includes('/auth/refresh');
      if (error.status !== 401 || isAuthRequest) {
        return throwError(() => error);
      }
      return authService.refreshAccessToken().pipe(
        switchMap((response) => next(req.clone({
          withCredentials: true,
          setHeaders: { Authorization: `Bearer ${response.accessToken}` },
        }))),
        catchError((refreshError) => {
          authService.clearSession(true);
          return throwError(() => refreshError);
        })
      );
    })
  );
};
