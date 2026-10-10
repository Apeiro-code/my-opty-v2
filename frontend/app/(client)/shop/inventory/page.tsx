import FrameManager from "@/features/catalog/components/FrameManager";

/**
 * The shop owner's inventory page. For now it holds the frame catalogue: add a
 * frame so it appears on the site, and edit one so its details stay accurate. A
 * thin server component; the catalogue lives in `features/catalog`, the catalog
 * module's ownership boundary.
 */
export default function ShopInventoryPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Frames</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Add a frame to put it on the site, and edit one to keep its model,
          colour, material, price and stock accurate. A frame you no longer sell
          can be hidden instead of deleted, so old orders still resolve.
        </p>
      </div>
      <FrameManager />
    </div>
  );
}
