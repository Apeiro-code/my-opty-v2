import Link from "next/link";
import Image from "next/image";
import AuthNav from "@/features/shared/components/AuthNav";

export default function ClientLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-slate-100 flex font-sans text-slate-800 w-full">
      {/* Left Sidebar - Professional Admin Theme */}
      <aside className="w-64 bg-slate-900 text-slate-300 md:flex flex-col justify-between hidden border-r border-slate-800 sticky top-0 h-screen">
        <div>
          {/* Logo and Brand Header */}
          <div className="p-6 border-b border-slate-800/60 flex items-center gap-3">
            <Link href="/dashboard" className="flex items-center gap-3 hover:opacity-90 transition-opacity">
              <Image
                src="/logo.png"
                alt="MyOpty Logo"
                width={120}
                height={36}
                className="object-contain"
                priority
              />
            </Link>
          </div>

          {/* Navigation Menu */}
          <nav className="p-4 space-y-1.5 text-sm font-medium">
            <Link href="/dashboard" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              📊 Dashboard
            </Link>
            <Link href="/shop/orders" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              🛍️ Orders
            </Link>
            <Link href="/shop/prescriptions" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              📝 Prescriptions
            </Link>
            <Link href="/shop/questions" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              ❓ Questions
            </Link>
            <Link href="/shop/inventory" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              📦 Inventory
            </Link>
            <Link href="/dealers" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              🤝 Dealers
            </Link>
            <Link href="/shop/discounts" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              🏷️ Discounts
            </Link>
            <Link href="/shop/tasks" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              📋 Tasks
            </Link>
            <Link href="/shop/billing" className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-slate-800 text-slate-400 hover:text-white transition-colors">
              💳 Billing
            </Link>
          </nav>
        </div>

        {/* User Auth Footer & Portal Switcher */}
        <div className="p-4 border-t border-slate-800/60 space-y-3">
          {/* Switch to Customer Portal Button */}
          <Link
            href="/"
            className="flex items-center justify-center gap-2 w-full py-2.5 px-3 bg-teal-600 hover:bg-teal-500 text-white font-semibold text-xs rounded-xl transition-all shadow-md"
          >
            🛍️ Switch to Customer View
          </Link>
          <AuthNav />
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 bg-slate-50 min-h-screen overflow-y-auto">
        {children}
      </main>
    </div>
  );
}
