/**
 * The customer Q&amp;A types, mirroring the shared module's
 * {@code QuestionResponse} and {@code FaqResponse}.
 *
 * <p>`askedAt` / `answeredAt` are ISO-8601 strings, because that is what Spring
 * serialises a `LocalDateTime` to; the components format them for display.
 */

/** One of the signed-in customer's own questions, answered or not. */
export type Question = {
  id: number;
  questionText: string;
  answerText: string | null;
  answered: boolean;
  askedAt: string;
  answeredAt: string | null;
};

/**
 * One entry on the public FAQ. Carries no customer identity — the API omits it,
 * and this type has no field for it.
 */
export type Faq = {
  id: number;
  questionText: string;
  answerText: string;
  answeredAt: string;
};

/** A new question to file under the signed-in customer. */
export type AskQuestionInput = {
  questionText: string;
};

/**
 * One unanswered question in the shop's queue, mirroring
 * {@code ShopQuestionResponse}. Unlike {@link Faq}, it carries the asker's
 * identity — it is only ever read by the signed-in shop owner.
 */
export type ShopQuestion = {
  id: number;
  customerName: string | null;
  customerEmail: string | null;
  questionText: string;
  askedAt: string;
};

/** The shop's answer to a customer's question. */
export type AnswerInput = {
  answerText: string;
};
