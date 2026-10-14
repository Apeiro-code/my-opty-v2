"use client";

import Link from "next/link";
import { useState, useEffect } from "react";

interface CustomerQuestion {
  id: number;
  customerName: string;
  questionText: string;
  askedDate: string;
}

/**
 * The shop owner's Q&A page: every customer question still waiting for an answer.
 */
export default function ShopQuestionsPage() {
  const [questions, setQuestions] = useState<CustomerQuestion[]>([]);
  const [loading, setLoading] = useState(true);
  const [replyText, setReplyText] = useState<{ [key: number]: string }>({});

  useEffect(() => {
    // Fallback mock pending questions when backend is offline
    const mockQuestions: CustomerQuestion[] = [
      {
        id: 1,
        customerName: "Mithsen Karunarathna",
        questionText: "Do you offer progressive lenses with anti-blue light coating for custom frames?",
        askedDate: "2026-10-12",
      },
      {
        id: 2,
        customerName: "Kamal Perera",
        questionText: "How long does frame repair and adjustment take at the Narammala branch?",
        askedDate: "2026-10-10",
      },
    ];

    setQuestions(mockQuestions);
    setLoading(false);
  }, []);

  const handleReplySubmit = (id: number) => {
    const answer = replyText[id];
    if (!answer || answer.trim() === "") {
      alert("Please write a reply before posting.");
      return;
    }
    setQuestions((prev) => prev.filter((q) => q.id !== id));
    alert(`Reply posted successfully and emailed to the customer (Mock Mode)!`);
  };

  return (
    <div className="p-6 md:p-10 space-y-12 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/shop/dashboard" className="hover:text-teal-600">Shop Portal</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Customer Questions</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Customer Inquiries Queue
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-3xl">
            Questions customers have asked that nobody has answered yet, oldest first. Posting an answer emails the customer automatically.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="px-4 py-2 bg-teal-50 text-teal-700 border border-teal-200 rounded-2xl text-xs font-bold shadow-sm">
            ❓ Support Portal Active
          </span>
        </div>
      </div>

      {/* Main Content Container */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h2 className="text-xl font-bold text-slate-900">Unanswered Questions</h2>
          <p className="text-xs text-slate-500 mt-0.5">Provide prompt answers to build trust and assist customers with their optical orders.</p>
        </div>

        {loading ? (
          <div className="py-8 text-center text-slate-400 text-sm">Loading customer questions...</div>
        ) : questions.length === 0 ? (
          <div className="py-8 text-center text-slate-400 text-sm bg-slate-50 rounded-2xl">All customer questions have been answered! Excellent work.</div>
        ) : (
          <div className="space-y-6">
            {questions.map((item) => (
              <div key={item.id} className="p-5 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-4">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-2">
                  <div className="flex items-center gap-3">
                    <span className="font-extrabold text-slate-900 text-sm">Question #{item.id}</span>
                    <span className="text-xs font-bold text-teal-700 bg-teal-50 px-3 py-1 rounded-full">
                      {item.customerName}
                    </span>
                  </div>
                  <span className="text-[11px] text-slate-400">Asked on: {item.askedDate}</span>
                </div>

                <p className="text-sm font-semibold text-slate-800 bg-white p-4 rounded-xl border border-slate-200/60">
                  &ldquo;{item.questionText}&rdquo;
                </p>

                <div className="space-y-3 pt-2">
                  <textarea
                    rows={2}
                    placeholder="Type your professional shop reply here..."
                    value={replyText[item.id] || ""}
                    onChange={(e) => setReplyText({ ...replyText, [item.id]: e.target.value })}
                    className="w-full p-3 bg-white border border-slate-200 rounded-xl text-xs text-slate-800 focus:outline-none focus:border-teal-500"
                  />
                  <div className="flex justify-end">
                    <button
                      onClick={() => handleReplySubmit(item.id)}
                      className="px-5 py-2.5 bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs rounded-xl transition-all shadow-sm"
                    >
                      Post Answer &amp; Email Customer →
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

    </div>
  );
}
