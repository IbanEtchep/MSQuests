import {
  ActionChainEditor,
  Button,
  type DocumentEditorProps,
  ItemInput,
  type ItemComparisonWire,
  MaterialInput,
  MiniMessageInput,
  type TextWrite,
  injectModuleStyles,
  newTextKey,
  selectDocumentItem,
  textValue,
  updateDocument,
  useContentLang,
  useProject,
  useUiLang,
} from "@artisan/editor-sdk";
import { type ReactNode, useEffect, useState } from "react";
import { describeCron, isCronShape } from "./cron";
import { type T, useT } from "./i18n";
import {
  KIND,
  type Objective,
  type Quest,
  type QuestGroup,
  type Raw,
  type Stage,
  type Text,
  HOOKS,
  asMap,
  freeKey,
  joinChain,
  legacyLabel,
  newObjectiveKey,
  newUid,
  slugKey,
  splitChain,
  typedKey,
} from "./model";
import {
  ENTITY_TYPES,
  itemValue,
  OBJECTIVE_TYPES,
  type ParamSpec,
  defaultParams,
  missingParams,
  objectiveSpec,
  objectiveSubject,
  objectiveTarget,
  writeItemParam,
} from "./objectives";
import { QUESTS_CSS } from "./styles";

const DURATION = /^(\d+|(\d+[dhms])+)$/;
const TRIGGERS = ["actor_load", "period_reset", "quest_complete"] as const;
const TIMEZONES: string[] = (() => {
  try {
    return (
      (Intl as { supportedValuesOf?: (k: string) => string[] }).supportedValuesOf?.("timeZone") ??
      []
    );
  } catch {
    return [];
  }
})();

/** One edit of the group: a copy changed by `recipe`, committed with the texts it sets. */
type Edit = (recipe: (draft: QuestGroup) => void, texts?: TextWrite[]) => void;

interface Ctx {
  projectId: string;
  project: unknown;
  lang: string;
  t: T;
  edit: Edit;
}

/**
 * The focus view of a `quest_group` (ArtisanQuests design §9), in the Properties panel:
 * the group's rules and its ordered quests, then one quest at a time with its stages,
 * objectives and rewards.
 */
export function QuestGroupEditor({ projectId, documentId, document, item }: DocumentEditorProps) {
  injectModuleStyles("msquests", QUESTS_CSS);
  const group = document as QuestGroup;
  const project = useProject(projectId);
  const lang = useContentLang();
  const t = useT();
  // The open quest follows the board: a quest row selected there opens it here, and opening
  // or leaving one here selects its row or the group. Outside the board (the Document's own
  // page) `item` never comes, so the local state carries it alone.
  const [openUid, setOpenUid] = useState<string | null>(item ?? null);
  useEffect(() => {
    setOpenUid(item ?? null);
  }, [item]);
  const open = (uid: string | null) => {
    setOpenUid(uid);
    selectDocumentItem({ documentId, item: uid });
  };

  const edit: Edit = (recipe, texts) => {
    const next = structuredClone(group) as QuestGroup;
    recipe(next);
    updateDocument({ projectId, kind: KIND, id: documentId, doc: next, texts });
  };
  const ctx: Ctx = { projectId, project, lang, t, edit };

  const index = openUid ? group.quests.findIndex((q) => q.uid === openUid) : -1;
  if (index >= 0) {
    return (
      <div className="aq">
        <Button variant="ghost" size="sm" className="aq-back" onClick={() => open(null)}>
          ← {t("back")}
        </Button>
        <QuestView ctx={ctx} group={group} index={index} />
      </div>
    );
  }
  return (
    <div className="aq">
      <GroupView ctx={ctx} group={group} onOpen={open} />
    </div>
  );
}

// ------------------------------------------------------------------ fields

function Field({
  label,
  children,
  hint,
}: { label: string; children: ReactNode; hint?: ReactNode }) {
  return (
    // biome-ignore lint/a11y/noLabelWithoutControl: the control is the child
    <label className="aq-field">
      <span className="field-label">{label}</span>
      {children}
      {hint}
    </label>
  );
}

function TextField({
  ctx,
  label,
  node,
  multiline,
}: { ctx: Ctx; label: string; node: Text; multiline?: boolean }) {
  const value = textValue(ctx.project, node.key, ctx.lang);
  const write = (v: string) => ctx.edit(() => {}, [{ key: node.key, lang: ctx.lang, value: v }]);
  return (
    <Field label={`${label} · ${ctx.lang}`}>
      <MiniMessageInput value={value} onChange={write} multiline={multiline} aria-label={label} />
    </Field>
  );
}

