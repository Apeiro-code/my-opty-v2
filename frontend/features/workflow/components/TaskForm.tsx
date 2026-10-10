import { useState } from "react";
import { TodoTaskRequest } from "@/types/workflow";

interface TaskFormProps {
  onSubmit: (data: TodoTaskRequest) => void;
  isLoading?: boolean;
}

export const TaskForm: React.FC<TaskFormProps> = ({ onSubmit, isLoading = false }) => {
  const [formData, setFormData] = useState<TodoTaskRequest>({
    title: "",
    description: "",
    dueDate: "",
    priority: "MEDIUM",
  });

  const [error, setError] = useState<string | null>(null);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    try {
      onSubmit(formData);
    } catch (err: any) {
      setError(err.message || "Failed to create task");
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="max-w-md space-y-4 p-4 bg-white rounded shadow-sm"
    >
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Task Title
        </label>
        <input
          type="text"
          name="title"
          value={formData.title}
          onChange={handleChange}
          required
          className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
          placeholder="Enter task title"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Description
        </label>
        <textarea
          name="description"
          value={formData.description}
          onChange={handleChange}
          rows={3}
          className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
          placeholder="Enter task description (optional)"
        ></textarea>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Due Date
        </label>
        <input
          type="date"
          name="dueDate"
          value={formData.dueDate}
          onChange={handleChange}
          className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
        />
      </div>

      <div className="flex space-x-2">
        <select
          name="priority"
          value={formData.priority}
          onChange={handleChange}
          className="flex-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
        >
          <option value="HIGH">High</option>
          <option value="MEDIUM" selected>
            Medium
          </option>
          <option value="LOW">Low</option>
        </select>

        <button
          type="submit"
          disabled={isLoading}
          className="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700 disabled:opacity-50 transition-colors"
        >
          {isLoading ? "Creating..." : "Create Task"}
        </button>
      </div>

      {error && (
        <div className="mt-2 text-sm text-red-600">{error}</div>
      )}
    </form>
  );
};