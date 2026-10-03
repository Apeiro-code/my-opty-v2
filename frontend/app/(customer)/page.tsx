import Link from "next/link";

/**
 * The public landing page.
 *
 * <p>Deliberately static. A page that fetches data belongs in `features/<module>/` with the
 * fetching itself behind a thin server component, so nothing here grows a data layer.
 */
export default function HomePage() {
  return (
    <div className="flex flex-col gap-10 py-8">
      <section className="flex flex-col gap-4">
        <h1 className="text-4xl font-semibold tracking-tight">MyOpty</h1>
        <p className="max-w-prose text-lg opacity-80">
          Frames and lenses from Flanet Opticals, Narammala. Browse the collection, send us
          your prescription, and follow your order through to collection.
        </p>
        <div className="flex gap-3">
          <Link
            href="/frames"
            className="rounded-md bg-foreground px-4 py-2 text-sm font-medium text-background"
          >
            Browse frames
          </Link>
          <Link
            href="/prescriptions"
            className="rounded-md border border-black/20 px-4 py-2 text-sm font-medium dark:border-white/25"
          >
            Submit a prescription
          </Link>
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-3">
        {[
          {
            href: "/frames",
            title: "Frames",
            body: "Filter by category, colour, material and price.",
          },
          {
            href: "/lenses",
            title: "Lenses",
            body: "Single vision, bifocal and progressive, with coatings.",
          },
          {
            href: "/orders",
            title: "Your orders",
            body: "See where each order has got to and when it is ready.",
          },
        ].map((card) => (
          <Link
            key={card.href}
            href={card.href}
            className="rounded-lg border border-black/10 p-4 transition hover:border-black/30 dark:border-white/15 dark:hover:border-white/30"
          >
            <h2 className="font-medium">{card.title}</h2>
            <p className="mt-1 text-sm opacity-75">{card.body}</p>
          </Link>
        ))}
      </section>
    </div>
  );
}