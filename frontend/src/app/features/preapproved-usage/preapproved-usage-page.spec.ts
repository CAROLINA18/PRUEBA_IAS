import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { ApiService } from './api.service';
import { PreapprovedUsagePage } from './preapproved-usage-page';
import { PreApproved, UsageResponse } from './models';

describe('PreapprovedUsagePage', () => {
  let fixture: ComponentFixture<PreapprovedUsagePage>;
  let component: PreapprovedUsagePage;
  let api: jasmine.SpyObj<ApiService>;

  const credits: PreApproved[] = [
    { id: 'PRA-1001', customerId: 'USR-10', status: 'ACTIVE', availableAmount: 1000000 }
  ];

  const authorized: UsageResponse = {
    requestReference: 'REF-001',
    preApprovedId: 'PRA-1001',
    customerId: 'USR-10',
    amount: 600000,
    status: 'AUTHORIZED',
    rejectionReason: null,
    processedAt: '2026-10-19T20:00:00Z',
    remainingAmount: 400000,
    replayed: false
  };

  beforeEach(async () => {
    api = jasmine.createSpyObj<ApiService>('ApiService', ['process', 'recent', 'preApproved']);
    api.recent.and.returnValue(of([]));
    api.preApproved.and.returnValue(of(credits));

    await TestBed.configureTestingModule({
      imports: [PreapprovedUsagePage],
      providers: [{ provide: ApiService, useValue: api }]
    }).compileComponents();

    fixture = TestBed.createComponent(PreapprovedUsagePage);
    component = fixture.componentInstance;
  });

  it('loads the credit options and recent requests on initialization', () => {
    api.recent.and.returnValue(of([authorized]));

    fixture.detectChanges();

    expect(component.preApproved).toEqual(credits);
    expect(component.recent).toEqual([authorized]);
    expect(api.preApproved).toHaveBeenCalledTimes(1);
    expect(api.recent).toHaveBeenCalledTimes(1);
  });

  it('does not submit an invalid amount', () => {
    component.form.controls.amount.setValue(0);

    component.submit();

    expect(component.form.invalid).toBeTrue();
    expect(api.process).not.toHaveBeenCalled();
  });

  it('shows an authorized result and refreshes server data', () => {
    api.process.and.returnValue(of(authorized));

    component.submit();

    expect(component.result).toEqual(authorized);
    expect(component.loading).toBeFalse();
    expect(api.recent).toHaveBeenCalledTimes(1);
    expect(api.preApproved).toHaveBeenCalledTimes(1);
  });

  it('shows the message returned by the API', () => {
    api.process.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 409,
      error: { message: 'La referencia ya fue procesada' }
    })));

    component.submit();

    expect(component.errorMessage).toBe('La referencia ya fue procesada');
    expect(component.loading).toBeFalse();
  });

  it('synchronizes the customer when a credit is selected', () => {
    component.preApproved = credits;
    component.form.controls.preApprovedId.setValue('PRA-1001');
    component.form.controls.customerId.setValue('USR-20');

    component.syncCustomer();

    expect(component.form.controls.customerId.value).toBe('USR-10');
  });
});
