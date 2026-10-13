/**
 * The identity a session carries, as `/api/auth/login` and `/api/auth/me` report
 * it. Mirrors `backend/shared/.../auth/dto/AuthenticatedUser.java`: the plain
 * enum name (`CUSTOMER` / `CLIENT`), never the `ROLE_` prefix the security layer
 * uses internally.
 *
 * <p>Deliberately small — no password, no verification flags, no status. A
 * session only exists for an ACTIVE account, so those are settled facts by the
 * time this is serialized.
 */
export type Role = "CUSTOMER" | "CLIENT";

export type AuthenticatedUser = {
  id: number;
  email: string;
  fullName: string;
  role: Role;
};
