"use client";

import { useState, useEffect } from "react";
import { useParams } from "next/navigation";
import { api } from "@/lib/api";

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

  useEffect(() => {
    const loadSalesReport = async () => {
      setLoading(true);
      try {
        const monthParam = selectedMonth !== undefined ? `?month=${selectedMonth}` : "";
        const yearParam = selectedYear !== undefined ? `&year=${selectedYear}` : "";
        const fullPath = `/reports/sales/monthly${monthParam}${yearParam}`;

        const result: any = await api.fetchWrapper(fullPath);

        if (result && (result.success || result.data)) {
          const reportData = result.data || result;
          setData({
            currentMonth: reportData.currentMonth || { month: selectedMonth || 1, year: selectedYear || 2026, totalOrders: 0, totalRevenue: 0 },
            history: reportData.history || [],
            filters: {
              month: selectedMonth || 0,
              year: selectedYear || new Date().getFullYear(),
              months: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
              years: [2023, 2024, 2025, 2026],
            },
          });
        } else {
          setData({
            currentMonth: { month: selectedMonth || 1, year: selectedYear || 2026, totalOrders: 0, totalRevenue: 0 },
            history: [],
            filters: {
              month: selectedMonth || 0,
              year: selectedYear || new Date().getFullYear(),
              months: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
              years: [2023, 2024, 2025, 2026],
            },
          });
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
  const { month: filterMonth, year: filterYear, months, years } = data.filters;

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
                  defaultValue={selectedMonth || ""}
                  onChange={(e) => {
                    const url = new URL(window.location.href);
                    url.searchParams.set("month", e.target.value);
                    url.searchParams.set("year", String(selectedYear || new Date().getFullYear()));
                    window.location.href = url.href;
                  }}
                  className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <option value="">All Months</option>
                {months.map((m) => (
                    <option key={m} value={m}>
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
                  defaultValue={selectedYear || ""}
                  onChange={(e) => {
                    const url = new URL(window.location.href);
                    url.searchParams.set("year", e.target.value);
                    url.searchParams.set("month", String(selectedMonth || (new Date().getMonth() + 1)));
                    window.location.href = url.href;
                  }}
                  className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <option value="">All Years</option>
                {years.map((y) => (
                    <option key={y} value={y}>
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
            {filterYear ? `${filterYear} Month ${filterMonth}` : "Current Month"} Summary
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
      </div>
  );
}