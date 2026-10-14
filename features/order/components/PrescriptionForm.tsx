"use client";

import { useState, type ChangeEvent, type FormEvent } from "react";
import { submitPrescription } from "../api";
import type { Prescription } from "../types";

const ALLOWED_TYPES = ["application/pdf", "image/jpeg", "image/png"];
const ALLOWED_EXTENSIONS = [".pdf", ".jpg", ".jpeg", ".png"];
const MAX_DOCUMENT_BYTES = 10 * 1024 * 1024;

const OPTICAL_FIELDS = [
  { key: "sph", label: "Sphere (SPH)", placeholder: "+06.25" },
  { key: "cyl", label: "Cylinder (CYL)", placeholder: "-1.75" },
  { key: "axis", label: "Axis", placeholder: "180" },
  { key: "add", label: "Add power (ADD)", placeholder: "+2.00" },
] as const;

type EyeField = (typeof OPTICAL_FIELDS)[number]["key"];
type Eye = Record<EyeField, string>;

const EMPTY_EYE: Eye = { sph: "", cyl: "", axis: "", add: "" };

/**
 * The customer's prescription form: the four optical values per eye, an optional
 * "progressive" flag, and an optional scan or photo to back it up.
 *
 * <p>A client component because the whole thing is state and an event handler.
 * It validates the document's type and size before sending so an obviously
 * unsupported file fails instantly, but the backend is the authority — it
 * re-checks both, and this is only the fast half of the same rule.
 */
export default function PrescriptionForm() {
  const [left, setLeft] = useState<Eye>(EMPTY_EYE);
  const [right, setRight] = useState<Eye>(EMPTY_EYE);
  const [progressive, setProgressive] = useState(false);
  const [document, setDocument] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [submitted, setSubmitted] = useState<Prescription | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function documentProblem(file: File): string | null {
    const type = file.type.toLowerCase();
    const extension = "." + (file.name.split(".").pop() ?? "").toLowerCase();
    if (
      !ALLOWED_TYPES.includes(type) &&
      !ALLOWED_EXTENSIONS.includes(extension)
    ) {
      return "Upload a PDF, JPEG or PNG.";
    }
    if (file.size > MAX_DOCUMENT_BYTES) {
      return "The document must be 10 MB or smaller.";
    }
    return null;
  }

  function onDocumentChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    if (!file) {
      setDocument(null);
      return;
    }
    const problem = documentProblem(file);
    if (problem) {
      setError(problem);
      setDocument(null);
      event.target.value = "";
      return;
    }
    setError(null);
    setDocument(file);
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    const values = { ...left, ...right };
    const hasValue = Object.values(values).some((value) => value.trim() !== "");
    if (!hasValue) {
      setError("Enter at least one optical value.");
      return;
    }

    setSubmitting(true);
    const result = await submitPrescription({
      sphLeft: left.sph,
      cylLeft: left.cyl,
      axisLeft: left.axis,
      addPowerLeft: left.add,
      sphRight: right.sph,
      cylRight: right.cyl,
      axisRight: right.axis,
      addPowerRight: right.add,
      progressive,
      document,
    });
    setSubmitting(false);

    if (!result.ok) {
      setError(
        result.error.code === "UNAUTHENTICATED"
          ? "Please sign in to submit a prescription."
          : result.error.message,
      );
      return;
    }
    setSubmitted(result.data);
  }

  if (submitted) {
    return (
      <div className="max-w-prose rounded-lg border border-black/10 p-6 dark:border-white/15">
        <h2 className="text-lg font-medium">Prescription submitted</h2>
        <p className="mt-2 text-sm opacity-80">
          Reference #{submitted.id}. The shop will check it against your
          document and let you know if the details match what you entered.
        </p>
        <p className="mt-1 text-sm opacity-80">
          Status:{" "}
          {submitted.verificationStatus.replace(/_/g, " ").toLowerCase()}.
        </p>
        {submitted.hasDocument ? (
          <p className="mt-1 text-sm opacity-80">Document uploaded.</p>
        ) : (
          <p className="mt-1 text-sm opacity-80">No document uploaded.</p>
        )}
      </div>
    );
  }

  return (
    <form onSubmit={onSubmit} className="flex max-w-2xl flex-col gap-6">
      <div className="grid gap-6 sm:grid-cols-2">
        <EyeFields
          legend="Left eye (OS)"
          idPrefix="left"
          value={left}
          onChange={setLeft}
        />
        <EyeFields
          legend="Right eye (OD)"
          idPrefix="right"
          value={right}
          onChange={setRight}
        />
      </div>

      <label className="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          checked={progressive}
          onChange={(event) => setProgressive(event.target.checked)}
        />
        This prescription is for progressive lenses
      </label>

      <div className="flex flex-col gap-2">
        <label htmlFor="document" className="text-sm font-medium">
          Prescription document (optional)
        </label>
        <input
          id="document"
          name="document"
          type="file"
          accept={ALLOWED_EXTENSIONS.join(",")}
          onChange={onDocumentChange}
          className="text-sm file:mr-3 file:rounded-md file:border file:border-black/15 file:bg-transparent file:px-3 file:py-1.5 file:text-sm dark:file:border-white/20"
        />
        <p className="text-xs opacity-70">
          A scan or photo, up to 10 MB. PDF, JPEG or PNG.
        </p>
      </div>

      {error ? (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      ) : null}

      <button
        type="submit"
        disabled={submitting}
        className="w-fit rounded-md bg-foreground px-4 py-2 text-sm font-medium text-background disabled:opacity-50"
      >
        {submitting ? "Submitting…" : "Submit prescription"}
      </button>
    </form>
  );
}

function EyeFields({
  legend,
  idPrefix,
  value,
  onChange,
}: {
  legend: string;
  idPrefix: string;
  value: Eye;
  onChange: (next: Eye) => void;
}) {
  return (
    <fieldset className="flex flex-col gap-3 rounded-lg border border-black/10 p-4 dark:border-white/15">
      <legend className="px-1 text-sm font-medium">{legend}</legend>
      {OPTICAL_FIELDS.map((field) => {
        const id = `${idPrefix}-${field.key}`;
        return (
          <div key={field.key} className="flex flex-col gap-1">
            <label htmlFor={id} className="text-xs opacity-80">
              {field.label}
            </label>
            <input
              id={id}
              name={id}
              inputMode="decimal"
              placeholder={field.placeholder}
              value={value[field.key]}
              onChange={(event) =>
                onChange({ ...value, [field.key]: event.target.value })
              }
              className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
            />
          </div>
        );
      })}
    </fieldset>
  );
}
