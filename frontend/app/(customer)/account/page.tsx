import RequireAuth from "@/lib/auth/RequireAuth";
import AccountPanel from "@/features/shared/components/AccountPanel";

/**
 * The customer's account page. Its content is the signed-in user, so it is
 * wrapped in `RequireAuth` to send a signed-out visitor to the login page first.
 *
 * <p>A thin server component: the guard and the panel live in `lib/auth` and
 * `features/shared`, the shared module's boundary.
 */
export default function AccountPage() {
  return (
    <div className="flex flex-col gap-6 py-4">
      <div>
        <h1 className="text-2xl font-semibold">Your account</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          The details you signed in with. Use Log out in the header to end your
          session on this device.
        </p>
      </div>
      <RequireAuth>
        <AccountPanel />
      </RequireAuth>
    </div>
  );
}
