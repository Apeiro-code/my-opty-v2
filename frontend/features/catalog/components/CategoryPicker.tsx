import type { Category, CategoryItemType } from "../types";

/**
 * The category picker shared by the frame and lens forms. It offers only the
 * categories that can hold the product being edited — those typed for it, plus
 * any typed for both — and an "Unfiled" option, because the schema lets a
 * record leave its category empty.
 */
export default function CategoryPicker({
  categories,
  itemType,
  value,
  onChange,
}: {
  categories: Category[];
  itemType: Exclude<CategoryItemType, "BOTH">;
  value: string;
  onChange: (value: string) => void;
}) {
  const options = categories.filter(
    (category) =>
      category.itemType === itemType || category.itemType === "BOTH",
  );

  return (
    <select
      value={value}
      onChange={(event) => onChange(event.target.value)}
      className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
    >
      <option value="">Unfiled</option>
      {options.map((category) => (
        <option key={category.id} value={String(category.id)}>
          {category.name}
        </option>
      ))}
    </select>
  );
}
