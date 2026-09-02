import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { errorInterceptor } from './error.interceptor';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

describe('errorInterceptor', () => {
  let client: HttpClient;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [
      provideRouter([]),
      provideHttpClient(withInterceptors([errorInterceptor])),
      provideHttpClientTesting(),
      AuthService,
    ] });
    client = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => { http.verify(); localStorage.clear(); });

  it('shares one refresh request when concurrent calls receive 401, then retries both', () => {
    const results: number[] = [];
    client.get<number>(`${environment.apiUrl}/one`).subscribe((value) => results.push(value));
    client.get<number>(`${environment.apiUrl}/two`).subscribe((value) => results.push(value));
    http.expectOne(`${environment.apiUrl}/one`).flush({}, { status: 401, statusText: 'Unauthorized' });
    http.expectOne(`${environment.apiUrl}/two`).flush({}, { status: 401, statusText: 'Unauthorized' });

    const refresh = http.expectOne(`${environment.apiUrl}/auth/refresh`);
    expect(refresh.request.withCredentials).toBeTrue();
    refresh.flush({ accessToken: 'fresh', userId: 1, name: 'Ada', email: 'ada@example.test', avatarUrl: null });

    const retryOne = http.expectOne(`${environment.apiUrl}/one`);
    const retryTwo = http.expectOne(`${environment.apiUrl}/two`);
    expect(retryOne.request.headers.get('Authorization')).toBe('Bearer fresh');
    expect(retryTwo.request.headers.get('Authorization')).toBe('Bearer fresh');
    retryOne.flush(1);
    retryTwo.flush(2);
    expect(results).toEqual([1, 2]);
  });
});
