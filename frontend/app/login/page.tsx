import Link from "next/link";
import LoginForm from "@/features/shared/components/LoginForm";

/**
 * The sign-in page. Deliberately outside both route groups: a visitor arrives
 * here before they are a customer or a shop owner, so it inherits neither the
 * storefront nor the shop navigation. After a successful login the form sends
 * each role to its own side.
 *
 * <p>`next` is the page the visitor was trying to reach, handed over by the
 * `RequireAuth` guard so login can return them there.
 */
export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ next?: string }>;
}) {
  const { next } = await searchParams;

  return (
    <main className="mx-auto flex w-full max-w-5xl flex-col gap-6 px-6 py-16">
      <Link href="/" className="text-lg font-semibold">
        MyOpty
      </Link>
      <div>
        <h1 className="text-2xl font-semibold">Sign in</h1>
        <p className="mt-2 max-w-prose text-sm opacity-80">
          Access your account, keep track of your prescriptions and orders, and
          follow them through to collection.
        </p>
      </div>
      <LoginForm next={next} />
    </main>
  );
}
