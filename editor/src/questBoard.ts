import type { BoardItem } from "@artisan/editor-sdk";
import { textValue } from "@artisan/editor-sdk";
import { type T, translator } from "./i18n";
import type { Objective, Quest, QuestGroup } from "./model";
import { objectiveSpec, objectiveSubject, objectiveTarget } from "./objectives";

/** The colour of a quest group's node on the board. */
export const QUEST_ACCENT = "#f59e0b";

export function typeLabel(t: T, type: string): string {
  return objectiveSpec(type) ? t(`objective.${type}` as Parameters<T>[0]) : type;
}

/** What an objective asks, in one line: `Break blocks · STONE · 0/64`. */
export function objectiveLine(t: T, o: Objective): string {
  const what = objectiveSubject(o.type, o.params);
  const target = objectiveTarget(o.params);
  return `${typeLabel(t, o.type)}${what ? ` · ${what}` : ""}${target !== undefined ? ` · 0/${String(target)}` : ""}`;
}

/** MiniMessage tags out: the board draws a row's label as plain text. */
export function plainText(source: string): string {
  return source.replace(/<[^<>]*>/g, "").trim();
}

/** Whether the group hands its quests out in list order. */
export function isOrdered(group: QuestGroup): boolean {
  const strategy = group.distribution?.strategy;
  return typeof strategy === "string" && strategy.toLowerCase() === "sequential";
}

/**
 * The group's quests on its board node: one row each, numbered when they are handed out in
 * order, with the quest's description and objectives in the row's tooltip.
 */
export function questBoardItems(group: QuestGroup, project: unknown, lang: string): BoardItem[] {
  const t = translator(lang);
  const ordered = isOrdered(group);
  return group.quests.map((quest, i) => {
    const title = textValue(project, quest.title.key, lang);
    return {
      id: quest.uid,
      label: plainText(title) || quest.key,
      detail: quest.tier || undefined,
      marker: ordered ? String(i + 1) : undefined,
      hover: {
        title: title || quest.key,
        text: textValue(project, quest.description.key, lang) || undefined,
        lines: objectiveLines(t, quest, project, lang),
      },
    };
  });
}

function objectiveLines(t: T, quest: Quest, project: unknown, lang: string): string[] {
  const staged = quest.stages.length > 1;
  return quest.stages.flatMap((stage, si) =>
    stage.objectives.map((o) => {
      const line = objectiveLine(t, o);
      if (!staged) return line;
      const stageTitle = plainText(textValue(project, stage.title.key, lang));
      return `<gray>${stageTitle || t("stage", { n: si + 1 })} ·</gray> ${line}`;
    }),
  );
}

/** The node's second line: `5 quests · in list order`. */
export function questBoardSummary(group: QuestGroup, lang: string): string {
  const t = translator(lang);
  const strategy =
    typeof group.distribution?.strategy === "string"
      ? group.distribution.strategy.toLowerCase()
      : "";
  const count = t("questCount", { count: group.quests.length });
  return ["none", "sequential", "random"].includes(strategy)
    ? `${count} · ${t(`strategy.${strategy}` as Parameters<T>[0]).toLowerCase()}`
    : count;
}
