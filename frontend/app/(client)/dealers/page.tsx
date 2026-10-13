"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

interface DealerContact {
  dealerId: number;
  name: string;
  email: string;
  itemType: "FRAME" | "LENS" | "BOTH";
  status: "ACTIVE" | "INACTIVE";
}

interface DealerFormValues {
  dealerId?: number;
  name: string;
  email: string;
  itemType: "FRAME" | "LENS" | "BOTH";
}

export default function DealerContactsPage() {
  const router = useRouter();
  const [dealers, setDealers] = useState<DealerContact[]>([]);
  const [form, setForm] = useState<DealerFormValues>({
    name: "",
    email: "",
    itemType: "FRAME",
  });
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Fetch dealers from API
  useEffect(() => {
    const loadDealers = async () => {
      setLoading(true);
      try {
        const response = await fetch("http://localhost:8080/api/dealers?clientId=1");

        if (!response.ok) {
          setErrorMsg(`Backend Error: Server returned ${response.status}`);
          return;
        }

        const result = await response.json();

        if (result.success && result.data) {
          setDealers(result.data);
        } else if (Array.isArray(result)) {
          setDealers(result);
        }
      } catch (err: any) {
        console.error(err);
        setErrorMsg("Failed to connect to backend server");
      } finally {
        setLoading(false);
      }
    };

    void loadDealers();
  }, []);

  // Add new dealer
  const handleAddDealer = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg(null);
    try {
      const response = await fetch("http://localhost:8080/api/dealers?clientId=1", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: form.name,
          email: form.email,
          itemType: form.itemType,
        }),
      });

      if (!response.ok) {
        setErrorMsg(`Failed to create dealer: ${response.status}`);
        return;
      }

      const result = await response.json();
      const newDealer = result.data || result;
      setDealers([...dealers, newDealer]);

      setForm({ name: "", email: "", itemType: "FRAME" });
      router.refresh(); // 404 error eka wathuru wage yanna meka damme
    } catch (err: any) {
      setErrorMsg(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Delete dealer function
  const deleteDealer = async (dealerId: number) => {
    if (window.confirm("Are you sure you want to delete this dealer?")) {
      try {
        await fetch(`http://localhost:8080/api/dealers/${dealerId}?clientId=1`, {
          method: "DELETE"
        });
        setDealers(dealers.filter((d) => d.dealerId !== dealerId));
      } catch (err) {
        console.error("Delete failed", err);
      }
    }
  };

  return (
      <div className="space-y-6 p-4">
        <header className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold">Dealer Contacts</h1>
          <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-600">
            {dealers.length} dealer(s)
          </span>
          </div>
        </header>

        {errorMsg && (
            <div className="p-3 bg-red-100 text-red-700 rounded mb-4 border border-red-200">
              {errorMsg}
            </div>
        )}

        {/* Dealer Form */}
        <div className="bg-white rounded p-4 shadow-sm mb-6">
          <h3 className="text-lg font-medium mb-3">Add New Dealer</h3>
          <form onSubmit={handleAddDealer} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
              <input
                  type="text"
                  name="name"
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  required
                  className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
                  placeholder="Dealer name"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  required
                  className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
                  placeholder="dealer@email.com"
              />
            </div>

            <div className="flex space-x-2">
              <select
                  name="itemType"
                  value={form.itemType}
                  onChange={(e) => setForm({ ...form, itemType: e.target.value as "FRAME" | "LENS" | "BOTH" })}
                  className="flex-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <option value="FRAME">Frames</option>
                <option value="LENS">Lenses</option>
                <option value="BOTH">Frames & Lenses</option>
              </select>

              <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                Add Dealer
              </button>
            </div>
          </form>
        </div>

        {/* Dealers List */}
        <div className="overflow-x-auto">
          <table className="min-w-full rounded border">
            <thead>
            <tr className="bg-gray-50">
              <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
              <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
              <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Item Type</th>
              <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              <th className="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
            </tr>
            </thead>
            <tbody>
            {dealers.map((dealer) => (
                <tr key={dealer.dealerId} className="border-b">
                  <td className="p-3">{dealer.name}</td>
                  <td className="p-3">{dealer.email}</td>
                  <td className="p-3">
                  <span className="text-xs capitalize font-semibold px-2 py-1 rounded bg-blue-100 text-blue-800">
                    {dealer.itemType}
                  </span>
                  </td>
                  <td className="p-3">
                  <span className="text-xs font-medium text-green-600">
                    {dealer.status === "ACTIVE" ? "Active" : "Inactive"}
                  </span>
                  </td>
                  <td className="p-3">
                    <div className="flex space-x-2">
                      <button
                          onClick={() => router.replace(`/client/dealers/${dealer.dealerId}/edit`)}
                          className="text-sm text-blue-600 hover:text-blue-800"
                      >
                        Edit
                      </button>
                      <button
                          onClick={() => deleteDealer(dealer.dealerId)}
                          className="text-sm text-red-600 hover:text-red-800"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
            ))}
            {dealers.length === 0 && (
                <tr>
                  <td colSpan={5} className="p-4 text-center text-gray-500">
                    No dealers found
                  </td>
                </tr>
            )}
            </tbody>
          </table>
        </div>
      </div>
  );
}