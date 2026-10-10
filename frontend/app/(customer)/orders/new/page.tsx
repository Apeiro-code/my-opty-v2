import OrderForm from "@/features/order/components/OrderForm";

/**
 * The order creation page. A thin server component: the form is the whole feature
 * and lives in `features/order`, the order module's ownership boundary.
 *
 * <p>The frame and lens ids are read from the query string so a future catalog
 * page can link straight here with a chosen product. They stay editable in the
 * form, because those catalog pages do not exist yet.
 */
export default async function NewOrderPage({
  searchParams,
}: {
  searchParams: Promise<{ frameId?: string; lensId?: string }>;
}) {
  const { frameId, lensId } = await searchParams;

  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Place an order</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Choose the prescription the order is built from, the order type, and
          the frame and lens. Progressive orders are routed to the shop&apos;s
          progressive workflow.
        </p>
      </div>
      <OrderForm initialFrameId={frameId} initialLensId={lensId} />
    </div>
  );
}
