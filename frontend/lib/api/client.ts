import type { ApiError, ApiMeta, ApiResult } from "@/types/api";

/**
 * The one place `fetch` is called from.
 *
 * <p>Features call `apiGet` / `apiPost` rather than `fetch` directly, so the base URL, the
 * credentials and the envelope unwrapping are defined once. A feature that calls `fetch`
 * itself silently opts out of all three.
 *
 * <p>Nothing calls this yet: the skeleton has no endpoints to call. It exists so the first
 * feature does not get to invent a second convention.
 */

const BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class ApiRequestError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, error: ApiError) {
    super(error.message);
    this.name = "ApiRequestError";
    this.status = status;
    this.code = error.code;
  }
}

async function request<T>(
  path: string,
  init: RequestInit & {
    params?: Record<string, string | number | boolean | undefined>;
  },
): Promise<ApiResult<T>> {
  const { params, ...rest } = init;
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params ?? {})) {
    if (value !== undefined) {
      search.append(key, String(value));
    }
  }
  const query = search.size > 0 ? `?${search.toString()}` : "";

  let response: Response;
  try {
    response = await fetch(`${BASE_URL}${path}${query}`, {
      ...rest,
      headers: { Accept: "application/json", ...rest.headers },
    });
  } catch {
    // A refused connection has no envelope to unwrap, so it is reported in the same shape.
    return {
      ok: false,
      error: {
        code: "NETWORK_ERROR",
        message: `Could not reach the API at ${BASE_URL}. Is the backend running?`,
      },
    };
  }

  if (!response.ok) {
    const error = await readError(response);
    return { ok: false, error };
  }

  const body = (await response.json()) as
    | { success: true; data: T; meta?: ApiMeta }
    | { success: false; error: ApiError };

  if (!body.success) {
    return { ok: false, error: body.error };
  }

  return { ok: true, data: body.data, meta: body.meta };
}

async function readError(response: Response): Promise<ApiError> {
  try {
    const body = (await response.json()) as { error?: ApiError };
    if (body.error) {
      return body.error;
    }
  } catch {
    // Not every failure carries an envelope: a gateway 502 or a proxy timeout will not.
  }
  return { code: `HTTP_${response.status}`, message: response.statusText };
}

export const apiGet = <T>(
  path: string,
  params?: Record<string, string | number | boolean | undefined>,
) => request<T>(path, { method: "GET", params });

export const apiPost = <T>(path: string, body: unknown) =>
  request<T>(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

export const apiPut = <T>(path: string, body: unknown) =>
  request<T>(path, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

export const apiDelete = <T>(path: string) =>
  request<T>(path, { method: "DELETE" });
