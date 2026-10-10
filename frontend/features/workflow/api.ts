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

// Dealer APIs
export const dealerApi = {
  getAllActive: () => api.todoTasks.getTaskQueue(0), // Placeholder - would be separate endpoint
  createDealer: (name: string, email: string, itemType: "FRAME" | "LENS" | "BOTH") =>
    api.todoTasks.create(
      { title: name, description: email, dueDate: "", priority: itemType },
      0
    ), // Placeholder
  getDealerByEmail: (email: string) => api.todoTasks.getStatus(0), // Placeholder
  updateDealer: (dealerId: number, name: string, email: string, itemType: string) =>
    api.todoTasks.getTaskQueue(0), // Placeholder
  deleteDealer: (dealerId: number) => api.todoTasks.getTaskQueue(0), // Placeholder
  calculateLowStock: () => api.todoTasks.getOverdue(0), // Placeholder
  generateStockRequestEmail: (dealerId: number) =>
    api.todoTasks.getTaskQueue(0), // Placeholder
};