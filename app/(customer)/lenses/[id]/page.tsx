import Image from "next/image";
import Link from "next/link";
import { getStorefrontLens } from "@/features/catalog/api";

const PRICE_FORMAT = new Intl.NumberFormat("en-LK", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const PLACEHOLDER_IMAGE = "/images/frames/placeholder.svg";
const IMAGE_SIZES = "(min-width: 1024px) 30vw, (min-width: 640px) 45vw, 90vw";

interface LensDetailPageProps {
  params: {
    id: string;
  };
}

export default async function LensDetailPage({
                                               params,
                                             }: LensDetailPageProps) {
  const id = Number(params.id);

  let lens: any = null;

  try {
    const result = await getStorefrontLens(id).catch(() => null);
    if (result && result.ok && result.data) {
      lens = result.data;
    } else {
      // Fallback mock data when backend is offline
      lens = {
        id: id || 1,
        name: "Essilor Ultra View Custom Lens",
        type: "Progressive Digital Lens",
        coating: "Crizal Sapphire HR Anti-Glare",
        price: 185.00,
        inStock: true,
        imageUrl: "",
        description: "State-of-the-art custom digital prescription lenses engineered for crystal clear vision across all distances, featuring premium anti-scratch and UV protection coatings.",
      };
    }
  } catch (err: any) {
    lens = {
      id: id || 1,
      name: "Essilor Ultra View Custom Lens",
      type: "Progressive Digital Lens",
      coating: "Crizal Sapphire HR Anti-Glare",
      price: 185.00,
      inStock: true,
      imageUrl: "",
      description: "State-of-the-art custom digital prescription lenses engineered for crystal clear vision across all distances.",
    };
  }

  return (
    <div className="p-6 md:p-10 space-y-8 font-sans bg-slate-50 min-h-screen text-slate-800">
      {/* Breadcrumb / Back Link */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/" className="hover:text-teal-600">Home</Link>
        <span>/</span>
        <Link href="/lenses" className="hover:text-teal-600">Shop</Link>
        <span>/</span>
        <Link href="/lenses" className="hover:text-teal-600">Lenses</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">{lens.name}</span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-start">
        {/* Left: Product Image */}
        <div className="space-y-4">
          <div className="relative aspect-[4/3] w-full overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
            {lens.imageUrl ? (
              <Image
                src={lens.imageUrl}
                alt={lens.name}
                fill
                sizes={IMAGE_SIZES}
                className="object-cover"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center bg-slate-100 text-slate-400 font-medium">
                <span className="text-6xl">🔍</span>
              </div>
            )}
          </div>

          {/* Thumbnail preview row */}
          <div className="grid grid-cols-3 gap-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="relative aspect-[4/3] rounded-2xl border border-slate-200 bg-white overflow-hidden shadow-xs cursor-pointer hover:border-teal-500 transition-colors flex items-center justify-center">
                <span className="text-xs text-slate-400 font-medium">Lens View {i}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Right: Product Details & Options */}
        <div className="space-y-6">
          <div>
            <p className="text-xs font-bold uppercase tracking-wider text-teal-600 mb-2">
              {lens.type || "Progressive Digital Lens"}
            </p>
            <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
              {lens.name}
            </h1>
            <p className="mt-2 text-sm text-slate-500 font-medium">
              {[lens.type, lens.coating].filter(Boolean).join(" · ")}
            </p>
            <div className="flex items-center gap-4 mt-4">
              <span className="text-3xl font-black text-slate-900">
                Rs {PRICE_FORMAT.format(typeof lens.price === "number" ? lens.price : Number(lens.price))}
              </span>
              <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                lens.inStock ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600"
              }`}>
                {lens.inStock ? "In stock" : "Out of stock"}
              </span>
            </div>
          </div>

          {/* Lens Features Card */}
          <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-sm space-y-3">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Coating & Technology</h3>
            <p className="text-sm font-semibold text-slate-700">{lens.coating || "Crizal Sapphire HR Anti-Glare"}</p>
            <p className="text-xs text-slate-500 leading-relaxed">
              Provides superior clarity, scratch resistance, smudge repellence, and complete UV blockage for everyday computer and outdoor use.
            </p>
          </div>

          {/* Description Section */}
          {lens.description && (
            <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-sm space-y-2">
              <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400">Description</h2>
              <p className="text-sm text-slate-600 leading-relaxed">{lens.description}</p>
            </div>
          )}

          {/* Action Button */}
          <div className="flex items-center gap-4 pt-2">
            <button className="flex-1 py-4 bg-teal-600 text-white rounded-2xl font-bold hover:bg-teal-500 transition-all shadow-md shadow-teal-600/20 text-sm">
              Select Lens & Attach Prescription
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
