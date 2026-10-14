import Link from "next/link";
import LoginForm from "@/features/shared/components/LoginForm";

export default async function LoginPage({
                                          searchParams,
                                        }: {
  searchParams: Promise<{ next?: string }>;
}) {
  const { next } = await searchParams;

  return (
    <main className="min-h-screen flex items-center justify-center bg-slate-950 px-4 py-12 text-slate-100">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-8 space-y-6">
        <div className="text-center space-y-2">
          <Link href="/" className="inline-block text-3xl font-extrabold tracking-tight text-blue-500 hover:text-blue-400 transition-colors">
            MyOpty
          </Link>
          <h1 className="text-2xl font-bold tracking-tight text-white">
            Sign In to Account
          </h1>
          <p className="text-sm text-slate-400">
            Access your dashboard, track prescriptions, and manage orders seamlessly.
          </p>
        </div>

        {/* Form Container Wrapper for Styling Inputs */}
        <div className="bg-slate-950/50 border border-slate-800/80 rounded-xl p-6 shadow-inner">
          <LoginForm next={next} />
        </div>

        <div className="text-center text-xs text-slate-500">
          Protected by MyOpty Secure Authentication
        </div>
      </div>
    </main>
  );
}
