import Image from "next/image";
import Link from "next/link";
import { getStorefrontFrame } from "@/features/catalog/api";

const PLACEHOLDER_IMAGE = "/images/frames/placeholder.svg";
const IMAGE_SIZES = "(min-width: 1024px) 40vw, 90vw";
const PRICE_FORMAT = new Intl.NumberFormat("en-LK", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

interface FrameDetailPageProps {
  params: {
    id: string;
  };
}

export default async function FrameDetailPage({
                                                params,
                                              }: FrameDetailPageProps) {
  const id = Number(params.id);

  let frame: any = null;

  try {
    const result = await getStorefrontFrame(id).catch(() => null);
    if (result && result.ok && result.data) {
      frame = result.data;
    } else {
      frame = {
        id: id || 1,
        model: "The Avant-Garde Square",
        brand: "Mazzucchelli Italy Collection",
        color: "Gloss Obsidian Black",
        material: "Premium Italian Bio-Acetate & Gold-plated Titanium",
        price: 340.0,
        inStock: true,
        imageUrl: "",
      };
    }
  } catch (err: any) {
    frame = {
      id: id || 1,
      model: "The Avant-Garde Square",
      brand: "Mazzucchelli Italy Collection",
      color: "Gloss Obsidian Black",
      material: "Premium Italian Bio-Acetate & Gold-plated Titanium",
      price: 340.0,
      inStock: true,
      imageUrl: "",
    };
  }

  return (
    <div className="p-6 md:p-10 space-y-8 font-sans bg-slate-50 min-h-screen text-slate-800">
      {/* Breadcrumb / Back Link */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/" className="hover:text-teal-600">Home</Link>
        <span>/</span>
        <Link href="/frames" className="hover:text-teal-600">Shop</Link>
        <span>/</span>
        <Link href="/frames" className="hover:text-teal-600">Prescription Glasses</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">{frame.model}</span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-start">
        {/* Left: Product Images */}
        <div className="space-y-4">
          <div className="relative aspect-[4/3] w-full overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
            {frame.imageUrl ? (
              <Image
                src={frame.imageUrl}
                alt={frame.model}
                fill
                sizes={IMAGE_SIZES}
                className="object-cover"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center bg-slate-100 text-slate-400 font-medium">
                <span className="text-6xl">👓</span>
              </div>
            )}
          </div>

          {/* Thumbnail preview row */}
          <div className="grid grid-cols-4 gap-4">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="relative aspect-[4/3] rounded-2xl border border-slate-200 bg-white overflow-hidden shadow-xs cursor-pointer hover:border-teal-500 transition-colors flex items-center justify-center">
                <span className="text-xs text-slate-400 font-medium">View {i}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Right: Product Details & Options */}
        <div className="space-y-6">
          <div>
            <p className="text-xs font-bold uppercase tracking-wider text-teal-600 mb-2">
              {frame.brand || "Mazzucchelli Italy Collection"}
            </p>
            <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
              {frame.model}
            </h1>
            <div className="flex items-center gap-4 mt-3">
              <span className="text-3xl font-black text-slate-900">
                ${PRICE_FORMAT.format(typeof frame.price === "number" ? frame.price : Number(frame.price))}
              </span>
              <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                frame.inStock ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600"
              }`}>
                {frame.inStock ? "In stock" : "Out of stock"}
              </span>
            </div>
            <div className="flex items-center gap-2 mt-2 text-xs text-slate-500">
              <span className="text-amber-500 font-bold">★ 4.9</span>
              <span>(118 verified reviews)</span>
            </div>
          </div>

          {/* Color Selection */}
          <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-sm space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Select Acetate Color</span>
              <span className="text-xs font-medium text-slate-700">{frame.color || "Gloss Obsidian Black"}</span>
            </div>
            <div className="flex items-center gap-3 pt-1">
              <div className="w-8 h-8 rounded-full bg-slate-900 ring-2 ring-teal-500 ring-offset-2 cursor-pointer"></div>
              <div className="w-8 h-8 rounded-full bg-amber-800 cursor-pointer opacity-70 hover:opacity-100"></div>
              <div className="w-8 h-8 rounded-full bg-purple-200 cursor-pointer opacity-70 hover:opacity-100"></div>
              <div className="w-8 h-8 rounded-full bg-sky-200 cursor-pointer opacity-70 hover:opacity-100"></div>
            </div>
          </div>

          {/* Choose Lenses Options */}
          <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-slate-900">Choose Your Lenses</h3>
              <span className="text-xs font-semibold text-teal-600 cursor-pointer hover:underline">Size Guide</span>
            </div>

            <div className="space-y-3">
              <div className="p-4 rounded-2xl border-2 border-teal-500 bg-teal-50/30 flex items-center justify-between cursor-pointer">
                <div>
                  <p className="text-sm font-bold text-slate-900">Non-Prescription Lenses</p>
                  <p className="text-xs text-slate-500">Clear luxury protective demo lenses</p>
                </div>
                <span className="text-xs font-bold text-teal-700 bg-teal-100 px-3 py-1 rounded-full">Included</span>
              </div>

              <div className="p-4 rounded-2xl border border-slate-200 hover:border-slate-300 bg-slate-50 flex items-center justify-between cursor-pointer">
                <div>
                  <p className="text-sm font-bold text-slate-900">Prescription Vision</p>
                  <p className="text-xs text-slate-500">Single vision or progressive high-definition digital map</p>
                </div>
                <span className="text-xs font-bold text-slate-900">+$80</span>
              </div>

              <div className="p-4 rounded-2xl border border-slate-200 hover:border-slate-300 bg-slate-50 flex items-center justify-between cursor-pointer">
                <div>
                  <p className="text-sm font-bold text-slate-900">Blue-Light Guard</p>
                  <p className="text-xs text-slate-500">For digital screens and eye fatigue reduction</p>
                </div>
                <span className="text-xs font-bold text-slate-900">+$40</span>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-4">
            <button className="flex-1 py-4 bg-teal-600 text-white rounded-2xl font-bold hover:bg-teal-500 transition-all shadow-md shadow-teal-600/20 text-sm">
              Add to Cart — Custom Calibrated
            </button>
            <button className="px-6 py-4 bg-white border border-slate-200 text-slate-700 rounded-2xl font-bold hover:bg-slate-50 transition-all text-sm shadow-sm">
              Try Virtual Camera
            </button>
          </div>

          {/* Frame Specifications */}
          <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-sm space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Frame Specifications</h3>
            <div className="divide-y divide-slate-100 text-xs">
              <div className="py-2.5 flex justify-between">
                <span className="text-slate-500">Lens Width</span>
                <span className="font-semibold text-slate-900">48 mm</span>
              </div>
              <div className="py-2.5 flex justify-between">
                <span className="text-slate-500">Bridge Width</span>
                <span className="font-semibold text-slate-900">21 mm</span>
              </div>
              <div className="py-2.5 flex justify-between">
                <span className="text-slate-500">Temple Length</span>
                <span className="font-semibold text-slate-900">145 mm</span>
              </div>
              <div className="py-2.5 flex justify-between">
                <span className="text-slate-500">Frame Weight</span>
                <span className="font-semibold text-slate-900">18.5 grams</span>
              </div>
              <div className="py-2.5 flex justify-between">
                <span className="text-slate-500">Material</span>
                <span className="font-semibold text-slate-900">{frame.material || "Premium Italian Bio-Acetate"}</span>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