function asNumber(value: unknown): string {
  return typeof value === "number" ? String(value) : typeof value === "string" ? value : "";
}

/** A whole number written into `map[key]`; cleared, the key leaves the map. */
function NumberField({
  label,
  value,
  onChange,
}: { label: string; value: unknown; onChange: (v: number | undefined) => void }) {
  return (
    <Field label={label}>
      <input
        className="input"
        type="number"
        min={0}
        step={1}
        value={asNumber(value)}
        onChange={(e) => {
          const raw = e.target.value.trim();
          onChange(raw === "" ? undefined : Math.max(0, Math.trunc(Number(raw))));
        }}
      />
    </Field>
  );
}

function setOrDelete(map: Raw, key: string, value: unknown) {
  if (value === undefined || value === "") delete map[key];
  else map[key] = value;
}

function move<X>(list: X[], from: number, to: number) {
  if (to < 0 || to >= list.length) return;
  const [item] = list.splice(from, 1);
  list.splice(to, 0, item!);
}

function IconButton({
  label,
  onClick,
  disabled,
  children,
}: { label: string; onClick: () => void; disabled?: boolean; children: ReactNode }) {
  return (
    <button
      type="button"
      className="aq-icon-btn"
      title={label}
      aria-label={label}
      onClick={onClick}
      disabled={disabled}
    >
      {children}
    </button>
  );
}

function Reorder({
  t,
  index,
  count,
  onMove,
  onRemove,
}: {
  t: T;
  index: number;
  count: number;
  onMove: (to: number) => void;
  onRemove: () => void;
}) {
  return (
    <>
      <IconButton label={t("moveUp")} disabled={index === 0} onClick={() => onMove(index - 1)}>
        ↑
      </IconButton>
      <IconButton
        label={t("moveDown")}
        disabled={index === count - 1}
        onClick={() => onMove(index + 1)}
      >
        ↓
      </IconButton>
      <IconButton label={t("remove")} onClick={onRemove}>
        ×
      </IconButton>
    </>
  );
}

// ------------------------------------------------------------------ group

