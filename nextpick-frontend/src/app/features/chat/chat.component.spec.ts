import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ChatService } from '../../core/services/chat.service';
import { ToastService } from '../../core/services/toast.service';
import { ChatComponent } from './chat.component';

describe('ChatComponent', () => {
  it('mantiene el mensaje del usuario y muestra el motivo si el proveedor falla', async () => {
    const chat = {
      sendMessageStreaming: jasmine.createSpy().and.rejectWith(
        new Error('Se ha alcanzado temporalmente el límite del servicio de IA. Inténtalo más tarde.')
      ),
    } as unknown as ChatService;
    const toast = { show: jasmine.createSpy() } as unknown as ToastService;
    const component = new ChatComponent(
      chat,
      toast,
      {} as AuthService,
      {} as Router,
    );

    await component.send('Recomiéndame ciencia ficción');

    expect(component.messages()).toEqual([
      jasmine.objectContaining({ sender: 'USER', content: 'Recomiéndame ciencia ficción' }),
      jasmine.objectContaining({
        sender: 'ASSISTANT',
        content: 'Se ha alcanzado temporalmente el límite del servicio de IA. Inténtalo más tarde.',
        streaming: false,
        error: true,
      }),
    ]);
    expect(toast.show).toHaveBeenCalledWith(
      'Se ha alcanzado temporalmente el límite del servicio de IA. Inténtalo más tarde.', 'error'
    );
  });
});
