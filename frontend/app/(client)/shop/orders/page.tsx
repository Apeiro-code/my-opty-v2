import OrderApprovalQueue from "@/features/order/components/OrderApprovalQueue";
import OrderFulfilmentQueue from "@/features/order/components/OrderFulfilmentQueue";

/**
 * The shop owner's order pages: the pending queue with its approve/reject
 * decision, and the production line that follows. A thin server component; both
 * queues live in `features/order`, the order module's ownership boundary.
 */
export default function ShopOrdersPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Order approvals</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Approve an order to send it to production, or reject it with a reason.
          An order can only be approved once its prescription has been verified.
          Approval quotes an estimated receive date from the order type&apos;s
          lab lead time.
        </p>
      </div>
      <OrderApprovalQueue />

      <div className="mt-4">
        <h2 className="text-xl font-semibold">Production</h2>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Move each approved order through processing, ready and dispatched.
          Each move emails the customer automatically.
        </p>
      </div>
      <OrderFulfilmentQueue />
    </div>
  );
}
