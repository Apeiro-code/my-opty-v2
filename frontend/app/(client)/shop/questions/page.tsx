import UnansweredQuestionsQueue from "@/features/qa/components/UnansweredQuestionsQueue";

/**
 * The shop owner's Q&amp;A page: every customer question still waiting for an
 * answer, with the box to reply. A thin server component; the queue lives in
 * `features/qa`, the shared module's ownership boundary.
 */
export default function ShopQuestionsPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Customer questions</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Questions customers have asked that nobody has answered yet, oldest
          first. Posting an answer emails the customer automatically.
        </p>
      </div>
      <UnansweredQuestionsQueue />
    </div>
  );
}
