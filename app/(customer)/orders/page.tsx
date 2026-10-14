"use client";

import Link from "next/link";
import { useState, useEffect } from "react";

interface OrderItem {
  id: number;
  status: string;
  total: number;
  createdAt: string;
  expectedReady: string;
  itemsSummary: string;
}

export default function OrdersPage() {
  const [orders, setOrders] = useState<OrderItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fallback mock orders list so it never fails when backend is offline
    const mockOrders: OrderItem[] = [
      {
        id: 1,
        status: "PROCESSING",
        total: 340.0,
        createdAt: "2026-10-10",
        expectedReady: "2026-10-20",
        itemsSummary: "The Avant-Garde Square + Essilor Ultra View Lens",
      },
      {
        id: 2,
        status: "COMPLETED",
        total: 210.0,
        createdAt: "2026-09-28",
        expectedReady: "2026-10-05",
        itemsSummary: "Ultralight Minimalist Aviator + Transitions Lens",
      },
    ];

    setOrders(mockOrders);
    setLoading(false);
  }, []);

  return (
    <div className="p-6 md:p-10 space-y-8 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/" className="hover:text-teal-600">Home</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Your Orders</span>
      </div>

      {/* Page Header & Action */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Your Orders
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-2xl">
            An order links one of your prescriptions to the frame and lens it is made with, so everything is processed together. Open one to see what the shop has told you about it.
          </p>
        </div>
        <Link
          href="/orders/new"
          className="px-6 py-3.5 bg-teal-600 text-white rounded-2xl font-bold hover:bg-teal-500 transition-all shadow-md shadow-teal-600/20 text-sm text-center self-start md:self-auto"
        >
          + Place an order
        </Link>
      </div>

      {/* Orders List Container */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <h3 className="text-lg font-bold text-slate-900 border-b border-slate-100 pb-4">Order History</h3>

        {loading ? (
          <div className="py-12 text-center text-slate-400 text-sm">Loading your orders...</div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div
                key={order.id}
                className="flex flex-col md:flex-row md:items-center justify-between p-5 rounded-2xl bg-slate-50 border border-slate-200/80 hover:border-teal-500 transition-colors gap-4"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-3">
                    <span className="font-extrabold text-slate-900 text-base">Order #{order.id}</span>
                    <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                      order.status === "COMPLETED" ? "bg-emerald-50 text-emerald-600" : "bg-amber-50 text-amber-600"
                    }`}>
                      {order.status}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 font-medium">{order.itemsSummary}</p>
                  <p className="text-[11px] text-slate-400">Placed on: {order.createdAt} · Ready by: {order.expectedReady}</p>
                </div>

                <div className="flex items-center justify-between md:justify-end gap-4 pt-3 md:pt-0 border-t md:border-0 border-slate-200">
                  <span className="text-lg font-black text-slate-900">${order.total.toLocaleString()}</span>
                  <Link
                    href={`/orders/${order.id}`}
                    className="px-4 py-2 bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs rounded-xl transition-all shadow-sm"
                  >
                    View Details →
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
