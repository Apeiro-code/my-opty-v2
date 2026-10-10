import OrderApprovalQueue from "@/features/order/components/OrderApprovalQueue";

/**
 * The shop owner's order approval page. A thin server component: the queue is the
 * whole feature and lives in `features/order`, which is the order module's
 * ownership boundary.
 */
export default function ShopOrdersPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Order approvals</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Approve an order to send it to production, or reject it with a reason.
          An order can only be approved once its prescription has been verified.
        </p>
      </div>
      <OrderApprovalQueue />
    </div>
  );
}
