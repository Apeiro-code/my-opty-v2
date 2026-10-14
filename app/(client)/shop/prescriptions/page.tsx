"use client";

import Link from "next/link";
import { useState, useEffect } from "react";

interface PrescriptionSubmission {
  id: number;
  customerName: string;
  submittedDate: string;
  eyeDetails: string;
  status: "PENDING" | "VERIFIED" | "REJECTED";
}

/**
 * The shop owner's prescription review page.
 */
export default function ShopPrescriptionsPage() {
  const [prescriptions, setPrescriptions] = useState<PrescriptionSubmission[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fallback mock submissions when backend is offline
    const mockSubmissions: PrescriptionSubmission[] = [
      {
        id: 1,
        customerName: "Mithsen Karunarathna",
        submittedDate: "2026-10-12",
        eyeDetails: "OD: -2.00 / OS: -1.75 - Cyl: -0.50",
        status: "PENDING",
      },
      {
        id: 2,
        customerName: "Kamal Perera",
        submittedDate: "2026-10-11",
        eyeDetails: "OD: -1.50 / OS: -1.25",
        status: "PENDING",
      },
    ];

    setPrescriptions(mockSubmissions);
    setLoading(false);
  }, []);

  const handleVerify = (id: number) => {
    setPrescriptions((prev) => prev.filter((p) => p.id !== id));
    alert(`Prescription #${id} verified successfully (Mock Mode)!`);
  };

  const handleReject = (id: number) => {
    setPrescriptions((prev) => prev.filter((p) => p.id !== id));
    alert(`Prescription #${id} rejected with feedback (Mock Mode).`);
  };

  return (
    <div className="p-6 md:p-10 space-y-12 font-sans bg-slate-50 min-h-screen text-slate-800">

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Link href="/dashboard" className="hover:text-teal-600">Shop Portal</Link>
        <span>/</span>
        <span className="text-slate-900 font-semibold">Prescription Review</span>
      </div>

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-900 tracking-tight">
            Prescription Review Queue
          </h1>
          <p className="text-sm text-slate-500 mt-1 max-w-3xl">
            Check each submission for completeness. Verify it, or reject it and say what is missing so the customer can submit a corrected prescription.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="px-4 py-2 bg-teal-50 text-teal-700 border border-teal-200 rounded-2xl text-xs font-bold shadow-sm">
            📋 Verification Portal Active
          </span>
        </div>
      </div>

      {/* Main Content Container */}
      <section className="bg-white rounded-3xl p-6 md:p-8 shadow-sm border border-slate-200/80 space-y-6">
        <div className="border-b border-slate-100 pb-4">
          <h2 className="text-xl font-bold text-slate-900">Pending Submissions</h2>
          <p className="text-xs text-slate-500 mt-0.5">Review customer-uploaded scan files and numerical sphere/cylinder values.</p>
        </div>

        {loading ? (
          <div className="py-8 text-center text-slate-400 text-sm">Loading prescription queue...</div>
        ) : prescriptions.length === 0 ? (
          <div className="py-8 text-center text-slate-400 text-sm bg-slate-50 rounded-2xl">No prescriptions pending review right now.</div>
        ) : (
          <div className="space-y-4">
            {prescriptions.map((item) => (
              <div key={item.id} className="flex flex-col md:flex-row md:items-center justify-between p-5 rounded-2xl bg-slate-50 border border-slate-200/80 gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-3">
                    <span className="font-extrabold text-slate-900 text-base">Submission #{item.id}</span>
                    <span className="text-xs font-bold text-slate-600">({item.customerName})</span>
                    <span className="text-xs font-bold px-3 py-1 rounded-full bg-amber-50 text-amber-600">
                      {item.status}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 font-mono font-medium">{item.eyeDetails}</p>
                  <p className="text-[11px] text-slate-400">Submitted on: {item.submittedDate}</p>
                </div>

                <div className="flex items-center gap-3">
                  <button
                    onClick={() => handleVerify(item.id)}
                    className="px-4 py-2 bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs rounded-xl transition-all shadow-sm"
                  >
                    Verify Prescription
                  </button>
                  <button
                    onClick={() => handleReject(item.id)}
                    className="px-4 py-2 bg-rose-50 hover:bg-rose-100 text-rose-600 font-bold text-xs rounded-xl transition-all"
                  >
                    Reject &amp; Request Fix
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

    </div>
  );
}
