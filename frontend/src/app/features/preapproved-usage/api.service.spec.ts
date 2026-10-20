import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ApiService } from './api.service';
import { UsageCommand } from './models';

describe('ApiService', () => {
  let service: ApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ApiService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('posts a usage command to the requests endpoint', () => {
    const command: UsageCommand = {
      requestReference: 'REF-001',
      preApprovedId: 'PRA-1001',
      customerId: 'USR-10',
      amount: 100000
    };

    service.process(command).subscribe();

    const request = http.expectOne('http://localhost:8080/api/requests');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(command);
    request.flush({});
  });

  it('gets recent requests', () => {
    service.recent().subscribe();

    const request = http.expectOne('http://localhost:8080/api/requests/recent');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('gets a request by its encoded reference', () => {
    service.findByReference(' REF/001 ').subscribe();

    const request = http.expectOne('http://localhost:8080/api/requests/REF%2F001');
    expect(request.request.method).toBe('GET');
    request.flush({});
  });

  it('gets the available pre-approved credits', () => {
    service.preApproved().subscribe();

    const request = http.expectOne('http://localhost:8080/api/pre-approved');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });
});
