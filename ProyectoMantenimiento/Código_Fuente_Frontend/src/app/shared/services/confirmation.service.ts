import { Injectable } from '@angular/core';

/**
 * ===========================
 * SERVICIO DE CONFIRMACIÓN
 * ===========================
 * 
 * Servicio para mostrar modales de confirmación personalizados
 * que reemplazan los window.confirm() nativos del navegador.
 * 
 * Usa los estilos de toast.scss para mantener consistencia visual.
 * 
 * @example
 * // Confirmación básica
 * const confirmed = await this.confirmationService.confirm({
 *   message: '¿Estás seguro de realizar esta acción?',
 *   title: 'Confirmación',
 *   type: 'warning'
 * });
 * if (confirmed) {
 *   // Realizar acción
 * }
 * 
 * @example
 * // Confirmación de eliminación
 * const confirmed = await this.confirmationService.confirmDelete('nombre del elemento');
 * if (confirmed) {
 *   // Eliminar elemento
 * }
 * 
 * @example
 * // Confirmación de logout
 * const confirmed = await this.confirmationService.confirmLogout();
 * if (confirmed) {
 *   // Cerrar sesión
 * }
 */

export interface ConfirmationModal {
  id: number;
  message: string;
  title: string;
  type: 'warning' | 'error' | 'info';
  confirmText: string;
  cancelText: string;
  onConfirm: () => void;
  onCancel?: () => void;
  isInactivityWarning?: boolean; // Flag para identificar modales de inactividad
}

@Injectable({
  providedIn: 'root'
})
export class ConfirmationService {
  private readonly modals: ConfirmationModal[] = [];
  private nextId = 0;

  constructor() {
    this.initConfirmationContainer();
  }

  private initConfirmationContainer(): void {
    if (typeof document !== 'undefined') {
      let container = document.getElementById('confirmation-container');
      if (!container) {
        container = document.createElement('div');
        container.id = 'confirmation-container';
        container.className = 'confirmation-container';
        document.body.appendChild(container);
      }
    }
  }

  /**
   * Muestra un modal de confirmación
   * @param options Opciones del modal
   * @returns Promise que se resuelve con true si se confirma, false si se cancela
   */
  confirm(options: {
    message: string;
    title?: string;
    type?: 'warning' | 'error' | 'info';
    confirmText?: string;
    cancelText?: string;
    isInactivityWarning?: boolean;
  }): Promise<boolean> {
    return new Promise((resolve) => {
      if (typeof document === 'undefined') {
        resolve(false);
        return;
      }

      const modal: ConfirmationModal = {
        id: this.nextId++,
        message: options.message,
        title: options.title ?? 'Confirmación',
        type: options.type ?? 'warning',
        confirmText: options.confirmText ?? 'Confirmar',
        cancelText: options.cancelText ?? 'Cancelar',
        isInactivityWarning: options.isInactivityWarning ?? false,
        onConfirm: () => {
          this.remove(modal.id);
          resolve(true);
        },
        onCancel: () => {
          this.remove(modal.id);
          resolve(false);
        }
      };

      this.modals.push(modal);
      this.createModalElement(modal);
    });
  }

  /**
   * Atajo para confirmación de eliminación
   * @param itemName Nombre del elemento a eliminar
   * @returns Promise que se resuelve con true si se confirma
   */
  confirmDelete(itemName: string): Promise<boolean> {
    return this.confirm({
      message: `¿Estás seguro de que deseas eliminar "${itemName}"?\n\nEsta acción no se puede deshacer.`,
      title: 'Confirmar eliminación',
      type: 'error',
      confirmText: 'Eliminar',
      cancelText: 'Cancelar'
    });
  }

  /**
   * Atajo para confirmación de logout
   * @returns Promise que se resuelve con true si se confirma
   */
  confirmLogout(): Promise<boolean> {
    return this.confirm({
      message: '¿Estás seguro de que deseas cerrar sesión?',
      title: 'Cerrar sesión',
      type: 'warning',
      confirmText: 'Cerrar sesión',
      cancelText: 'Cancelar'
    });
  }

  private createModalElement(modal: ConfirmationModal): void {
    const container = document.getElementById('confirmation-container');
    if (!container) return;

    // Crear overlay
    const overlay = document.createElement('div');
    overlay.id = `confirmation-overlay-${modal.id}`;
    overlay.className = 'confirmation-overlay';

    // Crear modal
    const modalEl = document.createElement('div');
    modalEl.id = `confirmation-modal-${modal.id}`;
    modalEl.className = `confirmation-modal confirmation-modal-${modal.type}`;
    modalEl.innerHTML = `
      <div class="confirmation-header">
        <strong class="confirmation-title">${this.getIcon(modal.type)} ${modal.title}</strong>
      </div>
      <div class="confirmation-body">${modal.message.replaceAll('\n', '<br>')}</div>
      <div class="confirmation-footer">
        <button type="button" class="confirmation-btn confirmation-btn-cancel">${modal.cancelText}</button>
        <button type="button" class="confirmation-btn confirmation-btn-confirm">${modal.confirmText}</button>
      </div>
    `;

    // Agregar eventos
    const cancelBtn = modalEl.querySelector('.confirmation-btn-cancel');
    const confirmBtn = modalEl.querySelector('.confirmation-btn-confirm');

    cancelBtn?.addEventListener('click', () => modal.onCancel?.());
    confirmBtn?.addEventListener('click', () => modal.onConfirm());
    overlay.addEventListener('click', () => modal.onCancel?.());

    // Prevenir que el click en el modal cierre el overlay
    modalEl.addEventListener('click', (e) => e.stopPropagation());

    container.appendChild(overlay);
    container.appendChild(modalEl);

    // Animar entrada
    setTimeout(() => {
      overlay.classList.add('show');
      modalEl.classList.add('show');
    }, 10);
  }

  private getIcon(type: string): string {
    const icons = {
      warning: '⚠',
      error: '✕',
      info: 'ℹ'
    };
    return icons[type as keyof typeof icons] || 'ℹ';
  }

  private remove(id: number): void {
    const index = this.modals.findIndex(m => m.id === id);
    if (index !== -1) {
      this.modals.splice(index, 1);
    }

    const modalEl = document.getElementById(`confirmation-modal-${id}`);
    const overlayEl = document.getElementById(`confirmation-overlay-${id}`);

    if (modalEl && overlayEl) {
      modalEl.classList.remove('show');
      overlayEl.classList.remove('show');
      setTimeout(() => {
        modalEl.remove();
        overlayEl.remove();
      }, 300);
    }
  }

  /**
   * Cierra todos los modales de advertencia de inactividad
   */
  closeInactivityWarnings(): void {
    // Obtener IDs de modales de inactividad
    const inactivityModals = this.modals.filter(m => m.isInactivityWarning);
    
    // Cerrar cada uno usando for...of
    for (const modal of inactivityModals) {
      // Llamar al callback de cancelación si existe
      modal.onCancel?.();
    }
  }
}
