import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Tag {
  idTag: string;
  nombre: string;
}

@Injectable({
  providedIn: 'root'
})
export class TagsService {

  private readonly baseUrl = `${environment.apiBaseUrl}/tags`;


  constructor(private readonly http: HttpClient) {}

  getAllTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.baseUrl);
  }
}
