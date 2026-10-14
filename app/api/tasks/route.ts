import { NextRequest } from "next/server";
import { api } from "@/features/workflow/api";
import type { TodoTaskResponse } from "@/types/workflow";

export async function GET(request: NextRequest) {
  const { searchParams } = new URL(request.url);
  const clientId = searchParams.get("clientId");

  if (clientId) {
    const result = await api.getTasks(Number(clientId));
    return new Response(JSON.stringify(result), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  }

  // Return all tasks (no client filter) - for admin/debug
  const result = await api.getTasks(0); // placeholder
  return new Response(JSON.stringify(result), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const clientId = body.clientId;

    if (!clientId) {
      return new Response(
        JSON.stringify({ success: false, error: { code: "MISSING_CLIENT_ID", message: "clientId is required" } }),
        { status: 400, headers: { "Content-Type": "application/json" } }
      );
    }

    const result = await api.createTask(clientId, {
      title: body.title,
      description: body.description,
      dueDate: body.dueDate,
      priority: body.priority,
    });

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