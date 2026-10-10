import PrescriptionForm from "@/features/order/components/PrescriptionForm";

/**
 * The prescription submission page. A thin server component: the form is the
 * whole feature and lives in `features/order`, which is the order module's
 * ownership boundary.
 */
export default function PrescriptionsPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Submit a prescription</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Enter the values from your prescription for each eye. Uploading a
          photo or scan of the paper prescription helps the shop verify it.
        </p>
      </div>
      <PrescriptionForm />
    </div>
  );
}
