"use client";

import { useState, useEffect } from "react";

export default function DashboardPage() {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setTimeout(() => {
      setData({
        revenue: "Rs. 4,820.00",
        exams: "18 Patients",
        activeOrders: "42 Pending",
        capacity: "84%",
        recentOrders: [
          { id: "ORD-9841", customer: "Marcus Aurelius", style: "Avant-Garde Square (Opal)", total: "Rs. 340.00", status: "Delivered" },
          { id: "ORD-9840", customer: "Selena Gomez", style: "Classic D-Frame Tortoise", total: "Rs. 290.00", status: "Processing" },
          { id: "ORD-9839", customer: "Julius Caesar", style: "Ultralight Aviator + Rx Map", total: "Rs. 500.00", status: "Pending" },
        ],
        lowStock: [
          { name: "Avant-Garde Square (Obsidian)", detail: "Mazzucchelli", left: "2 left", status: "warning" },
          { name: "Classic D-Frame (Amber)", detail: "Kala Crafted", left: "1 left", status: "warning" },
          { name: "Chrono Shield Spectacle", detail: "Aero Blue", left: "Out of Stock", status: "danger" },
        ],
        queue: [
          { time: "09:30", name: "Clara Oswald", desc: "Comprehensive Exam • Dr. Vance" },
          { time: "10:45", name: "Bruce Banner", desc: "Dry Eye Therapy • Dr. Banner" },
          { time: "01:15", name: "Diana Prince", desc: "Pediatric Mapping • Dr. Vance" },
        ]
      });
      setLoading(false);
    }, 500);
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 text-slate-800">
        <div className="text-center space-y-4">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto"></div>
          <p className="text-slate-500 font-medium text-sm">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 flex font-sans text-slate-800">
      {/* Left Sidebar */}
      <aside className="w-64 bg-slate-900 text-slate-300 md:flex flex-col justify-between hidden border-r border-slate-800">
        <div>
          {/* Logo */}
          <div className="p-6 flex items-center gap-3 border-b border-slate-800/60">
            <div className="w-10 h-10 rounded-xl bg-teal-500 flex items-center justify-center text-white font-bold text-xl shadow-md">
              👓
            </div>
            <div>
              <h1 className="text-white font-bold text-lg tracking-tight">MyOpty</h1>
              <p className="text-xs text-teal-400 font-medium tracking-wider uppercase">B2B Portal</p>
            </div>
          </div>

          {/* Navigation Menu */}
          <nav className="p-4 space-y-1.5 text-sm font-medium">
            <a href="#" className="flex items-center gap-3 px-4 py-3 rounded-xl bg-teal-500 text-white shadow-lg shadow-teal-500/20 font-semibold">
              📊 Dashboard
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              🛍️ Orders
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              📦 Inventory
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              📅 Appointments
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              👥 Customers
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              📝 Prescriptions
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              📈 Analytics
            </a>
            <a href="#" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              ⚙️ Settings
            </a>
          </nav>
        </div>

        {/* User Profile Footer */}
        <div className="p-4 border-t border-slate-800/60 flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-slate-700 flex items-center justify-center font-bold text-white">
            EV
          </div>
          <div>
            <p className="text-sm font-bold text-white">Dr. Evelyn Vance</p>
            <p className="text-xs text-slate-400">Clinic Administrator</p>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 bg-slate-50 min-h-screen overflow-y-auto p-6 md:p-10">

        {/* Top Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
              Welcome Back, Evelyn
            </h1>
            <p className="text-sm text-slate-500 mt-1">
              Here's what's happening at MyOpty Clinic & Luxury Eyewear today.
            </p>
          </div>
          <div className="bg-white border border-slate-200 px-4 py-2.5 rounded-2xl shadow-sm text-sm font-semibold text-slate-700 self-start md:self-auto">
            Thursday, Oct 12, 2026
          </div>
        </div>

        {/* 4 Metric Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm relative overflow-hidden">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Today's Revenue</span>
              <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">+14.2%</span>
            </div>
            <div className="text-2xl font-black text-slate-900">{data.revenue}</div>
            <div className="mt-3 flex items-end gap-1 h-4">
              <div className="w-1.5 bg-teal-500 h-2 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-3 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-4 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-3 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-4 rounded-full"></div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm relative overflow-hidden">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">New Eye Exams</span>
              <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">+8.3%</span>
            </div>
            <div className="text-2xl font-black text-slate-900">{data.exams}</div>
            <div className="mt-3 flex items-end gap-1 h-4">
              <div className="w-1.5 bg-teal-500 h-2 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-4 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-3 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-4 rounded-full"></div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm relative overflow-hidden">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Active Orders</span>
              <span className="text-xs font-bold text-rose-600 bg-rose-50 px-2 py-0.5 rounded-full">-2.4%</span>
            </div>
            <div className="text-2xl font-black text-slate-900">{data.activeOrders}</div>
            <div className="mt-3 flex items-end gap-1 h-4">
              <div className="w-1.5 bg-slate-300 h-3 rounded-full"></div>
              <div className="w-1.5 bg-slate-300 h-2 rounded-full"></div>
              <div className="w-1.5 bg-slate-300 h-4 rounded-full"></div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm relative overflow-hidden">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Clinic Capacity</span>
              <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">+5.1%</span>
            </div>
            <div className="text-2xl font-black text-slate-900">{data.capacity}</div>
            <div className="mt-3 flex items-end gap-1 h-4">
              <div className="w-1.5 bg-teal-500 h-3 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-4 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-4 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-3 rounded-full"></div>
              <div className="w-1.5 bg-teal-500 h-4 rounded-full"></div>
            </div>
          </div>
        </div>

        {/* Middle Section: Chart & Low Stock */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          {/* Revenue Trend Chart */}
          <div className="lg:col-span-2 bg-white p-6 md:p-8 rounded-3xl border border-slate-200/80 shadow-sm">
            <h3 className="text-lg font-bold text-slate-900 mb-6">Revenue Trend (Weekly)</h3>
            <div className="h-48 flex items-end justify-between gap-2 pt-4 px-2 border-b border-slate-100">
              <div className="w-full bg-teal-500/10 hover:bg-teal-500/20 rounded-t-xl h-[45%] flex items-end justify-center pb-2 transition-all"><div className="w-6 bg-teal-600 h-[60%] rounded-lg"></div></div>
              <div className="w-full bg-teal-500/10 hover:bg-teal-500/20 rounded-t-xl h-[60%] flex items-end justify-center pb-2 transition-all"><div className="w-6 bg-teal-600 h-[75%] rounded-lg"></div></div>
              <div className="w-full bg-teal-500/10 hover:bg-teal-500/20 rounded-t-xl h-[85%] flex items-end justify-center pb-2 transition-all"><div className="w-6 bg-teal-600 h-[90%] rounded-lg"></div></div>
              <div className="w-full bg-teal-500/10 hover:bg-teal-500/20 rounded-t-xl h-[65%] flex items-end justify-center pb-2 transition-all"><div className="w-6 bg-teal-600 h-[70%] rounded-lg"></div></div>
              <div className="w-full bg-teal-500/10 hover:bg-teal-500/20 rounded-t-xl h-[95%] flex items-end justify-center pb-2 transition-all"><div className="w-6 bg-teal-600 h-[85%] rounded-lg"></div></div>
              <div className="w-full bg-teal-500/10 hover:bg-teal-500/20 rounded-t-xl h-[100%] flex items-end justify-center pb-2 transition-all"><div className="w-6 bg-teal-600 h-[95%] rounded-lg"></div></div>
              <div className="w-full bg-teal-500/10 hover:bg-teal-500/20 rounded-t-xl h-[80%] flex items-end justify-center pb-2 transition-all"><div className="w-6 bg-teal-600 h-[80%] rounded-lg"></div></div>
            </div>
            <div className="flex justify-between text-xs font-semibold text-slate-400 mt-3 px-2">
              <span>Mon</span><span>Tue</span><span>Wed</span><span>Thu</span><span>Fri</span><span>Sat</span><span>Sun</span>
            </div>
          </div>

          {/* Low-Stock Alerts */}
          <div className="bg-white p-6 md:p-8 rounded-3xl border border-slate-200/80 shadow-sm">
            <h3 className="text-lg font-bold text-slate-900 mb-6">Low-Stock Alerts</h3>
            <div className="space-y-4">
              {data.lowStock.map((item: any, i: number) => (
                <div key={i} className="p-4 rounded-2xl bg-slate-50 border border-slate-100 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-bold text-slate-900">{item.name}</p>
                    <p className="text-xs text-slate-400">{item.detail}</p>
                  </div>
                  <span className={`text-xs font-bold px-3 py-1.5 rounded-xl ${item.status === 'danger' ? 'bg-rose-50 text-rose-600' : 'bg-amber-50 text-amber-600'}`}>
                    {item.left}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Bottom Section: Recent Orders & Clinical Queue */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Recent Orders */}
          <div className="lg:col-span-2 bg-white p-6 md:p-8 rounded-3xl border border-slate-200/80 shadow-sm">
            <h3 className="text-lg font-bold text-slate-900 mb-6">Recent Orders</h3>
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                <tr className="text-xs font-bold text-slate-400 uppercase border-b border-slate-100 pb-3">
                  <th className="pb-3">Order ID</th>
                  <th className="pb-3">Customer</th>
                  <th className="pb-3">Frame Styles</th>
                  <th className="pb-3">Total</th>
                  <th className="pb-3">Status</th>
                </tr>
                </thead>
                <tbody className="text-sm divide-y divide-slate-100">
                {data.recentOrders.map((ord: any, i: number) => (
                  <tr key={i} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-4 font-bold text-teal-600">{ord.id}</td>
                    <td className="py-4 font-semibold text-slate-800">{ord.customer}</td>
                    <td className="py-4 text-slate-500 text-xs">{ord.style}</td>
                    <td className="py-4 font-bold text-slate-900">{ord.total}</td>
                    <td className="py-4">
                        <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                          ord.status === 'Delivered' ? 'bg-emerald-50 text-emerald-600' :
                            ord.status === 'Processing' ? 'bg-sky-50 text-sky-600' : 'bg-amber-50 text-amber-600'
                        }`}>
                          {ord.status}
                        </span>
                    </td>
                  </tr>
                ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Today's Clinical Queue */}
          <div className="bg-white p-6 md:p-8 rounded-3xl border border-slate-200/80 shadow-sm">
            <h3 className="text-lg font-bold text-slate-900 mb-6">Today's Clinical Queue</h3>
            <div className="space-y-4">
              {data.queue.map((q: any, i: number) => (
                <div key={i} className="p-4 rounded-2xl bg-slate-50 border border-slate-100 flex items-center gap-4">
                  <div className="bg-teal-50 text-teal-700 font-bold text-xs px-3 py-2 rounded-xl">
                    {q.time}
                  </div>
                  <div>
                    <p className="text-sm font-bold text-slate-900">{q.name}</p>
                    <p className="text-xs text-slate-400">{q.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

      </main>
    </div>
  );
}
