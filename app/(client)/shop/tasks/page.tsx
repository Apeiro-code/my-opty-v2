"use client";

import Link from "next/link";
import { useState, useEffect } from "react";

interface ShopTask {
  id: number;
  title: string;
  assignedTo: string;
  dueDate: string;
  status: "PENDING" | "IN_PROGRESS" | "COMPLETED";
}

export default function ShopTasksPage() {
  const [tasks, setTasks] = useState<ShopTask[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Mock fallback tasks when backend is offline
    const mockTasks: ShopTask[] = [
      { id: 1, title: "Calibrate lens edger machine for Order #1", assignedTo: "Workshop Staff", dueDate: "2026-10-15", status: "PENDING" },
      { id: 2, title: "Verify physical prescription scan for Customer #2", assignedTo: "Optometrist", dueDate: "2026-10-14", status: "IN_PROGRESS" },
    ];
    setTasks(mockTasks);
    setLoading(false);
  }, []);

  const handleCompleteTask = (id: number) => {
    setTasks((prev) => prev.filter((t) => t.id !== id));
    alert(`Task #${id} marked as completed (Mock Mode)!`);
  };

  return (
    <div className="p-6 md:p-10 space-y-12 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/shop/dashboard" className="hover:text-teal-600">Shop Portal</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Staff Tasks</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Shop Operations &amp; Tasks
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-3xl">
            Track daily optical laboratory tasks, machine maintenance schedules, and staff assignments.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="px-4 py-2 bg-amber-50 text-amber-700 border border-amber-200 rounded-2xl text-xs font-bold shadow-sm">
            📝 Task Manager Active
          </span>
        </div>
      </div>

      {/* Content Container */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h2 className="text-xl font-bold text-slate-900">Pending Operational Tasks</h2>
          <p className="text-xs text-slate-500 mt-0.5">Complete and update task statuses across shop departments.</p>
        </div>

        {loading ? (
          <div className="py-8 text-center text-slate-400 text-sm">Loading tasks...</div>
        ) : tasks.length === 0 ? (
          <div className="py-8 text-center text-slate-400 text-sm bg-slate-50 rounded-2xl">All tasks completed for today!</div>
        ) : (
          <div className="space-y-4">
            {tasks.map((task) => (
              <div key={task.id} className="flex flex-col md:flex-row md:items-center justify-between p-5 rounded-2xl bg-slate-50 border border-slate-200/80 gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-3">
                    <span className="font-extrabold text-slate-900 text-base">Task #{task.id}</span>
                    <span className="text-xs font-bold text-amber-700 bg-amber-50 px-3 py-1 rounded-full">
                      {task.status}
                    </span>
                  </div>
                  <p className="text-sm font-semibold text-slate-800">{task.title}</p>
                  <p className="text-xs text-slate-500">Assigned To: {task.assignedTo} · Due: {task.dueDate}</p>
                </div>
                <button
                  onClick={() => handleCompleteTask(task.id)}
                  className="px-4 py-2 bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs rounded-xl transition-all shadow-sm"
                >
                  Mark Complete ✓
                </button>
              </div>
            ))}
          </div>
        )}
      </section>

    </div>
  );
}
