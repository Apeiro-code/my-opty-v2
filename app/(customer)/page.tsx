import Link from "next/link";
import NewArrivals from "@/features/catalog/components/NewArrivals";

/**
 * The public landing page.
 */
export default function HomePage() {
  return (
    <div className="p-6 md:p-10 space-y-12 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Hero Section */}
      <section className="bg-white rounded-3xl p-8 md:p-12 shadow-sm border border-slate-200/80 space-y-6 relative overflow-hidden">
        <div className="max-w-2xl space-y-4">
          <span className="px-3 py-1 bg-teal-50 text-teal-700 rounded-full text-xs font-bold border border-teal-200 inline-block">
            Flanet Opticals, Narammala
          </span>
          <h1 className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight leading-tight">
            Elevating Your Vision with Precision Eyewear
          </h1>
          <p className="text-base text-slate-600 leading-relaxed">
            Frames and lenses from Flanet Opticals, Narammala. Browse the collection, send us your prescription, and follow your order through to collection.
          </p>
          <div className="flex flex-wrap items-center gap-4 pt-2">
            <Link
              href="/frames"
              className="px-6 py-3.5 bg-teal-600 text-white rounded-2xl font-bold hover:bg-teal-500 transition-all shadow-md shadow-teal-600/20 text-sm text-center"
            >
              Browse frames
            </Link>
            <Link
              href="/prescriptions"
              className="px-6 py-3.5 bg-white border border-slate-200 text-slate-700 hover:text-teal-600 rounded-2xl font-bold transition-all text-sm shadow-sm text-center"
            >
              Submit a prescription
            </Link>
          </div>
        </div>
      </section>

      {/* Quick Navigation Feature Cards */}
      <section className="grid gap-6 sm:grid-cols-3">
        {[
          {
            href: "/frames",
            title: "Frames",
            body: "Filter by category, colour, material and price.",
            icon: "👓",
          },
          {
            href: "/lenses",
            title: "Lenses",
            body: "Single vision, bifocal and progressive, with coatings.",
            icon: "🔍",
          },
          {
            href: "/orders",
            title: "Your orders",
            body: "See where each order has got to and when it is ready.",
            icon: "📦",
          },
        ].map((card) => (
          <Link
            key={card.href}
            href={card.href}
            className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200/80 hover:border-teal-500 transition-all space-y-3 group block"
          >
            <div className="w-10 h-10 rounded-2xl bg-teal-50 flex items-center justify-center text-lg group-hover:bg-teal-600 group-hover:text-white transition-colors">
              {card.icon}
            </div>
            <h2 className="font-bold text-slate-900 text-lg">{card.title}</h2>
            <p className="text-xs text-slate-500 leading-relaxed">{card.body}</p>
          </Link>
        ))}
      </section>

      {/* New Arrivals Section */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <h2 className="text-xl font-bold text-slate-900">New Arrivals &amp; Featured Masterpieces</h2>
        <NewArrivals />
      </section>

    </div>
  );
}
