import Link from "next/link";
import RegisterForm from "@/features/shared/components/RegisterForm";

/**
 * The customer's registration page: a form that creates a new account and
 * then redirects the user to the login page or dashboard seamlessly.
 *
 * <p>Uses the same styling and structure as the existing LoginForm for visual
 * consistency across the authentication flow.</p>
 */
export default function RegisterPage() {
  return (
    <main className="min-h-screen flex items-center justify-center bg-slate-950 px-4 py-12 text-slate-100">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-8 space-y-6">
        <div className="text-center space-y-2">
          <h1 className="text-2xl font-bold tracking-tight text-white">
            Create Account
          </h1>
          <p className="text-sm text-slate-400">
            Join the MyOpty community and start managing your optical store today.
          </p>
        </div>

        {/* Form Container Wrapper for Styling Inputs */}
        <div className="bg-slate-950/50 border border-slate-800/80 rounded-xl p-6 shadow-inner">
          <RegisterForm next={undefined} />
        </div>

        {/* Footer Links */}
        <div className="text-center text-xs text-slate-500">
          <Link href="/login" className="hover:text-slate-400">
            Already have an account? Sign in
          </Link>
        </div>
      </div>
    </main>
  );
}