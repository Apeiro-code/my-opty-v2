import Link from "next/link";

/**
 * Layout for the shop owner. Separate from `(customer)` so the owner does not inherit the
 * storefront navigation, and so a guard added here cannot accidentally apply to customers.
 *
 * <p>Every route under this layout sits behind a `/shop` segment. `(client)` and `(customer)` are
 * both route groups, so neither appears in a URL — on its own that means the two groups could
 * claim the same path, which is exactly what happened with `/orders`. The segment is what keeps
 * them apart: a customer sees `/orders`, the owner sees `/shop/orders`.
 *
 * <p>Keeping the prefix on *every* owner route, rather than only on the ones that would
 * otherwise collide, means the guard added with authentication is one matcher over
 * `/shop/:path*` instead of a list of paths someone has to remember to update.
 */
export default function ClientLayout({ children }: LayoutProps<"/">) {
  return (
    <>
      <header className="border-b border-black/10 bg-black/5 dark:border-white/15 dark:bg-white/5">
        <nav className="mx-auto flex max-w-5xl items-center gap-6 px-6 py-4">
          <span className="text-lg font-semibold">MyOpty · Shop</span>
          <Link href="/shop/orders" className="text-sm hover:underline">
            Orders
          </Link>
          <Link href="/shop/prescriptions" className="text-sm hover:underline">
            Prescriptions
          </Link>
          <Link href="/shop/inventory" className="text-sm hover:underline">
            Inventory
          </Link>
          <Link href="/shop/tasks" className="text-sm hover:underline">
            Tasks
          </Link>
          <Link href="/shop/billing" className="text-sm hover:underline">
            Billing
          </Link>
        </nav>
      </header>

      <main className="mx-auto w-full max-w-5xl flex-1 px-6 py-8">
        {children}
      </main>
    </>
  );
}
