import { Injectable} from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  title: string;
  type: 'success' | 'error' | 'warning' | 'info';
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly toasts: Toast[] = [];
  private nextId = 0;

  constructor(
  ) {
    this.initToastContainer();
  }

  private initToastContainer(): void {
    if (typeof document !== 'undefined') {
      let container = document.getElementById('toast-container');
      if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
      }
    }
  }

  /**
   * Muestra una notificación de éxito
   * @param message Mensaje a mostrar
   * @param title Título opcional
   */
  success(message: string, title: string = '¡Éxito!'): void {
    this.show(message, title, 'success');
  }

  /**
   * Muestra una notificación de error
   * @param message Mensaje a mostrar
   * @param title Título opcional
   */
  error(message: string, title: string = 'Error'): void {
    this.show(message, title, 'error');
  }

  /**
   * Muestra una notificación de advertencia
   * @param message Mensaje a mostrar
   * @param title Título opcional
   */
  warning(message: string, title: string = 'Advertencia'): void {
    this.show(message, title, 'warning');
  }

  /**
   * Muestra una notificación de información
   * @param message Mensaje a mostrar
   * @param title Título opcional
   */
  info(message: string, title: string = 'Información'): void {
    this.show(message, title, 'info');
  }

  private show(message: string, title: string, type: 'success' | 'error' | 'warning' | 'info'): void {
    if (typeof document === 'undefined') return;

    const toast: Toast = {
      id: this.nextId++,
      message,
      title,
      type
    };

    this.toasts.push(toast);
    this.createToastElement(toast);

    // Auto-remover después de 4 segundos
    setTimeout(() => {
      this.remove(toast.id);
    }, 4000);
  }

  private createToastElement(toast: Toast): void {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toastEl = document.createElement('div');
    toastEl.id = `toast-${toast.id}`;
    toastEl.className = `toast toast-${toast.type}`;
    toastEl.innerHTML = `
      <div class="toast-header">
        <strong class="toast-title">${this.getIcon(toast.type)} ${toast.title}</strong>
        <button type="button" class="toast-close" aria-label="Close">×</button>
      </div>
      <div class="toast-body">${toast.message}</div>
      <div class="toast-progress"></div>
    `;

    // Agregar evento de cierre
    const closeBtn = toastEl.querySelector('.toast-close');
    closeBtn?.addEventListener('click', () => this.remove(toast.id));

    container.appendChild(toastEl);

    // Animar entrada
    setTimeout(() => toastEl.classList.add('show'), 10);
  }

  private getIcon(type: string): string {
    const icons = {
      success: '✓',
      error: '✕',
      warning: '⚠',
      info: 'ℹ'
    };
    return icons[type as keyof typeof icons] || 'ℹ';
  }

  private remove(id: number): void {
    const index = this.toasts.findIndex(t => t.id === id);
    if (index !== -1) {
      this.toasts.splice(index, 1);
    }

    const toastEl = document.getElementById(`toast-${id}`);
    if (toastEl) {
      toastEl.classList.remove('show');
      toastEl.classList.add('hide');
      setTimeout(() => toastEl.remove(), 300);
    }
  }
}
