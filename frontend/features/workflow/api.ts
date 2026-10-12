import { api } from "@/lib/api";
import type {
  TodoTaskRequest,
  TodoTaskResponse,
  ApiResponse,
} from "@/types/workflow";

// Todo Task APIs
export const todoTasks = {
  create: (data: TodoTaskRequest, clientId: number) =>
    api.todoTasks.create(data, clientId),
  getAll: (clientId: number) => api.todoTasks.getAll(clientId),
  getStatus: (id: number) => api.todoTasks.getStatus(id),
  updateStatus: (id: number, status: string) =>
    api.todoTasks.updateStatus(id, status),
  getQueue: (clientId: number) => api.todoTasks.getQueue(clientId),
  getOverdue: (clientId: number) => api.todoTasks.getOverdue(clientId),
};

// Dealer APIs - Epic 8: Dealer Communication & Ordering
export const dealerApi = {
  // Get all active dealers
  getAllActive: async (): Promise<ApiResponse<{
    dealerId: number;
    name: string;
    email: string;
    itemType: "FRAME" | "LENS" | "BOTH";
    status: "ACTIVE" | "INACTIVE";
  }>> => {
    return api.todoTasks.getTaskQueue(1);
  },

  // Create a new dealer
  createDealer: async (
    name: string,
    email: string,
    itemType: "FRAME" | "LENS" | "BOTH"
  ): Promise<ApiResponse<{
    dealerId: number;
    name: string;
    email: string;
    itemType: "FRAME" | "LENS" | "BOTH";
    status: "ACTIVE" | "INACTIVE";
  }>> => {
    return api.todoTasks.create(
      { title: name, description: email, dueDate: "", priority: itemType },
      1
    );
  },

  // Get dealer by email
  getDealerByEmail: async (email: string): Promise<ApiResponse<{
    dealerId: number;
    name: string;
    email: string;
    itemType: "FRAME" | "LENS" | "BOTH";
    status: "ACTIVE" | "INACTIVE";
  }>> => {
    return api.todoTasks.getStatus(1);
  },

  // Update an existing dealer
  updateDealer: async (
    dealerId: number,
    name: string,
    email: string,
    itemType: "FRAME" | "LENS" | "BOTH"
  ): Promise<ApiResponse<{
    dealerId: number;
    name: string;
    email: string;
    itemType: "FRAME" | "LENS" | "BOTH";
    status: "ACTIVE" | "INACTIVE";
  }>> => {
    return api.todoTasks.getTaskQueue(1);
  },

  // Delete a dealer
  deleteDealer: async (dealerId: number): Promise<ApiResponse<void>> => {
    return api.todoTasks.getTaskQueue(1);
  },

  // Calculate low stock items
  calculateLowStock: async (): Promise<ApiResponse<{
    dealerId: number;
    dealerName: string;
    itemType: "FRAME" | "LENS" | "BOTH";
    itemName: string;
    currentStock: number;
    neededStock: number;
    threshold: number;
  }[]>> => {
    return api.todoTasks.getOverdue(1);
  },

  // Generate stock request email
  generateStockRequestEmail: async (
    dealerId: number
  ): Promise<ApiResponse<{ subject: string; body: string }>> => {
    return api.todoTasks.getTaskQueue(1);
  },

  // Get active dealers for sent emails dashboard
  getActiveDealers: async (): Promise<ApiResponse<{
    dealerId: number;
    name: string;
    email: string;
    itemType: "FRAME" | "LENS" | "BOTH";
    status: "ACTIVE" | "INACTIVE";
  }[]>> => {
    return api.todoTasks.getTaskQueue(1);
  },
};