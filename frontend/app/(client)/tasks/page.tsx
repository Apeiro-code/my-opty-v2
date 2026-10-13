"use client";

import React, { useState, useEffect } from "react";
import { TaskBoard } from "@/features/workflow/components/TaskBoard";
import { TaskForm } from "@/features/workflow/components/TaskForm";
import { TaskQueue } from "@/features/workflow/components/TaskQueue";
import { todoTasks } from "@/features/workflow/api";

interface TodoTaskResponse {
  taskId: number;
  clientId: number;
  title: string;
  description: string | null;
  status: "PENDING" | "IN_PROGRESS" | "DONE";
  dueDate: string;
  priority: "HIGH" | "MEDIUM" | "LOW";
  createdAt: string;
  reminderSent: boolean;
}

export default function ClientWorkflowPage() {
  const [tasks, setTasks] = useState<TodoTaskResponse[]>([]);
  const [queueTasks, setQueueTasks] = useState<TodoTaskResponse[]>([]);
  const [overdueTasks, setOverdueTasks] = useState<TodoTaskResponse[]>([]);
  const [clientId, setClientId] = useState<number>(1); // 0 වෙනුවට 1 දාලා ටෙස්ට් කරන්න පුළුවන්
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Client ID ලබාගැනීම
  useEffect(() => {
    const storedClientId = localStorage.getItem("clientId");
    if (storedClientId) {
      setClientId(Number(storedClientId));
    }
  }, []);

  // ඩේටා ලෝඩ් කිරීම
  useEffect(() => {
    if (clientId > 0) {
      const loadData = async () => {
        setLoading(true);
        try {
          const tasksResult: any = await todoTasks.getAll(clientId);
          if (tasksResult.success && tasksResult.data) {
            setTasks(tasksResult.data);
          } else if (Array.isArray(tasksResult)) {
            setTasks(tasksResult);
          }

          const queueResult: any = await todoTasks.getQueue(clientId);
          if (queueResult.success && queueResult.data) {
            setQueueTasks(queueResult.data);
          } else if (Array.isArray(queueResult)) {
            setQueueTasks(queueResult);
          }

          const overdueResult: any = await todoTasks.getOverdue(clientId);
          if (overdueResult.success && overdueResult.data) {
            setOverdueTasks(overdueResult.data);
          } else if (Array.isArray(overdueResult)) {
            setOverdueTasks(overdueResult);
          }
        } catch (err: any) {
          setError(err.message || "Failed to load data");
        } finally {
          setLoading(false);
        }
      };

      void loadData();
    }
  }, [clientId]);

  // අලුත් ටාස්ක් එකක් සෑදීම
  const handleCreateTask = async (data: any) => {
    try {
      const response = await todoTasks.create(clientId, data);
      const newTask = response.data || response;
      setTasks([...tasks, newTask]);
    } catch (err) {
      console.error("Failed to create task", err);
    }
  };

  // ටාස්ක් එකක ස්ටේටස් එක වෙනස් කිරීම
  const handleStatusChange = async (taskId: number, newStatus: string) => {
    try {
      await todoTasks.updateStatus(taskId, newStatus);
      setTasks(
          tasks.map((task) =>
              task.taskId === taskId ? { ...task, status: newStatus as any } : task
          )
      );
    } catch (err) {
      console.error("Failed to update status", err);
    }
  };

  if (loading) {
    return <div className="min-h-80 flex items-center justify-center">Loading...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-600">Error: {error}</div>;
  }

  return (
      <div className="space-y-6 p-4">
        <header className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold">My Tasks</h1>
          <div className="flex items-center space-x-2">
            <span className="text-sm text-gray-600">Client ID: {clientId}</span>
          </div>
        </header>

        <TaskForm onSubmit={handleCreateTask} />

        <TaskBoard tasks={tasks} onStatusChange={handleStatusChange} />

        <TaskQueue
            clientId={clientId}
        />
        {/* Overdue Tasks Section */}
        <div>
          <h3 className="text-xl font-medium mb-3">
            Overdue Tasks ({overdueTasks.length})
          </h3>

          {overdueTasks.length === 0 && (
              <p className="text-sm text-gray-500">No overdue tasks</p>
          )}

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {overdueTasks.map((task) => (
                <div
                    key={task.taskId}
                    className="p-3 rounded bg-red-50 border border-red-200 hover:bg-red-100 transition-colors"
                >
                  <h4 className="font-medium text-red-800">{task.title}</h4>
                  <p className="text-sm text-red-600">Due: {task.dueDate}</p>
                  <p className="text-xs text-red-500">Priority: {task.priority}</p>
                </div>
            ))}
          </div>
        </div>
      </div>
  );
}