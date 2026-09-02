import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  id: number;
  text: string;
  kind: 'info' | 'success' | 'error';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  readonly messages = signal<ToastMessage[]>([]);

  show(text: string, kind: ToastMessage['kind'] = 'info'): void {
    const id = this.nextId++;
    this.messages.update((messages) => [...messages, { id, text, kind }]);
    window.setTimeout(() => this.dismiss(id), 4500);
  }

  dismiss(id: number): void {
    this.messages.update((messages) => messages.filter((message) => message.id !== id));
  }
}
