import OrderForm from "@/features/order/components/OrderForm";
import Link from "next/link";

interface NewOrderPageProps {
  searchParams: Promise<{ frameId?: string; lensId?: string }>;
}

/**
 * The order creation page. A thin server component.
 */
export default async function NewOrderPage({
                                             searchParams,
                                           }: NewOrderPageProps) {
  const { frameId, lensId } = await searchParams;

  return (
    <div className="p-6 md:p-10 space-y-8 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/" className="hover:text-teal-600">Home</Link>
        <span>/</span>
        <Link href="/orders" className="hover:text-teal-600">Orders</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Place New Order</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Place Custom Eyewear Order
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-2xl">
            Choose the prescription the order is built from, the order type, and the frame and lens. Progressive orders are routed to the shop&apos;s progressive workflow.
          </p>
        </div>
        <Link
          href="/orders"
          className="px-5 py-2.5 bg-white border border-slate-200 text-slate-700 hover:text-teal-600 rounded-2xl text-xs font-bold shadow-sm transition-all self-start md:self-auto"
        >
          ← Back to Orders
        </Link>
      </div>

      {/* Form Container */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <OrderForm initialFrameId={frameId} initialLensId={lensId} />
      </div>
    </div>
  );
}
