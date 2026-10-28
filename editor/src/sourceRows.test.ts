/// <reference types="vite/client" />
import { type EditorHost, installEditorHost } from "@artisan/editor-sdk";
import { beforeEach, describe, expect, it } from "vitest";
import { type QuestGroup, parseQuestGroup } from "./model";
import { ACTIVE, GROUPS, QUESTS, durationSeconds, questsLocalRows } from "./sourceRows";

type Row = Record<string, unknown>;

let n = 0;
beforeEach(() => {
  n = 0;
  installEditorHost({ newTextKey: () => `k${++n}` } as unknown as EditorHost);
});

/** A project holding one group parsed from `src`, its texts resolved the way the host does. */
function load(src: Row, lang: string) {
  const { doc, i18nKeys } = parseQuestGroup(src, { projectId: "quests", lang });
  const group = { ...(doc as QuestGroup), id: String(src.id) };
  const project = { defaultLang: lang, documents: { quest_group: [group] } };
  // The host resolves `{key}` text nodes of local rows before a menu reads them.
  const resolve = (v: unknown): unknown => {
    if (Array.isArray(v)) return v.map(resolve);
    if (v && typeof v === "object") {
      const o = v as Row;
      if (Object.keys(o).length === 1 && typeof o.key === "string") {
        return i18nKeys[o.key]?.[lang] ?? "";
      }
      return Object.fromEntries(Object.entries(o).map(([k, x]) => [k, resolve(x)]));
    }
    return v;
  };
  const rows = (ref: string, ctx?: Parameters<typeof questsLocalRows>[2]) =>
    resolve(questsLocalRows(project, ref, ctx)) as Row[];
  return { project, rows };
}

/** `actual` reduced to the shape of `expected`: the fields the vector pins, nothing else. */
function pick(actual: unknown, expected: unknown): unknown {
  if (Array.isArray(expected) && Array.isArray(actual)) {
    return actual.map((a, i) => pick(a, expected[i] ?? expected[0]));
  }
  if (expected && typeof expected === "object" && actual && typeof actual === "object") {
    return Object.fromEntries(
      Object.keys(expected).map((k) => [k, pick((actual as Row)[k], (expected as Row)[k])]),
    );
  }
  return actual;
}

interface Vector {
  lang: string;
  document: Row;
  expected: Record<string, Row[]>;
}

const VECTORS = import.meta.glob<Vector>("../../testdata/sources/*.json", {
  eager: true,
  import: "default",
});

describe("shared vectors (QuestSourceVectorsTest reads the same files)", () => {
  for (const [file, vector] of Object.entries(VECTORS)) {
    for (const [ref, expected] of Object.entries(vector.expected)) {
      it(`${file}: ${ref} from the draft matches the plugin's rows`, () => {
        const { rows } = load(vector.document, vector.lang);
        expect(pick(rows(ref), expected)).toEqual(expected);
      });
    }
  }
});

const daily = VECTORS["../../testdata/sources/daily.json"]!.document;

describe("questsLocalRows", () => {
  it("leaves other sources and projects without quest groups to the server", () => {
    const { project } = load(daily, "en");
    expect(questsLocalRows(project, "boutique:items")).toBeNull();
    expect(questsLocalRows({ documents: {} }, QUESTS)).toBeNull();
  });

  it("keeps the player's state of a quest and takes its definition from the draft", () => {
    const { rows } = load(daily, "en");
    const serverRows = [
      {
        id: "daily:miner",
        name: "Old miner",
        status_key: "IN_PROGRESS",
        status: "In progress",
        assigned: true,
        progress_ratio: 0.25,
        stages: [
          {
            key: "dig",
            active: true,
            objectives: [
              { key: "stone", type: "block_break", target: 64, name: "Mine 64 <lang:block.minecraft.stone>", progress: 32 },
              { key: "walk", type: "travel", target: 50, name: "Travel 50 blocks", progress: 10 },
            ],
          },
        ],
      },
      { id: "daily:removed", name: "Deleted in the draft" },
    ];
    const quests = rows(QUESTS, { serverRows });
    expect(quests.map((q) => q.id)).toEqual(["daily:miner", "daily:talker"]);
    const miner = quests[0]!;
    expect(miner.name).toBe("Miner");
    expect(miner.status_key).toBe("IN_PROGRESS");
    expect(miner.assigned).toBe(true);
    const [stone, walk] = (miner.stages as Row[])[0]!.objectives as Row[];
    // Unchanged objective: the server's exact name and progress.
    expect(stone).toMatchObject({ name: "Mine 64 <lang:block.minecraft.stone>", progress: 32 });
    // Its target changed in the draft: the name follows the draft, the progress stays.
    expect(walk).toMatchObject({ name: "Travel 100 blocks", target: 100, progress: 10 });
    // A quest nobody started: the server's neutral state.
    expect(quests[1]).toMatchObject({ status_key: "NOT_STARTED", status: "Available", assigned: false });
  });

  it("narrows quests to the group the menu asks for", () => {
    const { rows } = load(daily, "en");
    expect(rows(QUESTS, { params: { group: "weekly" } })).toEqual([]);
    expect(rows(QUESTS, { params: { group: "daily" } })).toHaveLength(2);
  });

  it("joins active instances to the draft: renamed, dropped when the quest is gone", () => {
    const { rows } = load(daily, "en");
    const serverRows = [
      {
        id: "uuid-1",
        quest_key: "miner",
        group: "daily",
        name: "Old miner",
        progress_ratio: 0.5,
        stages: [{ key: "dig", active: true, objectives: [{ key: "stone", progress: 3 }] }],
      },
      { id: "uuid-2", quest_key: "removed", group: "daily", name: "Gone" },
      { id: "uuid-3", quest_key: "x", group: "elsewhere", name: "Other project's group" },
    ];
    const active = rows(ACTIVE, { serverRows });
    expect(active.map((r) => r.id)).toEqual(["uuid-1", "uuid-3"]);
    expect(active[0]).toMatchObject({
      name: "Miner",
      progress_ratio: 0.5,
      stage_name: "Dig",
      stage_flow: "SEQUENTIAL",
      stage_count: 1,
    });
    expect((active[0]!.objectives as Row[]).map((o) => [o.key, o.progress])).toEqual([
      ["stone", 3],
      ["walk", 0],
    ]);
    expect(active[1]!.name).toBe("Other project's group");
  });

  it("has no active instance to show without the server's rows", () => {
    const { project } = load(daily, "en");
    expect(questsLocalRows(project, ACTIVE)).toBeNull();
  });

  it("names a changed objective from the plugin's default names", () => {
    const { rows } = load(daily, "fr");
    const quests = rows(GROUPS)[0]!.quests as Row[];
    const [stone] = (quests[0]!.stages as Row[])[0]!.objectives as Row[];
    expect(stone!.name).toBe("Miner 64 × <lang:block.minecraft.stone>");
  });

  it("reads durations like QuestDuration.parse", () => {
    expect(durationSeconds("")).toBe(0);
    expect(durationSeconds("90")).toBe(90);
    expect(durationSeconds("1h30m")).toBe(5400);
    expect(durationSeconds("2d")).toBe(172_800);
  });
});
