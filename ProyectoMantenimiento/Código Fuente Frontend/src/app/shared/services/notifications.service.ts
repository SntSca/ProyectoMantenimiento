import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SharedService } from './shared.services';

export interface Notification {
  id: string;
  type: string;
  subtype: string;
  title: string;
  body: string;
  payload: any;
  read: boolean;
  createdAt: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationsService {

  private readonly baseUrl = `${environment.apiBaseUrl}/api/notifications`;

  constructor(
    private readonly http: HttpClient,
    private readonly sharedService: SharedService
  ) {}

  // Buzón de notificaciones
  getInbox(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.baseUrl, {
      headers: this.sharedService.getAuthHeaders(),
    });
  }

  // Número de no leídas
  getUnreadCount(): Observable<number> {
    return this.http
      .get<{ unreadCount: number }>(`${this.baseUrl}/unread-count`, {
        headers: this.sharedService.getAuthHeaders(),
      })
      .pipe(map(res => res.unreadCount ?? 0));
  }

  // Marcar como leída
  markAsRead(id: string): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/read`, {}, {
      headers: this.sharedService.getAuthHeaders(),
    });
  }

  // Eliminar notificación
  deleteNotification(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, {
      headers: this.sharedService.getAuthHeaders(),
    });
  }
}
