"use client";

import { useState, useEffect, useParams } from "react";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/workflow";

interface HistoricalMetric {
  metricType: string;
  metricValue: number;
  recordedAt: string;
}

interface HistoricalReportFilters {
  clientId: number;
  metricType: string;
  month: number;
  year: number;
}

interface HistoricalReportData {
  metrics: HistoricalMetric[];
  availableMetrics: string[];
  filters: {
    clientId: number;
    metricTypes: string[];
    selectedMetric: string;
    months: number[];
    years: number[];
    selectedMonth: number;
    selectedYear: number;
  };
}

export default function HistoricalReportsPage() {
  const { month, year, metricType } = useParams<{
    month?: string;
    year?: string;
    metricType?: string;
  }>();

  const [data, setData] = useState<HistoricalReportData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadHistoricalReports = async () => {
      setLoading(true);
      try {
        // Build query parameters
        const monthParam = month ? `?month=${month}` : "";
        const yearParam = year ? `&year=${year}` : "";
        const metricParam = metricType ? `&metricType=${metricType}` : "";
        const fullPath = `/reports/historical${monthParam}${yearParam}${metricParam}`;

        const result: ApiResponse<{
          metrics: HistoricalMetric[];
          availableMetrics: string[];
        }> = await api.fetchWrapper(fullPath);

        if (result.success && result.data) {
          setData({
            metrics: result.data.metrics,
            availableMetrics: result.data.availableMetrics,
            filters: {
              clientId: 1,
              metricTypes: result.data.availableMetrics,
              selectedMetric: metricType || "SALES",
              months: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
              years: [2023, 2024, 2025],
              selectedMonth: month ? Number(month) : new Date().getMonth() + 1,
              selectedYear: year ? Number(year) : new Date().getFullYear(),
            },
          });
        } else {
          setError(result.error?.message || "Failed to load historical reports");
        }
      } catch (err: any) {
        setError(err.message || "Failed to load historical reports");
      } finally {
        setLoading(false);
      }
    };

    loadHistoricalReports();
  }, [month, year, metricType]);

  if (loading) {
    return (
      <div className="min-h-80 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-info-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading historical reports...</p>
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

  const { metrics, availableMetrics } = data;
  const { selectedMetric, selectedMonth, selectedYear, metricTypes } = data.filters;

  return (
    <div className="p-4">
      <h1 className="text-3xl font-bold text-info-800 mb-6">
        Historical Reports
      </h1>

      {/* Filter Section */}
      <div className="bg-white rounded-2xl p-6 shadow-xl mb-8 border border-info-200">
        <h2 className="text-xl font-semibold text-gray-800 mb-4">Filters</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Metric Type
            </label>
            <select
              onChange={(e) => {
                const url = new URL(window.location.href);
                url.searchParams.set("metricType", e.target.value);
                url.searchParams.set("month", selectedMonth.toString());
                url.searchParams.set("year", selectedYear.toString());
                window.location.href = url.href;
              }}
              className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-info-500"
            >
              <option value="">All Metrics</option>
              {metricTypes.map((m) => (
                <option
                  key={m}
                  value={m}
                  selected={selectedMetric === m}
                >
                  {m}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Month
            </label>
            <select
              onChange={(e) => {
                const url = new URL(window.location.href);
                url.searchParams.set("month", e.target.value);
                url.searchParams.set("year", selectedYear.toString());
                url.searchParams.set("metricType", selectedMetric);
                window.location.href = url.href;
              }}
              className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-info-500"
            >
              <option value="">All Months</option>
              {data.filters.months.map((m) => (
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
              onChange={(e) => {
                const url = new URL(window.location.href);
                url.searchParams.set("year", e.target.value);
                url.searchParams.set("month", selectedMonth.toString());
                url.searchParams.set("metricType", selectedMetric);
                window.location.href = url.href;
              }}
              className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-info-500"
            >
              <option value="">All Years</option>
              {data.filters.years.map((y) => (
                <option key={y} value={y} selected={selectedYear === y}>
                  {y}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Metrics Summary */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        {metrics.map((metric) => (
          <div
            key={metric.metricType}
            className="bg-white rounded-xl p-5 shadow-lg hover:shadow-2xl transition-shadow border border-info-100"
          >
            <p className="text-sm text-gray-500 mb-2">{metric.metricType}</p>
            <p className="text-3xl font-bold text-info-600">
              {metric.metricValue.toLocaleString()}
            </p>
          </div>
        ))}
      </div>

      {/* Historical Data Table */}
      <div className="bg-white rounded-2xl p-6 shadow-xl">
        <h3 className="text-xl font-semibold text-gray-800 mb-4">
          {selectedMetric} History
        </h3>
        {metrics.length === 0 && (
          <p className="text-gray-500">No historical data available for this metric</p>
        )}
        <div className="overflow-x-auto">
          <table className="min-w-full">
            <thead>
              <tr className="bg-gray-50">
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Period
                </th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Value
                </th>
                <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Recorded At
                </th>
              </tr>
            </thead>
            <tbody>
              {metrics.map((metric, index) => (
                <tr key={index} className="border-b hover:bg-gray-50">
                  <td className="p-3">
                    <span className="text-sm font-medium text-gray-700">
                      {metric.metricType}-{selectedMonth}-{selectedYear}
                    </span>
                  </td>
                  <td className="p-3">
                    <span className="text-sm text-gray-700">
                      {metric.metricValue.toLocaleString()}
                    </span>
                  </td>
                  <td className="p-3">
                    <span className="text-sm text-gray-700">
                      {new Date(metric.recordedAt).toLocaleDateString()}
                    </span>
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