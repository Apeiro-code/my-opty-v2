export type DiscountState = "ACTIVE" | "SCHEDULED" | "EXPIRED" | "ENDED";

export type Discount = {
  id: number;
  description: string;
  percentage: number;
  targetItemIds: number[] | null;
  sitewide: boolean;
  validFrom: string;
  validTo: string;
  active: boolean;
  state: DiscountState;
  endedAt: string | null;
  createdBy: number | null;
  createdAt: string;
};
