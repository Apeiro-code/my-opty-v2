import FrameGrid from "@/features/catalog/components/FrameGrid";

/**
 * The customer's frames page: the shop wall, a grid of every frame on sale, so a
 * visitor can browse available styles. A thin server component: the grid is the
 * whole feature and lives in `features/catalog`, the catalog module's boundary.
 */
export default function FramesPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Frames</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Browse the frames currently on the wall. Pick one to build your order
          around, or send us your prescription and ask us for a recommendation.
        </p>
      </div>
      <FrameGrid />
    </div>
  );
}
