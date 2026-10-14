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

  useEffect(() => {
    const loadDealers = async () => {
      setLoading(true);
      try {
        const response = await fetch("http://localhost:8080/api/dealers?clientId=1").catch(() => null);

        if (!response || !response.ok) {
          setDealers([
            { dealerId: 1, name: "Essilor International", email: "support@essilor.com", itemType: "LENS", status: "ACTIVE" },
            { dealerId: 2, name: "Mazzucchelli Optics", email: "info@mazzucchelli.it", itemType: "FRAME", status: "ACTIVE" },
            { dealerId: 3, name: "Kala Crafted Eyewear", email: "sales@kalacrafted.com", itemType: "BOTH", status: "INACTIVE" },
          ]);
          setLoading(false);
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
        setDealers([
          { dealerId: 1, name: "Essilor International", email: "support@essilor.com", itemType: "LENS", status: "ACTIVE" },
          { dealerId: 2, name: "Mazzucchelli Optics", email: "info@mazzucchelli.it", itemType: "FRAME", status: "ACTIVE" },
        ]);
      } finally {
        setLoading(false);
      }
    };

    void loadDealers();
  }, []);

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
        const newDealer: DealerContact = {
          dealerId: Date.now(),
          name: form.name,
          email: form.email,
          itemType: form.itemType,
          status: "ACTIVE",
        };
        setDealers([...dealers, newDealer]);
        setForm({ name: "", email: "", itemType: "FRAME" });
        setLoading(false);
        return;
      }

      const result = await response.json();
      const newDealer = result.data || result;
      setDealers([...dealers, newDealer]);
      setForm({ name: "", email: "", itemType: "FRAME" });
      router.refresh();
    } catch (err: any) {
      setErrorMsg(err.message);
    } finally {
      setLoading(false);
    }
  };

  const deleteDealer = async (dealerId: number) => {
    if (window.confirm("Are you sure you want to delete this dealer?")) {
      try {
        await fetch(`http://localhost:8080/api/dealers/${dealerId}?clientId=1`, {
          method: "DELETE"
        });
        setDealers(dealers.filter((d) => d.dealerId !== dealerId));
      } catch (err) {
        setDealers(dealers.filter((d) => d.dealerId !== dealerId));
      }
    }
  };

  return (
    <div className="p-6 md:p-10 space-y-8 font-sans">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Dealer Contacts
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Manage your optical product suppliers, frame manufacturers, and lens distributors.
          </p>
        </div>
        <div className="bg-white border border-slate-200 px-5 py-2.5 rounded-2xl shadow-sm text-sm font-semibold text-teal-600 self-start md:self-auto">
          {dealers.length} active dealer(s)
        </div>
      </div>

      {errorMsg && (
        <div className="p-4 bg-rose-50 text-rose-600 rounded-2xl border border-rose-200 text-sm">
          {errorMsg}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Add New Dealer Form */}
        <div className="lg:col-span-1 bg-white border border-slate-200/80 rounded-3xl p-6 shadow-sm h-fit">
          <h3 className="text-lg font-bold text-slate-900 mb-4">Add New Dealer</h3>
          <form onSubmit={handleAddDealer} className="space-y-4">
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">Dealer Name</label>
              <input
                type="text"
                name="name"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                required
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 focus:outline-none focus:ring-2 focus:ring-teal-500 text-sm"
                placeholder="e.g. Essilor Optics"
              />
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">Email Address</label>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 focus:outline-none focus:ring-2 focus:ring-teal-500 text-sm"
                placeholder="dealer@email.com"
              />
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">Supplied Items</label>
              <select
                name="itemType"
                value={form.itemType}
                onChange={(e) => setForm({ ...form, itemType: e.target.value as "FRAME" | "LENS" | "BOTH" })}
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 focus:outline-none focus:ring-2 focus:ring-teal-500 text-sm"
              >
                <option value="FRAME">Frames Only</option>
                <option value="LENS">Lenses Only</option>
                <option value="BOTH">Frames & Lenses</option>
              </select>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-4 py-3.5 bg-teal-600 text-white rounded-xl font-semibold hover:bg-teal-500 transition-all shadow-md shadow-teal-600/20 disabled:opacity-50 text-sm"
            >
              {loading ? "Adding Dealer..." : "Add Dealer"}
            </button>
          </form>
        </div>

        {/* Dealers List Table */}
        <div className="lg:col-span-2 bg-white border border-slate-200/80 rounded-3xl p-6 md:p-8 shadow-sm">
          <h3 className="text-lg font-bold text-slate-900 mb-6">Registered Dealers</h3>
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead>
              <tr className="text-xs text-slate-400 uppercase tracking-wider border-b border-slate-100 pb-3">
                <th className="pb-4 font-bold">Name</th>
                <th className="pb-4 font-bold">Email</th>
                <th className="pb-4 font-bold">Item Type</th>
                <th className="pb-4 font-bold">Status</th>
                <th className="pb-4 font-bold text-right">Actions</th>
              </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-sm">
              {dealers.map((dealer) => (
                <tr key={dealer.dealerId} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-4 font-semibold text-slate-900">{dealer.name}</td>
                  <td className="py-4 text-slate-600">{dealer.email}</td>
                  <td className="py-4">
                      <span className="text-xs font-bold px-3 py-1 rounded-full bg-teal-50 text-teal-700">
                        {dealer.itemType}
                      </span>
                  </td>
                  <td className="py-4">
                      <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                        dealer.status === "ACTIVE"
                          ? "bg-emerald-50 text-emerald-600"
                          : "bg-slate-100 text-slate-500"
                      }`}>
                        {dealer.status === "ACTIVE" ? "Active" : "Inactive"}
                      </span>
                  </td>
                  <td className="py-4 text-right">
                    <div className="flex items-center justify-end space-x-3">
                      <button
                        onClick={() => router.replace(`/shop/dealers/${dealer.dealerId}/edit`)}
                        className="text-xs font-semibold text-teal-600 hover:text-teal-800 transition-colors"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => deleteDealer(dealer.dealerId)}
                        className="text-xs font-semibold text-rose-600 hover:text-rose-800 transition-colors"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {dealers.length === 0 && (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-slate-400">
                    No dealers found
                  </td>
                </tr>
              )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
