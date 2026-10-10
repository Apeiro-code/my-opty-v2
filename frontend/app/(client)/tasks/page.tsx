import { useState, useEffect } from "react";
import { TaskBoard } from "@/features/workflow/components/TaskBoard";
import { TaskForm } from "@/features/workflow/components/TaskForm";
import { TaskQueue } from "@/features/workflow/components/TaskQueue";
import { getTasks, getTaskQueue, getOverdueTasks } from "@/features/workflow/api";
import { ApiResponse } from "@/types/workflow";

export default function ClientWorkflowPage() {
  const [tasks, setTasks] = useState<TodoTaskResponse[]>([]);
  const [queueTasks, setQueueTasks] = useState<TodoTaskResponse[]>([]);
  const [overdueTasks, setOverdueTasks] = useState<TodoTaskResponse[]>([]);
  const [clientId, setClientId] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch client ID from localStorage or session - in production this would come from auth
  useEffect(() => {
    const storedClientId = localStorage.getItem("clientId");
    if (storedClientId) {
      setClientId(Number(storedClientId));
    }
  }, []);

  // Fetch initial data
  useEffect(() => {
    if (clientId > 0) {
      const loadData = async () => {
        setLoading(true);
        try {
          // Fetch all tasks
          const tasksResult: ApiResponse<TodoTaskResponse[]> = await getTasks(clientId);
          if (tasksResult.success && tasksResult.data) {
            setTasks(tasksResult.data);
          } else {
            setError(tasksResult.error?.message);
          }

          // Fetch queue tasks
          const queueResult: ApiResponse<TodoTaskResponse[]> = await getTaskQueue(clientId);
          if (queueResult.success && queueResult.data) {
            setQueueTasks(queueResult.data);
          }

          // Fetch overdue tasks
          const overdueResult: ApiResponse<TodoTaskResponse[]> = await getOverdueTasks(clientId);
          if (overdueResult.success && overdueResult.data) {
            setOverdueTasks(overdueResult.data);
          }
        } catch (err: any) {
          setError(err.message || "Failed to load data");
        } finally {
          setLoading(false);
        }
      };

      loadData();
    }
  }, [clientId]);

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

      <TaskForm onSubmit={(data) => handleCreateTask(data)} />

      <TaskBoard tasks={tasks} onStatusChange={(id, status) => handleStatusChange(id, status)} />

      <TaskQueue
        clientId={clientId}
        tasks={queueTasks}
        onStatusChange={(id, status) => handleStatusChange(id, status)}
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

// Type assertion for the task response
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

function handleCreateTask(data: any) {
  // Form submission handler - would call API
  console.log("Creating task:", data);
}

function handleStatusChange(taskId: number, newStatus: string) {
  // Status change handler - would call API
  console.log(`Task ${taskId} status changed to ${newStatus}`);
}