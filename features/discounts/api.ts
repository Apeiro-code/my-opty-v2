import { apiGet } from "@/lib/api/client";
import type { ApiResult } from "@/types/api";
import type { Discount } from "./types";

export function getActiveDiscounts(): Promise<ApiResult<Discount[]>> {
  return apiGet<Discount[]>("/api/discounts/active");
}
