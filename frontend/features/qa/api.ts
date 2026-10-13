import { apiGet, apiPost, apiPut } from "@/lib/api/client";
import type { ApiResult } from "@/types/api";
import type { AskQuestionInput, Faq, Question, ShopQuestion } from "./types";

/**
 * The Q&amp;A surface.
 *
 * <p>The customer's three calls (ask, own list, public FAQ) go through
 * `lib/api/client.ts`, so the base URL, the session cookie and the envelope
 * unwrapping are the same as every other feature. The FAQ read is public; the
 * other calls need a session, and the asker or answerer is taken from that
 * session rather than sent in the body. The shop's two calls go to `/api/shop/**`,
 * which the backend restricts to `ROLE_CLIENT`.
 */

/** Files a question under the signed-in customer. */
export function askQuestion(
  input: AskQuestionInput,
): Promise<ApiResult<Question>> {
  return apiPost<Question>("/api/questions", input);
}

/** The signed-in customer's own questions, newest first. */
export function listOwnQuestions(): Promise<ApiResult<Question[]>> {
  return apiGet<Question[]>("/api/questions");
}

/** The public FAQ: answered questions the shop marked as generally useful. */
export function listFaq(): Promise<ApiResult<Faq[]>> {
  return apiGet<Faq[]>("/api/questions/faq");
}

/** The shop's answering queue: every open question, longest-waiting first. */
export function listUnansweredQuestions(): Promise<ApiResult<ShopQuestion[]>> {
  return apiGet<ShopQuestion[]>("/api/shop/questions");
}

/**
 * Posts the shop's answer to one question. The backend then emails the customer
 * who asked it and refuses a question that is already answered.
 */
export function answerQuestion(
  id: number,
  answerText: string,
): Promise<ApiResult<Question>> {
  return apiPut<Question>(`/api/shop/questions/${id}/answer`, { answerText });
}
