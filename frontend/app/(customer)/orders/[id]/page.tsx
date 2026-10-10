import { notFound } from "next/navigation";
import CustomerOrderDetail from "@/features/order/components/CustomerOrderDetail";

/**
 * One order's tracking page. A thin server component: the detail view is the
 * whole feature and lives in `features/order`, the order module's ownership
 * boundary. A non-numeric id has no order behind it, so it 404s here rather than
 * being sent to the API.
 */
export default async function OrderDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const orderId = Number(id);
  if (!Number.isInteger(orderId) || orderId <= 0) {
    notFound();
  }

  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Order {id}</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Everything the shop has told you about this order, including when it
          is expected to be ready.
        </p>
      </div>
      <CustomerOrderDetail orderId={orderId} />
    </div>
  );
}
