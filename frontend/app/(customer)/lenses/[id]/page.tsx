import Image from "next/image";
import Link from "next/link";
import { getStorefrontLens } from "@/features/catalog/api";

const PLACEHOLDER_IMAGE = "/images/frames/placeholder.svg";
const IMAGE_SIZES = "(min-width: 1024px) 40vw, 90vw";
const PRICE_FORMAT = new Intl.NumberFormat("en-LK", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

interface LensDetailPageProps {
  params: {
    id: string;
  };
}

export default async function LensDetailPage({ params }: LensDetailPageProps) {
  const id = Number(params.id);
  const result = await getStorefrontLens(id);

  if (!result.ok) {
    return (
      <div className="flex flex-col gap-4 py-6">
        <Link href="/lenses" className="text-sm hover:underline">
          ← Back to lenses
        </Link>
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {result.error.message}
        </p>
      </div>
    );
  }

  const lens = result.data;

  return (
    <div className="flex flex-col gap-6 py-6">
      <Link href="/lenses" className="text-sm hover:underline">
        ← Back to lenses
      </Link>
      <div className="grid grid-cols-1 gap-8 md:grid-cols-2">
        <div className="relative aspect-[4/3] w-full overflow-hidden rounded-lg border border-black/10 bg-black/5 dark:border-white/15 dark:bg-white/10">
          {lens.imageUrl ? (
            <Image
              src={lens.imageUrl}
              alt={lens.name}
              fill
              sizes={IMAGE_SIZES}
              className="object-cover"
            />
          ) : (
            <Image
              src={PLACEHOLDER_IMAGE}
              alt=""
              fill
              sizes={IMAGE_SIZES}
              unoptimized
              className="object-contain p-12 opacity-60"
            />
          )}
        </div>
        <div className="flex flex-col gap-4">
          <div>
            <h1 className="text-2xl font-semibold">{lens.name}</h1>
            <p className="mt-2 text-sm opacity-80">
              {[lens.type, lens.coating].filter(Boolean).join(" · ")}
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <p className="text-xl font-medium">
              Rs{" "}
              {PRICE_FORMAT.format(
                typeof lens.price === "number"
                  ? lens.price
                  : Number(lens.price),
              )}
            </p>
            <span
              className={
                lens.inStock
                  ? "rounded bg-green-600/10 px-2 py-0.5 text-sm text-green-700 dark:text-green-400"
                  : "rounded bg-red-600/10 px-2 py-0.5 text-sm text-red-700 dark:text-red-400"
              }
            >
              {lens.inStock ? "In stock" : "Out of stock"}
            </span>
          </div>
          {lens.description && (
            <div>
              <h2 className="text-sm font-medium">Description</h2>
              <p className="mt-1 text-sm opacity-80">{lens.description}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
