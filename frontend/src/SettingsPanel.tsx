import { useEffect, useState } from "react";
import type { Api } from "./api";
import { messageOf } from "./messages";
import {
    RESTORE_ALL,
    anyEdited,
    flip,
    formatValue,
    groups,
    heldKey,
    isEdited,
    release,
    restoreOne,
    settle,
    shownValue,
    withoutKey,
    type Held,
} from "./settings";
import type { Command, SettingDeclaration, Settings } from "./types";

type Props = { api: Api; id: string; values: Settings; onError(message: string): void };

export function SettingsPanel({ api, id, values, onError }: Props) {
    const [declarations, setDeclarations] = useState<SettingDeclaration[]>([]);
    const [drafts, setDrafts] = useState<Held>({});
    const [pending, setPending] = useState<Held>({});

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

    // Each new snapshot lets go of the values it now shows, before this render reads them.
    const [seen, setSeen] = useState(values);
    if (seen !== values) {
        setSeen(values);
        setPending(settle(pending, declarations, values));
    }

    const send = (command: Command, onFailure: () => void = () => undefined) => {
        void api.command(id, command).catch((error: unknown) => {
            onFailure();
            onError(messageOf(error));
        });
    };
    const commit = (declaration: SettingDeclaration) => {
        const released = release(drafts, pending, declaration, values);
        setDrafts(released.drafts);
        setPending(released.pending);
        if (released.command !== null) {
            send(released.command, () =>
                setPending((current) => withoutKey(current, heldKey(declaration))),
            );
        }
    };

    return (
        <aside className="settings">
            <div className="settings-head">
                <h2>Settings</h2>
                <button
                    disabled={!anyEdited(declarations, values)}
                    onClick={() => {
                        setPending({});
                        send(RESTORE_ALL);
                    }}
                >
                    Restore all
                </button>
            </div>
            {groups(declarations).map((group) => (
                <fieldset key={group.owner}>
                    <legend>{group.label}</legend>
                    {group.settings.map((declaration) => {
                        const key = heldKey(declaration);
                        const shown = shownValue(drafts, pending, declaration, values);
                        const edited = isEdited(declaration, values);
                        const described = `setting-${group.owner}-${declaration.key}`;
                        return (
                            <div key={key} className={edited ? "setting edited" : "setting"}>
                                <span className="setting-name">{declaration.key}</span>
                                {declaration.shape === "toggle" ? (
                                    <input
                                        type="checkbox"
                                        aria-label={`${group.label} ${declaration.key}`}
                                        aria-describedby={described}
                                        checked={shown === 1}
                                        onChange={() => {
                                            const flipped = flip(pending, declaration, values);
                                            setPending(flipped.pending);
                                            send(flipped.command, () =>
                                                setPending((current) => withoutKey(current, key)),
                                            );
                                        }}
                                    />
                                ) : (
                                    <>
                                        <input
                                            type="range"
                                            aria-label={`${group.label} ${declaration.key}`}
                                            aria-describedby={described}
                                            min={declaration.min}
                                            max={declaration.max}
                                            step={declaration.step}
                                            value={shown}
                                            onChange={(event) => {
                                                const value = Number(event.target.value);
                                                setDrafts((current) => ({
                                                    ...current,
                                                    [key]: value,
                                                }));
                                            }}
                                            onPointerUp={() => commit(declaration)}
                                            onPointerCancel={() => commit(declaration)}
                                            onKeyUp={() => commit(declaration)}
                                            onBlur={() => commit(declaration)}
                                        />
                                        <output>{formatValue(shown, declaration)}</output>
                                    </>
                                )}
                                <button
                                    disabled={!edited}
                                    aria-label={`Restore ${group.label} ${declaration.key}`}
                                    title="Restore the default"
                                    onClick={() => {
                                        setPending((current) => withoutKey(current, key));
                                        send(restoreOne(declaration));
                                    }}
                                >
                                    ↺
                                </button>
                                <small id={described}>{declaration.description}</small>
                            </div>
                        );
                    })}
                </fieldset>
            ))}
        </aside>
    );
}
