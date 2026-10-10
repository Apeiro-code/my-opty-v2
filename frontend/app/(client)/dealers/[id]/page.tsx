"use client";

import { useState, useEffect, useParams } from "react";
import { useRouter } from "next/navigation";
import { dealerApi } from "@/features/workflow/api";
import { dealerTypes } from "@/types/workflow";

interface DealerContact {
  dealerId: number;
  name: string;
  email: string;
  itemType: "FRAME" | "LENS" | "BOTH";
  status: "ACTIVE" | "INACTIVE";
}

interface DealerFormValues {
  name: string;
  email: string;
  itemType: "FRAME" | "LENS" | "BOTH";
}

export default function DealerEditPage() {
  const router = useRouter();
  const [form, setForm] = useState<DealerFormValues>({
    name: "",
    email: "",
    itemType: "FRAME",
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [dealer, setDealer] = useState<DealerContact | null>(null);

  // Fetch dealer by ID
  useEffect(() => {
    if (id) {
      const loadDealer = async () => {
        setLoading(true);
        try {
          const result = await dealerApi.getDealerByEmail(Number(id));
          if (result.success && result.data) {
            setDealer(result.data);
            setForm({
              name: result.data.name,
              email: result.data.email,
              itemType: result.data.itemType,
            });
          } else {
            setError(result.error?.message || "Failed to load dealer");
          }
        } catch (err: any) {
          setError(err.message || "Failed to load dealer");
        } finally {
          setLoading(false);
        }
      };

      loadDealer();
    }
  }, [id]);

  // Update dealer
  const handleUpdateDealer = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const result = await dealerApi.updateDealer(Number(id), {
        name: form.name,
        email: form.email,
        itemType: form.itemType,
      });
      if (result.success) {
        setDealer(result.data);
        setForm({
          name: "",
          email: "",
          itemType: "FRAME",
        });
        router.push("/client/dealers", { replace: true });
      } else {
        setError(result.error?.message || "Failed to update dealer");
      }
    } catch (err: any) {
      setError(err.message || "Failed to update dealer");
    } finally {
      setLoading(false);
    }
  };

  if (!id) {
    return null;
  }

  if (loading) {
    return <div className="min-h-80 flex items-center justify-center">Loading dealer...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-600">Error: {error}</div>;
  }

  return (
    <div className="p-6">
      <h2 className="text-2xl font-bold mb-4">Edit Dealer</h2>
      <form onSubmit={handleUpdateDealer} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Name
          </label>
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
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Email
          </label>
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
            className="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700 transition-colors disabled:opacity-50"
          >
            Update Dealer
          </button>
        </form>
      </div>
    </div>
  );
}