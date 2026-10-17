/**
 * The objective types ArtisanQuests registers today (`ObjectiveTypes` and the Bukkit
 * handlers) and the fields each one reads. An objective of a type not listed here keeps
 * its fields and shows them as plain text: a type an add-on registers is still editable.
 */

export type ParamKind = "int" | "material" | "entity" | "text";

export interface ParamSpec {
  name: string;
  kind: ParamKind;
  required?: boolean;
  /** Shown under "More options" only: a field most objectives leave empty. */
  advanced?: boolean;
}

export interface ObjectiveSpec {
  type: string;
  /** A glyph telling the types apart in a list. */
  icon: string;
  params: ParamSpec[];
}

const amount: ParamSpec = { name: "amount", kind: "int", required: true };

export const OBJECTIVE_TYPES: ObjectiveSpec[] = [
  { type: "block_break", icon: "⛏", params: [{ name: "material", kind: "material" }, amount] },
  { type: "craft_item", icon: "⚒", params: [{ name: "material", kind: "material" }, amount] },
  {
    type: "harvest_crop",
    icon: "🌾",
    params: [{ name: "crop", kind: "material", required: true }, amount],
  },
  { type: "fishing", icon: "🎣", params: [{ name: "fish_type", kind: "material" }, amount] },
  {
    type: "deliver_item",
    icon: "📦",
    params: [
      { name: "material", kind: "material" },
      amount,
      { name: "itemKey", kind: "text", advanced: true },
    ],
  },
  { type: "kill_entity", icon: "⚔", params: [{ name: "entity_type", kind: "entity" }, amount] },
  { type: "breed_animal", icon: "♥", params: [{ name: "entity_type", kind: "entity" }, amount] },
  { type: "travel", icon: "➜", params: [{ name: "distance", kind: "int", required: true }] },
  {
    type: "execute_command",
    icon: "/",
    params: [{ name: "command", kind: "text", required: true }],
  },
  {
    type: "placeholder",
    icon: "%",
    params: [
      { name: "placeholder", kind: "text", required: true },
      { name: "value", kind: "text", required: true },
    ],
  },
];

export function objectiveSpec(type: string): ObjectiveSpec | undefined {
  return OBJECTIVE_TYPES.find((s) => s.type === type);
}

/** The fields of a fresh objective of `type`: its amount at 1, the rest empty. */
export function defaultParams(type: string): Record<string, unknown> {
  const out: Record<string, unknown> = {};
  for (const p of objectiveSpec(type)?.params ?? []) {
    if (p.kind === "int") out[p.name] = 1;
  }
  return out;
}

/** The objective's target: its `amount`, else its `distance`. */
export function objectiveTarget(params: Record<string, unknown>): unknown {
  return params.amount ?? params.distance;
}

/** What the objective is about (`STONE`, `ZOMBIE`, a command…), "" when any. */
export function objectiveSubject(type: string, params: Record<string, unknown>): string {
  const spec = objectiveSpec(type);
  const names = spec
    ? spec.params.filter((p) => p.kind !== "int").map((p) => p.name)
    : Object.keys(params).filter((k) => typeof params[k] === "string");
  return names
    .map((n) => params[n])
    .filter((v) => typeof v === "string" && v !== "")
    .join(" = ");
}

/** The required fields left empty. */
export function missingParams(type: string, params: Record<string, unknown>): string[] {
  return (objectiveSpec(type)?.params ?? [])
    .filter((p) => p.required && (params[p.name] === undefined || params[p.name] === ""))
    .map((p) => p.name);
}

/**
 * Entity types of the living mobs (Paper 1.21), offered while typing: what `kill_entity`
 * and `breed_animal` compare `EntityType` against. Anything else may still be typed.
 */
export const ENTITY_TYPES = [
  "ALLAY", "ARMADILLO", "AXOLOTL", "BAT", "BEE", "BLAZE", "BOGGED", "BREEZE", "CAMEL", "CAT",
  "CAVE_SPIDER", "CHICKEN", "COD", "COW", "CREAKING", "CREEPER", "DOLPHIN", "DONKEY", "DROWNED",
  "ELDER_GUARDIAN", "ENDER_DRAGON", "ENDERMAN", "ENDERMITE", "EVOKER", "FOX", "FROG", "GHAST",
  "GLOW_SQUID", "GOAT", "GUARDIAN", "HOGLIN", "HORSE", "HUSK", "ILLUSIONER", "IRON_GOLEM",
  "LLAMA", "MAGMA_CUBE", "MOOSHROOM", "MULE", "OCELOT", "PANDA", "PARROT", "PHANTOM", "PIG",
  "PIGLIN", "PIGLIN_BRUTE", "PILLAGER", "PLAYER", "POLAR_BEAR", "PUFFERFISH", "RABBIT",
  "RAVAGER", "SALMON", "SHEEP", "SHULKER", "SILVERFISH", "SKELETON", "SKELETON_HORSE", "SLIME",
  "SNIFFER", "SNOW_GOLEM", "SPIDER", "SQUID", "STRAY", "STRIDER", "TADPOLE", "TRADER_LLAMA",
  "TROPICAL_FISH", "TURTLE", "VEX", "VILLAGER", "VINDICATOR", "WANDERING_TRADER", "WARDEN",
  "WITCH", "WITHER", "WITHER_SKELETON", "WOLF", "ZOGLIN", "ZOMBIE", "ZOMBIE_HORSE",
  "ZOMBIE_VILLAGER", "ZOMBIFIED_PIGLIN",
];
