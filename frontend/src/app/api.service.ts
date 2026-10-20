import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { PreApproved, UsageCommand, UsageResponse } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api';

  process(command: UsageCommand): Observable<UsageResponse> {
    return this.http.post<UsageResponse>(`${this.baseUrl}/requests`, command);
  }

  recent(): Observable<UsageResponse[]> {
    return this.http.get<UsageResponse[]>(`${this.baseUrl}/requests/recent`);
  }

  preApproved(): Observable<PreApproved[]> {
    return this.http.get<PreApproved[]>(`${this.baseUrl}/pre-approved`);
  }
}

