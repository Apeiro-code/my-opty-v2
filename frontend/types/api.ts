/**
 * The response envelope every endpoint returns.
 *
 * <p>Mirrors the shape the API is specified to produce, so a feature never has to know whether
 * it is talking to a real backend or a mock. Keep it in step with `backend/contracts`.
 */

export type ApiMeta = {
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
} & Record<string, unknown>;

export type ApiError = {
  code: string;
  message: string;
  fieldErrors?: Record<string, string>;
};

export type ApiSuccess<T> = {
  success: true;
  data: T;
  meta?: ApiMeta;
};

export type ApiFailure = {
  success: false;
  error: ApiError;
};

export type ApiResponse<T> = ApiSuccess<T> | ApiFailure;

/** A response whose `data` has been unwrapped, or which threw. */
export type ApiResult<T> =
  { ok: true; data: T; meta?: ApiMeta } | { ok: false; error: ApiError };
