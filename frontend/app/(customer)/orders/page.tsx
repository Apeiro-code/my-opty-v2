import Link from "next/link";
import CustomerOrderList from "@/features/order/components/CustomerOrderList";

/**
 * The customer's orders page: their own orders, newest first, each showing the
 * status and the estimated receive date, and each linking to its detail page.
 *
 * <p>A thin server component: the list is the whole feature and lives in
 * `features/order`, the order module's ownership boundary.
 */
export default function OrdersPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Your orders</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          An order links one of your prescriptions to the frame and lens it is
          made with, so everything is processed together. Open one to see what
          the shop has told you about it.
        </p>
      </div>
      <Link
        href="/orders/new"
        className="w-fit rounded-md bg-foreground px-4 py-2 text-sm font-medium text-background"
      >
        Place an order
      </Link>
      <CustomerOrderList />
    </div>
  );
}
