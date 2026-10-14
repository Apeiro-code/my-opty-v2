import FaqList from "@/features/qa/components/FaqList";
import QuestionsPanel from "@/features/qa/components/QuestionsPanel";

/**
 * The Q&amp;A page: the public FAQ, then the customer's own questions and the box
 * to ask a new one.
 *
 * <p>A thin server component. The FAQ is readable without a session, so the page
 * is not wrapped in `RequireAuth` — the ask-and-see-own half lives in
 * `QuestionsPanel`, which asks an anonymous visitor to sign in instead of
 * bouncing them away from content they came to read.
 */
export default function QuestionsPage() {
  return (
    <div className="flex flex-col gap-10 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Questions &amp; answers</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Answers the shop has given to questions like yours. Check here first —
          the answer you need may already be waiting.
        </p>
      </div>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium">Frequently asked</h2>
        <FaqList />
      </section>

      <QuestionsPanel />
    </div>
  );
}
