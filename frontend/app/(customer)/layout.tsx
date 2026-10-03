import Link from "next/link";

/**
 * Layout for everything a customer or logged-out visitor sees.
 *
 * <p>These paths have no prefix. A customer's URL should read like a shopfront, not like a
 * dashboard, so `/orders` here is a customer's own orders and the owner's equivalent queue lives
 * at `/shop/orders`.
 *
 * <p>The shop owner's layout in `(client)` is a sibling of this one. Both are route groups, so
 * neither appears in a URL and each gets its own shell without wrapping the app in two `<body>`
 * elements.
 */
export default function CustomerLayout({ children }: LayoutProps<"/">) {
  return (
    <>
      <header className="border-b border-black/10 dark:border-white/15">
        <nav className="mx-auto flex max-w-5xl items-center gap-6 px-6 py-4">
          <Link href="/" className="text-lg font-semibold">
            MyOpty
          </Link>
          <Link href="/frames" className="text-sm hover:underline">
            Frames
          </Link>
          <Link href="/lenses" className="text-sm hover:underline">
            Lenses
          </Link>
          <Link href="/orders" className="text-sm hover:underline">
            Orders
          </Link>
          <Link href="/questions" className="text-sm hover:underline">
            Q&amp;A
          </Link>
          <Link href="/account" className="text-sm hover:underline">
            Account
          </Link>
        </nav>
      </header>

      <main className="mx-auto w-full max-w-5xl flex-1 px-6 py-8">{children}</main>

      <footer className="border-t border-black/10 py-6 text-center text-sm opacity-70">
        Flanet Opticals, Narammala
      </footer>
    </>
  );
}