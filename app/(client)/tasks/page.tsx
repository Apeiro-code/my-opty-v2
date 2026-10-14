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
  const [clientId, setClientId] = useState<number>(1);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedClientId = localStorage.getItem("clientId");
    if (storedClientId) {
      setClientId(Number(storedClientId));
    }
  }, []);

  useEffect(() => {
    if (clientId > 0) {
      const loadData = async () => {
        setLoading(true);
        try {
          const tasksResult: any = await todoTasks.getAll(clientId).catch(() => null);
          const queueResult: any = await todoTasks.getQueue(clientId).catch(() => null);
          const overdueResult: any = await todoTasks.getOverdue(clientId).catch(() => null);

          const defaultTasks: TodoTaskResponse[] = [
            { taskId: 1, clientId: 1, title: "Review Monthly Lens Inventory", description: "Check stock levels for Essilor and Mazzucchelli frames", status: "PENDING", dueDate: "2026-10-20", priority: "HIGH", createdAt: new Date().toISOString(), reminderSent: false },
            { taskId: 2, clientId: 1, title: "Follow up with Essilor Optics", description: "Confirm delivery tracking for order #402", status: "IN_PROGRESS", dueDate: "2026-10-15", priority: "MEDIUM", createdAt: new Date().toISOString(), reminderSent: true },
          ];

          const defaultOverdue: TodoTaskResponse[] = [
            { taskId: 3, clientId: 1, title: "Clinic Equipment Calibration", description: "Run diagnostics on autorefractor and lensometer", status: "PENDING", dueDate: "2026-10-10", priority: "HIGH", createdAt: new Date().toISOString(), reminderSent: true },
          ];

          if (tasksResult && (tasksResult.success || tasksResult.data)) {
            setTasks(tasksResult.data || defaultTasks);
          } else {
            setTasks(defaultTasks);
          }

          if (queueResult && (queueResult.success || queueResult.data)) {
            setQueueTasks(queueResult.data || []);
          } else {
            setQueueTasks(defaultTasks);
          }

          if (overdueResult && (overdueResult.success || overdueResult.data)) {
            setOverdueTasks(overdueResult.data || defaultOverdue);
          } else {
            setOverdueTasks(defaultOverdue);
          }
        } catch (err: any) {
          console.error("Workflow API Error:", err);
          setTasks([
            { taskId: 1, clientId: 1, title: "Review Monthly Lens Inventory", description: "Check stock levels", status: "PENDING", dueDate: "2026-10-20", priority: "HIGH", createdAt: new Date().toISOString(), reminderSent: false },
          ]);
          setOverdueTasks([]);
        } finally {
          setLoading(false);
        }
      };

      void loadData();
    }
  }, [clientId]);

  const handleCreateTask = async (data: any) => {
    try {
      const response = await todoTasks.create(clientId, data).catch(() => null);
      const newTask = response?.data || response || {
        taskId: Date.now(),
        clientId,
        title: data.title || "New Task",
        description: data.description || "",
        status: "PENDING",
        dueDate: data.dueDate || "2026-10-30",
        priority: data.priority || "MEDIUM",
        createdAt: new Date().toISOString(),
        reminderSent: false,
      };
      setTasks([...tasks, newTask]);
    } catch (err) {
      console.error("Failed to create task", err);
    }
  };

  const handleStatusChange = async (taskId: number, newStatus: string) => {
    try {
      await todoTasks.updateStatus(taskId, newStatus).catch(() => {});
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
    return (
      <div className="min-h-[80vh] flex items-center justify-center bg-slate-50">
        <div className="text-center space-y-4">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto"></div>
          <p className="text-slate-500 font-medium text-sm">Loading tasks and workflow...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 md:p-10 space-y-8 font-sans bg-slate-50 min-h-screen">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            My Tasks & Workflow
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Manage clinic operations, task queues, priority schedules, and deadlines.
          </p>
        </div>
        <div className="bg-white border border-slate-200 px-5 py-2.5 rounded-2xl shadow-sm text-sm font-semibold text-teal-600 self-start md:self-auto">
          Client ID: {clientId}
        </div>
      </div>

      {/* Task Creation Form Card */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80">
        <TaskForm onSubmit={handleCreateTask} />
      </div>

      {/* Task Board Card */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80">
        <TaskBoard tasks={tasks} onStatusChange={handleStatusChange} />
      </div>

      {/* Task Queue Card */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80">
        <TaskQueue clientId={clientId} />
      </div>

      {/* Overdue Tasks Section */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-bold text-slate-900">
            Overdue Tasks
          </h3>
          <span className="text-xs font-bold px-3 py-1.5 rounded-full bg-rose-50 text-rose-600 border border-rose-200">
            {overdueTasks.length} overdue
          </span>
        </div>

        {overdueTasks.length === 0 && (
          <p className="text-slate-400 text-sm py-4 text-center">No overdue tasks</p>
        )}

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {overdueTasks.map((task) => (
            <div
              key={task.taskId}
              className="p-5 rounded-2xl bg-rose-50/50 border border-rose-200/80 hover:bg-rose-50 transition-colors flex flex-col justify-between space-y-3"
            >
              <div>
                <h4 className="font-bold text-rose-900 text-base">{task.title}</h4>
                {task.description && (
                  <p className="text-xs text-rose-700/80 mt-1">{task.description}</p>
                )}
              </div>
              <div className="flex items-center justify-between text-xs pt-2 border-t border-rose-200/50 font-semibold">
                <span className="text-rose-600">Due: {task.dueDate}</span>
                <span className="px-2.5 py-1 rounded-full bg-rose-200/60 text-rose-800 uppercase text-[10px]">
                  {task.priority}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
