export interface TodoTaskRequest {
  title: string;
  description: string;
  dueDate: string; // ISO date string
  priority: "HIGH" | "MEDIUM" | "LOW";
}

export interface TodoTaskResponse {
  taskId: number;
  clientId: number;
  title: string;
  description: string | null;
  status: "PENDING" | "IN_PROGRESS" | "DONE";
  dueDate: string; // ISO date string
  priority: "HIGH" | "MEDIUM" | "LOW";
  createdAt: string; // ISO datetime string
  reminderSent: boolean;
}

// API response envelope
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  meta?: {
    page: number;
    size: number;
    total: number;
  };
  error?: {
    code: string;
    message: string;
  };
}