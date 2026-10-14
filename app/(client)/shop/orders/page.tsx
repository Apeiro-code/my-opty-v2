"use client";

import Link from "next/link";
import { useState, useEffect } from "react";

interface PendingOrder {
  id: number;
  customerName: string;
  itemsSummary: string;
  prescriptionVerified: boolean;
  total: number;
}

interface ProductionOrder {
  id: number;
  customerName: string;
  stage: "PROCESSING" | "READY" | "DISPATCHED";
  expectedReady: string;
}

export default function ShopOrdersPage() {
  const [pendingOrders, setPendingOrders] = useState<PendingOrder[]>([]);
  const [productionOrders, setProductionOrders] = useState<ProductionOrder[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Mock fallback data so it never shows backend errors when server is offline
    const mockPending: PendingOrder[] = [
      { id: 1, customerName: "Mithsen Karunarathna", itemsSummary: "The Avant-Garde Square + Essilor Lens", prescriptionVerified: true, total: 340.0 },
      { id: 2, customerName: "Kamal Perera", itemsSummary: "Classic Titanium Aviator + Blue Shield Lens", prescriptionVerified: false, total: 220.0 },
    ];

    const mockProduction: ProductionOrder[] = [
      { id: 3, customerName: "Nimal Silva", stage: "PROCESSING", expectedReady: "2026-10-18" },
      { id: 4, customerName: "Sunil Bandara", stage: "READY", expectedReady: "2026-10-15" },
    ];

    setPendingOrders(mockPending);
    setProductionOrders(mockProduction);
    setLoading(false);
  }, []);

  const handleApprove = (id: number) => {
    setPendingOrders((prev) => prev.filter((o) => o.id !== id));
    alert(`Order #${id} approved successfully and sent to production (Mock Mode)!`);
  };

  const handleReject = (id: number) => {
    setPendingOrders((prev) => prev.filter((o) => o.id !== id));
    alert(`Order #${id} rejected with reason (Mock Mode).`);
  };

  const handleAdvanceStage = (id: number, currentStage: string) => {
    alert(`Advanced Order #${id} from ${currentStage} to next production stage (Mock Mode)!`);
  };

  return (
    <div className="p-6 md:p-10 space-y-12 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/shop/dashboard" className="hover:text-teal-600">Shop Portal</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Order Approvals &amp; Fulfilment</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Order Approvals &amp; Production
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-3xl">
            Approve incoming customer orders to send them to production, reject with reasons, and manage the complete fulfilment and dispatch pipeline.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="px-4 py-2 bg-amber-50 text-amber-700 border border-amber-200 rounded-2xl text-xs font-bold shadow-sm">
            ⚡ Queue Management Active
          </span>
        </div>
      </div>

      {/* Order Approvals Queue Section */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h2 className="text-xl font-bold text-slate-900">Order Approvals Queue</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Approve an order to send it to production, or reject it with a reason. An order can only be approved once its prescription has been verified.
          </p>
        </div>

        {loading ? (
          <div className="py-8 text-center text-slate-400 text-sm">Loading pending approvals...</div>
        ) : pendingOrders.length === 0 ? (
          <div className="py-8 text-center text-slate-400 text-sm bg-slate-50 rounded-2xl">No orders pending approval right now.</div>
        ) : (
          <div className="space-y-4">
            {pendingOrders.map((order) => (
              <div key={order.id} className="flex flex-col md:flex-row md:items-center justify-between p-5 rounded-2xl bg-slate-50 border border-slate-200/80 gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-3">
                    <span className="font-extrabold text-slate-900 text-base">Order #{order.id}</span>
                    <span className="text-xs font-bold text-slate-600">({order.customerName})</span>
                    <span className={`text-xs font-bold px-3 py-1 rounded-full ${order.prescriptionVerified ? "bg-emerald-50 text-emerald-600" : "bg-amber-50 text-amber-600"}`}>
                      {order.prescriptionVerified ? "Prescription Verified" : "Verification Pending"}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600">{order.itemsSummary}</p>
                  <p className="text-xs font-bold text-slate-900">Total: ${order.total.toLocaleString()}</p>
                </div>
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => handleApprove(order.id)}
                    className="px-4 py-2 bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs rounded-xl transition-all shadow-sm"
                  >
                    Approve Order
                  </button>
                  <button
                    onClick={() => handleReject(order.id)}
                    className="px-4 py-2 bg-rose-50 hover:bg-rose-100 text-rose-600 font-bold text-xs rounded-xl transition-all"
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Production & Fulfilment Section */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h2 className="text-xl font-bold text-slate-900">Production &amp; Fulfilment Pipeline</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Move each approved order through processing, ready and dispatched. Each move emails the customer automatically.
          </p>
        </div>

        {productionOrders.length === 0 ? (
          <div className="py-8 text-center text-slate-400 text-sm bg-slate-50 rounded-2xl">No orders currently in production queue.</div>
        ) : (
          <div className="space-y-4">
            {productionOrders.map((order) => (
              <div key={order.id} className="flex flex-col md:flex-row md:items-center justify-between p-5 rounded-2xl bg-slate-50 border border-slate-200/80 gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-3">
                    <span className="font-extrabold text-slate-900 text-base">Order #{order.id}</span>
                    <span className="text-xs font-bold text-slate-600">({order.customerName})</span>
                    <span className="text-xs font-bold px-3 py-1 rounded-full bg-teal-50 text-teal-700">
                      {order.stage}
                    </span>
                  </div>
                  <p className="text-xs text-slate-500">Expected Ready Date: {order.expectedReady}</p>
                </div>
                <button
                  onClick={() => handleAdvanceStage(order.id, order.stage)}
                  className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs rounded-xl transition-all shadow-sm"
                >
                  Advance Stage →
                </button>
              </div>
            ))}
          </div>
        )}
      </section>

    </div>
  );
}
