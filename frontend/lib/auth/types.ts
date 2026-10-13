/**
 * The identity a session carries, as `/api/auth/login` and `/api/auth/me` report
 * it. Mirrors `backend/shared/.../auth/dto/AuthenticatedUser.java`: the plain
 * enum name (`CUSTOMER` / `CLIENT`), never the `ROLE_` prefix the security layer
 * uses internally.
 *
 * <p>Deliberately small — no password, no verification flags, no status. A
 * session only exists for an ACTIVE account, so those are settled facts by the
 * time this is serialized.
 *
 * <p>`phone`, `address` and `preferredContact` ride along so the account page can
 * fill its edit form from the same answer the header already fetched; they are
 * null for a `CLIENT`, whose extra details belong to a different story.
 */
export type Role = "CUSTOMER" | "CLIENT";

export type PreferredContact = "EMAIL" | "PHONE" | "SMS";

export type AuthenticatedUser = {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  phone: string | null;
  address: string | null;
  preferredContact: PreferredContact | null;
};

/**
 * The customer's editable profile fields, as `PUT /api/auth/me` accepts them.
 * Email is absent on purpose: it is the login identity and changing it is a
 * verification story of its own, not an edit.
 */
export type UpdateProfileInput = {
  name: string;
  phone: string | null;
  address: string | null;
  preferredContact: PreferredContact | null;
};
