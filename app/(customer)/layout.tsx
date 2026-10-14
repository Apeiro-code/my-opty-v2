"use client";

import Link from "next/link";
import Image from "next/image";
import AuthNav from "@/features/shared/components/AuthNav";
import { useState } from "react";

/**
 * Layout for everything a customer or logged-out visitor sees.
 */
export default function CustomerLayout({ children }: { children: React.ReactNode }) {
  const [email, setEmail] = useState("");

  const handleNewsletterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (email) {
      alert(`Thank you for subscribing with: ${email}`);
      setEmail("");
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans text-slate-800">
      {/* Original Customer Navigation Bar */}
      <header className="bg-white border-b border-slate-200 sticky top-0 z-50 shadow-sm">
        <nav className="mx-auto flex max-w-5xl items-center gap-6 px-6 py-4">
          <Link href="/" className="flex items-center gap-2">
            <Image
              src="/logo.png"
              alt="MyOpty Logo"
              width={120}
              height={36}
              className="object-contain"
              priority
            />
          </Link>
          <Link href="/frames" className="text-sm font-medium text-slate-600 hover:text-teal-600 transition-colors">
            Frames
          </Link>
          <Link href="/lenses" className="text-sm font-medium text-slate-600 hover:text-teal-600 transition-colors">
            Lenses
          </Link>
          <Link href="/prescriptions" className="text-sm font-medium text-slate-600 hover:text-teal-600 transition-colors">
            Prescriptions
          </Link>
          <Link href="/orders" className="text-sm font-medium text-slate-600 hover:text-teal-600 transition-colors">
            Orders
          </Link>
          <Link href="/notifications" className="text-sm font-medium text-slate-600 hover:text-teal-600 transition-colors">
            Notifications
          </Link>
          <Link href="/questions" className="text-sm font-medium text-slate-600 hover:text-teal-600 transition-colors">
            Q&amp;A
          </Link>
          <div className="ml-auto">
            <AuthNav />
          </div>
        </nav>
      </header>

      {/* Main Content Area */}
      <main className="mx-auto w-full max-w-5xl flex-1 px-6 py-8">
        {children}
      </main>

      {/* Premium Dark Footer matching reference image */}
      <footer className="bg-[#0b0f19] text-slate-300 pt-16 pb-8 border-t border-slate-800">
        <div className="mx-auto max-w-6xl px-6 grid grid-cols-1 md:grid-cols-5 gap-10 pb-12 border-b border-slate-800/80">

          {/* Brand Column */}
          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-teal-500 flex items-center justify-center text-white font-bold text-lg shadow-md">
                0
              </div>
              <span className="text-xl font-bold text-white tracking-tight">MyOpty</span>
            </div>
            <p className="text-xs text-slate-400 leading-relaxed max-w-sm">
              Elevating your vision through premium custom lenses, designer frames, and state-of-the-art clinical optometry.
            </p>
            <div className="flex items-center gap-3 pt-2">
              <div className="w-9 h-9 rounded-full bg-slate-800/80 flex items-center justify-center text-slate-300 hover:bg-teal-600 hover:text-white transition-colors cursor-pointer text-sm">
                📷
              </div>
              <div className="w-9 h-9 rounded-full bg-slate-800/80 flex items-center justify-center text-slate-300 hover:bg-teal-600 hover:text-white transition-colors cursor-pointer text-sm">
                f
              </div>
              <div className="w-9 h-9 rounded-full bg-slate-800/80 flex items-center justify-center text-slate-300 hover:bg-teal-600 hover:text-white transition-colors cursor-pointer text-sm">
                🅧
              </div>
            </div>
          </div>

          {/* Shop Collections */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-white">Shop Collections</h4>
            <ul className="space-y-2 text-xs text-slate-400">
              <li><Link href="/frames" className="hover:text-white transition-colors">Prescription Glasses</Link></li>
              <li><Link href="/frames" className="hover:text-white transition-colors">Sunglasses</Link></li>
              <li><Link href="/lenses" className="hover:text-white transition-colors">Blue Light Blockers</Link></li>
              <li><Link href="/lenses" className="hover:text-white transition-colors">Premium Custom Lenses</Link></li>
            </ul>
          </div>

          {/* Our Clinic */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-white">Our Clinic</h4>
            <ul className="space-y-2 text-xs text-slate-400">
              <li><span className="hover:text-white transition-colors cursor-pointer">Book Eye Exam</span></li>
              <li><span className="hover:text-white transition-colors cursor-pointer">Meet Our Optometrists</span></li>
              <li><span className="hover:text-white transition-colors cursor-pointer">Dry Eye Therapy</span></li>
              <li><span className="hover:text-white transition-colors cursor-pointer">Pediatric Care</span></li>
            </ul>
          </div>

          {/* Stay in Focus / Newsletter */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-white">Stay in Focus</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Join the MyOpty inner circle for eye-care tips, exclusive brand launches, and early event access.
            </p>
            <form onSubmit={handleNewsletterSubmit} className="relative mt-2">
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="your.email@domain.com"
                className="w-full px-4 py-2.5 bg-slate-900 border border-slate-700/80 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
              />
              <button
                type="submit"
                className="absolute right-2 top-1.5 px-3 py-1 bg-transparent text-teal-400 hover:text-teal-300 font-bold text-sm"
              >
                →
              </button>
            </form>
          </div>

        </div>

        {/* Footer Bottom Row */}
        <div className="mx-auto max-w-6xl px-6 pt-6 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500 gap-4">
          <p>© 2026 MyOpty Premium Eyewear & Clinic. All rights reserved.</p>
          <div className="flex items-center gap-6">
            <span className="hover:text-slate-400 cursor-pointer">Privacy Policy</span>
            <span className="hover:text-slate-400 cursor-pointer">Terms of Service</span>
            <span className="hover:text-slate-400 cursor-pointer">Accessibility Statement</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
