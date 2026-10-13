import { apiGet, apiPost, apiPut } from "@/lib/api/client";
import type { ApiResult } from "@/types/api";
import type { AuthenticatedUser, UpdateProfileInput } from "./types";

/**
 * The auth surface, as three calls against the shared module's endpoints.
 *
 * <p>They go through `lib/api/client.ts` like every other feature, so the base
 * URL, the `credentials: "include"` that carries the session cookie, and the
 * envelope unwrapping are the same here as everywhere else. The endpoints are
 * public for login and session-scoped for `/me` and `/logout`; the controller's
 * `AuthResponse` is the standard envelope, so the wrapper hands back the
 * unwrapped `AuthenticatedUser`.
 */

/** Exchanges an email/password pair for a session cookie. */
export function login(
  email: string,
  password: string,
): Promise<ApiResult<AuthenticatedUser>> {
  return apiPost<AuthenticatedUser>("/api/auth/login", { email, password });
}

/**
 * Who the current session says the caller is. An anonymous caller gets
 * `UNAUTHENTICATED` (401); that is an expected answer, not an error to show.
 */
export function getCurrentUser(): Promise<ApiResult<AuthenticatedUser>> {
  return apiGet<AuthenticatedUser>("/api/auth/me");
}

/** Ends the session server-side and clears the session cookie. */
export function logout(): Promise<ApiResult<Record<string, never>>> {
  return apiPost<Record<string, never>>("/api/auth/logout", {});
}

/**
 * Saves the customer's editable profile fields and returns the updated identity,
 * in the same shape `getCurrentUser` does. The response is the source of truth
 * for what to display: the server may normalise a blank phone or address to null,
 * so the caller stores what came back rather than what was typed.
 */
export function updateProfile(
  input: UpdateProfileInput,
): Promise<ApiResult<AuthenticatedUser>> {
  return apiPut<AuthenticatedUser>("/api/auth/me", input);
}
