"use client";

import NotificationList from "@/features/order/components/NotificationList";
import Link from "next/link";
import { useState, useEffect } from "react";

/**
 * The customer's notifications: every message the shop has sent about their orders.
 */
export default function NotificationsPage() {
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
  }, []);

  return (
    <div className="p-6 md:p-10 space-y-8 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/" className="hover:text-teal-600">Home</Link>
        <span>/</span>
        <Link href="/orders" className="hover:text-teal-600">Orders</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Notifications</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Notifications & Updates
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-2xl">
            Real-time updates about your custom frames, lens grinding, shipping status, and shop communications.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="px-4 py-2 bg-white border border-slate-200 rounded-2xl text-xs font-bold text-slate-700 shadow-sm">
            🔔 Live Alerts Enabled
          </span>
        </div>
      </div>

      {/* Main Content Card Container */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="flex items-center justify-between border-b border-slate-100 pb-4">
          <h3 className="text-lg font-bold text-slate-900">Recent Messages</h3>
          <button className="text-xs font-bold text-teal-600 hover:underline">
            Mark all as read
          </button>
        </div>

        {/* Notification List Component wrapped safely */}
        <div className="space-y-4">
          {isClient ? (
            <NotificationList emptyText="No notifications yet. We will notify you as soon as your order updates." />
          ) : (
            <div className="py-12 text-center text-slate-400 text-sm">Loading notifications...</div>
          )}
        </div>
      </div>
    </div>
  );
}
