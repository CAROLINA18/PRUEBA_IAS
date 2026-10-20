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
    preApprovedId: ['', Validators.required],
    customerId: ['', Validators.required],
    amount: [600000, [Validators.required, Validators.min(1)]]
  });
  readonly referenceQuery = this.formBuilder.nonNullable.control('', [
    Validators.required,
    Validators.maxLength(80)
  ]);
  readonly pageSize = 5;

  preApproved: PreApproved[] = [];
  recent: UsageResponse[] = [];
  result: UsageResponse | null = null;
  lookupResult: UsageResponse | null = null;
  errorMessage = '';
  lookupError = '';
  loading = false;
  refreshing = false;
  searching = false;
  currentPage = 0;

  get paginatedRecent(): UsageResponse[] {
    const start = this.currentPage * this.pageSize;
    return this.recent.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.recent.length / this.pageSize));
  }

  get customers(): string[] {
    return [...new Set(this.preApproved.map((item) => item.customerId))];
  }

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
        next: (recent) => {
          this.recent = recent;
          this.currentPage = 0;
        },
        error: (error: HttpErrorResponse) => this.handleError(error)
      });
  }

  lookupByReference(): void {
    this.referenceQuery.markAsTouched();
    if (this.referenceQuery.invalid || this.searching) {
      return;
    }

    this.searching = true;
    this.lookupError = '';
    this.lookupResult = null;
    this.api.findByReference(this.referenceQuery.value)
      .pipe(finalize(() => (this.searching = false)))
      .subscribe({
        next: (result) => (this.lookupResult = result),
        error: (error: HttpErrorResponse) => {
          this.lookupError = this.errorText(error);
        }
      });
  }

  previousPage(): void {
    this.currentPage = Math.max(0, this.currentPage - 1);
  }

  nextPage(): void {
    this.currentPage = Math.min(this.totalPages - 1, this.currentPage + 1);
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
      next: (items) => {
        this.preApproved = items;
        const selectedStillExists = items.some(
          (item) => item.id === this.form.controls.preApprovedId.value
        );
        if (!selectedStillExists) {
          this.form.controls.preApprovedId.setValue(items[0]?.id ?? '');
          this.syncCustomer();
        }
      },
      error: (error: HttpErrorResponse) => this.handleError(error)
    });
  }

  private handleError(error: HttpErrorResponse): void {
    this.errorMessage = this.errorText(error);
  }

  private errorText(error: HttpErrorResponse): string {
    const apiError = error.error as Partial<ApiError> | null;
    return apiError?.message ?? 'No fue posible comunicarse con el backend.';
  }
}
