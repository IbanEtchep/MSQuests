import type { DocumentKindContribution } from "@artisan/editor-sdk";
import { textValue } from "@artisan/editor-sdk";
import { QuestGroupEditor } from "./QuestGroupEditor";
import { COLLECTION_LABELS, LABELS } from "./i18n";
import { QuestIcon } from "./icon";
import { QUEST_ACCENT, questBoardItems, questBoardSummary } from "./questBoard";
import {
  DIR,
  KIND,
  QUESTS_FOLDER,
  type QuestGroup,
  SCHEMA,
  newUid,
  parseQuestGroup,
  questGroupTextSites,
  serializeQuestGroup,
} from "./model";

/** The `quest_group` kind: one file per group, holding its quests (design §3). */
export const questGroupKind: DocumentKindContribution = {
  kind: KIND,
  dir: DIR,
  schemaVersion: SCHEMA,
  // Created in the module's folder only (SDK 1.8); a group already elsewhere still loads.
  projects: [QUESTS_FOLDER],
  // Data with no links: one "Quests" entry and page instead of a node per group (SDK 1.9).
  collection: { labels: COLLECTION_LABELS },
  // A group's id is its key on the server (`quest_group_key`): typed by hand, no prefix.
  label: LABELS.en,
  labels: LABELS,
  icon: QuestIcon,
  component: QuestGroupEditor,
  title: (doc, { project, lang }) => textValue(project, (doc as QuestGroup).title.key, lang),
  create: ({ id, projectId }) =>
    ({
      id,
      projectId,
      uid: newUid(),
      // Empty keys: the host mints them at their canonical address.
      title: { key: "" },
      description: { key: "" },
      actorType: "player",
      reset: {},
      limits: {},
      rotation: {},
      distribution: {},
      tiers: [],
      quests: [],
      rest: {},
    }) satisfies QuestGroup,
  parse: (src, ctx) => parseQuestGroup(src, ctx),
  serialize: (doc, project) => serializeQuestGroup(doc as QuestGroup, project),
  ltextSites: (doc) => questGroupTextSites(doc as QuestGroup),
  board: {
    size: { width: 300, height: 48 },
    accent: QUEST_ACCENT,
    // The group's quests, one selectable row each: the Properties panel opens the one clicked.
    items: (doc, { project, lang }) => questBoardItems(doc as QuestGroup, project, lang),
    summary: (doc, { lang }) => questBoardSummary(doc as QuestGroup, lang),
  },
};
