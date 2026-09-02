import { Component } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  template: `
    <div class="toasts" aria-live="polite" aria-atomic="false">
      @for (message of toast.messages(); track message.id) {
        <div class="toast" [class]="message.kind" role="status">
          <span>{{ message.text }}</span>
          <button type="button" (click)="toast.dismiss(message.id)" aria-label="Cerrar aviso">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 6l12 12M18 6L6 18"/></svg>
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toasts { position: fixed; z-index: 3000; right: 1rem; bottom: 1rem; display: grid; gap: .6rem; }
    .toast { width: min(360px, calc(100vw - 2rem)); display: flex; gap: 1rem; align-items: center;
      justify-content: space-between; padding: .85rem 1rem; border: 1px solid var(--border-subtle);
      border-left: 4px solid #6aa7ff; border-radius: var(--radius-sm); background: var(--bg-surface-alt);
      box-shadow: var(--shadow-elevated); }
    .toast.success { border-left-color: #5bcf8c; }
    .toast.error { border-left-color: var(--accent); }
    button { font-size: 1.2rem; color: var(--text-secondary); }
  `],
})
export class ToastContainerComponent {
  constructor(public toast: ToastService) {}
}
