import PrescriptionReviewQueue from "@/features/order/components/PrescriptionReviewQueue";

/**
 * The shop owner's prescription review page. A thin server component: the queue is
 * the whole feature and lives in `features/order`, which is the order module's
 * ownership boundary.
 */
export default function ShopPrescriptionsPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Prescription review</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Check each submission for completeness. Verify it, or reject it and
          say what is missing so the customer can submit a corrected
          prescription.
        </p>
      </div>
      <PrescriptionReviewQueue />
    </div>
  );
}
