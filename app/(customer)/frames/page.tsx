import FrameGrid from "@/features/catalog/components/FrameGrid";
import Link from "next/link";

/**
 * The customer's frames page: the shop wall, a grid of every frame on sale.
 */
export default function FramesPage() {
  return (
    <div className="p-6 md:p-10 space-y-8 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/" className="hover:text-teal-600">Home</Link>
        <span>/</span>
        <Link href="/frames" className="hover:text-teal-600">Shop</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">All Eyewear</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            All Premium Eyewear
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-2xl">
            Browse the frames currently on the wall. Pick one to build your order around, or send us your prescription and ask us for a recommendation.
          </p>
        </div>
      </div>

      {/* Filter Toolbar Section */}
      <div className="bg-white rounded-3xl p-5 shadow-sm border border-slate-200/80 flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3">
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 focus:outline-none">
            <option>Brand</option>
          </select>
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 focus:outline-none">
            <option>Frame Shape</option>
          </select>
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 focus:outline-none">
            <option>Material</option>
          </select>
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 focus:outline-none">
            <option>Price Range</option>
          </select>
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 focus:outline-none">
            <option>Lens Type</option>
          </select>
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 focus:outline-none">
            <option>Gender</option>
          </select>
        </div>

        <div className="flex items-center gap-3">
          <span className="text-xs text-slate-400 font-medium">Sort by:</span>
          <select className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-900 focus:outline-none">
            <option>Featured Masterpieces</option>
            <option>Price: Low to High</option>
            <option>Price: High to Low</option>
          </select>
        </div>
      </div>

      {/* Active Filters Pills */}
      <div className="flex flex-wrap items-center gap-2 text-xs">
        <span className="text-slate-400 font-semibold mr-1">Active filters:</span>
        <span className="px-3 py-1 bg-teal-50 text-teal-700 rounded-full font-bold flex items-center gap-1 border border-teal-200">
          Color: Tortoise ✕
        </span>
        <span className="px-3 py-1 bg-teal-50 text-teal-700 rounded-full font-bold flex items-center gap-1 border border-teal-200">
          Shape: D-Frame ✕
        </span>
        <span className="px-3 py-1 bg-teal-50 text-teal-700 rounded-full font-bold flex items-center gap-1 border border-teal-200">
          Price: Under $400 ✕
        </span>
        <button className="text-teal-600 font-bold hover:underline ml-2">Clear All</button>
      </div>

      {/* Main Content Layout (Sidebar + Grid) */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">

        {/* Left Sidebar (Categories & Virtual Mirror banner) */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200/80 space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Categories</h3>
            <ul className="space-y-2 text-sm font-semibold">
              <li className="flex items-center justify-between p-2 rounded-xl bg-teal-50 text-teal-700">
                <span>All Eyewear</span>
                <span className="text-xs bg-teal-200/60 px-2 py-0.5 rounded-full">288</span>
              </li>
              <li className="flex items-center justify-between p-2 rounded-xl text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer">
                <span>Prescription</span>
                <span className="text-xs text-slate-400">124</span>
              </li>
              <li className="flex items-center justify-between p-2 rounded-xl text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer">
                <span>Sunglasses</span>
                <span className="text-xs text-slate-400">88</span>
              </li>
              <li className="flex items-center justify-between p-2 rounded-xl text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer">
                <span>Blue Light Screen</span>
                <span className="text-xs text-slate-400">42</span>
              </li>
              <li className="flex items-center justify-between p-2 rounded-xl text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer">
                <span>Performance Sports</span>
                <span className="text-xs text-slate-400">34</span>
              </li>
              <li className="flex items-center justify-between p-2 rounded-xl text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer">
                <span>Clinically Calibrated</span>
                <span className="text-xs text-slate-400">15</span>
              </li>
            </ul>
          </div>

          {/* Virtual Try-On Banner */}
          <div className="bg-[#0b0f19] text-white rounded-3xl p-6 shadow-md space-y-4 relative overflow-hidden">
            <div className="w-10 h-10 rounded-2xl bg-teal-500/20 flex items-center justify-center text-teal-400 text-lg">
              📸
            </div>
            <h4 className="font-bold text-base leading-snug">Find the Perfect Frame Shape</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Use our award-winning virtual camera mirror to scan your face dimensions and map perfectly fitted frame matches.
            </p>
            <button className="w-full py-3 bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs rounded-xl transition-all shadow-md shadow-teal-600/30">
              Activate Real-time Mirror
            </button>
          </div>
        </div>

        {/* Right Frames Grid Area */}
        <div className="lg:col-span-3 space-y-8">
          {/* Original FrameGrid component rendering wall items */}
          <FrameGrid />

          {/* Pagination / Load More */}
          <div className="flex flex-col items-center justify-center pt-6 space-y-3">
            <p className="text-xs text-slate-400 font-medium">Showing 6 of 288 exquisite frames</p>
            <button className="px-8 py-3.5 bg-white border border-slate-200 text-teal-600 rounded-2xl font-bold hover:bg-slate-50 transition-all text-sm shadow-sm">
              Load More Masterpieces
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}
