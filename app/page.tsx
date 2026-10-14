import Link from "next/link";

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-950 font-sans text-slate-900 dark:text-slate-100">
      {/* Navigation */}
      <header className="w-full max-w-7xl mx-auto px-6 py-6 flex items-center justify-between">
        <div className="text-2xl font-extrabold tracking-tight text-blue-600 dark:text-blue-400">
          MyOpty
        </div>
        <div className="flex items-center gap-4">
          <Link
            href="/login"
            className="px-5 py-2.5 rounded-full text-sm font-semibold text-slate-700 dark:text-slate-200 hover:bg-slate-200/50 dark:hover:bg-slate-800 transition-colors"
          >
            Sign In
          </Link>
          <Link
            href="/dashboard"
            className="px-5 py-2.5 rounded-full text-sm font-semibold bg-blue-600 text-white hover:bg-blue-700 transition-colors shadow-sm"
          >
            Dashboard
          </Link>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-1 flex items-center justify-center px-6 py-16">
        <div className="max-w-3xl text-center space-y-6">
          <span className="inline-block px-4 py-1.5 rounded-full text-xs font-semibold bg-blue-50 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400 border border-blue-100 dark:border-blue-900">
            Next-Gen Optical Shop & Inventory Platform
          </span>
          <h1 className="text-4xl md:text-6xl font-extrabold tracking-tight">
            Manage Your Optical Store with <span className="text-blue-600 dark:text-blue-400">Precision</span>
          </h1>
          <p className="text-lg text-slate-600 dark:text-slate-400 max-w-2xl mx-auto leading-relaxed">
            Streamline prescriptions, catalog inventory, track orders, and handle invoices effortlessly in a unified workspace.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
            <Link
              href="/dashboard"
              className="w-full sm:w-auto px-8 py-3.5 rounded-full font-semibold bg-blue-600 text-white hover:bg-blue-700 transition-all shadow-md hover:shadow-lg"
            >
              Get Started
            </Link>
            <Link
              href="/login"
              className="w-full sm:w-auto px-8 py-3.5 rounded-full font-semibold border border-slate-300 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-900 transition-all"
            >
              Sign In to Account
            </Link>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="w-full py-6 text-center text-xs text-slate-400 dark:text-slate-600 border-t border-slate-100 dark:border-slate-900">
        &copy; 2026 MyOpty. All rights reserved.
      </footer>
    </div>
  );
}
