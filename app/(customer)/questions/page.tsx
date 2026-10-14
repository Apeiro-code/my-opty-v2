import FaqList from "@/features/qa/components/FaqList";
import QuestionsPanel from "@/features/qa/components/QuestionsPanel";
import Link from "next/link";

/**
 * The Q&A page: the public FAQ, then the customer's own questions.
 */
export default function QuestionsPage() {
  return (
    <div className="p-6 md:p-10 space-y-10 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/" className="hover:text-teal-600">Home</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Q&amp;A</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Questions &amp; Answers
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-2xl">
            Answers the shop has given to questions like yours. Check here first — the answer you need may already be waiting.
          </p>
        </div>
      </div>

      {/* Frequently Asked Questions Card Container */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h2 className="text-xl font-bold text-slate-900">Frequently Asked</h2>
          <p className="text-xs text-slate-500 mt-0.5">Browse common queries regarding custom eyewear and prescriptions.</p>
        </div>
        <div className="space-y-4">
          <FaqList />
        </div>
      </section>

      {/* Questions Panel Card Container */}
      <div className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <QuestionsPanel />
      </div>
    </div>
  );
}
