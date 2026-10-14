import { notFound } from "next/navigation";
import Link from "next/link";

interface OrderDetailPageProps {
  params: Promise<{ id: string }>;
}

export default async function OrderDetailPage({
                                                params,
                                              }: OrderDetailPageProps) {
  const { id } = await params;
  const orderId = Number(id);

  if (!Number.isInteger(orderId) || orderId <= 0) {
    notFound();
  }

  // Fallback mock data for order details when backend is offline
  const orderDetail = {
    id: orderId,
    status: "PROCESSING",
    createdAt: "2026-10-10",
    expectedReady: "2026-10-20",
    totalAmount: 340.0,
    items: [
      { name: "The Avant-Garde Square Frame", type: "FRAME", price: 340.0, quantity: 1 },
      { name: "Essilor Ultra View Custom Lens", type: "LENS", price: 185.0, quantity: 1 }
    ],
    shippingAddress: "128 Madison Avenue, New York NY 10016"
  };

  return (
    <div className="p-6 md:p-10 space-y-8 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/" className="hover:text-teal-600">Home</Link>
        <span>/</span>
        <Link href="/orders" className="hover:text-teal-600">Orders</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Order #{id}</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Order Tracking #{id}
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-2xl">
            Everything the shop has told you about this order, including live status updates and expected completion timeline.
          </p>
        </div>
        <Link
          href="/orders"
          className="px-5 py-2.5 bg-white border border-slate-200 text-slate-700 hover:text-teal-600 rounded-2xl text-xs font-bold shadow-sm transition-all self-start md:self-auto"
        >
          ← Back to All Orders
        </Link>
      </div>

      {/* Order Status & Timeline Card */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200/80 space-y-2">
          <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Order Status</p>
          <span className="inline-block px-3 py-1 bg-amber-50 text-amber-600 border border-amber-200 rounded-full text-xs font-bold">
            {orderDetail.status}
          </span>
          <p className="text-xs text-slate-500 pt-1">Your custom eyewear is currently being crafted.</p>
        </div>

        <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200/80 space-y-2">
          <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Expected Ready Date</p>
          <p className="text-xl font-black text-teal-600">{orderDetail.expectedReady}</p>
          <p className="text-xs text-slate-500">Estimated delivery within 3 business days after.</p>
        </div>

        <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200/80 space-y-2">
          <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Total Balance Paid</p>
          <p className="text-xl font-black text-slate-900">${orderDetail.totalAmount.toLocaleString()}</p>
          <p className="text-xs text-emerald-600 font-semibold">Secure Payment Verified</p>
        </div>
      </div>

      {/* Ordered Items Breakdown */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <h3 className="text-lg font-bold text-slate-900">Order Items & Custom Calibration</h3>

        <div className="space-y-4">
          {orderDetail.items.map((item, index) => (
            <div key={index} className="flex items-center justify-between p-4 rounded-2xl bg-slate-50 border border-slate-100">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-xl bg-teal-100 flex items-center justify-center text-teal-700 font-bold text-lg">
                  👓
                </div>
                <div>
                  <h4 className="font-bold text-slate-900 text-sm">{item.name}</h4>
                  <p className="text-xs text-slate-500">Category: {item.type} · Quantity: {item.quantity}</p>
                </div>
              </div>
              <span className="font-black text-slate-900 text-sm">${item.price.toLocaleString()}</span>
            </div>
          ))}
        </div>

        <div className="pt-4 border-t border-slate-100">
          <p className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">Shipping Address</p>
          <p className="text-sm font-semibold text-slate-700">{orderDetail.shippingAddress}</p>
        </div>
      </div>
    </div>
  );
}
