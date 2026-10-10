import Link from "next/link";

/**
 * The customer's orders landing page.
 *
 * <p>The order history itself is a later story; today this gives the nav's
 * "Orders" link and the home page's card a real destination and a way into the
 * create flow.
 */
export default function OrdersPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Your orders</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          An order links one of your prescriptions to the frame and lens it is
          made with, so everything is processed together.
        </p>
      </div>
      <Link
        href="/orders/new"
        className="w-fit rounded-md bg-foreground px-4 py-2 text-sm font-medium text-background"
      >
        Place an order
      </Link>
    </div>
  );
}
