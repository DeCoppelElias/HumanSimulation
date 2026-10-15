import { useEffect, useState } from "react";
import type { Api } from "./api";
import { messageOf } from "./messages";
import {
    RESTORE_ALL,
    anyEdited,
    commitDraft,
    currentValue,
    draftKey,
    formatValue,
    groups,
    isEdited,
    restoreOne,
    withoutDraft,
    type Drafts,
} from "./settings";
import type { Command, SettingDeclaration, Settings } from "./types";

type Props = { api: Api; id: string; values: Settings; onError(message: string): void };

export function SettingsPanel({ api, id, values, onError }: Props) {
    const [declarations, setDeclarations] = useState<SettingDeclaration[]>([]);
    const [drafts, setDrafts] = useState<Drafts>({});

    useEffect(() => {
        let live = true;
        api.settings(id)
            .then((loaded) => {
                if (live) {
                    setDeclarations(loaded);
                }
            })
            .catch((error: unknown) => onError(messageOf(error)));
        return () => {
            live = false;
        };
    }, [api, id, onError]);

    const send = (command: Command) => {
        void api.command(id, command).catch((error: unknown) => onError(messageOf(error)));
    };
    const commit = (declaration: SettingDeclaration) => {
        const command = commitDraft(drafts[draftKey(declaration)], declaration, values);
        setDrafts((current) => withoutDraft(current, draftKey(declaration)));
        if (command !== null) {
            send(command);
        }
    };

    return (
        <aside className="settings">
            <div className="settings-head">
                <h2>Settings</h2>
                <button
                    disabled={!anyEdited(declarations, values)}
                    onClick={() => send(RESTORE_ALL)}
                >
                    Restore all
                </button>
            </div>
            {groups(declarations).map((group) => (
                <fieldset key={group.owner}>
                    <legend>{group.label}</legend>
                    {group.settings.map((declaration) => {
                        const shown =
                            drafts[draftKey(declaration)] ?? currentValue(declaration, values);
                        const edited = isEdited(declaration, values);
                        return (
                            <div
                                key={declaration.key}
                                className={edited ? "setting edited" : "setting"}
                            >
                                <span className="setting-name">{declaration.key}</span>
                                <input
                                    type="range"
                                    aria-label={declaration.key}
                                    min={declaration.min}
                                    max={declaration.max}
                                    step={declaration.step}
                                    value={shown}
                                    onChange={(event) => {
                                        const value = Number(event.target.value);
                                        setDrafts((current) => ({
                                            ...current,
                                            [draftKey(declaration)]: value,
                                        }));
                                    }}
                                    onPointerUp={() => commit(declaration)}
                                    onKeyUp={() => commit(declaration)}
                                />
                                <output>{formatValue(shown, declaration.step)}</output>
                                <button
                                    disabled={!edited}
                                    title="Restore the default"
                                    onClick={() => send(restoreOne(declaration))}
                                >
                                    ↺
                                </button>
                                <small>{declaration.description}</small>
                            </div>
                        );
                    })}
                </fieldset>
            ))}
        </aside>
    );
}