function GroupView({
  ctx,
  group,
  onOpen,
}: { ctx: Ctx; group: QuestGroup; onOpen: (uid: string) => void }) {
  const { t, edit, project, lang } = ctx;
  const uiLang = useUiLang();
  const cron = typeof group.reset.cron === "string" ? group.reset.cron : "";
  const cronWords = cron ? describeCron(cron, uiLang) : null;
  const timezone = typeof group.reset.timezone === "string" ? group.reset.timezone : "";
  const triggers = Array.isArray(group.distribution.triggers)
    ? (group.distribution.triggers as string[])
    : [];
  const perTier = (group.distribution.tiers ?? {}) as Record<string, unknown>;
  const kept = Object.keys(group.rest).filter((k) => k !== "actions");

  const addQuest = () => {
    const titleKey = newTextKey();
    const uid = newUid();
    edit(
      (g) => {
        g.quests.push({
          key: freeKey(
            "quest",
            g.quests.map((q) => q.key),
          ),
          uid,
          tier: "",
          title: { key: titleKey },
          description: { key: newTextKey() },
          duration: "",
          stages: [newStage(1)],
          rest: {},
        });
      },
      [{ key: titleKey, lang, value: t("newQuestTitle") }],
    );
    onOpen(uid);
  };

  return (
    <>
      <section className="aq-section">
        <span className="aq-h">{t("group")}</span>
        <TextField ctx={ctx} label={t("title")} node={group.title} />
        <TextField ctx={ctx} label={t("description")} node={group.description} multiline />
        <Field label={t("actorType")}>
          <select
            className="select"
            value={group.actorType}
            onChange={(e) =>
              edit((g) => {
                g.actorType = e.target.value;
              })
            }
          >
            <option value="player">{t("actor.player")}</option>
            <option value="global">{t("actor.global")}</option>
            {!["player", "global"].includes(group.actorType) && (
              <option value={group.actorType}>{group.actorType}</option>
            )}
          </select>
        </Field>
      </section>

      <section className="aq-section">
        <span className="aq-h">{t("reset")}</span>
        <Field
          label={t("cron")}
          hint={
            !cron ? (
              <span className="aq-hint">{t("noReset")}</span>
            ) : !isCronShape(cron) ? (
              <span className="aq-err">{t("cronInvalid")}</span>
            ) : (
              <span className="aq-ok">
                {cronWords ?? t("cronRaw")}
                {timezone ? `, ${timezone}` : ""}
              </span>
            )
          }
        >
          <input
            className="input"
            value={cron}
            placeholder="0 4 * * *"
            onChange={(e) => edit((g) => setOrDelete(g.reset, "cron", e.target.value))}
          />
        </Field>
        <Field label={t("timezone")}>
          <input
            className="input"
            list="aq-timezones"
            value={timezone}
            placeholder={t("timezoneServer")}
            onChange={(e) => edit((g) => setOrDelete(g.reset, "timezone", e.target.value.trim()))}
          />
          <datalist id="aq-timezones">
            {TIMEZONES.map((z) => (
              <option key={z} value={z} />
            ))}
          </datalist>
        </Field>
      </section>

      <section className="aq-section">
        <span className="aq-h">{t("distribution")}</span>
        <div className="aq-row">
          <Field label={t("strategy")}>
            <select
              className="select"
              value={
                typeof group.distribution.strategy === "string"
                  ? group.distribution.strategy.toLowerCase()
                  : ""
              }
              onChange={(e) => edit((g) => setOrDelete(g.distribution, "strategy", e.target.value))}
            >
              <option value="">—</option>
              {(["none", "sequential", "random"] as const).map((s) => (
                <option key={s} value={s}>
                  {t(`strategy.${s}`)}
                </option>
              ))}
            </select>
          </Field>
          <NumberField
            label={t("amount")}
            value={group.distribution.amount}
            onChange={(v) => edit((g) => setOrDelete(g.distribution, "amount", v))}
          />
        </div>
        <span className="field-label">{t("triggers")}</span>
        {TRIGGERS.map((trigger) => (
          <label key={trigger} className="aq-check">
            <input
              type="checkbox"
              checked={triggers.includes(trigger)}
              onChange={(e) =>
                edit((g) => {
                  const list = (
                    Array.isArray(g.distribution.triggers) ? g.distribution.triggers : []
                  ) as string[];
                  const next = e.target.checked
                    ? [...list, trigger]
                    : list.filter((x) => x !== trigger);
                  setOrDelete(g.distribution, "triggers", next.length ? next : undefined);
                })
              }
            />
            {t(`trigger.${trigger}`)}
          </label>
        ))}
        {group.tiers.length > 0 && (
          <>
            <span className="field-label">{t("perTier")}</span>
            <div className="aq-row">
              {group.tiers.map((tier) => (
                <NumberField
                  key={tier.key}
                  label={textValue(project, tier.title.key, lang) || tier.key}
                  value={perTier[tier.key]}
                  onChange={(v) =>
                    edit((g) => {
                      const map = { ...((g.distribution.tiers ?? {}) as Raw) };
                      setOrDelete(map, tier.key, v);
                      setOrDelete(
                        g.distribution,
                        "tiers",
                        Object.keys(map).length ? map : undefined,
                      );
                    })
                  }
                />
              ))}
            </div>
          </>
        )}
      </section>

      <section className="aq-section">
        <span className="aq-h">{t("limits")}</span>
        <div className="aq-row">
          <NumberField
            label={t("maxActive")}
            value={group.limits.max_active}
            onChange={(v) => edit((g) => setOrDelete(g.limits, "max_active", v))}
          />
          <NumberField
            label={t("maxPerPeriod")}
            value={group.limits.max_per_period}
            onChange={(v) => edit((g) => setOrDelete(g.limits, "max_per_period", v))}
          />
        </div>
        <span className="aq-h">{t("rotation")}</span>
        <div className="aq-row">
          <label className="aq-check">
            <input
              type="checkbox"
              checked={group.rotation.enabled === true}
              onChange={(e) =>
                edit((g) => setOrDelete(g.rotation, "enabled", e.target.checked ? true : undefined))
              }
            />
            {t("rotationEnabled")}
          </label>
          <NumberField
            label={t("maxPerPeriod")}
            value={group.rotation.max_per_period}
            onChange={(v) => edit((g) => setOrDelete(g.rotation, "max_per_period", v))}
          />
        </div>
      </section>

      <section className="aq-section">
        <div className="aq-head">
          <span className="aq-h">{t("tiers")}</span>
          <Button
            size="sm"
            variant="ghost"
            onClick={() =>
              edit((g) => {
                g.tiers.push({
                  key: freeKey(
                    "tier",
                    g.tiers.map((x) => x.key),
                  ),
                  title: { key: newTextKey() },
                  rest: {},
                });
              })
            }
          >
            + {t("addTier")}
          </Button>
        </div>
        {group.tiers.map((tier, i) => (
          <div key={tier.key} className="aq-row">
            <Field label={t("tierKey")}>
              <input
                className="input"
                defaultValue={tier.key}
                onBlur={(e) => {
                  const next = slugKey(e.target.value);
                  if (next === tier.key) return;
                  edit((g) => {
                    const key = freeKey(
                      next,
                      g.tiers.map((x) => x.key),
                    );
                    g.tiers[i]!.key = key;
                    for (const q of g.quests) if (q.tier === tier.key) q.tier = key;
                    const map = g.distribution.tiers as Raw | undefined;
                    if (map && tier.key in map) {
                      map[key] = map[tier.key];
                      delete map[tier.key];
                    }
                  });
                }}
              />
            </Field>
            <TextField ctx={ctx} label={t("title")} node={tier.title} />
            <IconButton
              label={t("remove")}
              onClick={() =>
                edit((g) => {
                  g.tiers.splice(i, 1);
                })
              }
            >
              ×
            </IconButton>
          </div>
        ))}
      </section>

      <section className="aq-section">
        <div className="aq-head">
          <span className="aq-h">
            {t("quests")} · {group.quests.length}
          </span>
          <Button size="sm" variant="primary" onClick={addQuest}>
            + {t("addQuest")}
          </Button>
        </div>
        {group.quests.length === 0 && <span className="aq-hint">{t("noQuests")}</span>}
        <div className="aq-list">
          {group.quests.map((q, i) => (
            <div key={q.uid} className="aq-item">
              <button
                type="button"
                className="aq-item-main"
                onClick={() => onOpen(q.uid)}
                title={t("open")}
              >
                <span className="aq-item-title">
                  {textValue(project, q.title.key, lang) || q.key}
                </span>
                <span className="aq-item-sub">
                  {t("questSummary", {
                    stages: q.stages.length,
                    objectives: q.stages.reduce((n, s) => n + s.objectives.length, 0),
                  })}
                  {" · "}
                  {q.key}
                  {q.tier ? ` · ${q.tier}` : ""}
                  {q.duration ? ` · ${q.duration}` : ""}
                </span>
              </button>
              <Reorder
                t={t}
                index={i}
                count={group.quests.length}
                onMove={(to) => edit((g) => move(g.quests, i, to))}
                onRemove={() =>
                  edit((g) => {
                    g.quests.splice(i, 1);
                  })
                }
              />
            </div>
          ))}
        </div>
        {kept.length > 0 && (
          <span className="aq-hint">{t("kept", { fields: kept.join(", ") })}</span>
        )}
      </section>

      <HooksSection ctx={ctx} group={group} />
    </>
  );
}

