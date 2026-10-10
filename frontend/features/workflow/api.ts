import { api } from "@/lib/api";
import type {
  TodoTaskRequest,
  TodoTaskResponse,
  ApiResponse,
} from "@/types/workflow";

// Create a new task
export const createTask = async (
  clientId: number,
  data: TodoTaskRequest
): Promise<ApiResponse<TodoTaskResponse>> => {
  return api.todoTasks.create({ title: data.title, description: data.description, dueDate: data.dueDate, priority: data.priority }, clientId);
};

// Get all tasks for a client
export const getTasks = async (clientId: number): Promise<ApiResponse<TodoTaskResponse[]>> => {
  return api.todoTasks.getAll(clientId);
};

// Update task status
export const updateTaskStatus = async (
  id: number,
  status: "PENDING" | "IN_PROGRESS" | "DONE"
): Promise<ApiResponse<TodoTaskResponse>> => {
  return api.todoTasks.updateStatus(id, status);
};

// Get the order-processing queue (pending tasks FIFO)
export const getTaskQueue = async (clientId: number): Promise<ApiResponse<TodoTaskResponse[]>> => {
  return api.todoTasks.getQueue(clientId);
};

// Get overdue tasks
export const getOverdueTasks = async (clientId: number): Promise<ApiResponse<TodoTaskResponse[]>> => {
  return api.todoTasks.getOverdue(clientId);
};