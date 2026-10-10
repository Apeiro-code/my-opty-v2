import { useState, useEffect } from "react";
import { TodoTaskResponse } from "@/types/workflow";

type TaskStatus = "PENDING" | "IN_PROGRESS" | "DONE";
type Priority = "HIGH" | "MEDIUM" | "LOW";

interface TaskBoardProps {
  tasks: TodoTaskResponse[];
  onStatusChange: (taskId: number, newStatus: TaskStatus) => void;
}

export const TaskBoard: React.FC<TaskBoardProps> = ({ tasks, onStatusChange }) => {
  // Group tasks by status
  const pendingTasks = tasks.filter((t) => t.status === "PENDING");
  const inProgressTasks = tasks.filter((t) => t.status === "IN_PROGRESS");
  const doneTasks = tasks.filter((t) => t.status === "DONE");

  return (
    <div className="space-y-6">
      {/* PENDING Section */}
      <div className="border-l-4 border-green-500 bg-green-50 rounded p-4">
        <h3 className="text-lg font-medium text-green-800 mb-2">Pending Tasks</h3>
        <p className="text-sm text-gray-500">
          {pendingTasks.length} task(s) pending
        </p>
        {pendingTasks.length === 0 && (
          <p className="text-sm text-gray-400 italic">No pending tasks</p>
        )}
        <div className="space-y-2 pt-2">
          {pendingTasks.map((task) => (
            <div
              key={task.taskId}
              className="flex items-center justify-between p-2 rounded bg-white shadow-sm"
            >
              <div className="flex items-center space-x-2">
                <span className="font-medium">{task.title}</span>
                <span className="text-xs capitalize font-semibold px-2 py-1 rounded bg-yellow-100 text-yellow-800">
                  {task.priority}
                </span>
              </div>
              <div className="text-sm text-gray-500">
                Due: {task.dueDate || "No due date"}
              </div>
              <button
                onClick={() => onStatusChange(task.taskId, "IN_PROGRESS")}
                className="text-sm text-blue-600 hover:text-blue-800"
              >
                In Progress
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* IN_PROGRESS Section */}
      <div className="border-l-4 border-yellow-500 bg-yellow-50 rounded p-4">
        <h3 className="text-lg font-medium text-yellow-800 mb-2">In Progress</h3>
        <p className="text-sm text-gray-500">
          {inProgressTasks.length} task(s) in progress
        </p>
        {inProgressTasks.length === 0 && (
          <p className="text-sm text-gray-400 italic">No in progress tasks</p>
        )}
        <div className="space-y-2 pt-2">
          {inProgressTasks.map((task) => (
            <div key={task.taskId} className="flex items-center justify-between p-2 rounded bg-white shadow-sm">
              <div className="flex items-center space-x-2">
                <span className="font-medium">{task.title}</span>
                <span className="text-xs capitalize font-semibold px-2 py-1 rounded bg-orange-100 text-orange-800">
                  {task.priority}
                </span>
              </div>
              <div className="text-sm text-gray-500">Due: {task.dueDate || "No due date"}</div>
              <button
                onClick={() => onStatusChange(task.taskId, "DONE")}
                className="text-sm text-green-600 hover:text-green-800"
              >
                Done
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* DONE Section */}
      <div className="border-l-4 border-blue-500 bg-blue-50 rounded p-4">
        <h3 className="text-lg font-medium text-blue-800 mb-2">Completed</h3>
        <p className="text-sm text-gray-500">
          {doneTasks.length} task(s) completed
        </p>
        {doneTasks.length === 0 && (
          <p className="text-sm text-gray-400 italic">No completed tasks</p>
        )}
        <div className="space-y-2 pt-2">
          {doneTasks.map((task) => (
            <div key={task.taskId} className="flex items-center justify-between p-2 rounded bg-white shadow-sm opacity-50">
              <span className="font-line-through text-gray-400">{task.title}</span>
              <span className="text-xs capitalize font-semibold px-2 py-1 rounded bg-gray-200 text-gray-600">
                {task.priority}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};