"use client";

import { useState, useEffect } from "react";
import Link from "next/link";

interface LensItem {
  id: number;
  name: string;
  type: string;
  coating: string;
  price: number;
  inStock: boolean;
  imageUrl?: string;
}

export default function LensesPage() {
  const [lenses, setLenses] = useState<LensItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fallback lenses list so it never fails when backend is offline
    const mockLenses: LensItem[] = [
      { id: 1, name: "Essilor Ultra View Custom Lens", type: "Progressive Digital", coating: "Crizal Sapphire HR", price: 185.0, inStock: true },
      { id: 2, name: "Blue Shield Guard Lens", type: "Single Vision", coating: "Blue-Light Blocker", price: 120.0, inStock: true },
      { id: 3, name: "Transitions Gen 8 Photochromic", type: "Adaptive Tint", coating: "Anti-Glare UV Protection", price: 210.0, inStock: true },
      { id: 4, name: "Standard Crystal Clear Lens", type: "Single Vision", coating: "Scratch Resistant", price: 75.0, inStock: false },
    ];

    setLenses(mockLenses);
    setLoading(false);
  }, []);

  return (
    <div className="p-6 md:p-10 space-y-8 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/" className="hover:text-teal-600">Home</Link>
        <span>/</span>
        <Link href="/lenses" className="hover:text-teal-600">Shop</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Custom Lenses</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Prescription & Custom Lenses
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-2xl">
            Browse our lens options. Filtered by type and coating, with live stock status and custom calibration support.
          </p>
        </div>
      </div>

      {/* Filter Toolbar Section */}
      <div className="bg-white rounded-3xl p-5 shadow-sm border border-slate-200/80 flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3">
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 focus:outline-none">
            <option>Lens Type</option>
          </select>
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 focus:outline-none">
            <option>Coating & Protection</option>
          </select>
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 focus:outline-none">
            <option>Price Range</option>
          </select>
        </div>

        <div className="flex items-center gap-3">
          <span className="text-xs text-slate-400 font-medium">Sort by:</span>
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-900 focus:outline-none">
            <option>Recommended Lenses</option>
            <option>Price: Low to High</option>
            <option>Price: High to Low</option>
          </select>
        </div>
      </div>

      {/* Main Content Layout (Sidebar + Grid) */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">

        {/* Left Sidebar (Categories & Help Banner) */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200/80 space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Lens Categories</h3>
            <ul className="space-y-2 text-sm font-semibold">
              <li className="flex items-center justify-between p-2 rounded-xl bg-teal-50 text-teal-700">
                <span>All Lenses</span>
                <span className="text-xs bg-teal-200/60 px-2 py-0.5 rounded-full">64</span>
              </li>
              <li className="flex items-center justify-between p-2 rounded-xl text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer">
                <span>Single Vision</span>
                <span className="text-xs text-slate-400">28</span>
              </li>
              <li className="flex items-center justify-between p-2 rounded-xl text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer">
                <span>Progressive / Bifocal</span>
                <span className="text-xs text-slate-400">18</span>
              </li>
              <li className="flex items-center justify-between p-2 rounded-xl text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer">
                <span>Blue-Light Blocking</span>
                <span className="text-xs text-slate-400">12</span>
              </li>
            </ul>
          </div>

          {/* Prescription Help Banner */}
          <div className="bg-[#0b0f19] text-white rounded-3xl p-6 shadow-md space-y-4 relative overflow-hidden">
            <div className="w-10 h-10 rounded-2xl bg-teal-500/20 flex items-center justify-center text-teal-400 text-lg">
              📄
            </div>
            <h4 className="font-bold text-base leading-snug">Have a Prescription?</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Upload your optometrist prescription or enter details manually. Our specialists will match the ideal custom lens.
            </p>
            <button className="w-full py-3 bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs rounded-xl transition-all shadow-md shadow-teal-600/30">
              Upload Prescription
            </button>
          </div>
        </div>

        {/* Right Lenses Grid Area */}
        <div className="lg:col-span-3 space-y-8">
          {loading ? (
            <div className="py-20 text-center text-slate-400">Loading custom lenses...</div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-6">
              {lenses.map((lens) => (
                <div key={lens.id} className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200/80 flex flex-col justify-between space-y-4 hover:border-teal-500 transition-colors">
                  <div className="aspect-[4/3] rounded-2xl bg-slate-100 flex items-center justify-center text-slate-400 text-4xl">
                    🔍
                  </div>
                  <div className="space-y-1">
                    <p className="text-xs font-bold uppercase tracking-wider text-teal-600">{lens.type}</p>
                    <h3 className="font-bold text-slate-900 text-lg">{lens.name}</h3>
                    <p className="text-xs text-slate-500">{lens.coating}</p>
                  </div>
                  <div className="flex items-center justify-between pt-2 border-t border-slate-100">
                    <span className="text-xl font-black text-slate-900">Rs {lens.price.toLocaleString()}</span>
                    <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                      lens.inStock ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600"
                    }`}>
                      {lens.inStock ? "In stock" : "Out of stock"}
                    </span>
                  </div>
                  <Link
                    href={`/lenses/${lens.id}`}
                    className="w-full py-3 bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs rounded-xl text-center transition-all shadow-md shadow-teal-600/20 block"
                  >
                    View Lens Details
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
