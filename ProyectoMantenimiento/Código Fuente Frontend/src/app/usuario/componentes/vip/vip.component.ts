import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-vip',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './vip.component.html',
})
export class VipComponent {
  @Input() showVipConfirmation: boolean = false;
  @Input() showCancelVipConfirmation: boolean = false;

  @Output() confirmVipUpgrade = new EventEmitter<void>();
  @Output() cancelVipUpgrade = new EventEmitter<void>();
  @Output() confirmVipCancel = new EventEmitter<void>();
  @Output() cancelVipCancel = new EventEmitter<void>();

  // Método para manejar teclas en los modales
  onModalKeyDown(event: KeyboardEvent, action: () => void): void {
    if (event.key === 'Escape') {
      action();
    }
  }
}