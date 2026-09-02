import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();

  const withCredentials = req.url.startsWith(environment.apiUrl)
    ? req.clone({ withCredentials: true })
    : req;

  if (!token) return next(withCredentials);

  const cloned = withCredentials.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });

  return next(cloned);
};
