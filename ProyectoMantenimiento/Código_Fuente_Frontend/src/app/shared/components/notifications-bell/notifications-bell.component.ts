import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationsService } from '../../services/notifications.service';
import { Subscription, interval, switchMap } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-notifications-bell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications-bell.component.html'
})
export class NotificationsBellComponent implements OnInit, OnDestroy {

  unreadCount = 0;
  private sub?: Subscription;

  constructor(
    private readonly notificationsService: NotificationsService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadUnreadCount();

    // Refresco cada 60s (opcional)
    this.sub = interval(60000)
      .pipe(switchMap(() => this.notificationsService.getUnreadCount()))
      .subscribe({
        next: count => this.unreadCount = count,
        error: err => console.error('Error cargando unreadCount', err)
      });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  private loadUnreadCount(): void {
    this.notificationsService.getUnreadCount().subscribe({
      next: count => this.unreadCount = count,
      error: err => console.error('Error cargando unreadCount', err)
    });
  }

  onClickBell(): void {
    // Por ejemplo: ir a inicio usuario donde estará el buzón
    this.router.navigate(['/usuario/inicio']);
  }
}
