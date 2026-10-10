import { useEffect, useState } from "react";
import { TodoTaskResponse } from "@/types/workflow";

interface TaskQueueProps {
  clientId: number;
  onStatusChange?: (taskId: number, newStatus: "PENDING" | "IN_PROGRESS" | "DONE") => void;
}

export const TaskQueue: React.FC<TaskQueueProps> = ({
  clientId,
  onStatusChange,
}) => {
  const [tasks, setTasks] = useState<TodoTaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch("/api/tasks/queue?clientId=" + clientId, {
      method: "GET",
      credentials: "include",
    })
      .then((res) => res.json())
      .then((data: any) => {
        if (data.success) {
          setTasks(data.data || []);
        } else {
          setError(data.error?.message || "Failed to load queue");
        }
      })
      .finally(() => setLoading(false));
  }, [clientId]);

  if (loading) {
    return <p className="text-center py-8">Loading queue...</p>;
  }

  if (error) {
    return <p className="text-center text-red-600 py-8">Error: {error}</p>;
  }

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-medium">
        Order Processing Queue ({tasks.length} pending)
      </h3>

      {tasks.length === 0 && (
        <p className="text-sm text-gray-500">No pending tasks in queue</p>
      )}

      <div className="space-y-3">
        {tasks.map((task) => (
          <div
            key={task.taskId}
            className="flex items-center justify-between p-3 rounded bg-white shadow-sm border-l-4 border-blue-500"
          >
            <div className="flex items-center space-x-3">
              <span className="font-medium">{task.title}</span>
              <span className="text-xs capitalize font-semibold px-2 py-1 rounded bg-yellow-100 text-yellow-800">
                {task.priority}
              </span>
            </div>
            <div className="text-sm text-gray-500">Due: {task.dueDate || "N/A"}</div>

            {onStatusChange && (
              <button
                onClick={() => onStatusChange(task.taskId, "IN_PROGRESS")}
                className="text-sm text-blue-600 hover:text-blue-800"
              >
                Start
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};