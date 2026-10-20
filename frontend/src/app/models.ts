export interface UsageCommand {
  requestReference: string;
  preApprovedId: string;
  customerId: string;
  amount: number;
}

export interface UsageResponse extends UsageCommand {
  status: 'AUTHORIZED' | 'REJECTED';
  rejectionReason: string | null;
  processedAt: string;
  remainingAmount: number | null;
  replayed: boolean;
}

export interface PreApproved {
  id: string;
  customerId: string;
  status: 'ACTIVE' | 'BLOCKED';
  availableAmount: number;
}

export interface ApiError {
  code: string;
  message: string;
  timestamp: string;
}

