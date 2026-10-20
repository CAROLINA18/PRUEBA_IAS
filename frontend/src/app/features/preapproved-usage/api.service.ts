import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { PreApproved, UsageCommand, UsageResponse } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private static readonly BASE_URL = 'http://localhost:8080/api';
  private readonly http = inject(HttpClient);

  process(command: UsageCommand): Observable<UsageResponse> {
    return this.http.post<UsageResponse>(`${ApiService.BASE_URL}/requests`, command);
  }

  recent(): Observable<UsageResponse[]> {
    return this.http.get<UsageResponse[]>(`${ApiService.BASE_URL}/requests/recent`);
  }

  preApproved(): Observable<PreApproved[]> {
    return this.http.get<PreApproved[]>(`${ApiService.BASE_URL}/pre-approved`);
  }
}
