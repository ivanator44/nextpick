import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { AuthModalComponent } from './shared/components/auth-modal/auth-modal.component';
import { ToastContainerComponent } from './shared/components/toast-container/toast-container.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, AuthModalComponent, ToastContainerComponent],
  template: `
    @if (showNavbar()) {
      <app-navbar (loginRequested)="authModalOpen.set(true)" />
    }

    <router-outlet />
    @if (showNavbar()) {
      <footer class="credits">
        <span>NextPick utiliza la API de TMDB, pero no está respaldada ni certificada por TMDB.</span>
        <span>La disponibilidad en plataformas es proporcionada por JustWatch.</span>
      </footer>
    }
    <app-toast-container />

    @if (authModalOpen()) {
      <app-auth-modal (closed)="authModalOpen.set(false)" />
    }
  `,
  styles: [`
    .credits { display: flex; flex-wrap: wrap; justify-content: center; gap: .5rem 1.5rem;
      padding: 1.5rem; border-top: 1px solid var(--border-subtle); color: var(--text-secondary); font-size: .75rem; text-align: center; }
  `],
})
export class AppComponent {
  private readonly router = inject(Router);

  private readonly currentUrl = signal(this.router.url);

  readonly authModalOpen = signal(false);

  readonly showNavbar = computed(() =>
      !this.currentUrl().startsWith('/chat')
  );

  constructor() {
    this.router.events
        .pipe(
            filter(
                (event): event is NavigationEnd =>
                    event instanceof NavigationEnd
            )
        )
        .subscribe((event) => {
          this.currentUrl.set(event.urlAfterRedirects);
        });
  }
}
