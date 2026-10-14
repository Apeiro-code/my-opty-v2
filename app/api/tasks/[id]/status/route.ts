import { NextRequest } from "next/server";
import { api } from "@/features/workflow/api";
import type { TodoTaskResponse } from "@/types/workflow";

export async function PUT(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);
    const id = searchParams.get("id");
    const status = searchParams.get("status");

    if (!id || !status) {
      return new Response(
        JSON.stringify({ success: false, error: { code: "MISSING_PARAMS", message: "id and status are required" } }),
        { status: 400, headers: { "Content-Type": "application/json" } }
      );
    }

    const result = await api.updateTaskStatus(Number(id), status as "PENDING" | "IN_PROGRESS" | "DONE");

    return new Response(JSON.stringify(result), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    return new Response(
      JSON.stringify({ success: false, error: { code: "INTERNAL_ERROR", message: (error as Error).message } }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }
}