import { ApiResponse } from "../types/workflow";

const baseUrl = "http://localhost:8080";

const fetchWrapper = async (endpoint: string, options: RequestInit = {}) => {
  const url = `${baseUrl}/api${endpoint}`;
  const token = typeof window !== "undefined" ? localStorage.getItem("token") : "";

  const headers = {
    "Content-Type": "application/json",
    ...(token && { Authorization: `Bearer ${token}` }),
    ...options.headers,
  };

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: "include", // for auth cookie
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(
      (data as ApiResponse<any>).error?.message || "Request failed"
    );
  }

  return data as ApiResponse;
};

export const api = {
  // Todo Task endpoints
  todoTasks: {
    create: (data: any) => fetchWrapper("/tasks", {
      method: "POST",
      body: JSON.stringify(data),
    }),
    getAll: (clientId: number) => fetchWrapper(`/tasks?clientId=${clientId}`),
    getStatus: (id: number) => fetchTasks(`/tasks/${id}/status`),
    updateStatus: (id: number, status: string) =>
      fetchWrapper(`/tasks/${id}/status`, {
        method: "PUT",
        body: JSON.stringify({ status }),
      }),
    getQueue: (clientId: number) =>
      fetchWrapper(`/tasks/queue?clientId=${clientId}`),
    getOverdue: (clientId: number) =>
      fetchWrapper(`/tasks/overdue?clientId=${clientId}`),
  },
};