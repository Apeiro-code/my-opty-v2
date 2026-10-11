"use client";

import { useState, useEffect, useParams } from "react";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/workflow";

interface SalesMonthData {
  month: number;
  year: number;
  totalOrders: number;
  totalRevenue: number;
}

interface SalesHistoryItem {
  month: number;
  year: number;
  totalOrders: number;
  totalRevenue: number;
}

interface SalesReportData {
  currentMonth: SalesMonthData;
  history: SalesHistoryItem[];
  filters: {
    month: number;
    year: number;
    months: number[];
    years: number[];
  };
}

export default function SalesReportPage() {
  const { month, year } = useParams<{ month?: string; year?: string }>();
  const selectedMonth = month ? Number(month) : undefined;
  const selectedYear = year ? Number(year) : undefined;

  const [data, setData] = useState<SalesReportData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showExport, setShowExport] = useState(false);

  useEffect(() => {
    const loadSalesReport = async () => {
      setLoading(true);
      try {
        // Build query parameters
        const monthParam = selectedMonth !== undefined ? `?month=${selectedMonth}` : "";
        const yearParam = selectedYear !== undefined ? `&year=${selectedYear}` : "";
        const fullPath = `/reports/sales/monthly${monthParam}${yearParam}`;

        const result: ApiResponse<{
          currentMonth: SalesMonthData;
          history: SalesHistoryItem[];
        }> = await api.fetchWrapper(fullPath);

        if (result.success && result.data) {
          setData({
            currentMonth: result.data.currentMonth,
            history: result.data.history,
            filters: {
              month: selectedMonth || 0,
              year: selectedYear || new Date().getFullYear(),
              months: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
              years: [2023, 2024, 2025],
            },
          });
        } else {
          setError(result.error?.message || "Failed to load sales report");
        }
      } catch (err: any) {
        setError(err.message || "Failed to load sales report");
      } finally {
        setLoading(false);
      }
    };

    loadSalesReport();
  }, [selectedMonth, selectedYear]);

  if (loading) {
    return (
      <div className="min-h-80 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading sales report...</p>
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

  const { currentMonth, history } = data;
  const { month, year, months, years } = data.filters;

  return (
    <div className="p-4">
      <h1 className="text-3xl font-bold text-primary-800 mb-6">
        Monthly Sales Report
      </h1>

      {/* Filter Section */}
      <div className="bg-white rounded-2xl p-6 shadow-xl mb-8 border border-gray-200">
        <h2 className="text-xl font-semibold text-gray-800 mb-4">Filter Report</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Month
            </label>
            <select
              onChange={(e) => {
                const url = new URL(window.location.href);
                url.searchParams.set("month", e.target.value);
                url.searchParams.set("year", selectedYear || new Date().getFullYear());
                window.location.href = url.href;
              }}
              className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="">All Months</option>
              {months.map((m) => (
                <option
                  key={m}
                  value={m}
                  selected={selectedMonth === m}
                >
                  {new Date(2000, m - 1).toLocaleString("default", { month: "long" })}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Year
            </label>
            <select
              onChange={(e) => {
                const url = new URL(window.location.href);
                url.searchParams.set("year", e.target.value);
                url.searchParams.set("month", selectedMonth || (new Date().getMonth() + 1));
                window.location.href = url.href;
              }}
              className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="">All Years</option>
              {years.map((y) => (
                <option key={y} value={y} selected={selectedYear === y}>
                  {y}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Current Month Summary */}
      <div className="bg-white rounded-2xl p-6 shadow-xl mb-8 border border-primary-100">
        <h3 className="text-xl font-semibold text-primary-700 mb-3">
          {year ? `${year} Month ${month}` : "Current Month"} Summary
        </h3>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-sm text-gray-500">Total Orders</p>
            <p className="text-3xl font-bold text-primary-600">{currentMonth.totalOrders}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Total Revenue</p>
            <p className="text-3xl font-bold text-primary-600">${currentMonth.totalRevenue.toLocaleString()}</p>
          </div>
        </div>
      </div>

      {/* Sales History Table */}
      <div className="bg-white rounded-2xl p-6 shadow-xl">
        <h3 className="text-xl font-semibold text-success-700 mb-4">Sales History</h3>
        {history.length === 0 && (
          <p className="text-gray-500">No historical data available</p>
        )}
        <div className="overflow-x-auto">
          <table className="min-w-full">
            <thead>
              <tr className="bg-gray-50">
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Period
                </th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Orders
                </th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Revenue
                </th>
              </tr>
            </thead>
            <tbody>
              {history.map((item, index) => (
                <tr key={index} className="border-b hover:bg-gray-50">
                  <td className="p-3">
                    <span className="text-sm font-medium text-gray-700">
                      {item.month}-{item.year}
                    </span>
                  </td>
                  <td className="p-3">
                    <span className="text-sm text-gray-700">{item.totalOrders}</span>
                  </td>
                  <td className="p-3">
                    <span className="text-sm text-gray-700">${item.totalRevenue.toLocaleString()}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Export Section */}
      <div className="mt-8 pt-8 border-t border-gray-200">
        <h3 className="text-xl font-semibold text-gray-800 mb-4">Export Report</h3>
        <div className="grid grid-cols-2 gap-3">
          <button
            onClick={() => {
              // Export as PDF
              window.open(
                `/api/reports/export/sales/pdf?clientId=1&month=${currentMonth.month}&year=${currentMonth.year}`,
                "_blank"
              );
            }}
            className="w-full px-4 py-2 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors flex items-center gap-2">
            <svg className="w-5 h-5" fill="none" stroke="currentColor">
              <path d="M21 7a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V2a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v1M4.22 5.76a2 2 0 0 1 1.42 1.06l1.36 1.36a2 2 0 0 1 .28 2.58l-1.06 2.55c-.18.45-.5.77-.95.77H5.5l-.42 1.17a1.996 1.996 0 0 1-1.07.02l-.45-1.85a2 2 0 0 1-1.98-1.07l-.1-1.68c0-.36.03-.7.05-1.03l.18-1.36a2 2 0 0 1 .28-2.58l1.36-1.36a2 2 0 0 1 2.58-.28l2.55-1.06c.45-.18.77-.5.77-.95V4.22zM5.3 4.28l1.85 1.85a2 2 0 0 1-1.06 2.06H3.57l-1.32 1.33c-.35.35-1.02.05-1.02-.7l0-3.38a2 2 0 0 1 1.06-.7l2.15.55c.6.16.84.43 1.03.7l.39 1.65c.1.88.03 1.98-.2 2.33l-1.84.75a2 2 0 0 1-2.22 0L.5 9.68c-.57-.16-1.02-.83-.85-1.38l3.88 1.1c.38.1 .84.03 1.06-.13l.32 2.05a2 2 0 0 1-.2 2.33l-.39 1.65c-.1.88-.87 1.3-1.48.88l-3.05-1.55a2 2 0 0 1-1.96-1.07l-.1-1.68c0-.36.03-.7.05-1.03l.18-1.36a2 2 0 0 1 .28-2.58l-1.36-1.36a2 2 0 0 1-2.58.28l-2.55 1.06c-.45.18-.77.5-.77.95V15.78a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1M5.3 4.28l1.85 1.85a2 2 0 0 1-1.06 2.06H3.57l-1.32 1.33c-.35.35-1.02.05-1.02-.7l0-3.38a2 2 0 0 1 1.06-.7l2.15.55c.6.16.84.43 1.03.7l.39 1.65c.1.88.03 1.98-.2 2.33l-1.84.75a2 2 0 0 1-2.22 0L.5 9.68c-.57-.16-1.02-.83-.85-1.38l3.88 1.1c.38.1 .84.03 1.06-.13l.32 2.05a2 2 0 0 1-.2 2.33l-.39 1.65c-.1.88-.87 1.3-1.48.88l-3.05-1.55a2 2 0 0 1-1.96-1.07l-.1-1.68c0-.36.03-.7.05-1.03l.18-1.36a2 2 0 0 1 .28-2.58l-1.36-1.36a2 2 0 0 1-2.58.28l-2.55 1.06c-.45.18-.77.5-.77.95V15.78a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1z" />
            </svg>
            Export PDF
          </button>
          <button
            onClick={() => {
              // Export as Excel
              window.open(
                `/api/reports/export/sales/excel?clientId=1&month=${currentMonth.month}&year=${currentMonth.year}`,
                "_blank"
              );
            }}
            className="w-full px-4 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors flex items-center gap-2">
            <svg className="w-5 h-5" fill="none" stroke="currentColor">
              <path d="M21 21l-5-5m0-5l-5 5M10 7l5 5M7 10l5-5" />
            </svg>
            Export Excel
          </button>
        </div>
      </div>
    </div>
  );
}