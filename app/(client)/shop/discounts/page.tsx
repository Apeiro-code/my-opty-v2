"use client";

import DiscountManager from "@/features/order/components/DiscountManager";
import Link from "next/link";
import { useState, useEffect } from "react";

/**
 * The shop owner's promotions page: create a discount, edit one, and end one early.
 */
export default function ShopDiscountsPage() {
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
  }, []);

  return (
    <div className="p-6 md:p-10 space-y-8 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/shop/dashboard" className="hover:text-teal-600">Shop Portal</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Discounts &amp; Promotions</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Discount Management
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-3xl">
            Set up a promotion as a percentage off everything or a chosen set of frames and lenses, running between two dates. Edit one to adjust it while it is scheduled or running, or end it early when the offer stops — ending keeps the record, so the history still shows what was scheduled.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="px-4 py-2 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-2xl text-xs font-bold shadow-sm">
            🛡️ Promotions Portal Active
          </span>
        </div>
      </div>

      {/* Main Content Container */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h3 className="text-lg font-bold text-slate-900">Active &amp; Scheduled Offers</h3>
          <p className="text-xs text-slate-500 mt-0.5">Manage store-wide discounts, frame promotions, and lens package deals.</p>
        </div>

        {/* DiscountManager Component wrapped safely */}
        <div className="space-y-4">
          {isClient ? (
            <DiscountManager />
          ) : (
            <div className="py-12 text-center text-slate-400 text-sm">Loading discount manager...</div>
          )}
        </div>
      </div>
    </div>
  );
}
