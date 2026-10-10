import FrameManager from "@/features/catalog/components/FrameManager";
import LensManager from "@/features/catalog/components/LensManager";

/**
 * The shop owner's inventory page: the frame catalogue and the lens collection.
 * A thin server component; both catalogues live in `features/catalog`, the
 * catalog module's ownership boundary.
 */
export default function ShopInventoryPage() {
  return (
    <div className="flex flex-col gap-10 py-4">
      <section className="flex flex-col gap-6">
        <div>
          <h1 className="text-2xl font-semibold">Frames</h1>
          <p className="mt-2 max-w-prose text-sm opacity-80">
            Add a frame to put it on the site, and edit one to keep its model,
            colour, material, price and stock accurate. A frame you no longer
            sell can be discontinued instead of deleted, so old orders still
            resolve.
          </p>
        </div>
        <FrameManager />
      </section>

      <section className="flex flex-col gap-6">
        <div>
          <h1 className="text-2xl font-semibold">Lenses</h1>
          <p className="mt-2 max-w-prose text-sm opacity-80">
            Add a lens to put it in the collection: its name, the correction it
            provides, any coating, price and stock. A lens you no longer sell
            can be hidden instead of deleted, so old orders still resolve.
          </p>
        </div>
        <LensManager />
      </section>
    </div>
  );
}
