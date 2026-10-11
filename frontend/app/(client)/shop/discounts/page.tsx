import DiscountManager from "@/features/order/components/DiscountManager";

/**
 * The shop owner's promotions page: create a discount, edit one, and end one
 * early. A thin server component; everything lives in `features/order`, the
 * order module's ownership boundary — discounts are that module's story even
 * though they read the catalog's items.
 */
export default function ShopDiscountsPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Discounts</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Set up a promotion as a percentage off everything or a chosen set of
          frames and lenses, running between two dates. Edit one to adjust it
          while it is scheduled or running, or end it early when the offer stops
          — ending keeps the record, so the history still shows what was
          scheduled.
        </p>
      </div>
      <DiscountManager />
    </div>
  );
}
