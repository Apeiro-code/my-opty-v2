"use client";

import Link from "next/link";
import { useState, useEffect } from "react";

interface BillingRecord {
  id: number;
  orderId: number;
  customerName: string;
  amount: number;
  status: "PAID" | "PENDING" | "REFUNDED";
  date: string;
}

export default function ShopBillingPage() {
  const [bills, setBills] = useState<BillingRecord[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Mock fallback billing records when backend is offline
    const mockBills: BillingRecord[] = [
      { id: 101, orderId: 1, customerName: "Mithsen Karunarathna", amount: 340.0, status: "PAID", date: "2026-10-10" },
      { id: 102, orderId: 2, customerName: "Kamal Perera", amount: 210.0, status: "PAID", date: "2026-09-28" },
    ];
    setBills(mockBills);
    setLoading(false);
  }, []);

  return (
    <div className="p-6 md:p-10 space-y-12 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/shop/dashboard" className="hover:text-teal-600">Shop Portal</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Billing &amp; Payments</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Billing &amp; Invoices
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-3xl">
            Monitor customer transactions, verified payment receipts, and generated store invoices.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="px-4 py-2 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-2xl text-xs font-bold shadow-sm">
            💳 Financial Portal Active
          </span>
        </div>
      </div>

      {/* Content Container */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h2 className="text-xl font-bold text-slate-900">Transaction History</h2>
          <p className="text-xs text-slate-500 mt-0.5">All completed and verified payments from eyewear orders.</p>
        </div>

        {loading ? (
          <div className="py-8 text-center text-slate-400 text-sm">Loading billing records...</div>
        ) : (
          <div className="space-y-4">
            {bills.map((bill) => (
              <div key={bill.id} className="flex flex-col md:flex-row md:items-center justify-between p-5 rounded-2xl bg-slate-50 border border-slate-200/80 gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-3">
                    <span className="font-extrabold text-slate-900 text-base">Invoice #{bill.id}</span>
                    <span className="text-xs font-bold text-slate-600">({bill.customerName})</span>
                    <span className="text-xs font-bold px-3 py-1 rounded-full bg-emerald-50 text-emerald-600">
                      {bill.status}
                    </span>
                  </div>
                  <p className="text-xs text-slate-500">Linked Order ID: #{bill.orderId} · Date: {bill.date}</p>
                </div>
                <span className="text-lg font-black text-slate-900">${bill.amount.toLocaleString()}</span>
              </div>
            ))}
          </div>
        )}
      </section>

    </div>
  );
}