// ------------------------------------------------------------------ chains

/**
 * An action chain of the group (a hook, a quest's rewards) in the editor's chain editor.
 * Steps still in the MSQuests format stay apart, listed as they are (see `splitChain`).
 */
function ChainField({
  ctx,
  value,
  onChange,
}: { ctx: Ctx; value: unknown; onChange: (next: unknown[] | undefined) => void }) {
  const { chain, legacy } = splitChain(value);
  return (
    <div className="aq-chain">
      <ActionChainEditor
        projectId={ctx.projectId}
        value={chain}
        onChange={(next) => onChange(joinChain(next, legacy))}
      />
      {legacy.length > 0 && (
        <div className="aq-legacy">
          <span className="aq-hint">{ctx.t("legacySteps")}</span>
          {legacy.map((step, i) => (
            // biome-ignore lint/suspicious/noArrayIndexKey: a step has no identity
            <div key={i} className="aq-legacy-row">
              <code>{legacyLabel(step)}</code>
              <IconButton
                label={ctx.t("remove")}
                onClick={() => onChange(joinChain(chain, legacy.filter((_, j) => j !== i)))}
              >
                ×
              </IconButton>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function chainLength(value: unknown): number {
  return Array.isArray(value) ? value.length : value == null ? 0 : 1;
}

function HooksSection({ ctx, group }: { ctx: Ctx; group: QuestGroup }) {
  const { t, edit } = ctx;
  const hooks = asMap(group.rest.actions);
  return (
    <section className="aq-section">
      <span className="aq-h">{t("hooks")}</span>
      <span className="aq-hint">{t("hooksHint")}</span>
      {HOOKS.map((hook) => {
        const count = chainLength(hooks[hook]);
        return (
          <details key={hook} className="aq-fold">
            <summary>
              <span>{t(`hook.${hook}`)}</span>
              {count > 0 && (
                <span className="aq-badge" title={t("actionCount", { count })}>
                  {count}
                </span>
              )}
            </summary>
            <ChainField
              ctx={ctx}
              value={hooks[hook]}
              onChange={(next) =>
                edit((g) => {
                  const map = { ...asMap(g.rest.actions) };
                  setOrDelete(map, hook, next);
                  setOrDelete(g.rest, "actions", Object.keys(map).length ? map : undefined);
                })
              }
            />
          </details>
        );
      })}
    </section>
  );
}

// ------------------------------------------------------------------ quest

function newStage(n: number): Stage {
  // `flow` written: a stage without it had no active objective before QuestStage read it as parallel.
  return {
    key: `stage_${n}`,
    title: { key: newTextKey() },
    flow: "parallel",
    objectives: [],
    rest: {},
  };
}

function QuestView({ ctx, group, index }: { ctx: Ctx; group: QuestGroup; index: number }) {
  const { t, edit } = ctx;
  const quest = group.quests[index]!;
  const editQuest = (recipe: (q: Quest) => void, texts?: TextWrite[]) =>
    edit((g) => recipe(g.quests[index]!), texts);
  const keyTaken = group.quests.some((q, i) => i !== index && q.key === quest.key);
  const durationBad =
    quest.duration !== "" && !DURATION.test(quest.duration.replace(/\s/g, "").toLowerCase());
  const kept = Object.keys(quest.rest).filter((k) => k !== "actions");

  return (
    <>
      <section className="aq-section">
        <span className="aq-h">{t("general")}</span>
        <TextField ctx={ctx} label={t("title")} node={quest.title} />
        <TextField ctx={ctx} label={t("description")} node={quest.description} multiline />
        <div className="aq-row aq-top">
          <Field label={t("tier")}>
            <select
              className="select"
              value={quest.tier}
              onChange={(e) =>
                editQuest((q) => {
                  q.tier = e.target.value;
                })
              }
            >
              <option value="">{t("noTier")}</option>
              {group.tiers.map((tier) => (
                <option key={tier.key} value={tier.key}>
                  {textValue(ctx.project, tier.title.key, ctx.lang) || tier.key}
                </option>
              ))}
              {quest.tier && !group.tiers.some((x) => x.key === quest.tier) && (
                <option value={quest.tier}>{quest.tier}</option>
              )}
            </select>
          </Field>
          <Field
            label={t("duration")}
            hint={
              <span className={durationBad ? "aq-err" : "aq-hint"}>
                {durationBad ? t("durationInvalid") : t("durationHint")}
              </span>
            }
          >
            <input
              className="input"
              value={quest.duration}
              placeholder="24h"
              onChange={(e) =>
                editQuest((q) => {
                  q.duration = e.target.value.trim();
                })
              }
            />
          </Field>
        </div>
        <details className="aq-fold" open={keyTaken || undefined}>
          <summary>{t("more")}</summary>
          <Field
            label={t("key")}
            hint={
              <span className={keyTaken ? "aq-err" : "aq-hint"}>
                {keyTaken ? t("keyTaken") : t("questKeyHint")}
              </span>
            }
          >
            <input
              className="input aq-mono"
              value={quest.key}
              onChange={(e) =>
                editQuest((q) => {
                  q.key = typedKey(e.target.value);
                })
              }
              onBlur={() => {
                const final = slugKey(quest.key);
                if (final !== quest.key)
                  editQuest((q) => {
                    q.key = final;
                  });
              }}
            />
          </Field>
        </details>
      </section>

      <section className="aq-section">
        <div className="aq-head">
          <span className="aq-h">
            {t("stages")} · {quest.stages.length}
          </span>
          <Button
            size="sm"
            variant="ghost"
            onClick={() =>
              editQuest((q) => {
                const key = freeKey(
                  `stage_${q.stages.length + 1}`,
                  q.stages.map((s) => s.key),
                );
                q.stages.push({ ...newStage(q.stages.length + 1), key });
              })
            }
          >
            + {t("addStage")}
          </Button>
        </div>
        <ol className="aq-stages">
          {quest.stages.map((stage, si) => (
            <StageCard
              key={`${stage.key}-${si}`}
              ctx={ctx}
              quest={quest}
              stage={stage}
              si={si}
              editQuest={editQuest}
            />
          ))}
        </ol>
      </section>

      <section className="aq-section">
        <span className="aq-h">{t("rewards")}</span>
        <span className="aq-hint">{t("rewardsHint")}</span>
        <ChainField
          ctx={ctx}
          value={asMap(quest.rest.actions).complete}
          onChange={(next) =>
            editQuest((q) => {
              const actions = { ...asMap(q.rest.actions) };
              setOrDelete(actions, "complete", next);
              setOrDelete(q.rest, "actions", Object.keys(actions).length > 0 ? actions : undefined);
            })
          }
        />
        {kept.length > 0 && (
          <span className="aq-hint">{t("kept", { fields: kept.join(", ") })}</span>
        )}
      </section>

      <section className="aq-section">
        <span className="aq-h">{t("preview")}</span>
        <Preview ctx={ctx} quest={quest} />
      </section>
    </>
  );
}

function StageCard({
  ctx,
  quest,
  stage,
  si,
  editQuest,
}: {
  ctx: Ctx;
  quest: Quest;
  stage: Stage;
  si: number;
  editQuest: (recipe: (q: Quest) => void, texts?: TextWrite[]) => void;
}) {
  const { t } = ctx;
  const [openKey, setOpenKey] = useState<string | null>(null);
  const editStage = (recipe: (s: Stage) => void) => editQuest((q) => recipe(q.stages[si]!));
  const title = textValue(ctx.project, stage.title.key, ctx.lang);
  // A stage without `flow` reads as parallel (QuestStageConfig).
  const flow = stage.flow || "parallel";
  const allKeys = quest.stages.flatMap((s) => s.objectives.map((o) => o.key));

  return (
    <li className="aq-stage">
      <div className="aq-stage-head">
        <span className="aq-stage-n">{si + 1}</span>
        <div className="aq-stage-title">
          <MiniMessageInput
            value={title}
            placeholder={t("stageTitle")}
            aria-label={t("stage", { n: si + 1 })}
            onChange={(value) =>
              ctx.edit(() => {}, [{ key: stage.title.key, lang: ctx.lang, value }])
            }
          />
        </div>
        <Reorder
          t={t}
          index={si}
          count={quest.stages.length}
          onMove={(to) => editQuest((q) => move(q.stages, si, to))}
          onRemove={() =>
            editQuest((q) => {
              q.stages.splice(si, 1);
            })
          }
        />
      </div>
      {stage.objectives.length > 1 && (
        <div className="aq-seg" role="radiogroup" aria-label={t("flow")}>
          {(["parallel", "sequential"] as const).map((f) => (
            <button
              key={f}
              type="button"
              role="radio"
              aria-checked={flow === f}
              className={`chip${flow === f ? " is-active" : ""}`}
              onClick={() =>
                editStage((s) => {
                  s.flow = f;
                })
              }
            >
              {t(`flow.${f}`)}
            </button>
          ))}
        </div>
      )}
      {stage.objectives.length === 0 && <span className="aq-hint">{t("noObjectives")}</span>}
      <ul className="aq-objectives">
        {stage.objectives.map((objective, oi) => (
          <ObjectiveRow
            key={`${objective.key}-${oi}`}
            t={t}
            projectId={ctx.projectId}
            n={flow === "sequential" ? `${si + 1}.${oi + 1}` : "•"}
            objective={objective}
            open={openKey === objective.key}
            onToggle={() => setOpenKey(openKey === objective.key ? null : objective.key)}
            keyTaken={allKeys.filter((k) => k === objective.key).length > 1}
            index={oi}
            count={stage.objectives.length}
            onMove={(to) => editStage((s) => move(s.objectives, oi, to))}
            onChange={(recipe) => editStage((s) => recipe(s.objectives[oi]!))}
            onRename={(key) => {
              if (openKey === objective.key) setOpenKey(key);
            }}
            onRemove={() =>
              editStage((s) => {
                s.objectives.splice(oi, 1);
              })
            }
          />
        ))}
      </ul>
      <Button
        size="sm"
        variant="ghost"
        className="aq-add"
        onClick={() => {
          // Objective keys are unique within a quest: the database keys progress by them.
          const key = newObjectiveKey("block_break", allKeys);
          editQuest((q) => {
            q.stages[si]!.objectives.push({
              key,
              type: "block_break",
              params: defaultParams("block_break"),
            });
          });
          setOpenKey(key);
        }}
      >
        + {t("addObjective")}
      </Button>
    </li>
  );
}

function typeLabel(t: T, type: string): string {
  return objectiveSpec(type) ? t(`objective.${type}` as Parameters<T>[0]) : type;
}

/**
 * One objective: a line saying what it asks (`⛏ Break blocks · 64 × STONE`), which opens
 * its form. The key, generated from the type, waits under "More options".
 */
function ObjectiveRow({
  t,
  projectId,
  n,
  objective,
  open,
  onToggle,
  keyTaken,
  index,
  count,
  onMove,
  onChange,
  onRename,
  onRemove,
}: {
  t: T;
  projectId: string;
  n: string;
  objective: Objective;
  open: boolean;
  onToggle: () => void;
  keyTaken: boolean;
  index: number;
  count: number;
  onMove: (to: number) => void;
  onChange: (recipe: (o: Objective) => void) => void;
  onRename: (key: string) => void;
  onRemove: () => void;
}) {
  const spec = objectiveSpec(objective.type);
  const params: ParamSpec[] = spec
    ? spec.params
    : Object.keys(objective.params).map((name) => ({ name, kind: "text" as const }));
  const target = objectiveTarget(objective.params);
  const subject = objectiveSubject(objective.type, objective.params);
  const missing = missingParams(objective.type, objective.params);
  const problem = keyTaken || missing.length > 0;
  const field = (p: ParamSpec) =>
    p.kind === "item" ? (
      <ItemParamField
        key={p.name}
        t={t}
        projectId={projectId}
        spec={p}
        params={objective.params}
        onChange={(recipe) => onChange((o) => recipe(o.params))}
      />
    ) : (
    <ParamField
      key={p.name}
      t={t}
      spec={p}
      value={objective.params[p.name]}
      onChange={(v) => onChange((o) => setOrDelete(o.params, p.name, v))}
    />
  );

  return (
    <li className={`aq-obj${open ? " is-open" : ""}${problem ? " has-problem" : ""}`}>
      <div className="aq-obj-line">
        <button type="button" className="aq-obj-main" onClick={onToggle} aria-expanded={open}>
          <span className="aq-obj-n">{n}</span>
          <span className="aq-obj-icon" aria-hidden>
            {spec?.icon ?? "?"}
          </span>
          <span className="aq-obj-text">
            {typeLabel(t, objective.type)}
            {target !== undefined && <strong> · {String(target)}</strong>}
            {subject ? ` × ${subject}` : target !== undefined ? ` × ${t("any")}` : ""}
          </span>
          {problem && (
            <span
              className="aq-obj-dot"
              title={
                keyTaken
                  ? t("objectiveKeyTaken")
                  : t("missing", {
                      fields: missing.map((m) => t(`param.${m}` as Parameters<T>[0])).join(", "),
                    })
              }
            />
          )}
        </button>
        <Reorder t={t} index={index} count={count} onMove={onMove} onRemove={onRemove} />
      </div>
      {open && (
        <div className="aq-obj-form">
          <Field label={t("type")}>
            <select
              className="select"
              value={objective.type}
              onChange={(e) =>
                onChange((o) => {
                  o.type = e.target.value;
                  o.params = defaultParams(o.type);
                })
              }
            >
              {OBJECTIVE_TYPES.map((s) => (
                <option key={s.type} value={s.type}>
                  {s.icon} {t(`objective.${s.type}` as Parameters<T>[0])}
                </option>
              ))}
              {!spec && <option value={objective.type}>{objective.type}</option>}
            </select>
          </Field>
          <div className="aq-row aq-wrap">{params.filter((p) => !p.advanced).map(field)}</div>
          {missing.length > 0 && (
            <span className="aq-warn">
              {t("missing", {
                fields: missing.map((m) => t(`param.${m}` as Parameters<T>[0])).join(", "),
              })}
            </span>
          )}
          <details className="aq-fold" open={keyTaken || undefined}>
            <summary>{t("more")}</summary>
            {params.filter((p) => p.advanced).map(field)}
            <Field
              label={t("key")}
              hint={
                <span className={keyTaken ? "aq-err" : "aq-hint"}>
                  {keyTaken ? t("objectiveKeyTaken") : t("objectiveKeyHint")}
                </span>
              }
            >
              <input
                className="input aq-mono"
                value={objective.key}
                onChange={(e) => {
                  const key = typedKey(e.target.value);
                  onChange((o) => {
                    o.key = key;
                  });
                  onRename(key);
                }}
                onBlur={() => {
                  const final = slugKey(objective.key);
                  if (final !== objective.key) {
                    onChange((o) => {
                      o.key = final;
                    });
                    onRename(final);
                  }
                }}
              />
            </Field>
          </details>
        </div>
      )}
    </li>
  );
}

/**
 * An objective's item: the whole item of menus (vanilla, custom or captured item, name, lore,
 * enchantments…) and the criteria a player's item is compared on (`match`), ticked by default
 * from the item. Empty: any item. Replaces the legacy bare material on the first edit.
 */
function ItemParamField({
  t,
  projectId,
  spec,
  params,
  onChange,
}: {
  t: T;
  projectId: string;
  spec: ParamSpec;
  params: Raw;
  onChange: (recipe: (params: Raw) => void) => void;
}) {
  const item = itemValue(params, spec);
  const match = Array.isArray(params.match) ? (params.match as ItemComparisonWire[]) : undefined;
  const write = (next: Raw | undefined, nextMatch: ItemComparisonWire[] | undefined) =>
    onChange((p) => writeItemParam(p, spec, next, nextMatch));
  return (
    <Field
      label={t(`param.${spec.name}` as Parameters<T>[0])}
      hint={Object.keys(item).length === 0 ? <span className="aq-hint">{t("anyItem")}</span> : undefined}
    >
      <ItemInput
        projectId={projectId}
        value={item}
        match={match}
        onChange={(next) => write(next, match)}
        onMatchChange={(next) => write(item, next)}
      />
    </Field>
  );
}

function ParamField({
  t,
  spec,
  value,
  onChange,
}: {
  t: T;
  spec: ParamSpec;
  value: unknown;
  onChange: (v: unknown) => void;
}) {
  const label = t(`param.${spec.name}` as Parameters<T>[0]);
  const missing = spec.required && (value === undefined || value === "");
  const text = typeof value === "string" || typeof value === "number" ? String(value) : "";
  if (spec.kind === "int") {
    return <NumberField label={label} value={value} onChange={onChange} />;
  }
  const hint = missing ? (
    <span className="aq-warn">{t("required")}</span>
  ) : spec.name === "itemKey" ? (
    <span className="aq-hint">{t("itemKeyHint")}</span>
  ) : undefined;
  if (spec.kind === "material") {
    return (
      <Field label={label} hint={hint}>
        <MaterialInput
          value={text}
          placeholder={t("any")}
          aria-label={label}
          onChange={(v) => onChange(v.toUpperCase().replace(/\s+/g, "_"))}
        />
      </Field>
    );
  }
  const entity = spec.kind === "entity";
  return (
    <Field label={label} hint={hint}>
      <input
        className={`input${entity ? " aq-mono" : ""}`}
        list={entity ? "aq-entities" : undefined}
        value={text}
        placeholder={entity ? t("any") : ""}
        onChange={(e) =>
          onChange(entity ? e.target.value.toUpperCase().replace(/\s+/g, "_") : e.target.value)
        }
      />
      {entity && (
        <datalist id="aq-entities">
          {ENTITY_TYPES.map((e) => (
            <option key={e} value={e} />
          ))}
        </datalist>
      )}
    </Field>
  );
}

function Preview({ ctx, quest }: { ctx: Ctx; quest: Quest }) {
  const { t, project, lang } = ctx;
  const line = (o: Objective) => {
    const what = objectiveSubject(o.type, o.params);
    const target = objectiveTarget(o.params);
    return `${typeLabel(t, o.type)}${what ? ` · ${what}` : ""}${target !== undefined ? ` · 0/${String(target)}` : ""}`;
  };
  return (
    <div className="aq-preview">
      <span className="aq-preview-title">
        {textValue(project, quest.title.key, lang) || quest.key}
      </span>
      {textValue(project, quest.description.key, lang) && (
        <span className="aq-preview-line">{textValue(project, quest.description.key, lang)}</span>
      )}
      {quest.stages.map((s, si) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: stages are listed in order
        <span key={si} className="aq-preview-stage">
          {quest.stages.length > 1 && (
            <span className="aq-preview-line">
              {textValue(project, s.title.key, lang) || t("stage", { n: si + 1 })}
            </span>
          )}
          {s.objectives.map((o) => (
            <span key={o.key} className="aq-preview-line">
              • {line(o)}
            </span>
          ))}
        </span>
      ))}
    </div>
  );
}
