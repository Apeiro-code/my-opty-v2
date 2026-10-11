import AvailabilityReport from "@/features/catalog/components/AvailabilityReport";
import FrameManager from "@/features/catalog/components/FrameManager";
import LensManager from "@/features/catalog/components/LensManager";
import LowStockAlerts from "@/features/catalog/components/LowStockAlerts";

/**
 * The shop owner's inventory page: a live availability report over the frame
 * catalogue and the lens collection, with a low-stock banner above it. A thin
 * server component; everything lives in `features/catalog`, the catalog module's
 * ownership boundary.
 */
export default function ShopInventoryPage() {
  return (
    <div className="flex flex-col gap-10 py-4">
      <section className="flex flex-col gap-6">
        <div>
          <h1 className="text-2xl font-semibold">Availability</h1>
          <p className="mt-2 max-w-prose text-sm opacity-80">
            Every active frame and lens with what is on the shelf right now,
            banded as in stock, low stock or out of stock. Stock falls as orders
            are placed and comes back when an order is rejected, so this view
            tracks the shelf live — use it to plan what to reorder. The banner
            above flags anything already low or out, and the report downloads as
            a PDF or an Excel workbook for your records.
          </p>
        </div>
        <LowStockAlerts />
        <AvailabilityReport />
      </section>

      <section className="flex flex-col gap-6">
        <div>
          <h1 className="text-2xl font-semibold">Frames</h1>
          <p className="mt-2 max-w-prose text-sm opacity-80">
            Add a frame to put it on the site, and edit one to keep its model,
            colour, material, price and stock accurate. File a frame under a
            category so browsing stays structured. A frame you no longer sell
            can be discontinued instead of deleted, so old orders still resolve.
          </p>
        </div>
        <FrameManager />
      </section>

      <section className="flex flex-col gap-6">
        <div>
          <h1 className="text-2xl font-semibold">Lenses</h1>
          <p className="mt-2 max-w-prose text-sm opacity-80">
            Add a lens to put it in the collection, and edit one to keep its
            name, correction, coating, price and stock accurate. File a lens
            under a category so browsing stays structured. A lens you no longer
            sell can be hidden by unchecking it, so old orders still resolve.
          </p>
        </div>
        <LensManager />
      </section>
    </div>
  );
}
