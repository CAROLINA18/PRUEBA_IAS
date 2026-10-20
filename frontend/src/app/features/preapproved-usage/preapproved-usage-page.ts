import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { ApiService } from './api.service';
import { ApiError, PreApproved, UsageResponse } from './models';

@Component({
  selector: 'app-preapproved-usage',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './preapproved-usage-page.html',
  styleUrl: './preapproved-usage-page.scss'
})
export class PreapprovedUsagePage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly api = inject(ApiService);

  readonly form = this.formBuilder.nonNullable.group({
    requestReference: ['REF-001', [Validators.required, Validators.maxLength(80)]],
    preApprovedId: ['PRA-1001', Validators.required],
    customerId: ['USR-10', Validators.required],
    amount: [600000, [Validators.required, Validators.min(1)]]
  });

  preApproved: PreApproved[] = [];
  recent: UsageResponse[] = [];
  result: UsageResponse | null = null;
  errorMessage = '';
  loading = false;
  refreshing = false;

  ngOnInit(): void {
    this.loadPreApproved();
    this.loadRecent();
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.api.process(this.form.getRawValue())
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (result) => {
          this.result = result;
          this.loadRecent();
          this.loadPreApproved();
        },
        error: (error: HttpErrorResponse) => this.handleError(error)
      });
  }

  loadRecent(): void {
    this.refreshing = true;
    this.api.recent()
      .pipe(finalize(() => (this.refreshing = false)))
      .subscribe({
        next: (recent) => (this.recent = recent),
        error: (error: HttpErrorResponse) => this.handleError(error)
      });
  }

  syncCustomer(): void {
    const selected = this.preApproved.find(
      (item) => item.id === this.form.controls.preApprovedId.value
    );
    if (selected) {
      this.form.controls.customerId.setValue(selected.customerId);
    }
  }

  formatCurrency(value: number | null): string {
    if (value === null || value === undefined) {
      return 'No aplica';
    }
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0
    }).format(value);
  }

  formatDate(value: string): string {
    return new Intl.DateTimeFormat('es-CO', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(new Date(value));
  }

  private loadPreApproved(): void {
    this.api.preApproved().subscribe({
      next: (items) => (this.preApproved = items),
      error: (error: HttpErrorResponse) => this.handleError(error)
    });
  }

  private handleError(error: HttpErrorResponse): void {
    const apiError = error.error as Partial<ApiError> | null;
    this.errorMessage = apiError?.message ?? 'No fue posible comunicarse con el backend.';
  }
}
