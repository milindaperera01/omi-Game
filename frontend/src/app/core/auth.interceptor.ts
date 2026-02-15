import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, from, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const isRefreshRequest = req.url.includes('/api/auth/refresh');
  const alreadyRetried = req.headers.has('x-auth-retried');

  const token = auth.accessToken;
  const authReq = token
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      })
    : req;

  return next(authReq).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        auth.session?.refreshToken &&
        !isRefreshRequest &&
        !alreadyRetried
      ) {
        return from(auth.refreshTokens()).pipe(
          switchMap((ok) => {
            if (!ok) {
              auth.logout();
              router.navigate(['/login']);
              return throwError(() => error);
            }
            const nextToken = auth.accessToken;
            if (!nextToken) {
              auth.logout();
              router.navigate(['/login']);
              return throwError(() => error);
            }
            return next(
              req.clone({
                setHeaders: {
                  Authorization: `Bearer ${nextToken}`,
                  'x-auth-retried': '1'
                }
              })
            );
          })
        );
      }
      if (error instanceof HttpErrorResponse && error.status === 401 && isRefreshRequest) {
        auth.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
