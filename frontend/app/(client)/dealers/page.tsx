import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
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

export default function DealerContactsPage() {
  const navigate = useNavigate();
  const [dealers, setDealers] = useState<DealerContact[]>([]);
  const [form, setForm] = useState<DealerFormValues>({
    name: "",
    email: "",
    itemType: "FRAME",
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Fetch dealers from API
  useEffect(() => {
    const loadDealers = async () => {
      setLoading(true);
      try {
        const result = await dealerApi.getAllActive();
        if (result.success && result.data) {
          setDealers(result.data);
        } else {
          setError(result.error?.message || "Failed to load dealers");
        }
      } catch (err: any) {
        setError(err.message || "Failed to load dealers");
      } finally {
        setLoading(false);
      }
    };

    loadDealers();
  }, []);

  // Add new dealer
  const handleAddDealer = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const result = await dealerApi.createDealer({
        name: form.name,
        email: form.email,
        itemType: form.itemType,
      });
      if (result.success) {
        setDealers([...dealers, result.data]);
        setForm({
          name: "",
          email: "",
          itemType: "FRAME",
        });
        navigate("/client/dealers", { replace: true });
      } else {
        setError(result.error?.message || "Failed to create dealer");
      }
    } catch (err: any) {
      setError(err.message || "Failed to create dealer");
    } finally {
      setLoading(false);
    }
  };

  // Edit dealer
  const handleEditDealer = async (dealerId: number, e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const result = await dealerApi.updateDealer(dealerId, {
        name: form.name,
        email: form.email,
        itemType: form.itemType,
      });
      if (result.success) {
        // Update dealer in local state
        setDealers(
          dealers.map((d) => (d.dealerId === dealerId ? result.data : d))
        );
        setForm({
          name: "",
          email: "",
          itemType: "FRAME",
        });
        navigate("/client/dealers", { replace: true });
      } else {
        setError(result.error?.message || "Failed to update dealer");
      }
    } catch (err: any) {
      setError(err.message || "Failed to update dealer");
    } finally {
      setLoading(false);
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

      {/* Dealer Form */}
      <div className="bg-white rounded p-4 shadow-sm mb-6">
        <h3 className="text-lg font-medium mb-3">
          {form.dealerId ? "Edit Dealer" : "Add New Dealer"}
        </h3>
        <form onSubmit={handleAddDealer} className="space-y-4">
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
              {form.dealerId ? "Update Dealer" : "Add Dealer"}
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
                      onClick={() => navigate(`/client/dealers/${dealer.dealerId}/edit`, { replace: true })}
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
                <td colSpan="5" className="p-4 text-center text-gray-500">
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

interface DealerFormValues {
  name: string;
  email: string;
  itemType: "FRAME" | "LENS" | "BOTH";
}

// Delete dealer function
function deleteDealer(dealerId: number) {
  if (confirm("Are you sure you want to delete this dealer?")) {
    dealerApi.deleteDealer(dealerId).then(() => {
      setDealers(dealers.filter((d) => d.dealerId !== dealerId));
    });
  }
}