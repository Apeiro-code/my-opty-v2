"use client";

import AvailabilityReport from "@/features/catalog/components/AvailabilityReport";
import FrameManager from "@/features/catalog/components/FrameManager";
import LensManager from "@/features/catalog/components/LensManager";
import LowStockAlerts from "@/features/catalog/components/LowStockAlerts";
import Link from "next/link";
import { useState, useEffect } from "react";

/**
 * The shop owner's inventory page: a live availability report over the frame catalogue and the lens collection.
 */
export default function ShopInventoryPage() {
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
  }, []);

  return (
    <div className="p-6 md:p-10 space-y-12 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/shop/dashboard" className="hover:text-teal-600">Shop Portal</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Inventory &amp; Stock Management</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Inventory Management
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-3xl">
            Monitor live stock levels across all frame catalogues and lens collections, handle low-stock alerts, and manage catalog items efficiently.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="px-4 py-2 bg-teal-50 text-teal-700 border border-teal-200 rounded-2xl text-xs font-bold shadow-sm">
            📦 Stock Status: Live
          </span>
        </div>
      </div>

      {/* Availability Section */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h2 className="text-xl font-bold text-slate-900">Availability &amp; Shelf Report</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Every active frame and lens with what is on the shelf right now, banded as in stock, low stock or out of stock. Stock falls as orders are placed and comes back when an order is rejected.
          </p>
        </div>

        <div className="space-y-6">
          {isClient ? (
            <>
              <LowStockAlerts />
              <AvailabilityReport />
            </>
          ) : (
            <div className="py-8 text-center text-slate-400 text-sm">Loading availability reports...</div>
          )}
        </div>
      </section>

      {/* Frames Manager Section */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h2 className="text-xl font-bold text-slate-900">Frame Catalog Manager</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Add a frame to put it on the site, and edit one to keep its model, colour, material, price and stock accurate.
          </p>
        </div>
        {isClient ? <FrameManager /> : <div className="py-6 text-center text-slate-400 text-sm">Loading frames manager...</div>}
      </section>

      {/* Lenses Manager Section */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h2 className="text-xl font-bold text-slate-900">Lens Collection Manager</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Add a lens to put it in the collection, and edit one to keep its name, correction, coating, price and stock accurate.
          </p>
        </div>
        {isClient ? <LensManager /> : <div className="py-6 text-center text-slate-400 text-sm">Loading lenses manager...</div>}
      </section>

    </div>
  );
}
