import LensGrid from "@/features/catalog/components/LensGrid";

/**
 * The customer's lenses page: a grid of every lens on offer.
 */
export default function LensesPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Lenses</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Browse our lens options. Filtered by type and coating, with live stock
          status.
        </p>
      </div>
      <LensGrid />
    </div>
  );
}
