"use client";

import { useState, useEffect } from "react";

interface DashboardMetrics {
  totalSales: number;
  totalOrders: number;
  lowStockItems: number;
  pendingTasks: number;
}

interface DashboardData {
  metrics: DashboardMetrics;
  recentSales: any[];
  recentInventory: any[];
}

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadDashboard = async () => {
      setLoading(true);
      try {
        const [summaryRes, salesRes, inventoryRes] = await Promise.all([
          fetch("http://localhost:8080/api/reports/dashboard/summary?clientId=1"),
          fetch("http://localhost:8080/api/reports/sales/monthly/history?clientId=1"),
          fetch("http://localhost:8080/api/reports/inventory/monthly/history?clientId=1")
        ]);

        if (!summaryRes.ok || !salesRes.ok || !inventoryRes.ok) {
          throw new Error("Failed to load dashboard data");
        }

        const summaryData = await summaryRes.json();
        const salesData = await salesRes.json();
        const inventoryData = await inventoryRes.json();

        setData({
          metrics: {
            totalSales: summaryData.totalSales || 0,
            totalOrders: summaryData.totalOrders || 0,
            lowStockItems: summaryData.lowStockItems || 0,
            pendingTasks: summaryData.pendingTasks || 0,
          },
          recentSales: Array.isArray(salesData) ? salesData : [],
          recentInventory: Array.isArray(inventoryData) ? inventoryData : [],
        });
      } catch (err: any) {
        setError(err.message || "Failed to load dashboard");
      } finally {
        setLoading(false);
      }
    };

    void loadDashboard();
  }, []);


  if (loading) {
    return (
      <div className="min-h-80 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 text-red-600">
        <h3 className="text-lg font-medium mb-2">Error</h3>
        <p>{error}</p>
      </div>
    );
  }

  if (!data) {
    return <div className="p-6">No data available</div>;
  }

  const { metrics, recentSales, recentInventory } = data;


  return (
    <div className="p-4">
      <h1 className="text-3xl font-bold text-primary-800 mb-6">
        Dashboard - Daily Overview
      </h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {/* Total Sales Card */}
        <div className="bg-white rounded-2xl p-6 shadow-2xl hover:shadow-3xl transition-shadow transform hover:-translate-y-1 border border-primary-100">
          <div className="flex items-start">
            <div className="w-12 h-12 rounded-xl bg-primary-100 flex items-center justify-center flex-shrink-0">
              <svg className="w-6 h-6 text-primary-600" fill="none" stroke="currentColor">
                <path d="M12 2v2m0 10v2m-7-10v2m7-10v2m-3.5 7.5h15a3.5 3.5 0 0 1 0 7h-15a3.5 3.5 0 0 1 0-7zM5 10.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" />
              </svg>
            </div>
            <div className="ml-4">
              <p className="text-sm text-gray-500">Total Sales</p>
              <p className="text-3xl font-bold text-primary-600">${metrics.totalSales.toLocaleString()}</p>
            </div>
          </div>
        </div>

        {/* Total Orders Card */}
        <div className="bg-white rounded-2xl p-6 shadow-2xl hover:shadow-3xl transition-shadow transform hover:-translate-y-1 border border-primary-100">
          <div className="flex items-start">
            <div className="w-12 h-12 rounded-xl bg-success-100 flex items-center justify-center flex-shrink-0">
              <svg className="w-6 h-6 text-success-600" fill="none" stroke="currentColor">
                <path d="M12 2v2m0 10v2m-7-10v2m7-10v2m-3.5 7.5h15a3.5 3.5 0 0 1 0 7h-15a3.5 3.5 0 0 1 0-7zM5 10.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" />
              </svg>
              <div className="ml-4">
                <p className="text-sm text-gray-500">Total Orders</p>
                <p className="text-3xl font-bold text-success-600">{metrics.totalOrders}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Low Stock Items Card */}
        <div className="bg-white rounded-2xl p-6 shadow-2xl hover:shadow-3xl transition-shadow transform hover:-translate-y-1 border border-warning-100">
          <div className="flex items-start">
            <div className="w-12 h-12 rounded-xl bg-warning-100 flex items-center justify-center flex-shrink-0">
              <svg className="w-6 h-6 text-warning-600" fill="none" stroke="currentColor">
                <path d="M12 2v2m0 10v2m-7-10v2m7-10v2m-3.5 7.5h15a3.5 3.5 0 0 1 0 7h-15a3.5 3.5 0 0 1 0-7zM5 10.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" />
              </svg>
            </div>
            <div className="ml-4">
              <p className="text-sm text-gray-500">Low Stock Items</p>
              <p className="text-3xl font-bold text-warning-600">{metrics.lowStockItems}</p>
            </div>
          </div>
        </div>

        {/* Pending Tasks Card */}
        <div className="bg-white rounded-2xl p-6 shadow-2xl hover:shadow-3xl transition-shadow transform hover:-translate-y-1 border border-info-100">
          <div className="flex items-start">
            <div className="w-12 h-12 rounded-xl bg-info-100 flex items-center justify-center flex-shrink-0">
              <svg className="w-6 h-6 text-info-600" fill="none" stroke="currentColor">
                <path d="M12 2v2m0 10v2m-7-10v2m7-10v2m-3.5 7.5h15a3.5 3.5 0 0 1 0 7h-15a3.5 3.5 0 0 1 0-7zM5 10.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" />
              </svg>
            </div>
            <div className="ml-4">
              <p className="text-sm text-gray-500">Pending Tasks</p>
              <p className="text-3xl font-bold text-info-600">{metrics.pendingTasks}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Sales Section */}
      <div className="bg-white rounded-2xl p-6 shadow-xl mb-8">
        <h2 className="text-xl font-semibold text-primary-700 mb-4">Recent Sales</h2>
        {recentSales.length === 0 && (
          <p className="text-gray-500">No sales data available</p>
        )}
        <div className="overflow-x-auto">
          <table className="min-w-full">
            <thead>
              <tr className="bg-gray-50">
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Orders</th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Revenue</th>
              </tr>
            </thead>
            <tbody>
              {recentSales.map((sale, index) => (
                <tr key={index} className="border-b hover:bg-gray-50">
                  <td className="p-3">
                    <span className="text-sm text-gray-700">{sale?.date || "N/A"}</span>
                  </td>
                  <td className="p-3">
                    <span className="text-sm text-gray-700">{sale?.orders || 0}</span>
                  </td>
                  <td className="p-3">
                    <span className="text-sm text-gray-700">${(sale?.revenue || 0).toLocaleString()}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Recent Inventory Section */}
      <div className="bg-white rounded-2xl p-6 shadow-xl">
        <h2 className="text-xl font-semibold text-success-700 mb-4">Recent Inventory Movement</h2>
        {recentInventory.length === 0 && (
          <p className="text-gray-500">No inventory data available</p>
        )}
        <div className="overflow-x-auto">
          <table className="min-w-full">
            <thead>
              <tr className="bg-gray-50">
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Item</th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Stock Added</th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Stock Sold</th>
              </tr>
            </thead>
            <tbody>
              {recentInventory.map((item, index) => (
                <tr key={index} className="border-b hover:bg-gray-50">
                  <td className="p-3">
                    <span className="text-sm font-medium text-gray-700">{item?.itemName || "N/A"}</span>
                  </td>
                  <td className="p-3">
                    <span className="text-sm text-gray-700">{item?.stockAdded || 0}</span>
                  </td>
                  <td className="p-3">
                    <span className="text-sm text-gray-700">{item?.stockSold || 0}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}